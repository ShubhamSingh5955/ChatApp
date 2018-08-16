package com.example.dell.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ToolbarWidgetWrapper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

   private TextInputLayout nameText,emailText,passwordText;
   private Button regButton;
   private FirebaseAuth mAuth;
   private Toolbar regToolbar;

   private ProgressDialog mprogressDialog;

   private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        nameText=(TextInputLayout)findViewById(R.id.reg_name);
        emailText=(TextInputLayout)findViewById(R.id.reg_email);
        passwordText=(TextInputLayout)findViewById(R.id.reg_password);
        regButton=(Button)findViewById(R.id.reg_Button);

        mprogressDialog=new ProgressDialog(this);

        regToolbar=(Toolbar)findViewById(R.id.reg_toolbar);
        setSupportActionBar(regToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=nameText.getEditText().getText().toString();
                String email=emailText.getEditText().getText().toString();
                String password=passwordText.getEditText().getText().toString();

                mprogressDialog.setTitle("Create Account");
                mprogressDialog.setMessage("please wait while we create account...");
                mprogressDialog.setCanceledOnTouchOutside(false);
                mprogressDialog.show();

                registerUser(name,email,password);
            }
        });


    }

    private void registerUser(final String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser=mAuth.getCurrentUser();
                            String uid=currentUser.getUid();
                            mDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            String device_token= FirebaseInstanceId.getInstance().getToken();

                            HashMap<String,String> userMap=new HashMap<>();
                            userMap.put("name",name);
                            userMap.put("status","default");
                            userMap.put("image","default");
                            userMap.put("thumb_image","default");
                            userMap.put("device_token",device_token);
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mprogressDialog.dismiss();

                                    Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();

                                }
                            });


                        } else {

                            mprogressDialog.hide();

                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


}
