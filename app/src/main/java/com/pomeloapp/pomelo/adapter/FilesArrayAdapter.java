package com.pomeloapp.pomelo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pomeloapp.pomelo.R;

import java.io.File;
import java.util.List;

public class FilesArrayAdapter extends ArrayAdapter<File>
{

    private int mResource;
    private LayoutInflater mInflater;

    public FilesArrayAdapter(Context context, int resource, List<File> objects)
    {
        super(context, resource, objects);
        mResource = resource;
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(convertView==null)
        {
            view = mInflater.inflate(mResource, parent, false);
        }
        TextView textView = (TextView) view.findViewById(R.id.text_filename);
        File file = getItem(position);
        textView.setText((file.isDirectory() ? "D: " : "F: ") + file.getName());
        return view;
    }
}
