package com.pomeloapp.pomelo.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.pomeloapp.pomelo.R;
import com.pomeloapp.pomelo.adapter.FilesArrayAdapter;
import com.pomeloapp.pomelo.util.Constants;
import com.pomeloapp.pomelo.util.FileUtil;
import com.pomeloapp.pomelo.util.StorageUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * A placeholder fragment containing a simple view.
 */
public class FilesListFragment extends Fragment
{
    private static final int TASK_SHARE = 0;
    private static final int TASK_RENAME = 1;
    private static final int TASK_COPY = 2;
    private static final int TASK_CUT = 3;
    private static final int TASK_DELETE = 4;

    private boolean mCutOrCopyMode; //used in activity to know if paste menu item should be visible
    private boolean mCut;

    private File mCurrentDir;
    private ListView mListView;
    private FilesArrayAdapter mAdapter;
    private Map<String, List<File>> mFilesListCache = new HashMap<String, List<File>>();
    private File mRootDir;
    private Stack<Integer> mScrollPosStack = new Stack<Integer>();

    private void showToast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int id)
    {
        showToast(getActivity().getResources().getString(id));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.lay_files_frag, container, false);
        List<File> files = null;
        if(StorageUtil.isExternalStorageReadable())
        {
            mRootDir = mCurrentDir = Environment.getExternalStorageDirectory();
            try
            {
                Log.d(Constants.LOG_TAG, "canonical path is " + mRootDir.getCanonicalPath());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            Log.d(Constants.LOG_TAG, "absolute path is " + mRootDir.getAbsolutePath());
            Log.d(Constants.LOG_TAG, "path is " + mRootDir.getPath());
            files = getCurFilesList();
        }
        mListView = (ListView) rootView.findViewById(R.id.listview_files);
        mAdapter = new FilesArrayAdapter(getActivity(), R.layout.lay_files_adapter, files);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new FileItemClickListener());
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(mMultiModeListener);
        return rootView;
    }

    private Integer[] mLastSelectedItems;
    private File[] mLastSelectedFiles;

    private AbsListView.MultiChoiceModeListener mMultiModeListener = new AbsListView.MultiChoiceModeListener()
    {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
        {
            mAdapter.toggleItemSelection(position);
            mode.setTitle(mAdapter.getSelectedItems().size() + "/" + mAdapter.getCount());
            mode.invalidate();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getActivity().getMenuInflater().inflate(R.menu.contxtl_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            MenuItem renameItem = menu.findItem(R.id.action_rename);
            MenuItem shareItem = menu.findItem(R.id.action_share);
            if(mAdapter.getSelectedItems().size()>1)
            {
                renameItem.setVisible(false);
                shareItem.setVisible(false);
            }
            else
            {
                renameItem.setVisible(true);
                shareItem.setVisible(true);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            Object[] tempObjArr = mAdapter.getSelectedItems().toArray();
            mLastSelectedItems = Arrays.copyOf(tempObjArr, tempObjArr.length, Integer[].class);
            mLastSelectedFiles = getLastSelectedFiles(mAdapter.getSelectedItems());
            switch(item.getItemId())
            {
                case R.id.action_share:
                    onTaskPicked(TASK_SHARE);
                    break;

                case R.id.action_rename:
                    onTaskPicked(TASK_RENAME);
                    break;

                case R.id.action_copy:
                    onTaskPicked(TASK_COPY);
                    break;

                case R.id.action_cut:
                    onTaskPicked(TASK_CUT);
                    break;

                case R.id.action_delete:
                    onTaskPicked(TASK_DELETE);
                    break;
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            mAdapter.clearSelection();
        }
    };

    private File[] getLastSelectedFiles(Set<Integer> selectedPositions)
    {
        File[] files = new File[selectedPositions.size()];
        int j = 0;
        for(Integer i : selectedPositions)
        {
            files[j++] = mAdapter.getItem(i);
        }
        return files;
    }

    private void cacheInvalidate()
    {
        mFilesListCache.remove(mCurrentDir.getAbsolutePath());
    }

    private void cacheInvalidate(String key)
    {
        mFilesListCache.remove(key);
    }

    private List<File> getFilesListFromCache()
    {
        String path = mCurrentDir.getAbsolutePath();
        if(mFilesListCache.containsKey(path) && mFilesListCache.get(path).size()!=0)
        {
            // TODO find out why entry for root path becomes size 0
            return mFilesListCache.get(path);
        }
        else
        {
            File[] dirArr = mCurrentDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return !file.isHidden() && file.isDirectory();
                }
            });
            File[] fileArr = mCurrentDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return !file.isHidden() && !file.isDirectory();
                }
            });
            List<File> dirList = new ArrayList<File>(Arrays.asList(dirArr));
            Collections.sort(dirList, new FileComparator());

            List<File> filesList = new ArrayList<File>(Arrays.asList(fileArr));
            Collections.sort(filesList, new FileComparator());

            dirList.addAll(filesList);
            mFilesListCache.put(path, dirList);
            // TODO go one level deep and prepopulate the cache
            return dirList;
        }
    }

    static class FileComparator implements Comparator<File>
    {
        public int compare(File lhs, File rhs)
        {
            String lhsName = lhs.getName().toLowerCase();
            String rhsName = rhs.getName().toLowerCase();
            return lhsName.compareTo(rhsName);
        }
    }

    private List<File> getCurFilesList()
    {
        return getFilesListFromCache();
    }

    public void resetAdapter()
    {
        Log.d(Constants.FRAG_TAG, "resetAdapter()");
        mAdapter.clear();
        List<File> temp = getCurFilesList();
        Log.d(Constants.FRAG_TAG, "files list size is " + temp.size());
        mAdapter.addAll(temp);
        mAdapter.notifyDataSetChanged();
        Log.d(Constants.FRAG_TAG, "<----------------------------------------------------->");
    }

    public void setScrollPos()
    {
        Log.d(Constants.FRAG_TAG, "setScrollPos() from poping the stack.");
        Log.d(Constants.FRAG_TAG, mScrollPosStack.toString());
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mScrollPosStack.pop());
            }
        }, 50);
    }

    private void updateCurrentDir(int position)
    {
        Log.d(Constants.FRAG_TAG, "updateCurrentDir, old current" + mCurrentDir);
        mCurrentDir = mFilesListCache.get(mCurrentDir.getAbsolutePath()).get(position);
        Log.d(Constants.FRAG_TAG, "updateCurrentDir, new current " + mCurrentDir);
    }


    public boolean isCurDirRoot()
    {
        return mCurrentDir.getAbsolutePath().equals(mRootDir.getAbsolutePath());
    }

    public void goOneLevelUp()
    {
        Log.d(Constants.FRAG_TAG, "goOneLevelUp()");
        if (!isCurDirRoot())
        { // isCurDirRoot already checked.
            mCurrentDir = mCurrentDir.getParentFile();
        }
        Log.d(Constants.FRAG_TAG, "Current now is " + mCurrentDir);
    }


    private class FileItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            File clickedFile = mAdapter.getItem(position);
            Log.d(Constants.FRAG_TAG, "mScrollPosStack  " + parent.getFirstVisiblePosition() );
            if(clickedFile.isDirectory())
            {
                mScrollPosStack.push(parent.getFirstVisiblePosition());
                updateCurrentDir(position);
                resetAdapter();
                parent.setSelection(0);
            }
            else
            {
                try
                {
                    FileUtil.openFile(getActivity(), clickedFile);
                } catch (IOException e)
                {
                    Log.d(Constants.FRAG_TAG, "IO Exception when trying to open file.");
                    e.printStackTrace();
                }
            }
        }
    }

    public void onTaskPicked(int task)
    {
        if(task==TASK_SHARE)
        {
            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
            ShareFragment shareFragment = new ShareFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_FILE_NAME, mLastSelectedFiles[0].getName());
            shareFragment.setArguments(bundle);
            transaction.replace(R.id.container, shareFragment);
            transaction.addToBackStack("ShareFragTrans");
            transaction.commit();
        }
        else if(task==TASK_RENAME)
        {
            DialogFragment renameDialogFragment = new RenameDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_OLD_NAME, mLastSelectedFiles[0].getName());
            renameDialogFragment.setArguments(bundle);
            renameDialogFragment.show(getFragmentManager(), "Rename Dialog Fragment");
        }
        else if(task==TASK_COPY)
        {
            mCutOrCopyMode = true;
            getActivity().invalidateOptionsMenu();
        }
        else if(task==TASK_CUT)
        {
            mCutOrCopyMode = true;
            getActivity().invalidateOptionsMenu();
            mCut = true;
        }
        else if(task==TASK_DELETE){
            DialogFragment deleteFileDialogFragment = new DeleteFileDialogFragment();
            deleteFileDialogFragment.show(getFragmentManager(), "Delete File Dialog Fragment");
        }
    }

    public void onRenamed(String newName)
    {
        Log.d(Constants.FRAG_TAG, "onRenamed" + mLastSelectedItems);
        File from = mAdapter.getItem(mLastSelectedItems[0]);

        if(newName.equals(from.getName())) {
            return;
        }

        File to = new File(mCurrentDir, newName);
        if(to.exists())
        {
            showToast(from.isDirectory()? R.string.rename_dupd_found : R.string.rename_dupf_found);
        }
        else
        {
            showToast(from.renameTo(to) ? R.string.rename_success : R.string.rename_failed);
        }
        cacheInvalidate();
        resetAdapter();
    }

    public void onDeleted()
    {
        try
        {
            for (File file : mLastSelectedFiles)
            {
                FileUtil.delete(file);
            }
            showToast(R.string.delete_success);
            cacheInvalidate();
            resetAdapter();
            clearLastSeletedFiles();
        }
        catch (IOException e)
        {
            showToast(R.string.delete_failed);
        }
    }

    public void initPaste()
    {
        // TODO look for duplicate names in target directory and
        // abort the process if anything found
        new CopyFileTask(this, mCurrentDir, mLastSelectedFiles).execute();
    }

    //used by activity
    public boolean isCutOrCopyMode()
    {
        return mCutOrCopyMode;
    }

    private void onCutOrCopyDone()
    {
        cacheInvalidate();
        if (mCut)
        {
            try
            {
                for(File file : mLastSelectedFiles) {
                    cacheInvalidate(file.getParent());
                    FileUtil.delete(file);
                    cacheInvalidate(file.getAbsolutePath());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        mCut = false;
        resetAdapter();
        mCutOrCopyMode = false;
        clearLastSeletedFiles();
        getActivity().invalidateOptionsMenu();
    }

    private void clearLastSeletedFiles()
    {
        mLastSelectedFiles = null;
    }

    private static class CopyFileTask extends AsyncTask<Void, Integer, Boolean>
    {
        private WeakReference<FilesListFragment> weakFragRef;
        private long mTotalSize;
        private File[] mfiles;
        private File mCurrentDir;
        private ProgressDialog mProgressDialog;

        public CopyFileTask(FilesListFragment fragment, File currentDir, File[] lastSelectedFiles)
        {
            weakFragRef = new WeakReference<FilesListFragment>(fragment);
            mCurrentDir = currentDir;
            mfiles = Arrays.copyOf(lastSelectedFiles, lastSelectedFiles.length);
            calculateTotalSize();
        }

        private void calculateTotalSize()
        {
            for(File f : mfiles)
            {
                mTotalSize += (f.isDirectory() ? FileUtil.folderSize(f) : f.length());
            }
        }

        private int copyFile(File src, File dst, int progress) throws IOException
        {
            if(src.isDirectory())
            {
                if(!dst.exists())
                {
                    dst.mkdir();
                }

                String files[] = src.list();

                for (String file : files)
                {
                    File srcFile = new File(src, file);
                    File destFile = new File(dst, file);
                    //recursive copy
                    progress += copyFile(srcFile, destFile, progress);
                }
            }
            else
            {
                dst.createNewFile();
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dst);

                byte[] buffer = new byte[32 * 1024];

                int len;
                while ((len = in.read(buffer)) > 0)
                {
                    out.write(buffer, 0, len);
                    progress += len;
                    publishProgress(progress);
                }

                in.close();
                out.close();
            }
            return progress;
        }

        @Override
        protected void onPreExecute()
        {
            if(weakFragRef.get()!=null)
            {
                mProgressDialog = new ProgressDialog(weakFragRef.get().getActivity());
                String message = "Copying(0/" + mfiles.length + ")";
                mProgressDialog.setMessage("Copying()");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax((int) mTotalSize);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog.setProgress(0);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {

                    }
                });
                mProgressDialog.show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... _)
        {
            int progress = 0;
            for(File f:mfiles)
            {
                File trgFile = new File(mCurrentDir.getAbsolutePath() + "/" + f.getName());

                try
                {
                    progress = copyFile(f, trgFile, progress);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            if(weakFragRef.get()!=null)
            {
                weakFragRef.get().onCutOrCopyDone();
            }
            mProgressDialog.dismiss();
        }
    }
}
