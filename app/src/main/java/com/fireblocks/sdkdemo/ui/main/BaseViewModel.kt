package com.fireblocks.sdkdemo.ui.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fireblocks.sdk.Fireblocks
import com.fireblocks.sdkdemo.FireblocksManager
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.MultiDeviceManager
import com.fireblocks.sdkdemo.bl.core.extensions.fingerPrintCancelledDialogModel
import com.fireblocks.sdkdemo.bl.core.storage.StorageManager
import com.fireblocks.sdkdemo.bl.core.storage.models.BackupInfo
import com.fireblocks.sdkdemo.bl.dialog.DialogModel
import com.fireblocks.sdkdemo.bl.dialog.DialogType
import com.fireblocks.sdkdemo.bl.dialog.DialogUtil
import com.fireblocks.sdkdemo.log.filelogger.FileManager
import com.fireblocks.sdkdemo.ui.observers.ObservedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.time.Duration
import java.util.Date
import java.util.Locale

/**
 * Created by Fireblocks Ltd. on 23/03/2023.
 */
open class BaseViewModel: ViewModel(), DefaultLifecycleObserver {

    private val _userFlow = MutableStateFlow<UiState>(UiState.Idle)
    val userFlow: StateFlow<UiState> = _userFlow.asStateFlow()

    fun updateUserFlow(state: UiState) {
        _userFlow.update { state }
    }

    open fun clean() {
        _userFlow.update { UiState.Idle }
    }

    fun showProgress(show: Boolean) {
        when (show) {
            true -> updateUserFlow(UiState.Loading)
            false -> updateUserFlow(UiState.Idle)
        }
    }

    open fun onError(showError: Boolean = true) {
        if (showError) {
            showError()
        }
    }

    fun showError() {
        updateUserFlow(UiState.Error())
    }

    fun showError(error: UiState.Error = UiState.Error()) {
        updateUserFlow(error)
    }

    fun emailSDKLogs(context: Context) {
        val uri = Fireblocks.getUriForLogFiles(context)
        if (uri == null) {
            DialogUtil.getInstance().start("Can't send logs",
                "Unable to create zipFile",
                context.getString(R.string.OK))
            return
        }

        val email =  ""
        val emailIntent = createEmailIntent(context, uri, email)
        val intent = Intent.createChooser(emailIntent, "Pick an Email provider")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun emailAllLogs(context: Context) {
        val fileManager = FileManager()
        val email =  ""
        Timber.i(prepareLogData(context))

        fileManager.zipLogs(context) { file, error ->
            if (error != null || (file?.exists() == false || file?.canRead() == false)) {
                DialogUtil.getInstance().start("Can't send logs",
                    "Unable to create zipFile",
                    context.getString(R.string.OK))
                return@zipLogs
            }
            emailAllLogs(context, file!!, email)
        }
    }

    private fun createEmailIntent(context: Context, attachmentUri: Uri, email: String): Intent {
        return createEmailIntent(context, arrayListOf(attachmentUri), email)
    }

    private fun createEmailIntent(context: Context, uris: ArrayList<Uri>, email: String): Intent {
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        emailIntent.type = "vnd.android.cursor.dir/email"

        emailIntent.putExtra(Intent.EXTRA_TEXT, prepareLogData(context))
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email)) //TODO fix bug here, when sending list of emails
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "LogFiles Fireblocks: ${Date()}")
        return emailIntent
    }

    private fun emailAllLogs(context: Context, file: File, email: String) {
        val authority = context.packageName + ".provider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val sdkUri = Fireblocks.getUriForLogFiles(context)

        val intentBuilder = ShareCompat.IntentBuilder(context)
            .setType(context.contentResolver.getType(uri))
            .setSubject("LogFiles Fireblocks: ${Date()}")
            .setText(prepareLogData(context))
            .addStream(uri)
            .setEmailTo(arrayOf(email))
            .setChooserTitle("Pick an Email provider")

        sdkUri?.let { intentBuilder.addStream(sdkUri)}
        intentBuilder.intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentBuilder.startChooser()
    }

    private fun prepareLogData(context: Context): String {
        return "Fireblocks version:${getVersionString(context, context.packageName)}\n" +
                "Fireblocks NCW version:${getNCWVersion()}\n" +
                "\n${MultiDeviceManager.instance.usersStatus(context)}\n\n${
            valuableInfo()
        }"
    }

    fun getNCWVersion(): String {
        return "${com.fireblocks.sdk.BuildConfig.VERSION_NAME}_${com.fireblocks.sdk.BuildConfig.VERSION_CODE}"
    }

    private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                getPackageInfo(packageName, flags)
            }

    private fun getVersionString(context: Context, packageName: String): String {
        val packageManager = context.packageManager.getPackageInfoCompat(packageName, 0)
        return "${packageManager.versionName}_${getVersionCode(context, packageName)}"
    }

    @Suppress("DEPRECATION")
    private fun getVersionCode(context: Context, packageName: String): Long {
        val packageManager = context.packageManager.getPackageInfoCompat(packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) {
            packageManager.longVersionCode
        } else {
            packageManager.versionCode.toLong()
        }
    }

    private fun valuableInfo(): String {
        val androidVersion = Build.VERSION.SDK_INT
        val deviceName = "${Build.MANUFACTURER}/${Build.DEVICE}/${Build.MODEL}/${Build.PRODUCT}"

        val uptime = Duration.ofMillis(SystemClock.uptimeMillis()).toString().replace( "P" , "" ).replace( "T", " " ).lowercase(Locale.getDefault())
        return "\nAndroidVersion: $androidVersion" +
                "\nDevice: $deviceName" +
                "\nUptime: $uptime" +
                "\nOSVersion: ${System.getProperty("os.version")}" +
                "\nVersionRelease: ${Build.VERSION.RELEASE}" +
                "\nVersionIncremental: ${Build.VERSION.INCREMENTAL}" +
                "\nBoard: ${Build.BOARD}" +
                "\nBrand: ${Build.BRAND}" +
                "\nHost: ${Build.HOST}" +
                "\nId: ${Build.ID}" +
                "\nDisplay: ${Build.DISPLAY}"
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        dialogListener.removeObservers(owner)
        snackBar.removeObservers(owner)
    }

    private val dialogListener = MutableLiveData<ObservedData<DialogModel>>()
    private fun dialogListener(): LiveData<ObservedData<DialogModel>> = dialogListener

    fun observeDialogListener(lifecycleOwner: LifecycleOwner, context: Context) {
        FireblocksManager.getInstance().updateKeyStorageViewModel(getDeviceId(context), this)
        dialogListener().observe(lifecycleOwner) { observedEvent ->
            observedEvent.contentIfNotHandled?.let { value ->
                if (value == DialogModel.CLEAR_DIALOG_MODEL) {
                    return@observe
                }
                when (value.dialogType) {
                    DialogType.AlertDialog -> {
                            DialogUtil.getInstance().start(value.title,
                                value.subtitle,
                                value.positiveButtonText,
                                value.negativeButtonText,
                                value.resultReceiver,
                                value.shouldHaveEditText,
                                value.themeResId,
                                value.canceledOnTouchOutside,
                                value.editTextLayoutResId)
                    }
                }
                onDialogListenerHandled()
            }
        }
    }

    private fun onDialogListenerHandled() {
        dialogListener.postValue(ObservedData(DialogModel.CLEAR_DIALOG_MODEL))
    }

    internal val onFingerprintCancelled: suspend (context: Context, load: Boolean) -> Boolean = { context, load ->
        Timber.i("On fingerprint cancelled, is loading data: $load")
        val result = suspendCancellableCoroutine { cont ->
            dialogListener.postValue(ObservedData(fingerPrintCancelledDialogModel(context, cont)))
        }

        Timber.i("Returning result:$result")
        result
    }

    val snackBar = MutableLiveData<ObservedData<String>>()
    fun snackBar(): LiveData<ObservedData<String>> = snackBar

    val passLogin = MutableLiveData<ObservedData<Boolean>>()
    fun onPassLogin(): LiveData<ObservedData<Boolean>> = passLogin

    fun getDeviceId(context: Context): String {
        return FireblocksManager.getInstance().getDeviceId(context)
    }

    fun getBackupInfo(context: Context, callback: (backupInfo: BackupInfo?) -> Unit) {
        showProgress(true)
        runCatching {
            val walletId = StorageManager.get(context, getDeviceId(context)).walletId.value()
            FireblocksManager.getInstance().getLatestBackupInfo(context, walletId) { backupInfo ->
                if (backupInfo == null) {
                    onError()
                    callback( null)
                } else {
                    callback(backupInfo)
                }
            }
        }.onFailure {
            Timber.e(it)
            onError()
            snackBar.postValue(ObservedData("${it.message}"))
            callback( null)
        }
    }

    fun hasKeys(context: Context, deviceId: String = getDeviceId(context)): Boolean {
        return FireblocksManager.getInstance().hasKeys(context, deviceId)
    }
}