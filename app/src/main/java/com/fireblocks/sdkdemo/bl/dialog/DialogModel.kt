package com.fireblocks.sdkdemo.bl.dialog

import android.os.ResultReceiver
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import com.fireblocks.sdkdemo.R

/**
 * Created by Fireblocks ltd. on 2020-01-12
 */
@Keep
data class DialogModel(val title: String,
                       val subtitle: String,
                       val positiveButtonText: String,
                       val negativeButtonText: String? = "",
                       val resultReceiver: ResultReceiver? = null,
                       val shouldHaveEditText: Boolean = false,
                       @StyleRes val themeResId: Int = R.style.DialogTheme,
                       val canceledOnTouchOutside: Boolean? = false,
                       @LayoutRes val editTextLayoutResId: Int? = null,
                       val dialogType: DialogType = DialogType.AlertDialog) {

    constructor(
        title: String,
        subtitle: String,
        positiveButtonText: String,
        negativeButtonText: String = "",
        resultReceiver: ResultReceiver? = null
    ) : this(title, subtitle, positiveButtonText, negativeButtonText, resultReceiver, false)

    companion object {
        private const val CLEAR_DIALOG: String = "CLEAR_DIALOG"
        val CLEAR_DIALOG_MODEL = DialogModel(CLEAR_DIALOG, CLEAR_DIALOG, CLEAR_DIALOG)
    }


}

enum class DialogType {
    AlertDialog,
//    TextBottomSheetDialog
}