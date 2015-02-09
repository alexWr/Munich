package com.example.hotyun_a.munich;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


public class LoginFragment extends Fragment {

    private Button facebook;
    private Button twitter;
    private Button vk;
    private Button googleplus;
    private Button enter,registration;
    private EditText username,password;
    private int networkId = 0;
    private SharedPreferences sPref;
    private Editor ed;
    private String access_token,user,pass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.login_fragment, container, false);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        sPref= PreferenceManager.getDefaultSharedPreferences(getActivity());
        ed=sPref.edit();
        facebook = (Button) rootView.findViewById(R.id.facebook);
        facebook.setOnClickListener(loginClick);
        twitter = (Button) rootView.findViewById(R.id.twitter);
        twitter.setOnClickListener(loginClick);
        vk = (Button) rootView.findViewById(R.id.vk);
        vk.setOnClickListener(loginClick);
        googleplus = (Button) rootView.findViewById(R.id.googleplus);
        googleplus.setOnClickListener(loginClick);
        enter=(Button)rootView.findViewById(R.id.enter);
        enter.setOnClickListener(loginClick);
        registration=(Button)rootView.findViewById(R.id.registration);
        registration.setOnClickListener(loginClick);
        username=(EditText)rootView.findViewById(R.id.username);
        password=(EditText)rootView.findViewById(R.id.password);
        setHasOptionsMenu(true);
        return rootView;
    }
    private View.OnClickListener loginClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.facebook:
                    networkId = Const.FacebookId;
                    startProfile(networkId);
                    break;
                case R.id.vk:
                    networkId = Const.VkId;
                    startProfile(networkId);
                    break;
                case R.id.twitter:
                    networkId = Const.TwitterId;
                    startProfile(networkId);
                    break;
                case R.id.googleplus:
                    networkId = Const.GoogleId;
                    startProfile(networkId);
                    break;
                case R.id.enter:
                    networkId=Const.LocalId;
                    new RequestTaskLocalLogin().execute("http://digitalocean.onlinepc.com.ua:9000/auth/local");
                    break;
                case R.id.registration:
                    break;
            }
        }
    };
    private void startProfile(int networkId){
        ed.putInt("networkId",networkId);
        ed.commit();
        System.out.println(sPref.getInt("networkId",0));
        Intent intent=new Intent(getActivity(),MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
    }
    //---------------------------Local Login-----------------------------------
    class RequestTaskLocalLogin extends AsyncTask<String, String, String> {
        HttpResponse response;
        String responseString;
        @Override
        protected void onPreExecute() {
            user=username.getText().toString();
            pass=password.getText().toString();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            try{
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost request = new HttpPost(params[0]);
                StringEntity se = new StringEntity("email="+user+"&password="+pass);
                request.setHeader("Content-Type","application/x-www-form-urlencoded");
                request.setEntity(se);
                response = client.execute(request);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                parseData(responseString);
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
                ed.putString("token",access_token);
                ed.commit();
                startProfile(networkId);
            }
            else Toast.makeText(getActivity(), "ERROR: IncorrectData", Toast.LENGTH_LONG).show();
        }
    }
    void parseData(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        for(int i=0;i<jsonObject.length();i++) {
            access_token = jsonObject.getString("data");
        }
    }
    //---------------------------End local login---------------------------------------
}
