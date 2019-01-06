package com.helper.helper.view.group;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.model.Member;
import com.helper.helper.model.MemberList;


public class MemberListItemListAdapter extends ArrayAdapter<MemberList> {

    private LayoutInflater li;
    private boolean m_deleteMode;
    private View m_convertView;
    private View.OnTouchListener m_itemTouchListener;

    public MemberListItemListAdapter(Context context, List<MemberList> items) {
        super(context, 0, items);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDeleteMode(boolean mode) {
        m_deleteMode = mode;

        if( !mode ) {
            ViewHolder holder = new ViewHolder(m_convertView);

            holder.layout.setBackgroundColor(Color.WHITE);
            holder.checkBox.setChecked(false);
        }
    }

    public void setTouchItemListener(View.OnTouchListener listener) {
        m_itemTouchListener = listener;
    }

    @SuppressLint({"ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        m_convertView = convertView;

        final MemberList item = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(R.layout.item_listview_memberlist, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(R.id.holder, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.holder);
        }

        StringBuilder builder = new StringBuilder()
                .append("Group : ")
                .append(item.getNames().toString());

        holder.name.setText(builder.toString());

        if( m_deleteMode ) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.layout.setBackgroundColor(Color.WHITE);
        }

        holder.layout.setOnTouchListener(m_itemTouchListener);

        convertView.setTag(item.getNames());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public ViewHolder(View root) {
            name = root.findViewById(R.id.itemName);
            checkBox = root.findViewById(R.id.selectChk);
            layout = root.findViewById(R.id.layout);
        }

        public TextView name;
        public AppCompatCheckBox checkBox;
        public ConstraintLayout layout;
        public List<Member> m_members;
    }
}
