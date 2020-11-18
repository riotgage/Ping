package com.example.ping.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ping.MessageActivity;
import com.example.ping.Models.Chat;
import com.example.ping.Models.User;
import com.example.ping.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    String thelastMessage;

    public UserAdapter(Context mContext, List<User> mUsers,boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat=isChat;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username,last_msg;
        public ImageView profile_image;
        private ImageView img_on;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            last_msg=itemView.findViewById(R.id.last_msg);
            username=itemView.findViewById(R.id.username);
            profile_image=itemView.findViewById(R.id.profile_image);
            img_on=itemView.findViewById(R.id.img_on);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user=mUsers.get(position);
        ViewHolder newholder= (ViewHolder)holder;
        newholder.username.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            newholder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            Glide.with(mContext).load(user.getImageURL()).into(newholder.profile_image);
        }
        if(isChat){
            lastMessage(user.getId(),((ViewHolder) holder).last_msg);
        }else{
            ((ViewHolder) holder).last_msg.setVisibility(View.GONE);
        }

        if(isChat){
            if(user.getStatus().equals("Online")){
                newholder.img_on.setVisibility(View.VISIBLE);
            }else{
                newholder.img_on.setVisibility(View.GONE);
            }
        }else newholder.img_on.setVisibility(View.GONE);


        newholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    private void lastMessage(String userid,TextView last_msg){
        thelastMessage="default";
        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap:snapshot.getChildren()){
                    Chat chat=snap.getValue(Chat.class);
                    if(fUser!=null)
                    if(chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid) ||
                    chat.getReceiver().equals(userid) && chat.getSender().equals(fUser.getUid())){
                        thelastMessage=chat.getMessage();
                    }
                }
                switch (thelastMessage){
                    case "default":
                        last_msg.setText("");
                        break;
                    default:
                        last_msg.setText(thelastMessage);
                        break;
                }
                thelastMessage="default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
