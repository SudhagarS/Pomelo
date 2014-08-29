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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // if cut is true, delete the source
    private String mSrcPath;
    private boolean mCut;

    private File mCurrentDir;
    private ListView mListView;
    private ArrayAdapter<File> mAdapter;
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
        mListView.setOnItemLongClickListener(new FileItemLongClickListener());
        return rootView;
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

    private int mLastLongclickedPos = 0;
    private class FileItemLongClickListener implements AdapterView.OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            mLastLongclickedPos = position;
            DialogFragment newDialog = new TaskDialogFragment();
            newDialog.show(getActivity().getFragmentManager(), "Task Chooser Dialog");
            return true;
        }
    }

    public void onTaskPicked(int task)
    {
        if(task==TASK_SHARE)
        {
            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
            ShareFragment shareFragment = new ShareFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_FILE_NAME, mAdapter.getItem(mLastLongclickedPos).getName());
            shareFragment.setArguments(bundle);
            transaction.replace(R.id.container, shareFragment);
            transaction.addToBackStack("ShareFragTrans");
            transaction.commit();
        }
        else if(task==TASK_RENAME)
        {
            DialogFragment renameDialogFragment = new RenameDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_OLD_NAME, mAdapter.getItem(mLastLongclickedPos).getName());
            renameDialogFragment.setArguments(bundle);
            renameDialogFragment.show(getFragmentManager(), "Rename Dialog Fragment");
        }
        else if(task==TASK_COPY)
        {
            mSrcPath = mAdapter.getItem(mLastLongclickedPos).getAbsolutePath();
            getActivity().invalidateOptionsMenu();
        }
        else if(task==TASK_CUT)
        {
            mSrcPath = mAdapter.getItem(mLastLongclickedPos).getAbsolutePath();
            getActivity().invalidateOptionsMenu();
            mCut = true;
        }
        else if(task==TASK_DELETE){

            DialogFragment deleteFileDialogFragment = new DeleteFileDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_OLD_NAME, mAdapter.getItem(mLastLongclickedPos).getName());
            deleteFileDialogFragment.setArguments(bundle);
            deleteFileDialogFragment.show(getFragmentManager(), "Delete File Dialog Fragment");
        }
    }

    public void onRenamed(String newName)
    {
        int i = 0;
        File from = mAdapter.getItem(mLastLongclickedPos);

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
        File file = mAdapter.getItem(mLastLongclickedPos);
        try
        {
            FileUtil.delete(file);
            showToast(R.string.delete_success);
        }
        catch(IOException e)
        {
            showToast(R.string.delete_failed);
        }
        cacheInvalidate();
        resetAdapter();
    }

    public void initPaste()
    {
        File file = new File(mSrcPath);

        // ensure it is not pasted in same directory
        if(file.getParent().equals(mCurrentDir.getAbsolutePath()))
        {
            showToast(R.string.paste_desthassrc);
            return;
        }
        else
        {
            long size = file.isDirectory() ? FileUtil.folderSize(file) : file.length();
            new CopyFileTask(size).execute(file);
        }
    }

    //used by activity
    public String getSrcPath()
    {
        return mSrcPath;
    }

    private void onPasteDone()
    {
        cacheInvalidate();
        if (mCut)
        {
            try
            {
                File srcFile = new File(mSrcPath);
                cacheInvalidate(srcFile.getParent());
                FileUtil.delete(srcFile);
                cacheInvalidate(mSrcPath);
                mCut = false;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        resetAdapter();
        mSrcPath = null;
        getActivity().invalidateOptionsMenu();
    }

    private class CopyFileTask extends AsyncTask<File, Integer, Boolean>
    {
        private long mTotalSize;
        private ProgressDialog mProgressDialog;

        public CopyFileTask(long totalSize)
        {
            mTotalSize = totalSize;
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
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Copying...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax((int) mTotalSize);
            mProgressDialog.setProgressNumberFormat(null);
            mProgressDialog.setProgress(0);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                public void onCancel(DialogInterface arg0)
                {

                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(File... src)
        {
            File srcFile = src[0];
            File trgFile = new File(mCurrentDir.getAbsolutePath() + "/" + srcFile.getName());

            try
            {
                copyFile(srcFile, trgFile, 0);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
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
            onPasteDone();
            mProgressDialog.dismiss();
        }
    }
}
