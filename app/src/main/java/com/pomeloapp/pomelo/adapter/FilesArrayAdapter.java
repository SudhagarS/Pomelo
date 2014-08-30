package com.pomeloapp.pomelo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pomeloapp.pomelo.R;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilesArrayAdapter extends ArrayAdapter<File>
{

    private int mResource;
    private LayoutInflater mInflater;
    private Set<Integer> mSelectedItems;

    public FilesArrayAdapter(Context context, int resource, List<File> objects)
    {
        super(context, resource, objects);
        mResource = resource;
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedItems = new HashSet<Integer>();
    }

    public void toggleItemSelection(int position)
    {
        if(mSelectedItems.contains(position))
        {
            mSelectedItems.remove(position);
        }
        else
        {
            mSelectedItems.add(position);
        }
        notifyDataSetChanged();
    }

    public void clearSelection()
    {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedItems()
    {
        return mSelectedItems;
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
        textView.setBackgroundColor(mSelectedItems.contains(position) ? 0x9934B5E4 : Color.TRANSPARENT);
        File file = getItem(position);
        textView.setText((file.isDirectory() ? "D: " : "F: ") + file.getName());
        return view;
    }
}
