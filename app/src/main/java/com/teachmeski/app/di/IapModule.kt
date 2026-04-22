package com.teachmeski.app.di

import com.teachmeski.app.iap.IapManager
import com.teachmeski.app.iap.IapManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class IapModule {
    @Binds
    @Singleton
    abstract fun bindIapManager(impl: IapManagerImpl): IapManager
}
