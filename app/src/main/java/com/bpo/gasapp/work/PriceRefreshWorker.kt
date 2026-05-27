package com.bpo.gasapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bpo.gasapp.data.location.LocationProvider
import com.bpo.gasapp.data.settings.SettingsRepository
import com.bpo.gasapp.domain.repository.StationRepository
import com.bpo.gasapp.domain.util.distanceMeters
import com.bpo.gasapp.notifications.PriceNotifier
import androidx.glance.appwidget.updateAll
import com.bpo.gasapp.widget.GasWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class PriceRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: StationRepository,
    private val notifier: PriceNotifier,
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = repository.refreshAndDetectFavoriteDrops()
        return result.fold(
            onSuccess = { drops ->
                notifier.notifyDrops(drops)
                checkPriceAlert()
                GasWidget().updateAll(applicationContext)
                Result.success()
            },
            onFailure = { Result.retry() }
        )
    }

    private suspend fun checkPriceAlert() {
        val settings = settingsRepository.settings.first()
        val threshold = settings.alertThreshold ?: return
        val location = locationProvider.currentLocation() ?: return
        val match = repository.observeStations().first()
            .asSequence()
            .mapNotNull { s -> s.priceOf(settings.alertFuel)?.let { s to it } }
            .filter { it.second <= threshold }
            .filter { distanceMeters(location, it.first.latitude, it.first.longitude) <= ALERT_RADIUS_METERS }
            .minByOrNull { it.second }
        if (match != null) {
            notifier.notifyPriceAlert(match.first.brand, settings.alertFuel.label, match.second)
        }
    }

    companion object {
        private const val WORK_NAME = "price_refresh_periodic"
        private const val ALERT_RADIUS_METERS = 20_000f

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PriceRefreshWorker>(8, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
