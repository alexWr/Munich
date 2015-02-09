package com.example.hotyun_a.munich;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ShowUserProfile extends Fragment{
    private String firstName,lastName,photo,id,provider;
    private ImageView imageViewUser;
    private EditText edlastName,edFirstName,etInterest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.show_user_profile, container, false);
        provider=getArguments().getString("provider");
        firstName=getArguments().getString("first_name");
        lastName=getArguments().getString("last_name");
        if(provider.equals("facebook")) {
            id=getArguments().getString("id");
            photo = "https://graph.facebook.com/"+id+"/picture?type=large";
        }
        else
            photo=getArguments().getString("picture");
        imageViewUser=(ImageView)rootView.findViewById(R.id.imageViewUser);
        edFirstName=(EditText)rootView.findViewById(R.id.firstName);
        edlastName=(EditText)rootView.findViewById(R.id.secondName);
        etInterest=(EditText)rootView.findViewById(R.id.my_interest);
        etInterest=(EditText)rootView.findViewById(R.id.my_interest);
        Picasso.with(getActivity()).load(photo).fit().into(imageViewUser);
        edFirstName.setText(firstName);
        edlastName.setText(lastName);
        setHasOptionsMenu(true);
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit, menu);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.action_edit:
                EditUserProfile fragment=new EditUserProfile();
                bundle.putString("first_name", firstName);
                bundle.putString("last_name", lastName);
                bundle.putString("photo", photo);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().
                    addToBackStack(null).replace(R.id.content_frame, fragment).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
