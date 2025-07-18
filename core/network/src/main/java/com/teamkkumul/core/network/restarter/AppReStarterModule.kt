package com.teamkkumul.core.network.restarter

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppReStarterModule {
    @Binds
    @Singleton
    abstract fun bindsAppRestarter(
        appRestarter: DefaultAppReStarter,
    ): AppReStarter
}
