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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

private RecyclerView friendsList;

private DatabaseReference mFriendsDatabase,mUsers_Database;
private FirebaseAuth mAuth;

private String mCurrent_user_id;
private View mMainView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView= inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList=(RecyclerView)mMainView.findViewById(R.id.friends_list);

        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id= mAuth.getCurrentUser().getUid();
        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsers_Database= FirebaseDatabase.getInstance().getReference().child("users");
        mUsers_Database.keepSynced(true);

        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FrinendsViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FrinendsViewHolder>(
                Friends.class,
                R.layout.single_user_layout,
                FrinendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FrinendsViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());
                final String list_user_id=getRef(position).getKey();
                mUsers_Database.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name=dataSnapshot.child("name").getValue().toString();
                        String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String user_online= dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnline(user_online);
                        }

                   viewHolder.setName(name);
                   viewHolder.setThumbImage(thumb_image);
                   viewHolder.item.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           CharSequence options[] =new CharSequence[]{"View Profile","Send Message"};
                         final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                           builder.setTitle("Select Options");
                           builder.setItems(options, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {

                                   if(which==0) {
                                       Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                       profileIntent.putExtra("user_id", list_user_id);
                                       startActivity(profileIntent);
                                   }
                                    if(which==1){
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("user_id", list_user_id);
                                        chatIntent.putExtra("UserName",name);
                                        startActivity(chatIntent);
                                    }

                               }
                           });

                        builder.show();

                       }
                   });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        friendsList.setAdapter(firebaseRecyclerAdapter);

    }

public static class FrinendsViewHolder extends RecyclerView.ViewHolder{

            View item;
    public FrinendsViewHolder(View itemView) {
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
    }

    public void setThumbImage(String thumb_image) {
        CircleImageView circleImageView=(CircleImageView)item.findViewById(R.id.single_imageView);
        Picasso.get().load(thumb_image).into(circleImageView);
    }

    public void setOnline(String user_online) {
        ImageView imageView=(ImageView)item.findViewById(R.id.single_online_dot);

        if(user_online.equals("true")){
            imageView.setVisibility(View.VISIBLE);
        }else{
            imageView.setVisibility(View.INVISIBLE);
        }


    }
}



}
