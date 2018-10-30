package com.helper.helper.view.contact;

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
import com.helper.helper.model.ContactItem;


public class ItemListAdapter extends ArrayAdapter<ContactItem> {

    private LayoutInflater li;
    private boolean m_deleteMode;
    private View m_convertView;

    public ItemListAdapter(Context context, List<ContactItem> items) {
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

    @SuppressLint({"ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        m_convertView = convertView;

        final ContactItem item = getItem(position);

        final ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(R.layout.item_listview_contact, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(R.id.holder, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.holder);
        }

        holder.name.setText(item.getName());
        holder.phoneNumber.setText(item.getPhoneNumber());
        if( m_deleteMode ) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.layout.setBackgroundColor(Color.WHITE);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if( checked ) {
                    holder.layout.setBackgroundColor(Color.LTGRAY);
                } else {
                    holder.layout.setBackgroundColor(Color.WHITE);
                }
            }
        });

        holder.layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            @SuppressLint("ResourceAsColor")
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if( m_deleteMode ) {
                    if( !holder.checkBox.isChecked() ) {
                        holder.checkBox.setChecked(true);
                        view.setBackgroundColor(Color.LTGRAY);
                    } else {
                        holder.checkBox.setChecked(false);
                        view.setBackgroundColor(Color.WHITE);
                    }

                }
                return false;
            }
        });
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public ViewHolder(View root) {
            name = root.findViewById(R.id.itemName);
            phoneNumber = root.findViewById(R.id.itemPhoneNumber);
            checkBox = root.findViewById(R.id.deleteChk);
            layout = root.findViewById(R.id.layout);
        }

        public TextView name;
        public TextView phoneNumber;
        public AppCompatCheckBox checkBox;
        public ConstraintLayout layout;
    }
}
