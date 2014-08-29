package com.pomeloapp.pomelo.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.pomeloapp.pomelo.R;
import com.pomeloapp.pomelo.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class RenameDialogFragment extends DialogFragment
{
    private String mOldName;

    public interface RenameDialogListener
    {
        public void onRenamed(String newName);
    }

    RenameDialogListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mOldName = getArguments().getString(Constants.KEY_OLD_NAME);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (RenameDialogListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final EditText input = new EditText(getActivity());
        input.setText(mOldName);
        input.setSelectAllOnFocus(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.rename);
        builder.setView(input);

        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                mListener.onRenamed(input.getText().toString());
            }
        });
        return builder.create();
    }
}
