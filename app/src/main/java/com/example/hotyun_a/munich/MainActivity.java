package com.example.hotyun_a.munich;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKCaptchaDialog;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends ActionBarActivity implements View.OnClickListener,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;
    private GoogleApiClient mGoogleApiClient;
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    private AccessToken accessToken = null;
    private String oauthVerifier;
    private String twToken,twSecret,twId;
    private SearchView svEvent;
    private ListView lvMain,lvMain1;
    private ArrayList<String> nameEvent=new ArrayList<>();
    private ArrayList<String> description=new ArrayList<>();
    private ArrayList<String> startDate=new ArrayList<>();
    private ArrayList<String> endDate=new ArrayList<>();
    private ArrayList<String> EventId=new ArrayList<>();
    private ArrayList<String> EventForList=new ArrayList<>();
    private ArrayList<String> EventForListTom=new ArrayList<>();
    private CheckInterNetConnection check;
    private Boolean isInternetPresent = false;
    private long currentTime;
    private long nextDay;
    private TextView tomorrow;
    private Button btnLoad;
    private int count,networkId;
    private String[] mScreenTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private SharedPreferences sPref;
    private Editor ed;
    private String token,idFb;
    private String picture,firstName,lastName,provider,gender="none",nameOfuser;
    private String VK_KEY;
    private String[] vkScope={VKScope.FRIENDS,VKScope.WALL,VKScope.PHOTOS,VKScope.STATUS};
    private static String sTokenKey = "VK_ACCESS_TOKEN";
    private Menu menuGlob;
    private Fragment fragment=null;
    private  PendingIntent pi;
    private AlarmManager am;
    public void showAlertDialogInterNet(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setPositiveButton("Wi-Fi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                finish();
            }
        });
        alertDialog.setNegativeButton("Mobile internet",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                finish();
            }
        });
        alertDialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        check = new CheckInterNetConnection(getApplicationContext());
        isInternetPresent = check.isConnectingToInternet();
        if(!isInternetPresent) {
            showAlertDialogInterNet(MainActivity.this, "Error network connection","No Internet Connection, Please try again");
        }
        else {
            onNewIntent(getIntent());
            currentTime = System.currentTimeMillis()/1000L;
            setContentView(R.layout.main_fragment);
            sPref = PreferenceManager.getDefaultSharedPreferences(this);
            ed=sPref.edit();
            networkId = sPref.getInt("networkId", 0);
            mTitle = mDrawerTitle = getTitle();
            navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
            mScreenTitles = getResources().getStringArray(R.array.screen_array);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.left_drawer);
            navDrawerItems = new ArrayList<>();
            navDrawerItems.add(new NavDrawerItem(mScreenTitles[0], navMenuIcons.getResourceId(0, -1)));
            navDrawerItems.add(new NavDrawerItem(mScreenTitles[1], navMenuIcons.getResourceId(1, -1)));
            navDrawerItems.add(new NavDrawerItem(mScreenTitles[2], navMenuIcons.getResourceId(2, -1)));
            navDrawerItems.add(new NavDrawerItem(mScreenTitles[3], navMenuIcons.getResourceId(3, -1)));
            navDrawerItems.add(new NavDrawerItem(mScreenTitles[4], navMenuIcons.getResourceId(4, -1)));
            navDrawerItems.add(new NavDrawerItem(mScreenTitles[5], navMenuIcons.getResourceId(5, -1)));
            navMenuIcons.recycle();
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
            adapter = new NavDrawerListAdapter(getApplicationContext(),
                    navDrawerItems);
            mDrawerList.setAdapter(adapter);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    getSupportActionBar().setTitle(mTitle);
                    supportInvalidateOptionsMenu();
                }
                public void onDrawerOpened(View drawerView) {
                    getSupportActionBar().setTitle(mDrawerTitle);
                    supportInvalidateOptionsMenu();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            if (savedInstanceState == null) {
                selectItem(0);
            }
            count = 0;
            tomorrow = (TextView) findViewById(R.id.tommorow);
            tomorrow.setVisibility(View.INVISIBLE);
            btnLoad = (Button) findViewById(R.id.buttonLoad);
            btnLoad.setOnClickListener(this);
            if(networkId!=0) {
                switch (networkId) {
                    case Const.LocalId:
                        token = sPref.getString("token","bad very bad");
                        new GetInfoCurrentUser().execute("http://digitalocean.onlinepc.com.ua:9000/api/users/me");
                        break;
                    case Const.GoogleId:
                        mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Plus.API, Plus.PlusOptions.builder()
                            .addActivityTypes(Const.ACTIONS).build())
                            .addScope(Plus.SCOPE_PLUS_LOGIN)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                        break;
                    case Const.TwitterId:
                        new GetRequestTokenTask().execute();
                        break;
                    case Const.FacebookId:
                        Session.openActiveSession(this, true, new Session.StatusCallback() {
                            @Override
                            public void call(Session session, SessionState state, Exception exception) {
                                if (session.isOpened()) {
                                    Request.newMeRequest(session,new Request.GraphUserCallback() {
                                        @Override
                                        public void onCompleted(GraphUser user,Response response) {
                                        }
                                    }).executeAsync();
                                }
                            }
                        });
                        Session session = Session.getActiveSession();
                        if (session.isOpened()) {
                            token=session.getAccessToken();
                            new FacebookVkTwitterGoogleRequest().execute("http://digitalocean.onlinepc.com.ua:9000/auth/facebook/login?token="+token);
                        }
                        break;
                    case Const.VkId:
                        VKUIHelper.onCreate(this);
                        VK_KEY=this.getString(R.string.vk_app_id);
                        VKSdk.initialize(sdkListener, VK_KEY, VKAccessToken.tokenFromSharedPreferences(this,sTokenKey));
                        if (VKSdk.wakeUpSession()) {
                            Toast.makeText(this,"You login",Toast.LENGTH_SHORT).show();
                        }
                        VKSdk.authorize(vkScope,false,false);
                        break;
                }
            }
            new GetEventList().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/get_events_by_date/" + currentTime);
        }
    }
    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Property property= new Property();
            Bundle bundle=new Bundle();
            bundle.putString("nameEvent",extras.getString("name"));
            bundle.putString("description", extras.getString("description"));
            bundle.putString("startDate", extras.getString("startDate"));
            bundle.putString("token",extras.getString("token"));
            bundle.putString("event_id",extras.getString("id"));
            property.setArguments(bundle);
            fragment = property;
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menuGlob=menu;
        boolean drawerOpen;
        if(isInternetPresent) {
            drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            menu.findItem(R.id.action_search).setVisible(!drawerOpen);
            menu.findItem(R.id.action_refresh).setVisible(!drawerOpen);
        }
        if(networkId!=0)
            menu.findItem(R.id.action_logout).setVisible(true);
        else
            menu.findItem(R.id.action_logout).setVisible(false);
        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        svEvent = (SearchView) menu.findItem(R.id.action_search).getActionView();
        svEvent.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.action_search:
                return true;
            case R.id.action_logout:
                ed.clear();
                ed.commit();
                if(networkId!=0) {
                    networkId=sPref.getInt("networkId",0);
                    menuGlob.findItem(R.id.action_logout).setVisible(false);
                    /*if(am!=null)
                        am.cancel(pi);//close alarm manager*/
                    stopService(new Intent(MainActivity.this,ServiceCheckDateEvent.class));
                    switch (networkId) {
                        case Const.LocalId:
                            break;
                        case Const.GoogleId:
                            mGoogleApiClient.disconnect();
                            break;
                        case Const.TwitterId:
                            break;
                        case Const.FacebookId:
                            Session session = Session.getActiveSession();
                            if (session.isOpened()) {
                                session.closeAndClearTokenInformation();
                            }
                            break;
                        case Const.VkId:
                            VKSdk.logout();
                            break;
                    }
                }
                else
                    Toast.makeText(MainActivity.this,"You have already logged out",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_refresh:
                new GetEventList().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/get_events_by_date/" + currentTime);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount() == 0)
            setTitle(mScreenTitles[0]);
    }
    private void selectItem(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                fragment=null;
                mDrawerList.setItemChecked(position, true);
                setTitle(mScreenTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
                if(getSupportFragmentManager().getBackStackEntryCount()!=0) {
                    finish();
                    Intent intent=new Intent(this,MainActivity.class);
                    startActivity(intent);
                }
                else
                    break;
                break;
            case 1:
                fragment=null;
                if(networkId==0){
                    fragment=new LoginFragment();
                }
                else {
                    Toast.makeText(MainActivity.this,"You have already logged",Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(mDrawerList);
                    break;
                }
                break;
            case 2:
                fragment=null;
                if(networkId==0){
                    Toast.makeText(MainActivity.this,"You should login first.",Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(mDrawerList);
                    break;
                }
                else {
                    ShowUserProfile sh_user_prof= new ShowUserProfile();
                    bundle.putString("first_name", firstName);
                    bundle.putString("last_name", lastName);
                    bundle.putString("gender", gender);
                    bundle.putString("provider", provider);
                    if(provider.equals("facebook")) {
                        bundle.putString("id", idFb);
                    }
                    else
                        bundle.putString("picture", picture);
                    sh_user_prof.setArguments(bundle);
                    fragment = sh_user_prof;
                }
                break;
            case 3:
                fragment=null;
                if(networkId==0){
                    Toast.makeText(MainActivity.this,"You should login first.",Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(mDrawerList);
                    break;
                }
                else {
                    ShowCalendar shCal = new ShowCalendar();
                    bundle.putString("token",token);
                    shCal.setArguments(bundle);
                    fragment = shCal;
                }
                break;
            case 4:
                fragment=null;
                if(networkId==0){
                    Toast.makeText(MainActivity.this,"You should login first.",Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(mDrawerList);
                    break;
                }
                else{
                    FavouriteEvent fvEv = new FavouriteEvent();
                    bundle.putStringArrayList("nameEvent",nameEvent);
                    bundle.putStringArrayList("description",description);
                    bundle.putStringArrayList("startDate",startDate);
                    fvEv.setArguments(bundle);
                    fragment = fvEv;
                }
                break;
            case 5:
                finish();
                break;
        }
        if (fragment != null && bundle!=null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();
            mDrawerList.setItemChecked(position, true);
            setTitle(mScreenTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(isInternetPresent)
            mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
        @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.buttonLoad:
                count++;
                nameEvent.clear();
                tomorrow.setVisibility(View.VISIBLE);
                nextDay=currentTime+86400;
                new GetEventList().execute("http://digitalocean.onlinepc.com.ua:9000/api/events/get_events_by_date/"+nextDay);
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
            if (resultCode == RESULT_OK && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                oauthVerifier = data.getExtras().getString(Const.IEXTRA_OAUTH_VERIFIER);
                new GetAccessTokenTask().execute();
            }
        }
        if(networkId==Const.FacebookId){
            Session.getActiveSession().onActivityResult(this, requestCode,
                    resultCode, data);
        }
        if (requestCode == VKSdk.VK_SDK_REQUEST_CODE) {
            VKSdk.processActivityResult(requestCode, resultCode, data);
        }
    }
    //------------------------Start service------------------------------
    public void setScheduleMessage(Context context) {
        Intent intent;
        intent=new Intent(context, ServiceCheckDateEvent.class);
        pi = PendingIntent.getService(context, 0,intent
                ,PendingIntent.FLAG_UPDATE_CURRENT);
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 1); // For 1 PM or 2 PM
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
               AlarmManager.INTERVAL_DAY, pi);
    }
    //------------------------Get event list----------------
    class GetEventList extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ArrayAdapter for_list_today,for_list_tom;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog= ProgressDialog.show(MainActivity.this, null,"Get event list", true);
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(params[0]);
                response = client.execute(request);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                parseDataEventList(responseString);
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
                EventForList=nameEvent;
                if(count==0 && EventForList.size()!=0) {
                    lvMain = (ListView) findViewById(R.id.listToday);
                    for_list_today=new ArrayAdapter<>(MainActivity.this,R.layout.listtoday,EventForList);
                    lvMain.setAdapter(for_list_today);
                    lvMain.setTextFilterEnabled(true);
                    setListViewHeightBasedOnChildren(lvMain);
                    lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Property property= new Property();
                            Bundle bundle=new Bundle();
                            bundle.putString("nameEvent",EventForList.get(position));
                            bundle.putString("description", description.get(position));
                            bundle.putString("startDate", startDate.get(position));
                            bundle.putString("token",token);
                            bundle.putString("event_id",EventId.get(position));
                            if(networkId!=0)
                                bundle.putInt("check",1);
                            property.setArguments(bundle);
                            setTitle(EventForList.get(position));
                            fragment = property;
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();
                        }
                    });
                }
                else {
                    EventForListTom=nameEvent;
                    lvMain1 = (ListView) findViewById(R.id.listTommorow);
                    for_list_tom=new ArrayAdapter<>(MainActivity.this,R.layout.listtoday,EventForList);
                    lvMain1.setAdapter(for_list_tom);
                    lvMain1.setTextFilterEnabled(true);
                    setListViewHeightBasedOnChildren(lvMain1);
                    lvMain1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Property property= new Property();
                            Bundle bundle=new Bundle();
                            bundle.putString("nameEvent",EventForList.get(position));
                            bundle.putString("description", description.get(position));
                            bundle.putString("startDate", startDate.get(position));
                            bundle.putString("startDate",endDate.get(position));
                            bundle.putString("token",token);
                            bundle.putString("event_id",EventId.get(position));
                            if(networkId!=0)
                                bundle.putInt("check",1);
                            property.setArguments(bundle);
                            setTitle(EventForList.get(position));
                            fragment = property;
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.content_frame, fragment).commit();
                        }
                    });
                }
                SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                    public boolean onQueryTextChange(String newText) {
                        if (TextUtils.isEmpty(newText)) {
                            if(EventForList.size()!=0)
                                lvMain.clearTextFilter();
                            if(EventForListTom.size()!=0) {
                                lvMain1.clearTextFilter();
                                if(EventForList.size()!=0)
                                    lvMain.clearTextFilter();
                            }
                        }
                        else {
                            if(EventForList.size()!=0)
                                lvMain.setFilterText(newText.toString());
                            if(EventForListTom.size()!=0) {
                                lvMain1.setFilterText(newText.toString());
                                if(EventForList.size()!=0)
                                    lvMain.setFilterText(newText.toString());
                            }
                        }
                        return false;
                    }
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }
                };
                if(EventForList.size()!=0)
                    svEvent.setOnQueryTextListener(queryTextListener);
            }
            else
                Toast.makeText(MainActivity.this, "ERROR: can not load data", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    void parseDataEventList(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        JSONObject jsonData,jsonRes;
        JSONArray json_array;
        jsonData=jsonObject.getJSONObject("data");
        json_array=jsonData.getJSONArray("events");
        for(int k=0;k<json_array.length();k++){
            jsonRes=json_array.getJSONObject(k);
            nameEvent.add(jsonRes.getString("name"));
            description.add(jsonRes.getString("description"));
            startDate.add(jsonRes.getString("startDate"));
            endDate.add(jsonRes.getString("endDate"));
            EventId.add(jsonRes.getString("_id"));
        }
    }
    //------------------------End EVENTS----------------------
    public class CheckInterNetConnection {
        private Context _context;
        public CheckInterNetConnection(Context context){
            this._context = context;
        }
        public boolean isConnectingToInternet(){
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null){
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED){
                            return true;
                        }
            }
            return false;
        }
    }
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount())+55);
        listView.setLayoutParams(params);
    }
    //----------Modern request for all social from EventList.java--------------------------------------
    //--------------------------VK auth-----------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }
    private VKSdkListener sdkListener = new VKSdkListener(){
        @Override
        public void onCaptchaError(VKError captchaError){
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken){
            VKSdk.authorize(vkScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError){
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(authorizationError.errorMessage)
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken){
            String accessToken = newToken.accessToken;
            new FacebookVkTwitterGoogleRequest().execute("http://digitalocean.onlinepc.com.ua:9000/auth/vk/login?token="+accessToken);
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token){
            String accessToken = token.accessToken;
            new FacebookVkTwitterGoogleRequest().execute("http://digitalocean.onlinepc.com.ua:9000/auth/vk/login?token="+accessToken);
        }
    };

    //--------------------------End VK auth-------------------------------
    //---------------------GooglePlus access token-----------------------
    @Override
    protected void onStart() {
        super.onStart();
        if(networkId==Const.GoogleId)
            mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        final Context context = this.getApplicationContext();
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                String scope = "oauth2:" + Scopes.PLUS_LOGIN;
                try {
                    token = GoogleAuthUtil.getToken(context, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                    ed.putString("token",token);
                    ed.commit();
                } catch (UserRecoverableAuthException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);
                new FacebookVkTwitterGoogleRequest().execute("http://digitalocean.onlinepc.com.ua:9000/auth/google/login?token="+token);
            }
        };
        task.execute((Void) null);
    }
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        ConnectionResult mConnectionResult = result;
        if (result.hasResolution()) {
            try {
                mConnectionResult.startResolutionForResult(this,REQUEST_CODE_SIGN_IN);
            }
            catch (IntentSender.SendIntentException e) {
                // Fetch a new result to start.
                mGoogleApiClient.connect();
            }
        }
    }
    //---------------------End GP access token---------------------------
    //------------------------Twitter Login-------------------
    private class GetRequestTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ConfigurationBuilder confbuilder = new ConfigurationBuilder();
            twitter4j.conf.Configuration conf = confbuilder
                    .setOAuthConsumerKey(Const.CONSUMER_KEY)
                    .setOAuthConsumerSecret(Const.CONSUMER_SECRET)
                    .build();
            mTwitter = new TwitterFactory(conf).getInstance();
            mTwitter.setOAuthAccessToken(null);
            try {
                mRequestToken = mTwitter.getOAuthRequestToken(Const.CALLBACK_URL);
                Intent intent = new Intent(MainActivity.this, TwitterLogin.class);
                intent.putExtra(Const.IEXTRA_AUTH_URL, mRequestToken.getAuthorizationURL());
                startActivityForResult(intent, 0);
            }
            catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class GetAccessTokenTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... string) {
            try {
                accessToken = mTwitter.getOAuthAccessToken(mRequestToken, oauthVerifier);
            }
            catch(TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute (String result){
            super.onPostExecute(result);
            twId=Long.toString(accessToken.getUserId());
            twSecret=accessToken.getTokenSecret();
            twToken=accessToken .getToken();
            ed.putString("token",twToken);
            ed.putString("token_secret",twSecret);
            ed.putString("ID",twId);
            ed.commit();
            Toast.makeText(MainActivity.this, "authorized", Toast.LENGTH_SHORT).show();
            new FacebookVkTwitterGoogleRequest().execute("http://digitalocean.onlinepc.com.ua:9000/auth/twitter/login?token="
                    +twToken+"&tokenSecret="+twSecret+"&userId="+twId);
        }
    }
    //------------------------End twitter login---------------
    //--------------------------Facebook/Vk/Twitter/Google----------------
    class FacebookVkTwitterGoogleRequest extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            switch (networkId) {
                case Const.GoogleId:
                    progressDialog= ProgressDialog.show(MainActivity.this, null,"Connect to google...", true);
                    break;
                case Const.TwitterId:
                    progressDialog= ProgressDialog.show(MainActivity.this, null,"Connect to twitter...", true);
                    break;
                case Const.FacebookId:
                    progressDialog= ProgressDialog.show(MainActivity.this, null,"Connect to facebook...", true);
                    break;
                case Const.VkId:
                    progressDialog= ProgressDialog.show(MainActivity.this, null,"Connect to vk...", true);
                    break;
            }
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(params[0]);
                response = client.execute(request);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                parseDataFacebook(responseString);
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
                ed.clear();
                ed.commit();
                ed.putString("token",token);
                ed.putInt("networkId",networkId);
                ed.commit();
                //setScheduleMessage(getApplicationContext());
                startService(new Intent(MainActivity.this,ServiceCheckDateEvent.class));
                new GetInfoCurrentUser().execute("http://digitalocean.onlinepc.com.ua:9000/api/users/me");
            }
            else {
                Toast.makeText(MainActivity.this, "ERROR: error to get answer from server", Toast.LENGTH_LONG).show();
            }
            progressDialog.dismiss();
        }
    }
    void parseDataFacebook(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        for(int i=0;i<jsonObject.length();i++) {
            token = jsonObject.getString("data");
        }
    }
    //------------------------End Facebook/Vk/twitter/google login---------------------
    //------------------------Start get current user data------------------------------
    class GetInfoCurrentUser extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog= ProgressDialog.show(MainActivity.this, null,"Get info about current user", true);
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet(params[0]);
                request.setHeader("Authorization","Bearer "+token);
                response = client.execute(request);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                parseDataUser(responseString);
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
            }
            else
                Toast.makeText(MainActivity.this, "ERROR: error to get answer from server", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    void parseDataUser(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        provider=jsonObject.getString("provider");
        if(provider.equals("google")){
            JSONObject json=jsonObject.getJSONObject("google");
            picture=json.getString("picture");
            gender=json.getString("gender");
            firstName=json.getString("given_name");
            lastName=json.getString("family_name");
        }
        if(provider.equals("twitter")){
            JSONObject json=jsonObject.getJSONObject("twitter");
            picture=json.getString("profile_image_url");
            nameOfuser=json.getString("name");
            String str[]=nameOfuser.split(" ");
            firstName=str[0];
            lastName=str[1];
        }
        if(provider.equals("vkontakte")){
            JSONObject json=jsonObject.getJSONObject("vkontakte");
            picture=json.getString("photo");
            firstName=json.getString("first_name");
            lastName=json.getString("last_name");
            if(json.getString("sex").equals("2"))
                gender="female";
            else
                gender="male";
        }
        if(provider.equals("facebook")){
            JSONObject json=jsonObject.getJSONObject("facebook");
            firstName=json.getString("first_name");
            lastName=json.getString("last_name");
            gender=json.getString("gender");
            idFb=json.getString("id");
        }
        if(provider.equals("local")){
            nameOfuser=jsonObject.getString("name");
            String str[]=nameOfuser.split(" ");
            firstName=str[0];
            lastName=str[1];
        }
    }
    //------------------------End current user data-----------
    //----------End Modern request for all social from EventList.java----------------------------------
}