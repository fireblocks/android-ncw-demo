package com.fireblocks.sdkdemo.bl.dialog

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.os.ResultReceiver
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fireblocks.sdkdemo.R
import com.fireblocks.sdkdemo.bl.core.base.OnActivityAction
import com.fireblocks.sdkdemo.bl.core.base.postOnActivity
import com.fireblocks.sdkdemo.bl.core.extensions.isNotNullAndNotEmpty
import com.fireblocks.sdkdemo.bl.core.extensions.postOnMain
import com.fireblocks.sdkdemo.bl.core.extensions.put
import kotlinx.coroutines.*
import timber.log.Timber


/**
 * Created by Fireblocks Ltd. on 13/03/2023.
 */
class DialogUtil {
//    private var resultReceiver: ResultReceiver? = null
    private var job: Job? = null

    companion object {
        const val EDIT_FIELD_TEXT = "EditFieldText"
        const val CHECKED_ITEMS = "CheckedItems"
        const val SELECTED_ITEM = "SelectedItem"
        private var dialog: AlertDialog? = null
        private var dialogUtilInstance: DialogUtil? = null


        fun getInstance(): DialogUtil {
            if (dialogUtilInstance == null) {
                dialogUtilInstance = DialogUtil()
            }
            return dialogUtilInstance!!
        }
    }

    fun start(title: String?,
              message: String?,
              buttonText: String?,
              negativeButtonText: String? = null,
              resultReceiver: ResultReceiver? = null,
              editField: Boolean = false,
              @StyleRes themeResId: Int? = null,
              canceledOnTouchOutside: Boolean? = false,
              @LayoutRes editTextLayoutResId: Int? = null,
              postOnMainThread: Boolean = false,
              closeActiveDialog: Boolean = false,
              autoCloseTimeInMillis: Long? = null,
              inputText: String? = null,
              items: Set<String>? = null,
              preSelectedItemIndex: Int = 0,
              showSingleChoice: Boolean = false,
              showMultiChoice: Boolean = false,
    ) {
        if (closeActiveDialog) {
            closeActiveDialog()
        }

        postOnActivity(object : OnActivityAction {
            override fun onActivityAvailable(activity: Activity) {
                if (postOnMainThread) {
                    postOnMain {
                        showAlertDialog(activity,
                            title,
                            message,
                            buttonText,
                            negativeButtonText,
                            resultReceiver,
                            editField,
                            themeResId,
                            canceledOnTouchOutside,
                            editTextLayoutResId,
                            autoCloseTimeInMillis,
                            inputText,
                            items,
                            preSelectedItemIndex,
                            showSingleChoice,
                            showMultiChoice)
                    }
                } else {
                    showAlertDialog(activity,
                        title,
                        message,
                        buttonText,
                        negativeButtonText,
                        resultReceiver,
                        editField,
                        themeResId,
                        canceledOnTouchOutside,
                        editTextLayoutResId,
                        autoCloseTimeInMillis,
                        inputText,
                        items,
                        preSelectedItemIndex,
                        showSingleChoice,
                        showMultiChoice)
                }
            }
        })
    }

    private fun closeActiveDialog() {
        dialog?.let {
            if (it.isShowing) {
                cancel(it)
            }
            dialog = null
        }
    }

    private fun cancel(dialog: DialogInterface, dialogResultReceiver: ResultReceiver? = null) {
        try {
            dialog.dismiss()
            job?.cancel()
        } catch (e: IllegalArgumentException) {
            Timber.w(e)
        }
        dialogResultReceiver?.send(AppCompatActivity.RESULT_CANCELED, Bundle())
//        resultReceiver?.send(AppCompatActivity.RESULT_CANCELED, Bundle())
//        resultReceiver = null
    }

    private fun showAlertDialog(activity: Activity,
                                title: String?,
                                message: String?,
                                positiveButtonText: String?,
                                negativeButtonText: String? = null,
                                dialogResultReceiver: ResultReceiver? = null,
                                shouldHaveEditField: Boolean = false,
                                @StyleRes theme: Int? = null,
                                canceledOnTouchOutside: Boolean? = false,
                                @LayoutRes editTextLayoutResId: Int? = null,
                                autoCloseTimeInMillis: Long? = null,
                                inputText: String? = null,
                                items: Set<String>? = null,
                                preSelectedItemIndex: Int = 0,
                                showSingleChoice: Boolean = false,
                                showMultiChoice: Boolean = false,) {
//        resultReceiver = dialogResultReceiver
        var input: EditText? = null
        val useCustomEditText = editTextLayoutResId != null
        val hasNegativeButton = negativeButtonText.isNotNullAndNotEmpty()

        val itemsArray = arrayOfNulls<String>(items?.size ?: 0)
        val checkedItemsIndices = BooleanArray(items?.size ?: 0)
        val checkedItems = arrayListOf<String>()
        var selectedItemIndex = preSelectedItemIndex

        @StyleRes var themeResId: Int = R.style.DialogTheme
        if (theme != null) {
            themeResId = theme
        }
        val alertDialogBuilder = AlertDialog.Builder(activity, themeResId)
        if (hasNegativeButton) {
            alertDialogBuilder.setNegativeButton(negativeButtonText) { dialog, _ ->
                cancel(dialog, dialogResultReceiver)
            }
        }

        alertDialogBuilder.setTitle(title).setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            val bundle = Bundle()
            if (shouldHaveEditField) {
                input?.text?.toString()?.trim()?.let {
                    bundle.put(EDIT_FIELD_TEXT, it)
                }
            } else if (!items.isNullOrEmpty()) {
                if (showMultiChoice) {
                    itemsArray.forEachIndexed { index, item ->
                        if (item != null && checkedItemsIndices[index]) {
                            checkedItems.add(item)
                        }
                    }
                    bundle.put(CHECKED_ITEMS, checkedItems)
                } else if (showSingleChoice) {
                    val item = itemsArray[selectedItemIndex]
                    bundle.put(SELECTED_ITEM, item)
                }
            }
            //resultReceiver?.send(Activity.RESULT_OK, bundle)
            dialogResultReceiver?.send(Activity.RESULT_OK, bundle)
        }.setCancelable(canceledOnTouchOutside == true)

        if (!items.isNullOrEmpty()) {
            items.forEachIndexed { index, item ->
                itemsArray[index] = item
                checkedItemsIndices[index] = true
            }

            if (showMultiChoice) {
                alertDialogBuilder.setMultiChoiceItems(itemsArray, checkedItemsIndices) { _, which, isChecked ->
                    // Update the current focused item's checked status
                    checkedItemsIndices[which] = isChecked
                }
            } else if (showSingleChoice){
                alertDialogBuilder.setSingleChoiceItems(itemsArray, preSelectedItemIndex) { _, which ->
                    selectedItemIndex = which
                }
            }
        } else {
            alertDialogBuilder.setMessage(message)
        }

        dialog = alertDialogBuilder.create()

        dialog?.let { dialog ->
            dialog.setOnShowListener {
                val rootView: View = dialog.window!!.decorView.findViewById(android.R.id.content)
                rootView.isFocusable = true
                rootView.isFocusableInTouchMode = true
                rootView.requestFocus()

                autoCloseTimeInMillis?.let {
                    job = CoroutineScope(Dispatchers.Main).launch {
                        // Hide after some seconds
                        delay(it)
                        closeActiveDialog()
                    }
                }
            }

            if (shouldHaveEditField) {
                if (useCustomEditText) {
                    val customView = activity.layoutInflater.inflate(editTextLayoutResId!!, null)
                    dialog.setView(customView)
                    input = customView.findViewById(R.id.dialog_activity_edit_field_id)
                } else {
                    input = EditText(activity)
                    input?.id = R.id.dialog_activity_edit_field_id
                    dialog.setView(input)
                }
                inputText?.let {
                    input?.setText(it)
                }
            }
            setCanceledOnTouchOutside(dialog, canceledOnTouchOutside == true)
            dialog.show()

//            if (useCustomEditText) {
//                dialog.findViewById<View>(R.id.contentPanel)?.minimumHeight = 0
//            }

            alignPositiveButtonToCenter(dialog, hasNegativeButton)


        }

//        DialogDisplayed("$title#$message").execute()

        dialog?.setOnDismissListener {
            closeActiveDialog()
        }
    }

    private fun alignPositiveButtonToCenter(dialog: AlertDialog, hasNegativeButton: Boolean) {
        if (!hasNegativeButton) {
            val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            //Disable the material spacer view in case there is one
            val parent = btnPositive.parent as? LinearLayout
            parent?.gravity = Gravity.CENTER_HORIZONTAL
            val leftSpacer = parent?.getChildAt(1)
            leftSpacer?.visibility = View.GONE
        }
    }

    private fun setCanceledOnTouchOutside(dialog: AlertDialog, canceledOnTouchOutside: Boolean) {
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
        if (canceledOnTouchOutside) {
            dialog.setOnCancelListener {
                cancel(dialog)
            }
        }
    }
}