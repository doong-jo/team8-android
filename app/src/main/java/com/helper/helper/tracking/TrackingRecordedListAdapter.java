package com.helper.helper.tracking;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.helper.helper.R;
import com.helper.helper.ScrollingActivity;

import java.util.List;


public class TrackingRecordedListAdapter extends RecyclerView.Adapter<TrackingRecordedListAdapter.ViewHolder> {

    private static final int TAB_TRACKING = 2;

    private List<TrackingRecordedListItem> recordList;
    private int itemLayout;
    Context mContext;

    /**
     * 생성자
     * @param items
     * @param itemLayout
     */
    public TrackingRecordedListAdapter(List<TrackingRecordedListItem> items , int itemLayout, Context c){
        this.mContext = c;
        this.recordList = items;
        this.itemLayout = itemLayout;
    }

    /**
     * 레이아웃을 만들어서 Holer에 저장
     * @param viewGroup
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout,viewGroup,false);
        return new ViewHolder(view);
    }

    /**
     * listView getView 를 대체
     * 넘겨 받은 데이터를 화면에 출력하는 역할
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        final TrackingRecordedListItem item = recordList.get(position);
        viewHolder.cardView.setCardBackgroundColor(item.getColor());
        viewHolder.dateTextView.setText(item.getDate());
        viewHolder.startTimeTextView.setText(item.getStartTime());
        viewHolder.endTimeTextView.setText(item.getEndTime());
        viewHolder.distanceTextView.setText(item.getDistance());

        viewHolder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ViewPager viewPager = ((ScrollingActivity)mContext).getViewPager();

                TrackingFragment trackingFragment = (TrackingFragment)viewPager
                        .getAdapter()
                        .instantiateItem(viewPager, TAB_TRACKING);

                trackingFragment.setSelectedMapLoad(item.getDate().split("#")[1].split(" ")[0]);

                Toast.makeText(mContext, item.getDate(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    /**
     * 뷰 재활용을 위한 viewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public TextView dateTextView;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public TextView distanceTextView;

        public ViewHolder(View itemView){
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.recordedCard);
            dateTextView = (TextView) itemView.findViewById(R.id.recordedDate);
            startTimeTextView= (TextView) itemView.findViewById(R.id.recordedStartTime);
            endTimeTextView= (TextView) itemView.findViewById(R.id.recordedEndTime);
            distanceTextView = (TextView) itemView.findViewById(R.id.recordedDistance);
        }

    }
}