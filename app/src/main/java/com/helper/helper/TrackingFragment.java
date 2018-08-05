package com.helper.helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TrackingFragment extends Fragment {

    private final static String TAG = TrackingFragment.class.getSimpleName() + "/DEV";

    ListView mRecordedListView;
    TrackingListViewAdapter mRecordedListViewAdap;
    ArrayList<TrackingListViewItem> mRecordedListViewArrList;

    public TrackingFragment() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecordedListView = (ListView) getActivity().findViewById(R.id.recordedListView);
        mRecordedListViewArrList = new ArrayList<TrackingListViewItem>();

        mRecordedListViewArrList.add(
                new TrackingListViewItem(new Date(),10));

        mRecordedListViewArrList.add(
                new TrackingListViewItem(new Date(),20));

        mRecordedListViewArrList.add(
                new TrackingListViewItem(new Date(),30));

        mRecordedListViewAdap = new TrackingListViewAdapter(getActivity(), mRecordedListViewArrList);
        mRecordedListView.setAdapter(mRecordedListViewAdap);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_tracking, container, false );
//
//        mRecordedListView = (ListView) view.findViewById(R.id.recordedListView);
//        mRecordedListViewArrList = new ArrayList<TrackingListViewItem>();
//
//        mRecordedListViewArrList.add(
//                new TrackingListViewItem(new Date(),10));
//
//        mRecordedListViewArrList.add(
//                new TrackingListViewItem(new Date(),20));
//
//        mRecordedListViewArrList.add(
//                new TrackingListViewItem(new Date(),30));
//
//        mRecordedListViewAdap = new TrackingListViewAdapter(getActivity(), mRecordedListViewArrList);
//        mRecordedListView.setAdapter(mRecordedListViewAdap);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}