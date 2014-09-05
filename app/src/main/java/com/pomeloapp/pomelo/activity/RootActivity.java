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
import com.pomeloapp.pomelo.util.Constants;


public class RootActivity extends Activity implements
        RenameDialogFragment.RenameDialogListener,
        DeleteFileDialogFragment.DeleteFileDialogListener
{
    private FilesListFragment mFilesListFragment;

    private FilesListFragment getFilesListFragment()
    {
        if(mFilesListFragment==null)
        {
            mFilesListFragment = (FilesListFragment) getFragmentManager().findFragmentByTag(Constants.FILES_LIST_FRAG_TAG);
        }
        return mFilesListFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lay_root);
        if (savedInstanceState == null)
        {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new FilesListFragment(), Constants.FILES_LIST_FRAG_TAG)
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
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        FilesListFragment fragment = getFilesListFragment();
        menu.findItem(R.id.action_paste).setVisible(fragment.isCutOrCopyMode());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_paste)
        {
            FilesListFragment fragment = getFilesListFragment();
            fragment.initPaste();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Log.d(Constants.LOG_TAG, "onBackPressed");
        Log.d(Constants.LOG_TAG, "Share fragment"+getFragmentManager().findFragmentByTag(Constants.SHARE_FRAG_TAG));
        Log.d(Constants.LOG_TAG, "files fragment"+getFragmentManager().findFragmentByTag(Constants.FILES_LIST_FRAG_TAG));

        if(getFragmentManager().findFragmentByTag(Constants.SHARE_FRAG_TAG)!=null)
        {
            boolean popped = getFragmentManager().popBackStackImmediate();
            Log.d(Constants.LOG_TAG, "popped value is " + popped);
            return;
        }

        FilesListFragment fragment = getFilesListFragment();
        if(fragment!=null && !fragment.isCurDirRoot())
        {
            Log.d(Constants.LOG_TAG, "onBackPressed, current is not root.");
            fragment.goOneLevelUp();
            fragment.resetAdapter();
            fragment.setScrollPos();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onRenamed(String newName)
    {
        FilesListFragment fragment = getFilesListFragment();
        if(fragment!=null)
        {
            fragment.onRenamed(newName);
        }
    }

    @Override
    public void onDeleted()
    {
        FilesListFragment fragment = getFilesListFragment();
        if(fragment!=null)
        {
            fragment.onDeleted();
        }
    }

}
