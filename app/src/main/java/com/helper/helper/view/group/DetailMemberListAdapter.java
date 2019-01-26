package com.helper.helper.view.group;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.helper.helper.interfaces.CheckedInterface;
import com.helper.helper.model.ContactItem;
import com.helper.helper.model.Member;


public class DetailMemberListAdapter extends ArrayAdapter<Member> {

    private LayoutInflater li;
    private View m_convertView;
    private CheckedInterface m_callbackChecked;

    public DetailMemberListAdapter(Context context, List<Member> items, CheckedInterface callbackChecked) {
        super(context, 0, items);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_callbackChecked = callbackChecked;
    }

    public DetailMemberListAdapter(Context context, List<Member> items) {
        super(context, 0, items);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @SuppressLint({"ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        m_convertView = convertView;

        final Member item = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(R.layout.item_listview_detail_member, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(R.id.holder, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.holder);
        }

        final String itemName = item.getName();
        holder.name.setText(itemName);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public ViewHolder(View root) {
            name = root.findViewById(R.id.itemName);
        }

        public TextView name;
    }
}
