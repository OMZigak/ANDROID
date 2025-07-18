package com.teamkkumul.core.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KKUMUL

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WithoutTokenInterceptor
