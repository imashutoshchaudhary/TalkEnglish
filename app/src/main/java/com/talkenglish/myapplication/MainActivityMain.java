package com.talkenglish.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivityMain extends AppCompatActivity {

    ImageView gifImage;
    TextView callState;
    Button endCall;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef,mDatabaseRef1;
    StorageTask mUploadTask;
    ProgressDialog pd;
    EditText mainStatus,mainStatus2;
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String otherUserID;
    String name;
    Chronometer timer;

    private RecyclerView rv;
    private List<UploadProfileName> listData;
    private AdapterClass adapter;

    private static final String APP_KEY = "04c5290b-8d9d-4432-bf6b-2dd070164800";
    private static final String APP_SECRET = "gh+iAvVphEGnqiNKFW29xQ==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    SinchClient sinchClient;
    private Call call;

    private final String TAG = MainActivityMain.class.getSimpleName();
    private NativeAd nativeAd;
    private NativeAdLayout nativeAdLayout;
    private LinearLayout adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_main);

        FirebaseApp.initializeApp(this);
        AudienceNetworkAds.initialize(this);
        loadNativeAd();

        mainStatus = findViewById(R.id.mainStatus);
        mainStatus2 = findViewById(R.id.mainStatus2);
        gifImage = findViewById(R.id.gifImage);
        endCall = findViewById(R.id.endCall);
        callState = (TextView) findViewById(R.id.callState);
        timer = findViewById(R.id.timer);
        callState.setVisibility(View.INVISIBLE);
        //endCall.setVisibility(View.INVISIBLE);
        endCall.setEnabled(false);
        Glide.with(this).asGif().load(R.drawable.call_button_gif).into(gifImage);

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();

        mStorageRef = FirebaseStorage.getInstance().getReference("UserProfileName");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserProfileName");
        mDatabaseRef1 = FirebaseDatabase.getInstance().getReference("UserStatus");

        pd = new ProgressDialog(this);

        mDatabaseRef1.addValueEventListener(new ValueEventListener() {
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

        rv = findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        listData=new ArrayList();

        mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.icon)
                + '/' + getResources().getResourceTypeName(R.drawable.icon) +
                '/' + getResources().getResourceEntryName(R.drawable.icon) );

        name = getIntent().getExtras().getString("name");

        settingStatus();

        call = null;
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(uid)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        gifImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sinchClient.getCallClient().callUser("call-recipient-id");
                mDatabaseRef1.child(uid).removeValue();
                //call.addCallListener(new SinchCallListener());
                gettingOtherUserId();
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();
                callState.setVisibility(View.VISIBLE);
                gifImage.setVisibility(View.INVISIBLE);
            }
        });

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.hangup();
                callState.setText("Call Ended");
                gifImage.setVisibility(View.VISIBLE);
                callState.setVisibility(View.INVISIBLE);
                timer.stop();
                timer.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingStatus();
    }

    public void gettingOtherUserId() {
        Toast.makeText(this, "Getting Other Users", Toast.LENGTH_SHORT).show();

        final DatabaseReference login=mDatabaseRef1.child(uid);
        mDatabaseRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    UploadProfileName uploadProfileName = dataSnapshot.getValue(UploadProfileName.class);
                    Toast.makeText(getApplicationContext(),uploadProfileName.toString(),Toast.LENGTH_LONG).show();



            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean settingStatus() {

        pd.setMessage("Setting Status..");
        pd.show();
        pd.setCanceledOnTouchOutside(false);

        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg" );
            mUploadTask= fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri a=taskSnapshot.getDownloadUrl();
                            UploadProfileName upload = new UploadProfileName(uid,name,
                                    mainStatus.getText().toString(),
                                    a.toString());
                            mDatabaseRef.child(uid).setValue(upload);
                            mDatabaseRef1.child(uid).setValue(upload);
                            pd.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
            settingStatus2();
    }

    private boolean settingStatus2() {

        pd.setMessage("Setting Status..");
        pd.show();
        pd.setCanceledOnTouchOutside(false);

        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg" );
            mUploadTask= fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri a=taskSnapshot.getDownloadUrl();
                            UploadProfileName upload = new UploadProfileName(uid,name,
                                    mainStatus2.getText().toString(),
                                    a.toString());
                            mDatabaseRef.child(uid).setValue(upload);
                            finish();
                            pd.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        else
        {
            return false;
        }
    }

    public class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            //call ended by either party
            callState.setText("Call Ended");
            /*String timerTime = timer.getText().toString();
            if (Integer.parseInt(timerTime) >= 0 && Integer.parseInt(timerTime) <= 50){
                endCall.setEnabled(false);
            }
            else
            {
                endCall.setEnabled(true);
            }*/
            gifImage.setVisibility(View.VISIBLE);
            callState.setVisibility(View.INVISIBLE);
            timer.stop();
            timer.setVisibility(View.INVISIBLE);
            endCall.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            //incoming call was picked up
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();
            callState.setText("Connected");
            endCall.setVisibility(View.VISIBLE);
            endCall.setEnabled(false);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            //call is ringing
            callState.setText("Ringing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            //don't worry about this right now
        }
    }

    public class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            //Pick up the call!
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getApplicationContext());
            builder1.setMessage("Incoming Call");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Receive",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           call.answer();
                           timer.start();
                           dialog.dismiss();

                        }
                    });

            builder1.setNegativeButton(
                    "HangUP",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            call.hangup();
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    private void loadNativeAd() {
        nativeAd = new NativeAd(this, "412962772816027_412964726149165");
        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(nativeAd);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });
        nativeAd.loadAd();
    }

    private void inflateAd(NativeAd nativeAd) {

        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        nativeAdLayout = findViewById(R.id.native_ad_container);
        LayoutInflater inflater = LayoutInflater.from(MainActivityMain.this);
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(MainActivityMain.this, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }

}
