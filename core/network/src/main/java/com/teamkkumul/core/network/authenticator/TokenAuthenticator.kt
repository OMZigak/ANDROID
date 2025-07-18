package com.teamkkumul.core.network.authenticator

import com.teamkkumul.core.datastore.datasource.KumulPreferencesDataSource
import com.teamkkumul.core.network.api.LoginService
import com.teamkkumul.core.network.restarter.AppReStarter
import com.teamkkumul.core.network.util.runSuspendCatching
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val dataStore: KumulPreferencesDataSource,
    private val loginService: LoginService,
    private val appRestarter: AppReStarter,
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null

        if (response.request.header("Authorization-Retry") != null) return null

        return runBlocking {
            val newAccessToken = refreshToken() ?: return@runBlocking null
            response.request
                .newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .header("Authorization-Retry", "true")
                .build()
        }
    }

    private suspend fun refreshToken(): String? {
        return mutex.withLock {
            val refreshToken = dataStore.refreshToken.first()

            runSuspendCatching {
                loginService.postReissueToken(refreshToken)
            }.onSuccess {
                val newAccess = it.data?.accessToken.orEmpty()
                dataStore.updateAccessToken(newAccess)
                dataStore.updateRefreshToken(it.data?.refreshToken.orEmpty())
                return newAccess
            }.onFailure {
                Timber.e(it)
                dataStore.clear()
                notifyReLoginRequired()
            }
            null
        }
    }

    private fun notifyReLoginRequired() {
        appRestarter.makeToast("재 로그인이 필요해요")
        appRestarter.restartApp()
    }
}
