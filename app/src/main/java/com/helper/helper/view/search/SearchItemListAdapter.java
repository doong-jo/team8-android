package com.helper.helper.view.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.model.LED;

import java.util.List;

public class SearchItemListAdapter extends BaseAdapter{
    private LayoutInflater m_li;
    private ViewHolder m_viewHolder;
    private List<LED> m_list;

    public SearchItemListAdapter(Context context, List<LED> items) {
        this.m_list = items;
        m_li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return m_list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(convertView == null){
            convertView = m_li.inflate(R.layout.widget_search_item,null);

            m_viewHolder = new ViewHolder();
            m_viewHolder.name = convertView.findViewById(R.id.search_title);
            m_viewHolder.type = convertView.findViewById(R.id.search_type);

            convertView.setTag(m_viewHolder);
        }else{
            m_viewHolder = (ViewHolder)convertView.getTag();
        }

        if(m_list != null) {
            System.out.println("test : "+ m_list.get(position).getName());
            m_viewHolder.name.setText(m_list.get(position).getName());
            m_viewHolder.type.setText(m_list.get(position).getType());
        }

        return convertView;
    }

    class ViewHolder{
        public TextView name;
        public TextView type;

    }
}
