package com.example.hotyun_a.munich;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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

public class Property extends Fragment implements View.OnTouchListener,View.OnClickListener{
    private TextView des,showDate;
    private String startDate,endDate,description,nameEvent,dateToShow;
    private String token,event_id,status;
    private RelativeLayout relativeLayout;
    private ViewFlipper flipper;
    private float fromPosition, toPosition;
    private DateFormat format,needFormat;
    private ArrayList<String> EventId=new ArrayList<>();
    private Date date;
    private int result;
    private CheckBox cbFavourite,cbCalendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.property, container, false);
        status="false";
        cbFavourite=(CheckBox)rootView.findViewById(R.id.cbFavorite);
        cbCalendar=(CheckBox)rootView.findViewById(R.id.cbCalendar);
        cbFavourite.setOnClickListener(this);
        cbCalendar.setOnClickListener(this);
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        needFormat=new SimpleDateFormat("EEEE dd MMMM");
        nameEvent=getArguments().getString("nameEvent");
        description=getArguments().getString("description");
        startDate=getArguments().getString("startDate");
        token=getArguments().getString("token");
        event_id=getArguments().getString("event_id");
        result=getArguments().getInt("check", 0);
        if(result!=0) {
            new GetMemberEvent().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/get_upcoming_events");
            new GetCheckLike().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/"+event_id+"/is_like");
        }
        if(result==0){
            cbCalendar.setVisibility(View.INVISIBLE);
            cbFavourite.setVisibility(View.INVISIBLE);
        }
        try {
            date=format.parse(startDate);
            dateToShow=needFormat.format(date);
            System.out.println(dateToShow);
        }
        catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        des=(TextView)rootView.findViewById(R.id.des);
        showDate=(TextView)rootView.findViewById(R.id.showDate);
        des.setText(description);
        showDate.setText(dateToShow.substring(0,1).toUpperCase()+dateToShow.substring(1).toLowerCase());
        relativeLayout=(RelativeLayout)rootView.findViewById(R.id.relativeLayout);
        relativeLayout.setOnTouchListener(this);
        flipper=(ViewFlipper)rootView.findViewById(R.id.viewFlipper);
        setHasOptionsMenu(true);
        return rootView;
	}
    public boolean onTouch(View view, MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                fromPosition = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                toPosition= event.getX();
                if ((fromPosition - 150) > toPosition) {
                    fromPosition = toPosition;
                    flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.go_next_in));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.go_next_out));
                    flipper.showNext();
                }
                else if ((fromPosition + 150) < toPosition) {
                    fromPosition = toPosition;
                    flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.go_prev_in));
                    flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.go_prev_out));
                    flipper.showPrevious();
                }
            default:
                break;
        }
        return true;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbCalendar:
                if (cbCalendar.isChecked()) {
                    new AddToMembers().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/" + event_id + "/add_to_members");
                    break;
                }
                else{
                    new AddToMembers().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/" + event_id + "/add_to_members");
                    break;
                }
            case R.id.cbFavorite:
                if (cbFavourite.isChecked()) {
                    new Like().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/" + event_id + "/like");
                    break;
                }
                else{
                    new Like().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/" + event_id + "/like");
                    break;
                }
        }
    }
    //------------------Start add to members-----------------------
    class AddToMembers extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog= ProgressDialog.show(getActivity(), null,"Adding to your members list", true);
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
            }
            catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
                Toast.makeText(getActivity(), "Your data send correctly", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getActivity(), "ERROR: error to get answer from server", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    //------------------End add to members-------------------------
    //------------------Start add to favourite---------------------
    class Like extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(getActivity(), null,"Sending data to server", true);
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
                Log.d("myLogs",responseString);
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
                Toast.makeText(getActivity(), "Your data add correctly", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(getActivity(), "ERROR: error to get answer from server", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    //------------------End add to favourite-----------------------
    //------------------Start get members--------------------------
    class GetMemberEvent extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(getActivity(), null,"Check for member", true);
            EventId.clear();
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
                Log.d("myLogs",responseString);
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
                for(int i=0;i<EventId.size();i++){
                    if(EventId.get(i).equals(event_id)){
                        cbCalendar.setChecked(true);
                    }
                }
            }
            else
                Toast.makeText(getActivity(), "ERROR: can not load data", Toast.LENGTH_LONG).show();
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
            EventId.add(jsonObj.getString("_id"));
        }
    }
    //------------------End get members----------------------------
    //------------------Start check like---------------------------
    class GetCheckLike extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog= ProgressDialog.show(getActivity(), null,"Check for like", true);
            EventId.clear();
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
                Log.d("myLogs",responseString);
                parseDataCheckLike(responseString);
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
                    if(status.equals("true")){
                        cbFavourite.setChecked(true);
                    }
            }
            else
                Toast.makeText(getActivity(), "ERROR: can not load data", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    void parseDataCheckLike(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        status=jsonObject.getString("data");
    }
    //------------------End check like-----------------------------
}
