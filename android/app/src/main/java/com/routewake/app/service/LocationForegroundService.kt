package com.routewake.app.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.*
import com.routewake.app.data.local.SessionManager
import com.routewake.app.domain.engine.ArrivalDetector
import com.routewake.app.domain.engine.EtaEstimator
import com.routewake.app.domain.engine.HaversineCalculator
import com.routewake.app.domain.model.LocationPoint
import com.routewake.app.domain.model.MonitoringState
import com.routewake.app.domain.model.TripAlert
import com.routewake.app.domain.model.TripMonitoringSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var sessionManager: SessionManager
    private lateinit var alarmPlayer: AlarmAudioPlayer
    private lateinit var arrivalDetector: ArrivalDetector

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var androidLocationManager: LocationManager? = null
    private var androidLocationListener: LocationListener? = null

    private var previousLocationPoint: LocationPoint? = null

    companion object {
        const val ACTION_START_MONITORING = "com.routewake.app.START_MONITORING"
        const val ACTION_STOP_SERVICE = "com.routewake.app.STOP_SERVICE"
        const val ACTION_PAUSE_MONITORING = "com.routewake.app.PAUSE_MONITORING"
        const val ACTION_RESUME_MONITORING = "com.routewake.app.RESUME_MONITORING"
        const val ACTION_DISMISS_ALARM = "com.routewake.app.DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.routewake.app.SNOOZE_ALARM"

        const val EXTRA_TRIP_ID = "extra_trip_id"
        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_DEST_LAT = "extra_dest_lat"
        const val EXTRA_DEST_LNG = "extra_dest_lng"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val EXTRA_DIST_THRESHOLD = "extra_dist_threshold"
        const val EXTRA_TIME_THRESHOLD = "extra_time_threshold"

        private val _monitoringSession = MutableStateFlow(TripMonitoringSession())
        val monitoringSession: StateFlow<TripMonitoringSession> = _monitoringSession.asStateFlow()

        fun startService(context: Context, trip: TripAlert) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_START_MONITORING
                putExtra(EXTRA_TRIP_ID, trip.id)
                putExtra(EXTRA_DEST_NAME, trip.destinationName)
                putExtra(EXTRA_DEST_LAT, trip.destinationLatitude)
                putExtra(EXTRA_DEST_LNG, trip.destinationLongitude)
                putExtra(EXTRA_ALARM_TYPE, trip.alarmType)
                trip.distanceThresholdKm?.let { putExtra(EXTRA_DIST_THRESHOLD, it) }
                trip.timeThresholdMinutes?.let { putExtra(EXTRA_TIME_THRESHOLD, it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
        alarmPlayer = AlarmAudioPlayer(applicationContext)
        arrivalDetector = ArrivalDetector()
        NotificationHelper.createNotificationChannels(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                val trip = parseTripFromIntent(intent)
                startActiveMonitoring(trip)
            }
            ACTION_STOP_SERVICE -> {
                stopActiveMonitoring()
                stopSelf()
            }
            ACTION_PAUSE_MONITORING -> {
                pauseActiveMonitoring()
            }
            ACTION_RESUME_MONITORING -> {
                resumeActiveMonitoring()
            }
            ACTION_DISMISS_ALARM -> {
                dismissAlarm()
            }
            ACTION_SNOOZE_ALARM -> {
                snoozeAlarm()
            }
        }
        return START_STICKY
    }

    private fun parseTripFromIntent(intent: Intent): TripAlert {
        val tripId = intent.getLongExtra(EXTRA_TRIP_ID, System.currentTimeMillis())
        val destName = intent.getStringExtra(EXTRA_DEST_NAME) ?: "Destination"
        val destLat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
        val destLng = intent.getDoubleExtra(EXTRA_DEST_LNG, 0.0)
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: "DISTANCE"
        val distThresh = if (intent.hasExtra(EXTRA_DIST_THRESHOLD)) intent.getIntExtra(EXTRA_DIST_THRESHOLD, 1) else null
        val timeThresh = if (intent.hasExtra(EXTRA_TIME_THRESHOLD)) intent.getIntExtra(EXTRA_TIME_THRESHOLD, 10) else null

        return TripAlert(
            id = tripId,
            userId = sessionManager.getUser()?.id ?: 1L,
            destinationName = destName,
            destinationLatitude = destLat,
            destinationLongitude = destLng,
            alarmType = alarmType,
            distanceThresholdKm = distThresh,
            timeThresholdMinutes = timeThresh,
            status = "ACTIVE"
        )
    }

    private fun startActiveMonitoring(trip: TripAlert) {
        arrivalDetector.reset()
        _monitoringSession.value = TripMonitoringSession(
            activeTrip = trip,
            state = MonitoringState.MONITORING,
            arrivalTriggered = false
        )

        val notification = NotificationHelper.buildNavigationNotification(
            applicationContext,
            trip.destinationName,
            "Acquiring GPS...",
            "Estimating..."
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIFICATION_NAVIGATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_NAVIGATION_ID, notification)
        }

        initLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun initLocationUpdates() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setMinUpdateIntervalMillis(2000)
                .setMinUpdateDistanceMeters(2f)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { handleNewLocation(it) }
                }
            }

            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (_: Exception) {
            // Fallback to standard Android LocationManager
            initFallbackLocationManager()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initFallbackLocationManager() {
        try {
            androidLocationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            androidLocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    handleNewLocation(location)
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            androidLocationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                4000L,
                2f,
                androidLocationListener!!,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            _monitoringSession.value = _monitoringSession.value.copy(
                state = MonitoringState.ERROR,
                errorMessage = "Location permission or GPS provider unavailable"
            )
        }
    }

    private fun handleNewLocation(location: Location) {
        val currentTrip = _monitoringSession.value.activeTrip ?: return
        if (_monitoringSession.value.state != MonitoringState.MONITORING) return

        val speedKmh = if (location.hasSpeed() && location.speed > 0) location.speed * 3.6f else null
        val currentPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            speedKmh = speedKmh,
            timestamp = location.time
        )

        // Evaluate Arrival
        val detectionResult = arrivalDetector.evaluate(currentPoint, currentTrip, speedKmh)

        // Calculate ETA
        val etaResult = EtaEstimator.estimateEta(currentPoint, previousLocationPoint, detectionResult.distanceKm)
        previousLocationPoint = currentPoint

        // Update notification
        updateOngoingNotification(
            currentTrip.destinationName,
            String.format("%.2f km", detectionResult.distanceKm),
            etaResult.etaMinutes?.let { "${Math.round(it)} min" } ?: "Need movement"
        )

        // Update Session State
        _monitoringSession.value = _monitoringSession.value.copy(
            currentLocation = currentPoint,
            distanceRemainingKm = detectionResult.distanceKm,
            etaMinutes = etaResult.etaMinutes,
            speedKmh = speedKmh,
            gpsAccuracyOk = detectionResult.isAccuracyAcceptable,
            consecutiveReadingsInsideThreshold = detectionResult.consecutiveHits,
            lastUpdateTimestamp = System.currentTimeMillis()
        )

        if (detectionResult.shouldTriggerAlarm) {
            triggerArrivalAlarm(currentTrip, detectionResult.distanceKm)
        }
    }

    private fun triggerArrivalAlarm(trip: TripAlert, distanceKm: Double) {
        _monitoringSession.value = _monitoringSession.value.copy(
            state = MonitoringState.ARRIVED,
            arrivalTriggered = true
        )

        val soundEnabled = sessionManager.isSoundEnabled()
        val vibEnabled = sessionManager.isVibrationEnabled()
        alarmPlayer.startAlarm(soundEnabled, vibEnabled)

        val reason = if (trip.isDistanceMode) {
            "You are within ${String.format("%.2f", distanceKm)} km of ${trip.destinationName}."
        } else {
            "Arrival estimate reached for ${trip.destinationName}."
        }

        val alarmNotification = NotificationHelper.buildArrivalNotification(
            applicationContext,
            trip.destinationName,
            reason
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ALARM_ID, alarmNotification)
    }

    private fun updateOngoingNotification(destName: String, distance: String, eta: String) {
        val notification = NotificationHelper.buildNavigationNotification(
            applicationContext,
            destName,
            distance,
            eta
        )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_NAVIGATION_ID, notification)
    }

    private fun pauseActiveMonitoring() {
        _monitoringSession.value = _monitoringSession.value.copy(state = MonitoringState.PAUSED)
    }

    private fun resumeActiveMonitoring() {
        if (_monitoringSession.value.activeTrip != null) {
            _monitoringSession.value = _monitoringSession.value.copy(state = MonitoringState.MONITORING)
        }
    }

    private fun dismissAlarm() {
        alarmPlayer.stopAlarm()
        arrivalDetector.markAlarmDismissed()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationHelper.NOTIFICATION_ALARM_ID)
    }

    private fun snoozeAlarm() {
        alarmPlayer.stopAlarm()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationHelper.NOTIFICATION_ALARM_ID)

        // Snooze for configured duration (default 5 mins)
        val snoozeMins = sessionManager.getSnoozeDurationMins()
        serviceScope.launch {
            kotlinx.coroutines.delay(snoozeMins * 60 * 1000L)
            val trip = _monitoringSession.value.activeTrip
            if (trip != null && _monitoringSession.value.state == MonitoringState.ARRIVED) {
                val distance = _monitoringSession.value.distanceRemainingKm ?: 0.5
                triggerArrivalAlarm(trip, distance)
            }
        }
    }

    private fun stopActiveMonitoring() {
        alarmPlayer.stopAlarm()
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        androidLocationListener?.let { androidLocationManager?.removeUpdates(it) }

        _monitoringSession.value = TripMonitoringSession(state = MonitoringState.IDLE)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NotificationHelper.NOTIFICATION_NAVIGATION_ID)
        notificationManager.cancel(NotificationHelper.NOTIFICATION_ALARM_ID)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopActiveMonitoring()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
