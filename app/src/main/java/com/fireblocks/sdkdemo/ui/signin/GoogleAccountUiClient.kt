package com.fireblocks.sdkdemo.ui.signin

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 06/07/2023.
 * https://developers.google.com/identity/sign-in/android/sign-in
 * Legacy code, try to move to new code implemented in GoogleAuthUiClient
 */
class GoogleAccountUiClient(
    private val googleSignInClient: GoogleSignInClient
) {
    private val auth = Firebase.auth

    fun signIn(): Intent? {
        val result = try {
            googleSignInClient.signInIntent
               //.await()
        } catch (e: Exception){
            Timber.e(e, "Failed to sign in with Google")
            if (e is CancellationException) throw e
            null
        }
        return result
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {

        return try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
            task.result?.let { googleAccount ->

                val googleIdToken = googleAccount.idToken
                Timber.d("idToken: $googleIdToken")
                val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(googleCredentials).await().user

                SignInResult(
                    data = googleAccount.run {
                        UserData(
                            email = email,
                            userName = displayName,
                            profilePictureUrl = photoUrl?.toString(),
                            idToken = googleIdToken,
                        )
                    },
                    errorMessage = null
                )
            } ?: SignInResult(data = null)
        } catch (e: Exception){
            Timber.e(e, "Failed to sign in with Google")
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    fun signOut(callback: () -> Unit) {
        try {
            googleSignInClient.signOut().addOnCompleteListener {
                auth.signOut()
                Timber.i("Signed out successfully")
                callback()
            }
        } catch (e: Exception){
            Timber.e("Failed to sign out with Google", e)
            callback()
        }
    }

    fun getUserData(): UserData? = auth.currentUser?.run {
        UserData(
            email = email,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString(),
            idToken = null,
        )
    }

    suspend fun getSignInUser(): UserData? = auth.currentUser?.run {
        UserData(
            email = email,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString(),
            idToken = getIdToken(false).await()?.token,
        )
    }

    fun getFirebaseUser () : FirebaseUser? {
        return auth.currentUser
    }
}