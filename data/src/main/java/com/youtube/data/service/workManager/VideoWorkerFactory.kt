package com.youtube.data.service.workManager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.youtube.domain.repository.VideoLocalDataRepository
import javax.inject.Inject

class VideoWorkerFactory @Inject constructor(
    val localDataRepository: VideoLocalDataRepository
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return VideoWorkManager(appContext, workerParameters, localDataRepository)
    }
}