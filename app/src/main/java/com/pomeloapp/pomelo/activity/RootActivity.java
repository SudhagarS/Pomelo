package com.pomeloapp.pomelo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.pomeloapp.pomelo.R;
import com.pomeloapp.pomelo.fragment.DeleteFileDialogFragment;
import com.pomeloapp.pomelo.fragment.FilesListFragment;
import com.pomeloapp.pomelo.fragment.RenameDialogFragment;
import com.pomeloapp.pomelo.fragment.TaskDialogFragment;
import com.pomeloapp.pomelo.util.Constants;


public class RootActivity extends Activity implements
        TaskDialogFragment.TaskDialogListener,
        RenameDialogFragment.RenameDialogListener,
        DeleteFileDialogFragment.DeleteFileDialogListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lay_root);
        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new FilesListFragment(), Constants.FRAG_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.root, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FilesListFragment fragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FRAG_TAG);
        menu.findItem(R.id.action_paste).setVisible(fragment.isCutOrCopyMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_paste)
        {
            FilesListFragment fragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FRAG_TAG);
            fragment.initPaste();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Log.d(Constants.FRAG_TAG, "onBackPressed");
        FilesListFragment fragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FRAG_TAG);
        if(fragment!=null && !fragment.isCurDirRoot())
        {
            Log.d(Constants.FRAG_TAG, "onBackPressed, current is not root.");
            fragment.goOneLevelUp();
            fragment.resetAdapter();
            fragment.setScrollPos();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onTaskPicked(int task)
    {
        FilesListFragment fragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FRAG_TAG);
        if(fragment!=null)
        {
            fragment.onTaskPicked(task);
        }
    }

    @Override
    public void onRenamed(String newName)
    {
        FilesListFragment fragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FRAG_TAG);
        if(fragment!=null)
        {
            fragment.onRenamed(newName);
        }
    }

    @Override
    public void onDeleted()
    {
        FilesListFragment fragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FRAG_TAG);
        if(fragment!=null)
        {
            fragment.onDeleted();
        }
    }

}
