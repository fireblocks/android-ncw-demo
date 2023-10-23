package com.fireblocks.sdkdemo.ui.signin

import android.content.Context
import android.content.Intent
import com.fireblocks.sdkdemo.R
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.OAuthProvider

/**
 * Created by Fireblocks Ltd. on 06/07/2023.
 */
enum class SignInProvider {
    Google, Apple
}
class SignInUtil {

    private var appleUiClient: AppleUiClient? = null
    var signInProvider: SignInProvider = SignInProvider.Google
    companion object {
        private var instance: SignInUtil? = null
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: SignInUtil().also { instance = it }
                }
    }

    fun getGoogleAuthUiClient(context: Context): GoogleAuthUiClient {
        return GoogleAuthUiClient(
            context = context.applicationContext,
            onTapClient = Identity.getSignInClient(context.applicationContext)
        )
    }

    fun isSignedIn (context: Context): Boolean {
        return when (signInProvider) {
            SignInProvider.Google -> getGoogleSignInClient(context).getFirebaseUser() != null
            SignInProvider.Apple -> getAppleSignInClient().getFirebaseUser() != null
        }
    }

    fun signInWithGoogle(context: Context): Intent? {
        signInProvider = SignInProvider.Google
        return getGoogleSignInClient(context).signIn()
    }

    fun getGoogleSignInClient(context: Context): GoogleAccountUiClient {
        val serverClientId = context.getString(R.string.default_web_client_id)
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(serverClientId)
            .build()

        return GoogleAccountUiClient(
            googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
        )
    }

    fun signInWithApple(context: Context, callback: (result: SignInResult) -> Unit){
        signInProvider = SignInProvider.Apple
        getAppleSignInClient().signIn(context, callback)
    }

    private fun getAppleSignInClient(): AppleUiClient {
        if (appleUiClient == null) {
            val provider = OAuthProvider.newBuilder("apple.com")
            provider.scopes = listOf("email", "name")

            appleUiClient = AppleUiClient(appleProvider = provider)
        }
        return appleUiClient!!
    }

    suspend fun getIdToken(context: Context): String? {
        return when (signInProvider) {
            SignInProvider.Google -> getGoogleSignInClient(context).getSignInUser()?.idToken
            SignInProvider.Apple -> getAppleSignInClient().getSignInUser()?.idToken
        }
    }

    fun signOut(context: Context, callback: () -> Unit) {
        when (signInProvider) {
            SignInProvider.Google -> {
                getGoogleSignInClient(context).signOut(callback)
            }
            SignInProvider.Apple -> {
                appleUiClient = null
                getAppleSignInClient().signOut(callback)
            }
        }
    }

    fun getUserData(context: Context): UserData? {
        return when (signInProvider) {
            SignInProvider.Google -> getGoogleSignInClient(context).getUserData()
            SignInProvider.Apple -> getAppleSignInClient().getUserData()
        }
    }
}