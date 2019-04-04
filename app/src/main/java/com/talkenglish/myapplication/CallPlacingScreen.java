package com.talkenglish.myapplication;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mangstadt.vinnie.Utils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallListener;

import java.util.ArrayList;
import java.util.List;

public class CallPlacingScreen extends AppCompatActivity {

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    StorageTask mUploadTask;
    ProgressDialog pd;
    Button placeCall;

    private RecyclerView rv;
    private List<UploadProfileName> listData;
    private AdapterClass adapter;
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private static final String APP_KEY = "04c5290b-8d9d-4432-bf6b-2dd070164800";
    private static final String APP_SECRET = "gh+iAvVphEGnqiNKFW29xQ==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    SinchClient sinchClient;
    private Call call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_placing_screen);

        FirebaseApp.initializeApp(this);



        mStorageRef = FirebaseStorage.getInstance().getReference("UserProfileName");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserStatus");
        pd = new ProgressDialog(this);


        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    UploadProfileName uploadProfileName = childDataSnapshot.getValue(UploadProfileName.class);
                    listData.add(uploadProfileName);
                }
                adapter = new AdapterClass(listData);
                rv.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void placeCall() {
        mDatabaseRef.child(uid).removeValue();
    }

}