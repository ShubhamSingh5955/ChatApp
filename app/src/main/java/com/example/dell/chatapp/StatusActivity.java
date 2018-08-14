package com.example.dell.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private TextInputLayout status_text;
    private Button status_button;
    private Toolbar mtoolbar;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mprogressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        final String uid=mCurrentUser.getUid();
        mDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        status_text=(TextInputLayout)findViewById(R.id.status_Input_text);
        status_button=(Button)findViewById(R.id.status_button);

        mprogressDialog=new ProgressDialog(this);

        mtoolbar=(Toolbar)findViewById(R.id.status_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String mstatus=getIntent().getStringExtra("mstatus").toString();
        status_text.getEditText().setText(mstatus);

        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprogressDialog.setTitle("Changing Status");
                mprogressDialog.setMessage("Please wait while we chage status...");
                mprogressDialog.setCanceledOnTouchOutside(false);
                mprogressDialog.show();

                String status=status_text.getEditText().getText().toString();

                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mprogressDialog.dismiss();
                            Intent settingIntent=new Intent(StatusActivity.this,SettingActivity.class);
                            startActivity(settingIntent);
                        }else{
                            mprogressDialog.hide();
                            Toast.makeText(StatusActivity.this,"error in updating status",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });



    }


}
