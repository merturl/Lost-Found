package com.jonghyeon.tutorial;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by woong on 2017-12-02.
 */

public class ListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 arrayList
    private ArrayList<StoreItem> storeItemList = new ArrayList<StoreItem>();

    // ListviewAdapter의 생성자
    public ListViewAdapter() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴
    @Override
    public int getCount() {
        return storeItemList.size();
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
    @Override
    public View getView(int position, View convertview, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_storeItem" Layout을 inflate하여 converView 참조 획득
        if (convertview == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertview = inflater.inflate(R.layout.listview_storeitem, parent, false);
        }

        // 화면에 표시될 View(Layout이 infalte된)로부터 위젯에 대한 참조 획득
        ImageView inconImageView = (ImageView) convertview.findViewById(R.id.ImageView1);
        TextView msg = (TextView) convertview.findViewById(R.id.textview_msg);
        TextView getTime = (TextView) convertview.findViewById(R.id.textview_getTime);

        // Data set(StoreItem)에서 position에 위치한 데이터 참조 획득
        StoreItem storeItem = storeItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        inconImageView.setImageDrawable(storeItem.getIcon());
        msg.setText(storeItem.getmsg());
        getTime.setText(storeItem.getLostTime());

        return convertview;
    }

    // 지정한 위치(position)에 잇는 데이터와 관계된 아이템(row)의 ID를 리턴
    @Override
    public long getItemId(int postion) {
        return postion;
    }

    // 지정한 위치(position)에 잇는 데이터 리턴
    @Override
    public Object getItem(int position) {
        return storeItemList.get(position);
    }

    // 아이템 데이터 추가를 위한 함수
    public void addItem(Drawable icon, String msg, String time) {
        StoreItem item = new StoreItem();


        item.setmsg(msg);
        item.setLostTime(time);

        storeItemList.add(item);
    }


}
