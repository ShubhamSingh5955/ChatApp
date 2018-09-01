package com.example.dell.chatapp.Frgments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dell.chatapp.ChatActivity;
import com.example.dell.chatapp.ProfileActivity;
import com.example.dell.chatapp.R;
import com.example.dell.chatapp.model.Friends;
import com.example.dell.chatapp.model.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    public RequestFragment(){

    }

    private RecyclerView requestList;

    private DatabaseReference mFriendRequestDatabase,mUsers_Database;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;
    private View mMainView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView= inflater.inflate(R.layout.fragment_request, container, false);

        requestList=(RecyclerView)mMainView.findViewById(R.id.friend_request);

        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id= mAuth.getCurrentUser().getUid();
        mFriendRequestDatabase= FirebaseDatabase.getInstance().getReference().child("friend_request").child(mCurrent_user_id);
        mFriendRequestDatabase.keepSynced(true);
        mUsers_Database= FirebaseDatabase.getInstance().getReference().child("users");
        mUsers_Database.keepSynced(true);

        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Request,RequestFragment.RequestViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Request,RequestFragment.RequestViewHolder>(
                Request.class,
                R.layout.single_user_layout,
                RequestFragment.RequestViewHolder.class,
                mFriendRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position) {

                viewHolder.setDate(model.getDate());
                final String list_user_id=getRef(position).getKey();
                mUsers_Database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name=dataSnapshot.child("name").getValue().toString();
                        String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setThumbImage(thumb_image);
                        viewHolder.item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", list_user_id);
                                startActivity(profileIntent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
requestList.setAdapter(firebaseRecyclerAdapter);

}

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View item;
        public RequestViewHolder(View itemView) {
            super(itemView);
            item=itemView;
        }

        public void setDate(String date){
            TextView textDate=(TextView)item.findViewById(R.id.single_status_text);
            textDate.setText(date);
        }

        public void setName(String name) {
            TextView textName=(TextView)item.findViewById(R.id.single_name_text);
            textName.setText(name);

            ImageView imageView=(ImageView)item.findViewById(R.id.single_online_dot);
            imageView.setVisibility(View.INVISIBLE);
        }

        public void setThumbImage(String thumb_image) {
            CircleImageView circleImageView=(CircleImageView)item.findViewById(R.id.single_imageView);
            Picasso.get().load(thumb_image).into(circleImageView);
        }
    }
}
