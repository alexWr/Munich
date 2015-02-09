package com.example.hotyun_a.munich;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ShowCalendar extends Fragment{

    public GregorianCalendar month, itemmonth;// calendar instances.
    public CalendarAdapter adapter;// adapter instance
    public Handler handler;// for grabbing some event values for showing the dot marker.
    public ArrayList<String> items; // container to store calendar items which needs showing the event marker
    public static ArrayList<String> nameOfEvent = new ArrayList<String>();
    public static ArrayList<String> endDates = new ArrayList<String>();
    public static ArrayList<String> descriptions = new ArrayList<String>();
    private static ArrayList<String> date=new ArrayList<String>();
    private ArrayList<String> event;
    private LinearLayout rLayout;
    private ArrayList<String> desc;
    private Toast toast;
    private String token;
    private static String result;
    private int x,y;
    private TextView title;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendar, container, false);
        token=getArguments().getString("token");
        Locale.setDefault(Locale.getDefault());
        rLayout = (LinearLayout) rootView.findViewById(R.id.text);
        month = (GregorianCalendar) GregorianCalendar.getInstance();
        itemmonth = (GregorianCalendar) month.clone();
        items = new ArrayList<>();
        adapter = new CalendarAdapter(getActivity(), month);
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(adapter);
        new GetMemberEvent().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/get_upcoming_events");
        title = (TextView) rootView.findViewById(R.id.title);
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
        RelativeLayout previous = (RelativeLayout) rootView.findViewById(R.id.previous);
        previous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousMonth();
                refreshCalendar();
            }
        });
        RelativeLayout next = (RelativeLayout) rootView.findViewById(R.id.next);
        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            setNextMonth();
            refreshCalendar();
            }
        });
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (((LinearLayout) rLayout).getChildCount() > 0)
                    ((LinearLayout) rLayout).removeAllViews();
                desc = new ArrayList<>();
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);
                String selectedGridDate = CalendarAdapter.dayString.get(position);
                String[] separatedTime = selectedGridDate.split("-");
                String gridvalueString = separatedTime[2].replaceFirst("^0*","");// taking last part of date. ie; 2 from 2012-12-02.
                int gridvalue = Integer.parseInt(gridvalueString);// navigate to next or previous month on clicking offdays.
                if ((gridvalue > 10) && (position < 8)) {
                    setPreviousMonth();
                    refreshCalendar();
                }
                else if ((gridvalue < 7) && (position > 28)) {
                    setNextMonth();
                    refreshCalendar();
                }
                ((CalendarAdapter) parent.getAdapter()).setSelected(v);
                for (int i = 0; i < nameOfEvent.size(); i++) {
                    if (getDate(date.get(i)).equals(selectedGridDate)) {
                        desc.add(nameOfEvent.get(i));
                    }
                }
                if (desc.size() > 0) {
                    for (int i = 0; i < desc.size(); i++) {
                        TextView rowTextView = new TextView(getActivity());
                        rowTextView.setText(desc.get(i));
                        rowTextView.setTextColor(Color.BLACK);
                        toast=Toast.makeText(getActivity(),desc.get(i),Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.LEFT, x, y+200);
                        toast.show();
                        // add the textview to the linearlayout
                        rLayout.addView(rowTextView);
                    }
                }
                desc = null;
            }
        });
        gridview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x=(int)event.getX();
                y=(int)event.getY();
                return false;
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
    }
    protected void setNextMonth() {
        if (month.get(GregorianCalendar.MONTH) == month.getActualMaximum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) + 1),month.getActualMinimum(GregorianCalendar.MONTH), 1);
        }
        else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) + 1);
        }
    }
    protected void setPreviousMonth() {
        if (month.get(GregorianCalendar.MONTH) == month.getActualMinimum(GregorianCalendar.MONTH)) {
            month.set((month.get(GregorianCalendar.YEAR) - 1), month.getActualMaximum(GregorianCalendar.MONTH), 1);
        }
        else {
            month.set(GregorianCalendar.MONTH, month.get(GregorianCalendar.MONTH) - 1);
        }
    }
    public void refreshCalendar() {
        adapter.refreshDays();
        adapter.notifyDataSetChanged();
        handler.post(calendarUpdater); // generate some calendar items
        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
    }
    public Runnable calendarUpdater = new Runnable() {
        @Override
        public void run() {
            items.clear();
            // Print dates of the current week
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            event = nameOfEvent;
            Log.d("=====Event====", event.toString());
            Log.d("=====Date ARRAY====", date.toString());
            for (int i = 0; i < date.size(); i++) {
                itemmonth.add(GregorianCalendar.DATE, 1);
                items.add(getDate(date.get(i)));
            }
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };
    //------------------------------Get event and set to calendar-------------------------------------------------
    public static String getDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dateT;
        try {
            dateT=format.parse(date);
            result=formatter.format(dateT);
        }
        catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
    //------------------Start get members--------------------------
    class GetMemberEvent extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(getActivity(), null,"Get member event ", true);
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(params[0]);
                request.setHeader("Authorization","Bearer "+token);
                response = client.execute(request);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                parseDataMemberEvent(responseString);
            }
            catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                handler = new Handler();
                handler.post(calendarUpdater);
            }
            else Toast.makeText(getActivity(), "ERROR: can not load data", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    void parseDataMemberEvent(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONObject jsonRes,jsonObj;
        JSONArray jsonData;
        jsonData=jsonObject.getJSONArray("data");
        for(int i=0;i<jsonData.length();i++){
            jsonRes=jsonData.getJSONObject(i);
            jsonObj=jsonRes.getJSONObject("event");
            endDates.add(jsonObj.getString("endDate"));
            date.add(jsonObj.getString("startDate"));
            descriptions.add(jsonObj.getString("description"));
            nameOfEvent.add(jsonObj.getString("name"));

        }
    }
    //------------------End get members----------------------------
}
