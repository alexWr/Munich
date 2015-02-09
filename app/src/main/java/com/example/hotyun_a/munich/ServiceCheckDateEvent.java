package com.example.hotyun_a.munich;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ServiceCheckDateEvent extends Service{
    private String token;
    private static final int NOTIFY_ID = 101;
    private SharedPreferences sPref;
    private ArrayList<String> startDate=new ArrayList<>();
    private ArrayList<String> description=new ArrayList<>();
    private ArrayList<String> name=new ArrayList<>();
    private ArrayList<String> EventId=new ArrayList<>();
    private DateFormat format;
    private ArrayList<Date> date=new ArrayList<>();
    private Calendar calendar;
    @Override
    public void onCreate() {
        super.onCreate();
        sPref= PreferenceManager.getDefaultSharedPreferences(this);
        token=sPref.getString("token","");
        calendar=Calendar.getInstance(Locale.getDefault());
        /*calendar.set(Calendar.YEAR,2014);
        calendar.set(Calendar.MONTH,11);
        calendar.set(Calendar.DAY_OF_MONTH,11);*/
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        new GetMemberEvent().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/get_upcoming_events");
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotif(String name,String des,String startD,String id) {
        Context context=getApplicationContext();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.putExtra("name",name);
        notificationIntent.putExtra("description",des);
        notificationIntent.putExtra("startDate",startD);
        notificationIntent.putExtra("token",token);
        notificationIntent.putExtra("id",id);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context,0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        Resources res = context.getResources();
        Notification.Builder notif = new Notification.Builder(context);
        notif.setContentIntent(contentIntent)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
            .setTicker(name)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentTitle(name)
            .setContentText(des);
        Notification notification = notif.build();
        notification.flags=notification.flags|Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
        stopSelf();
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
    public void onDestroy() {
        super.onDestroy();
    }
    class GetMemberEvent extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        Calendar clMember=Calendar.getInstance();
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
                parseDate(startDate);
                for(int i=0;i<date.size();i++){
                    clMember.setTime(date.get(i));
                    if(clMember.get(Calendar.YEAR)==calendar.get(Calendar.YEAR))
                        if(clMember.get(Calendar.MONTH)==calendar.get(Calendar.MONTH))
                            if(clMember.get(Calendar.DAY_OF_MONTH)==calendar.get(Calendar.DAY_OF_MONTH))
                                sendNotif(name.get(i),description.get(i),startDate.get(i),EventId.get(i));
                }
            }
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
            startDate.add(jsonObj.getString("startDate"));
            name.add(jsonObj.getString("name"));
            description.add(jsonObj.getString("description"));
            EventId.add(jsonObj.getString("_id"));
        }
    }
    //------------------End get members----------------------------
    void parseDate(ArrayList<String> str){
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        for(int i=0;i<str.size();i++){
            try {
                date.add(format.parse(str.get(i)));
            }
            catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
