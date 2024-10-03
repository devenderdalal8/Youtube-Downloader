package com.youtube.data.di

import com.chaquo.python.PyObject
import com.youtube.domain.repository.PythonScriptRepository
import com.youtube.data.repositoryImpl.PythonScriptRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun providePythonRepository(pythonService: PyObject): PythonScriptRepository {
        return PythonScriptRepositoryImpl(pythonService)
    }
}