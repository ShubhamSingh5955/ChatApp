package com.example.dell.chatapp;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.TextView;


import com.example.dell.chatapp.classes.GetTimeAgo;
import com.example.dell.chatapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private DatabaseReference mRootRef,mUserRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private TextView textName,textSeen;
    private CircleImageView circleImageViewToolbar;
    private ImageButton sendMessageButton,addMeaasgeButton;
    private EditText mMessageView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String mChatUser,UserName;
    private static final int TOTAL_ITEM_TO_LOAD=10;
    private int mCurrentPage=1;

    private List<Message> messageList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatUser=getIntent().getStringExtra("user_id");
        UserName=getIntent().getStringExtra("UserName");

        mMessageView=(EditText)findViewById(R.id.chat_send_message_view);
        addMeaasgeButton= (ImageButton) findViewById(R.id.add_message_imageButton);
        sendMessageButton=(ImageButton) findViewById(R.id.send_message_imageButton);

        mAdapter=new MessageAdapter(messageList);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refesh_layout);

        //----recycler-----------

        recyclerView=(RecyclerView)findViewById(R.id.meassage_list);
        linearLayoutManager=new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);

        //------database------------

        mRootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId=mAuth.getCurrentUser().getUid();
        mUserRef = mRootRef.child("users").child(mCurrentUserId);

        //--------toolbar---------

        mToolbar=(Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        //----Custum Action Bar Items--------

        textName=(TextView)findViewById(R.id.custom_bar_title);
        textSeen=(TextView)findViewById(R.id.custom_bar_seen);
        circleImageViewToolbar=(CircleImageView)findViewById(R.id.custom_bar_image);

        textName.setText(UserName);
        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image=dataSnapshot.child("image").getValue().toString();
                String online=dataSnapshot.child("online").getValue().toString();
                Picasso.get().load(image).into(circleImageViewToolbar);
                if(online.equals("true")){
                    textSeen.setText("Online");
                }else{
                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    Long lastSeen=Long.parseLong(online);
                    String lastSeenTime=getTimeAgo.getTimeAgo(lastSeen,getApplicationContext());

                    textSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/"+mChatUser ,chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("Chat_Log",databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMeaasge();
            }
        });

        loadMessage();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                messageList.clear();
                loadMessage();
            }
        });
    }

    private void loadMessage() {
            DatabaseReference messageRef= mRootRef.child("message").child(mCurrentUserId).child(mChatUser);

            Query message_query=messageRef.limitToLast(mCurrentPage*TOTAL_ITEM_TO_LOAD);

        message_query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message=dataSnapshot.getValue(Message.class);
                messageList.add(message);
                mAdapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messageList.size()-1);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMeaasge() {
      String message=mMessageView.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String current_user_Ref="message/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_Ref="message/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference user_message_push=mRootRef.child("message")
                    .child(mCurrentUserId).child(mChatUser).push();
            String push_id=user_message_push.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_Ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_Ref+"/"+push_id,messageMap);

            mMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
               if(databaseError!=null){
                   Log.d("chat_log",databaseError.getMessage().toString());
               }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserRef.child("online").setValue("true");
    }

    @Override
    protected void onStop() {

        super.onStop();
    }
}
