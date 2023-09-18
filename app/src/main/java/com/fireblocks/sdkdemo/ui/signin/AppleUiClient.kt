package com.fireblocks.sdkdemo.ui.signin

import android.content.Context
import com.fireblocks.sdkdemo.bl.core.extensions.findActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Created by Fireblocks ltd. on 10/08/2023.
 */
class AppleUiClient(
    private val appleProvider: OAuthProvider.Builder
) {
    private val auth = Firebase.auth

    fun signOut(callback: () -> Unit) {
        try {
            auth.signOut()
            callback()
        } catch (e: Exception){
            Timber.e("Failed to sign out with Apple", e)
            callback()
        }
    }

    fun signIn(context: Context, callback: (result: SignInResult) -> Unit) {
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                Timber.d("checkPending:onSuccess:$authResult")
                // Get the user profile with authResult.getUser() and
                // authResult.getAdditionalUserInfo(), and the ID
                // token from Apple with authResult.getCredential().

                val firebaseUser = authResult.user
                val signInResult = SignInResult(
                    data = firebaseUser?.run {
                        UserData(
                            email = email,
                            userName = displayName,
                            profilePictureUrl = photoUrl?.toString(),
                            idToken = null,
                        )
                    },
                    errorMessage = null
                )
                callback(signInResult)
            }.addOnFailureListener { e ->
                Timber.w("checkPending:onFailure", e)
                val signInResult = SignInResult(
                    errorMessage = e.message
                )
                callback(signInResult)
            }
        } else {
            Timber.d("pending: null")
            auth.startActivityForSignInWithProvider(context.findActivity(), appleProvider.build())
                .addOnSuccessListener { authResult ->
                    // Sign-in successful!
                    Timber.d( "activitySignIn:onSuccess:${authResult.user}")
                    val signInResult = SignInResult(
                        data = authResult.user?.run {
                            UserData(
                                email = email,
                                userName = displayName,
                                profilePictureUrl = photoUrl?.toString(),
                                idToken = null,
                            )
                        },
                        errorMessage = null
                    )
                    callback(signInResult)
                    // ...
                }
                .addOnFailureListener { e ->
                    Timber.w("activitySignIn:onFailure", e)
                    val signInResult = SignInResult(
                        errorMessage = e.message
                    )
                    callback(signInResult)
                }
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