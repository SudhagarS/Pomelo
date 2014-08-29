package com.pomeloapp.pomelo.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pomeloapp.pomelo.R;
import com.pomeloapp.pomelo.adapter.ShareArrayAdapter;
import com.pomeloapp.pomelo.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShareFragment extends Fragment
{
    ListView mSharedListView;
    ArrayAdapter<String> mAdapter;
    String mFileName;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mFileName = getArguments().getString(Constants.KEY_FILE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.lay_share_frag, container, false);

        ( (TextView)rootView.findViewById(R.id.share_filename)).setText(mFileName);
        ( (TextView)rootView.findViewById(R.id.share_status)).setText(R.string.status_uploading);
        mSharedListView = (ListView) rootView.findViewById(R.id.share_shared_list);

        List<String> tempArray = new ArrayList<String>(Arrays.asList(new String[]{"Add", "hi", "there"}));
        mAdapter = new ShareArrayAdapter(getActivity(), R.layout.lay_share_list_adapter, tempArray);
        mSharedListView.setAdapter(mAdapter);
        return rootView;
    }
}
