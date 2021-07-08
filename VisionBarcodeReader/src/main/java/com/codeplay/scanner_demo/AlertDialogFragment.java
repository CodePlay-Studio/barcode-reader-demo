package com.codeplay.scanner_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Tham on 7/23/15.
 */
public class AlertDialogFragment extends DialogFragment {
    public static final String TAG = AlertDialogFragment.class.getSimpleName();
    // constants used to pass extra data in the intent
    public static final String EXT_ICON  = "mobile_handover.dialogfragment.extra.ICON";
    public static final String EXT_TITLE = "mobile_handover.dialogfragment.extra.TITLE";
    public static final String EXT_MSG   = "mobile_handover.dialogfragment.extra.MESSAGE";
    public static final String EXT_STR_MSG = "mobile_handover.dialogfragment.extra.STRING_MESSAGE";
    public static final String EXT_BUTTON_POS = "mobile_handover.dialogfragment.extra.POS_TEXT_ID";
    public static final String EXT_BUTTON_NEG = "mobile_handover.dialogfragment.extra.NEG_TEXT_ID";
    public static final String EXT_TYPE  = "mobile_handover.dialogfragment.extra.TYPE";
    private DialogInterface.OnClickListener onNeutralClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            final Fragment fragment = AlertDialogFragment.this.getTargetFragment();
            if (fragment!=null)
                fragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
        }
    };
    private DialogInterface.OnClickListener onPositiveClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            final Fragment fragment = AlertDialogFragment.this.getTargetFragment();
            if (fragment!=null)
                fragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
        }
    };
    private DialogInterface.OnClickListener onNegativeClicked = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            final Fragment fragment = AlertDialogFragment.this.getTargetFragment();
            if (fragment!=null)
                fragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        }
    };

    static AlertDialogFragment newInstance(int icon, int title, int msg, String message, int buttonPosTextId, int buttonNegTextId, boolean isNeutral) {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();

        Bundle args = new Bundle();
        if (icon>0)
            args.putInt(EXT_ICON, icon);

        if (title>0)
            args.putInt(EXT_TITLE, title);

        if (msg>0)
            args.putInt(EXT_MSG, msg);

        if (message!=null)
            args.putString(EXT_STR_MSG, message);

        if (buttonPosTextId>0)
            args.putInt(EXT_BUTTON_POS, buttonPosTextId);

        if (buttonNegTextId>0)
            args.putInt(EXT_BUTTON_NEG, buttonNegTextId);

        args.putBoolean(EXT_TYPE, isNeutral);
        alertDialogFragment.setArguments(args);

        return alertDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean isNeutral = args.getBoolean(EXT_TYPE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(args.getInt(EXT_ICON, 0));

        if (args.containsKey(EXT_TITLE))
            builder.setTitle(args.getInt(EXT_TITLE));

        if (args.containsKey(EXT_MSG))
            builder.setMessage(args.getInt(EXT_MSG));

        if (args.containsKey(EXT_STR_MSG))
            builder.setMessage(args.getString(EXT_STR_MSG));

        if (isNeutral) {
            builder.setNeutralButton(args.getInt(EXT_BUTTON_POS, android.R.string.ok), onNeutralClicked);
        } else {
            builder.setPositiveButton(args.getInt(EXT_BUTTON_POS, android.R.string.yes), onPositiveClicked)
                    .setNegativeButton(args.getInt(EXT_BUTTON_NEG, android.R.string.no), onNegativeClicked);
        }
        //builder.setCancelable(false);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // AlertDialog.Builder.setCancellable is not applicable to DialogFragment, use setCancellable() instead.
        setCancelable(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
