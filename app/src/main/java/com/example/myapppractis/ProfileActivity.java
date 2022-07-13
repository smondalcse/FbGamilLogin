package com.example.myapppractis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    TextView name,email,familyName;
    Button signOutBtn;
    ImageView imgProfileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        familyName = findViewById(R.id.familyName);
        imgProfileImage = findViewById(R.id.imgProfileImage);
        signOutBtn = findViewById(R.id.signout);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String fName = acct.getFamilyName();
            name.setText(personName);
            email.setText(personEmail);
            familyName.setText(fName);
            Uri profileImage = acct.getPhotoUrl();
            Log.i(TAG, "onCreate: " + profileImage);

            Picasso.get().load(profileImage).into(imgProfileImage);

        }

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });



    }

    void signOut(){
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                finish();
                startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            }
        });
    }
}