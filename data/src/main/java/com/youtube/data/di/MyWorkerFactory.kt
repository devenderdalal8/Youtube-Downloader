package com.youtube.data.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.youtube.data.service.workManager.VideoWorkManager
import com.youtube.domain.repository.VideoLocalDataRepository
import javax.inject.Inject

class MyWorkerFactory @Inject constructor(
    private val repository: VideoLocalDataRepository
) : WorkerFactory() {

    override fun createWorker(
        context: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return VideoWorkManager(
            context = context,
            workerParams = workerParameters,
        )

    }
}