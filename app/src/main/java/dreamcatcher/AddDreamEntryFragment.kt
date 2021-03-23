package edu.vt.cs.cs5254.dreamcatcher

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import edu.vt.cs.cs5254.dreamcatcher.util.KeyboardUtil.hideSoftKeyboard


class AddDreamEntryFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Inflating the dialog view
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_dialog_add_dream_entry, null)
        // Edit text field for comment
        val comment = view.findViewById<EditText>(R.id.comment_text)
        return AlertDialog.Builder(activity!!)
            .setView(view)
                // Listener for ok button
            .setPositiveButton(
                android.R.string.ok
            ) { _: DialogInterface?, _: Int ->
                sendResult(
                    Activity.RESULT_OK,
                    comment.text.toString()
                )
                // Hides the keyboard once the user clicks ok
                hideSoftKeyboard(context!!, view)
            }
                // Listener for Cancel button
            .setNegativeButton(
                android.R.string.cancel

            ) { _: DialogInterface?, _: Int ->}
            .create()
    }

    // Sends the response from the dialog fragment back to the detail activity fragment
    private fun sendResult(requestCode: Int, text: String) {
        if (targetFragment == null) return

        val intent = Intent()
        intent.putExtra(EXTRA_COMMENT, text)
        targetFragment?.onActivityResult(targetRequestCode, requestCode, intent)

    }

    companion object {
        const val EXTRA_COMMENT = "comment"
    }
}