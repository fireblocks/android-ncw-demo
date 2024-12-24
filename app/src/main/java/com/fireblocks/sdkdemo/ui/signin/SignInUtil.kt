package com.fireblocks.sdkdemo.ui.signin

import android.content.Context
import android.content.Intent
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
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
    private var googleAccountUiClient: GoogleAccountUiClient? = null

    companion object {
        private var instance: SignInUtil? = null
        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: SignInUtil().also { instance = it }
                }
    }

    private var signInProvider: SignInProvider?
        get() = MultiDeviceManager.instance.getLastSignInProvider()?.let {
            SignInProvider.valueOf(it)
        }
        set(value) {
            value?.let {
                MultiDeviceManager.instance.setLastSignInProvider(it.name)
            } ?: MultiDeviceManager.instance.clearLastSignInProvider()
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
            else -> false
        }
    }

    fun signInWithGoogle(context: Context): Intent? {
        signInProvider = SignInProvider.Google
        return getGoogleSignInClient(context).signIn()
    }

    fun getGoogleSignInClient(context: Context): GoogleAccountUiClient {
        if (googleAccountUiClient == null) {
            val serverClientId = context.getString(R.string.default_web_client_id)
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(serverClientId)
                .build()

            googleAccountUiClient = GoogleAccountUiClient(googleSignInClient = GoogleSignIn.getClient(context, signInOptions))
        }
        return googleAccountUiClient!!
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

    suspend fun getIdTokenBlocking(context: Context): String? {
        return when (signInProvider) {
            SignInProvider.Google -> getGoogleSignInClient(context).getSignInUser()?.idToken
            SignInProvider.Apple -> getAppleSignInClient().getSignInUser()?.idToken
            else -> null

        }
    }

    fun signOut(context: Context, callback: (() -> Unit)? = null) {
        when (signInProvider) {
            SignInProvider.Google -> {
                googleAccountUiClient = null
                signInProvider = null
                callback?.let { getGoogleSignInClient(context).signOut(it) }
            }
            SignInProvider.Apple -> {
                appleUiClient = null
                signInProvider = null
                callback?.let { getAppleSignInClient().signOut(it) }
            }
            else -> callback?.invoke()
        }
        MultiDeviceManager.instance.setSplashScreenSeen(false)
    }

    fun getUserData(context: Context): UserData? {
        return when (signInProvider) {
            SignInProvider.Google -> getGoogleSignInClient(context).getUserData()
            SignInProvider.Apple -> getAppleSignInClient().getUserData()
            else -> null
        }
    }
}