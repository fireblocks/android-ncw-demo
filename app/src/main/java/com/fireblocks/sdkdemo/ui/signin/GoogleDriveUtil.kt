package com.fireblocks.sdkdemo.ui.signin

import android.content.Context
import android.content.Intent
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.extensions.toFormattedTimestamp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream

/**
 * Created by Fireblocks Ltd. on 06/07/2023.
 */
object GoogleDriveUtil {
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        return GoogleSignIn.getClient(context, signInOptions)
    }

    fun getSignInUser(context: Context): UserData?  {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.run {
            UserData(
                email = email,
                userName = displayName,
                profilePictureUrl = photoUrl?.toString(),
                idToken = idToken
            )
        }
    }

    /**
     * https://proandroiddev.com/android-kotlin-jetpack-compose-interacting-with-google-drive-api-v3-2023-the-complete-b8bc1bdbb13b
     */
    fun getPassphraseFromDrive(
        context: Context,
        coroutineScope: CoroutineScope,
        intent: Intent,
        createPassphraseIfMissing: Boolean = false,
        updatePassphrase: Boolean = false,
        deviceId: String,
        callback: (success: Boolean, passphrase: String?, alreadyBackedUp: Boolean, lastBackupDate: String?) -> Unit,
    ) {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
        task.result?.let { googleAccount ->
//                GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->

            // get credentials
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccount = googleAccount.account!!

            // get Drive Instance
            val drive = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()

            var passphrase = ""
            var passphraseFile :java.io.File? = null
            coroutineScope.launch(Dispatchers.IO) {
                runCatching {
                    val passphraseFileName = "${deviceId}_passphrase.txt"
                    val gfile = File()
                    val mimeType = "text/plain"
                    gfile.name = passphraseFileName

                    val parents: MutableList<String> = ArrayList(1)
                    parents.add("appDataFolder") // Here you need to get the parent folder id

                    gfile.parents = parents
                    val files = drive.Files()

                    passphraseFile = java.io.File(context.filesDir, passphraseFileName)
                    try {
                        val fileListResult = files.list().setQ("name='$passphraseFileName'").setSpaces("appDataFolder").setFields("files(id,name,createdTime,modifiedTime, permissions(emailAddress, displayName))").execute()
                        Timber.i("Found ${fileListResult.files.count()} files")
                        fileListResult.files.forEach { file ->
                            Timber.i("Found file: ${file.name}, ${file.id}, ${file.modifiedTime}, ${file.createdTime}")
                            val outputStream = ByteArrayOutputStream()
                            files.get(file.id).executeMediaAndDownloadTo(outputStream)
                            val finalString = String(outputStream.toByteArray())
                            passphrase = finalString
                            Timber.d("Found passphrase: $finalString")
                            if (updatePassphrase) {
                                runCatching {
                                    val updatedFile = File()
                                    passphraseFile?.writeText(passphrase)
                                    val fileContent = FileContent(mimeType, passphraseFile)
                                    files.update(file.id, updatedFile, fileContent).execute()
                                }.onFailure {
                                    Timber.e(it, "Failed to update file")
                                }
                            }
                            val lastBackupDate = file.modifiedTime.value.toFormattedTimestamp(context, R.string.date_timestamp, dateFormat = "MM/dd/yyyy", useSpecificDays = false, useTime = false)
                            finish(callback,true, passphrase, true, lastBackupDate, passphraseFile)
                            return@launch
                        }
                    } catch (e: GoogleJsonResponseException) {
                        Timber.e(e)
                    }

                    if (createPassphraseIfMissing) {
                        if (passphrase.isEmpty()) {
                            passphrase = FireblocksManager.getInstance().getPassphrase(context)
                            passphraseFile?.writeText(passphrase)
                            val fileContent = FileContent(mimeType, passphraseFile)

                            files.create(gfile, fileContent).setFields("id").execute()
                            finish(callback,true, passphrase, false, null, passphraseFile)
                        }
                    } else {
                        finish(callback,false, null, false, null, passphraseFile)
                    }
                }.onFailure {
                    Timber.e(it)
                    finish(callback,false, null, false, null, passphraseFile)

                }
            }
        }
    }

    private fun finish(callback: (success: Boolean, passphrase: String?, alreadyBackedUp: Boolean, lastBackupDate: String?) -> Unit,
                       success: Boolean, passphrase: String?, alreadyBackedUp: Boolean, lastBackupDate: String?,
                       passphraseFile: java.io.File?) {
        passphraseFile?.delete()
        callback(success, passphrase, alreadyBackedUp, lastBackupDate)

    }
}