package com.helper.helper.view.search;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.DownloadImageTask;
import com.helper.helper.model.LED;
import com.snatik.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class SearchItemListAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater m_li;
    private ViewHolder m_viewHolder;
    private List<LED> m_list;

    public SearchItemListAdapter(Context context, List<LED> items) {
        this.m_list = items;
        this.m_li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
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
            convertView = m_li.inflate(R.layout.widget_search_item, null);

            m_viewHolder = new ViewHolder();
            m_viewHolder.name = convertView.findViewById(R.id.searchTitle);
            m_viewHolder.type = convertView.findViewById(R.id.searchType);
            m_viewHolder.img = convertView.findViewById(R.id.searchCardImg);

            convertView.setTag(m_viewHolder);
        }else{
            m_viewHolder = (ViewHolder)convertView.getTag();
        }

        if(m_list != null) {
            System.out.println("test : "+ m_list.get(position).getName());
            m_viewHolder.name.setText(m_list.get(position).getName());
            m_viewHolder.type.setText(m_list.get(position).getCategory());
            try {
                File f=new File(getOpenFilePath(m_list.get(position).getIndex()));
                Bitmap cardImageBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                m_viewHolder.img.setImageBitmap(cardImageBitmap);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        return convertView;
    }

    class ViewHolder{
        public TextView name;
        public TextView type;
        public ImageView img;
    }

    private String getOpenFilePath(String ledIndex) {
        Storage internalStorage = new Storage(context);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DownloadImageTask.DOWNLOAD_PATH;
        String openFilePath = dir + File.separator + ledIndex + context.getString(R.string.gif_format);

        return openFilePath;
    }
}
