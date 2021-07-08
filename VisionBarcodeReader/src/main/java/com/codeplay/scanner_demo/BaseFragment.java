package com.codeplay.scanner_demo;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * An non-instantiatable abstract base class for Fragments.
 *
 * Created by Tham on 16/07/16.
 */
public abstract class BaseFragment extends Fragment {
    public interface FragmentEventListener {
        void setFragment(BaseFragment fragment);
        void onResume(int index);
    }
    protected FragmentEventListener eventsListener;

    public abstract boolean onBackPressed();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            eventsListener = (FragmentEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentEventListener");
        }
    }

    protected void showDialog(Fragment targetFragment, int requestCode, int titleStringId, int messageStringId, String message, int buttonPosTextId, int buttonNegTextId, boolean dialogType) {
        // Remove any currently showing dialog.
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        Fragment dialogFragmet = getParentFragmentManager().findFragmentByTag(AlertDialogFragment.TAG);
        if (dialogFragmet!=null) {
            fragmentTransaction.remove(dialogFragmet);
        }

        AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(0, titleStringId, messageStringId, message, buttonPosTextId, buttonNegTextId, dialogType);
        if (targetFragment!=null)
            alertDialogFragment.setTargetFragment(targetFragment, requestCode);
        alertDialogFragment.show(fragmentTransaction, AlertDialogFragment.TAG);
    }
}
