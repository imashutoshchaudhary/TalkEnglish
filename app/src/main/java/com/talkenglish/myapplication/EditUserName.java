package com.talkenglish.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EditUserName extends AppCompatActivity {

    EditText editName,status;
    Button editNameBtn;
    ImageView imageURI;
    String str;
    FirebaseAuth mAuth;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    StorageTask mUploadTask;
    ProgressDialog pd;
    String currentVersion, latestVersion;
    Dialog dialog;
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String urlOfAppFromPlayStore="https://play.google.com/store/apps/details?id=com.talkenglish.myapplication&hl=en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_name);

        getCurrentVersion();
        FirebaseApp.initializeApp(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("UserProfileName");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("UserProfileName");
        pd = new ProgressDialog(this);

        mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.icon)
                + '/' + getResources().getResourceTypeName(R.drawable.icon) +
                '/' + getResources().getResourceEntryName(R.drawable.icon) );

        mAuth = FirebaseAuth.getInstance();

        editName = findViewById(R.id.editName);
        status = findViewById(R.id.status);
        editNameBtn = findViewById(R.id.editNameBtn);

        str = editName.getText().toString();

        editNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName.getText().toString().equals("")){
                    editName.setError("Please Enter Your name");
                }
                else {
                    uploadDetails();
                }
            }
        });
    }

    private boolean uploadDetails() {

        pd.setMessage("Uploading...");
        pd.show();
        pd.setCanceledOnTouchOutside(false);

        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg" );
            mUploadTask= fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri a=taskSnapshot.getDownloadUrl();
                            Toast.makeText(getApplicationContext(), "Upload successful", Toast.LENGTH_LONG).show();
                            UploadProfileName upload = new UploadProfileName(uid,editName.getText().toString(),
                                    status.getText().toString(),
                                    a.toString());
                            mDatabaseRef.child(uid).setValue(upload);
                            pd.dismiss();
                            Intent moveToHome = new Intent(EditUserName.this, MainActivityMain.class);
                            moveToHome.putExtra("name",editName.getText().toString());
                            moveToHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(moveToHome);

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
        super.onBackPressed();
    }

    private void getCurrentVersion(){
        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);

        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;

        new GetLatestVersion().execute();

    }

    private class GetLatestVersion extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                Document doc = Jsoup.connect(urlOfAppFromPlayStore).get();
                latestVersion = doc.getElementsByClass("htlgb").get(6).text();
            }catch (Exception e){
                e.printStackTrace();
            }
            return new JSONObject();
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if(latestVersion!=null) {
                if (!currentVersion.equalsIgnoreCase(latestVersion)){
                    if(!isFinishing()){
                        showUpdateDialog();
                    }
                }
            }
            else
            super.onPostExecute(jsonObject);
        }
    }

    private void showUpdateDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("A New Update is Available");
        builder.setCancelable(false);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        (urlOfAppFromPlayStore)));
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        dialog = builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentVersion();
    }
}
