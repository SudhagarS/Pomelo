package com.pomeloapp.pomelo.fragment;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.pomeloapp.pomelo.R;

public class TaskDialogFragment extends DialogFragment
{
    public interface TaskDialogListener
    {
        public void onTaskPicked(int task);
    }

    TaskDialogListener mListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (TaskDialogListener) activity;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_task)
                .setItems(R.array.task_list, new DialogInterface.OnClickListener()
                {
                    @Override

                    public void onClick(DialogInterface dialog, int task) {
                        mListener.onTaskPicked(task);
                    }
                });

        return builder.create();
    }

}
