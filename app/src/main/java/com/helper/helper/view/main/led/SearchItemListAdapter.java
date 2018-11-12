package com.helper.helper.view.main.led;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.model.SearchItem;

import java.util.List;

public class SearchItemListAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater li;
    private ViewHolder viewHolder;
    private List<SearchItem> list;

    public SearchItemListAdapter(Context context, List<SearchItem> items) {
        this.list = items;
        this.context = context;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
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
            convertView = li.inflate(R.layout.widget_search_item,null);

            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.search_title);
            viewHolder.type = (TextView)convertView.findViewById(R.id.search_type);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // 리스트에 있는 데이터를 리스트뷰 셀에 뿌린다.
        if(list != null) {
            System.out.println("test : "+ list.get(position).getTitle());
            viewHolder.title.setText(list.get(position).getTitle());
            viewHolder.type.setText(list.get(position).getType());
        }

        return convertView;
    }

    class ViewHolder{
        public TextView title;
        public TextView type;

    }
}
