package com.pomeloapp.pomelo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pomeloapp.pomelo.R;

import java.util.List;

public class ShareArrayAdapter extends ArrayAdapter<String>
{

    private int mResource;
    private LayoutInflater mInflater;

    public ShareArrayAdapter(Context context, int resource, List<String> objects)
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
        TextView textView = (TextView) view.findViewById(R.id.text_share_list_filename);
        textView.setText(getItem(position));
        if(position==0)
        {
            view.findViewById(R.id.img_share_list_remove).setVisibility(View.GONE);
        }
        else
        {
            view.findViewById(R.id.img_share_list_remove).setVisibility(View.VISIBLE);
        }
        return view;
    }
}
