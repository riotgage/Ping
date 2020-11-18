package com.example.ping.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ping.Models.Chat;
import com.example.ping.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

private Context mContext;
private List<Chat> mChat;
private String ImageURL;

FirebaseUser fuser;
public static final int MSG_TYPE_LEFT=0;
public static final int MSG_TYPE_RIGHT=1;

public MessageAdapter(Context mContext, List<Chat> mChat,String ImageURL) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.ImageURL=ImageURL;
        }
public static class ViewHolder extends RecyclerView.ViewHolder{

    public TextView show_message;
    public ImageView profile_image;
    public TextView txt_seen;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_seen=itemView.findViewById(R.id.txt_seen);
        show_message=itemView.findViewById(R.id.showMessage);
        profile_image=itemView.findViewById(R.id.profile_image);
    }
}

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else{
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat=mChat.get(position);
        holder.show_message.setText(chat.getMessage());

        if(ImageURL.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Glide.with(mContext).load(ImageURL).into(holder.profile_image);
        }

        if(position==mChat.size()-1){
            if(chat.getIsseen()){
                holder.txt_seen.setText("Seen");
            }else{
                holder.txt_seen.setText("Delivered");
            }
        }else{
            holder.txt_seen.setVisibility(View.GONE);
        }

    }
    @Override
    public int getItemCount() {
        return mChat.size();
    }


    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }
}
