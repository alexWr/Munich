package com.example.hotyun_a.munich;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class EditUserProfile extends Fragment {

    private Button change;
    private static int RESULT_LOAD_IMAGE = 1;
    private String firstName,lastName,photo;
    private ImageView imageViewUser;
    private EditText edlastName,edFirstName,etInterest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.edit_user_profile, container, false);
        firstName=getArguments().getString("first_name");
        lastName=getArguments().getString("last_name");
        photo=getArguments().getString("photo");
        change=(Button)rootView.findViewById(R.id.change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            Intent i = new  Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        imageViewUser=(ImageView)rootView.findViewById(R.id.imageUser);
        edFirstName=(EditText)rootView.findViewById(R.id.firstNameEdit);
        edlastName=(EditText)rootView.findViewById(R.id.secondNameEdit);
        etInterest=(EditText)rootView.findViewById(R.id.my_interestEdit);
        edFirstName.setText(firstName);
        edlastName.setText(lastName);
        Picasso.with(getActivity()).load(photo).fit().into(imageViewUser);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageViewUser.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}
