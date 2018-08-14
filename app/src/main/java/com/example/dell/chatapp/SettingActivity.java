package com.example.dell.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private TextView nameText,statusText;
    private Button changeStatusButton,changeImageButton;
    private CircleImageView circleImageView;

    public static final int GALLERY_PICK=1;

    private StorageReference imageStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseUser current_user;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        nameText=(TextView)findViewById(R.id.setting_name_textView);
        statusText=(TextView)findViewById(R.id.setting_status_textview);
        changeStatusButton=(Button)findViewById(R.id.setting_status_button);
        changeImageButton=(Button)findViewById(R.id.setting_image_button);
        circleImageView=(CircleImageView)findViewById(R.id.setting_circle_imgeview);

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String uid=current_user.getUid();

        mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        mDatabase.keepSynced(true);

        imageStorageRef= FirebaseStorage.getInstance().getReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();

                nameText.setText(name);
                statusText.setText(status);
                if(!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile_img).into(circleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image)
                                    .placeholder(R.drawable.profile_img).into(circleImageView);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });


        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               String mstatus= statusText.getText().toString();

                Intent statusIntent=new Intent(SettingActivity.this,StatusActivity.class);
                statusIntent.putExtra("mstatus",mstatus);
                startActivity(statusIntent);

            }});

        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

              /*        CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);
               */

            }});
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String uid=current_user.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(uid);


        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

            // Toast.makeText(SettingActivity.this,imageUri,Toast.LENGTH_SHORT).show();
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog=new ProgressDialog(SettingActivity.this);
                progressDialog.setTitle("Uploding Image");
                progressDialog.setMessage("Please wait while we upload image...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                File thumbFilePath =new File(resultUri.getPath());
                String current_user_id=current_user.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();




                StorageReference filePath=imageStorageRef.child("profile_images").child(uid+".jpg");
                final StorageReference thumb_path = imageStorageRef.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            final String imageUrl=task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_path.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                   String thumb_downloadUri=task.getResult().getDownloadUrl().toString();

                                        if(task.isSuccessful()){
                                            Map userHasmap=new HashMap();
                                            userHasmap.put("image",imageUrl);
                                            userHasmap.put("thumb_image",thumb_downloadUri);

                                            mDatabase.updateChildren(userHasmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        progressDialog.dismiss();
                                                        Toast.makeText(SettingActivity.this," image successfully uploaded",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }
                                        else{
                                            progressDialog.hide();
                                            Toast.makeText(SettingActivity.this," error in uploading thumb",Toast.LENGTH_SHORT).show();
                                        }
                                }
                            });

                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(SettingActivity.this," error in uploading image",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }





    }




