package com.example.hotyun_a.munich;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FavouriteEvent extends Fragment {
    private TextView tvNameEvent,tvstartDate,tvDescribe;
    private ArrayList<String> name,des,date;
    static Context context;
    private ListView lvFav;
    private DateFormat format,format1;
    private String show;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.favourite_event, container, false);
        context=getActivity();
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format1=new SimpleDateFormat("yyyy.MM.dd");
        name=getArguments().getStringArrayList("nameEvent");
        des=getArguments().getStringArrayList("description");
        date=getArguments().getStringArrayList("startDate");
        lvFav=(ListView)rootView.findViewById(R.id.lvFav);
        lvFav.setAdapter(new FavouriteAdapter(getActivity(),R.layout.list_favourite,name,date,des));
        setHasOptionsMenu(true);
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
    }
    public class FavouriteAdapter extends ArrayAdapter {
        Activity context;
        ArrayList<String> event;
        ArrayList<String> date_event;
        ArrayList<String> des_event;
        int layoutId;
        FavouriteAdapter(Activity context, int layoutId, ArrayList<String> name, ArrayList<String> date, ArrayList<String> des){
            super(context, layoutId, name);
            this.context = context;
            this.event = name;
            this.date_event=date;
            this.des_event=des;
            this.layoutId = layoutId;
        }
        public Object getItem(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater=context.getLayoutInflater();
            try {
                Date d = format.parse(date_event.get(position));
                show=format1.format(d);
            }
            catch (ParseException e){
                e.printStackTrace();
            }
            View row=inflater.inflate(layoutId, null);
            tvNameEvent=(TextView)row.findViewById(R.id.tvNameEvent);
            tvstartDate=(TextView)row.findViewById(R.id.tvstartDate);
            tvDescribe=(TextView)row.findViewById(R.id.tvDescribe);
            tvNameEvent.setText(event.get(position));
            tvstartDate.setText(show);
            tvDescribe.setText(des_event.get(position));
            return(row);
        }
    }
}
