package com.example.ping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.ping.Adapter.MessageAdapter;
import com.example.ping.Fragments.APIService;
import com.example.ping.Models.Chat;
import com.example.ping.Models.User;
import com.example.ping.Notification.Client;
import com.example.ping.Notification.Data;
import com.example.ping.Notification.MyResponse;
import com.example.ping.Notification.Sender;
import com.example.ping.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {


    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;
    ImageButton btn_send;
    EditText text_send;
    Intent intent;

    APIService apiService;

    MessageAdapter messageAdapter;
    List<Chat> mchat;
    TextView seen;
    ValueEventListener seenListener;
    RecyclerView recyclerView;
    String userId;
    boolean notify=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        apiService= Client.getClient("https://fcm.googleapis.com").create(APIService.class);


        seen=findViewById(R.id.txt_seen);
        recyclerView=findViewById(R.id.msg_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        btn_send=findViewById(R.id.btn_send);
        text_send=findViewById(R.id.text_send);

        intent=getIntent();
        userId=intent.getStringExtra("userid");      // other user

        fuser= FirebaseAuth.getInstance().getCurrentUser();         // sender
        reference=FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessage(fuser.getUid(),userId,user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                String msg=text_send.getText().toString();
                if(!msg.equals("")) {
                    sendMessage(fuser.getUid(), userId, msg);
                }
                text_send.setText("");
            }


        });
        seenMessage(userId);
    }

    private void seenMessage(String userId){
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){
                    Chat chat=snap.getValue(Chat.class);
                    if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userId)){
                        HashMap<String,Object>hashmap=new HashMap<>();
                        hashmap.put("isseen",true);
                        snap.getRef().updateChildren(hashmap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender,String receiver,String Message){
            DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

            HashMap<String,Object>hashmap=new HashMap<>();
            hashmap.put("sender",sender);
            hashmap.put("receiver",receiver);
            hashmap.put("message",Message);
            hashmap.put("isseen",false);

        reference.child("Chats").push().setValue(hashmap);

        DatabaseReference chatref=FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userId);

        chatref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatref.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final  String msg=Message;
        reference =FirebaseDatabase.getInstance().getReference().child("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(notify)
                    sendNotifications(receiver,user.getUsername(),msg);
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotifications(String receiver, String username, String msg) {

        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");

        Query query=tokens.orderByKey().equalTo(receiver);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){
                    Token token=snap.getValue(Token.class);
                    Data data=new Data(fuser.getUid(),R.mipmap.ic_launcher,username+":"+msg,"New Message",userId);

                    Sender sender=new Sender(data,token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code()==200){
                                        if(!response.isSuccessful()){
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void readMessage(String myId,String UserID,String imageurl){
            mchat=new ArrayList<>();
            reference=FirebaseDatabase.getInstance().getReference().child("Chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mchat.clear();
                    for(DataSnapshot snap:snapshot.getChildren()){
                        Chat chat=snap.getValue(Chat.class);
                        if(chat.getReceiver().equals(myId) && chat.getSender().equals(UserID) || chat.getReceiver().equals(UserID) && chat.getSender().equals(myId)){
                            mchat.add(chat);
                        }
                        messageAdapter=new MessageAdapter(MessageActivity.this,mchat,imageurl);
                        recyclerView.setAdapter(messageAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    private void currentUser(String userId){
        SharedPreferences.Editor editor=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",userId);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(userId);
    }

    @Override
    protected void onPause() {
        reference.removeEventListener(seenListener);
        super.onPause();
        currentUser("none");
    }
}