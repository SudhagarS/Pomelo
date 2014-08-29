package com.pomeloapp.pomelo.fragment;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.pomeloapp.pomelo.R;
import com.pomeloapp.pomelo.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DeleteFileDialogFragment extends DialogFragment
{

    private String mOldName;

    public interface DeleteFileDialogListener
    {
        public void onDeleted();
    }

    DeleteFileDialogListener mListener;

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
            mListener = (DeleteFileDialogListener) activity;
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
        StringBuilder messageBuilder = new StringBuilder(getActivity().
                getResources().getString(R.string.delete_confirm));
        messageBuilder.append(" ");
        messageBuilder.append(mOldName);
        messageBuilder.append("?");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete)
                .setMessage(messageBuilder.toString())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                 {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mListener.onDeleted();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
