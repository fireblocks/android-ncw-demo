package com.fireblocks.sdkdemo.ui.signin

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.fireblocks.sdkdemo.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 06/07/2023.
 * Uses Google More advanced library - https://developers.google.com/identity/one-tap/android/get-saved-credentials
 * The problem is that id doesn't work on emulators. fix it
 */
class GoogleAuthUiClient(
    private val context: Context,
    private val onTapClient: SignInClient
) {
    private val auth = Firebase.auth
    suspend fun signIn(): IntentSender? {
        val result = try {
            onTapClient.beginSignIn(
                buildSignInRequest(context)
            ).await()
        } catch (e: Exception){
            Timber.e(e, "Failed to sign in with Google")
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = onTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        email = email,
                        userName = displayName,
                        profilePictureUrl = photoUrl?.toString(),
                        idToken = googleIdToken,
                    )
                },
                errorMessage = null
            )
        } catch (e: Exception){
            Timber.e(e, "Failed to sign in with Google")
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            onTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception){
            Timber.e("Failed to sign in with Google", e)
            if (e is CancellationException) throw e
        }
    }

    suspend fun getSignInUser(): UserData? = auth.currentUser?.run {
        UserData(
            email = email,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString(),
            idToken = getIdToken(false).await()?.token,
        )
    }

    private fun buildSignInRequest(context: Context): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
            ).setAutoSelectEnabled(true)
            .build()
    }
}