package com.example.dell.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private Button sendReqButton,declineReqButton;
    private TextView nameText,statusText,TotalFriendText;
    private ImageView profileImageView;

    private DatabaseReference mNotificationDatabase, mDatabase,mfriendRequestDatabase, mFriendsDatabase;


    private FirebaseUser mCurrent_user;
    private String current_state;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");


        nameText=(TextView)findViewById(R.id.profile_nameText);
        statusText=(TextView)findViewById(R.id.profile_statusText);
        TotalFriendText=(TextView)findViewById(R.id.profile_TotlFriendText);
        sendReqButton=(Button)findViewById(R.id.profile_send_request_button);
        declineReqButton=(Button)findViewById(R.id.profile_decline_request_button);
        profileImageView=(ImageView)findViewById(R.id.profile_imageView);

        current_state="not_friends";

        mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mfriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("friend_request");
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Opening User Data");
        mProgressDialog.setMessage("please wait while we load users data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            String DispalyName=dataSnapshot.child("name").getValue().toString();
            String DispalyStatus=dataSnapshot.child("status").getValue().toString();
            String DisplayImage=dataSnapshot.child("image").getValue().toString();

            nameText.setText(DispalyName);
            statusText.setText(DispalyStatus);

            Picasso.get().load(DisplayImage).placeholder(R.drawable.profile_img).into(profileImageView);
                //---------------Friends list/Request feture---------------
            mfriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(user_id)){
                        String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                        if(req_type.equals("sent")){
                            current_state="request_sent";
                            sendReqButton.setText(" CANCLE REQUEST ");

                            declineReqButton.setVisibility(View.INVISIBLE);
                            declineReqButton.setEnabled(false);

                        }else if(req_type.equals("received")){
                            current_state="request_received";
                            sendReqButton.setText("ACCEPT REQUEST");

                            declineReqButton.setVisibility(View.VISIBLE);
                            declineReqButton.setEnabled(true);
                        }
                    }else{
                        mFriendsDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(user_id)){

                                    sendReqButton.setEnabled(true);
                                    sendReqButton.setText(" UNFRIEND THIS PERSON ");
                                    current_state="friends";

                                    declineReqButton.setVisibility(View.INVISIBLE);
                                    declineReqButton.setEnabled(false);
                                }
                                mProgressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                mProgressDialog.dismiss();
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }});

        declineReqButton.setVisibility(View.INVISIBLE);
        declineReqButton.setEnabled(false);

            sendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sendReqButton.setEnabled(false);
            //------------Not Friends state----------------

                    if(current_state.equals("not_friends")){

                        Map requestMap=new HashMap();
                        requestMap.put(mCurrent_user.getUid()+ "/" +user_id+ "/" + "request_type","sent");
                        requestMap.put(user_id+ "/" +mCurrent_user.getUid()+ "/" + "request_type","received");
                        mfriendRequestDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                HashMap<String,String> notificationData=new HashMap<>();
                                notificationData.put("from",mCurrent_user.getUid());
                                notificationData.put("type","request");
                                mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        sendReqButton.setEnabled(true);
                                        sendReqButton.setText(" CANCLE REQUEST ");
                                        current_state="request_sent";

                                        declineReqButton.setVisibility(View.INVISIBLE);
                                        declineReqButton.setEnabled(false);
                                    }
                                });

                            }
                        });
                    }

            //----------------Cancle request -------------------
                    if (current_state.equals("request_sent")){
                        Map requestMap=new HashMap();
                        requestMap.put(mCurrent_user.getUid()+ "/" +user_id+ "/" + "request_type",null);
                        requestMap.put(user_id+ "/" +mCurrent_user.getUid()+ "/" + "request_type",null);
                        mfriendRequestDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                sendReqButton.setEnabled(true);
                                sendReqButton.setText(" SEND REQUEST ");
                                current_state="not_friends";

                                declineReqButton.setVisibility(View.INVISIBLE);
                                declineReqButton.setEnabled(false);
                            }
                        });


                    }

            //---------------Request received state-------------------
                    if (current_state.equals("request_received")){
                        String current_time= DateFormat.getDateTimeInstance().format(new Date());
                        Map requestMap=new HashMap();
                        requestMap.put(mCurrent_user.getUid()+ "/" +user_id+ "/" + "request_type",current_time);
                        requestMap.put(user_id+ "/" +mCurrent_user.getUid()+ "/" + "request_type",current_time);
                        mFriendsDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Map requestMap=new HashMap();
                                requestMap.put(mCurrent_user.getUid()+ "/" +user_id+ "/" + "request_type",null);
                                requestMap.put(user_id+ "/" +mCurrent_user.getUid()+ "/" + "request_type",null);
                                mfriendRequestDatabase.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                        sendReqButton.setEnabled(true);
                                        sendReqButton.setText(" UNFRIEND THIS PERSON ");
                                        current_state="friends";

                                        declineReqButton.setVisibility(View.INVISIBLE);
                                        declineReqButton.setEnabled(false);
                                    }
                                });
                            }
                        });


                    }




                }});


    }





}
