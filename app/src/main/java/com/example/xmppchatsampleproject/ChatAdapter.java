
package com.example.xmppchatsampleproject;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    Context mContext;
    ArrayList<ChatMessage> mChatList;


    public ChatAdapter(Context context, ArrayList<ChatMessage> msgList){
        mContext = context;
        mChatList = msgList;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatbubble,parent,false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {
        final ChatMessage message = mChatList.get(position);

        if (message.getMsgType() == null) {
            holder.mBubbleParentLinearLayout.setVisibility(View.GONE);
        } else {
            holder.mBubbleParentLinearLayout.setVisibility(View.VISIBLE);
        }

        holder.mSenderTextView.setText(message.getSender() + " : ");
        holder.mTimeStampTextView.setText(Utility.getFormattedDate(message.getTimestamp()) );
        if(message.isFromMe()){
            holder.mBubbleLinearLayout.setBackgroundResource(R.color.appBlue);
            holder.mBubbleParentLinearLayout.setGravity(Gravity.RIGHT);
            holder.mBubbleTextView.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }else{
            holder.mBubbleLinearLayout.setBackgroundResource(R.color.colorAccent);
            holder.mBubbleParentLinearLayout.setGravity(Gravity.LEFT);
            holder.mBubbleTextView.setTextColor( ContextCompat.getColor(mContext, android.R.color.black) );
        }

        holder.mBubbleTextView.setText(message.getMessageBody());





    }


    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public void add(ChatMessage chatMessage) {
        mChatList.add(chatMessage);
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mBubbleParentLinearLayout;
        RelativeLayout mBubbleLinearLayout;
        TextView mSenderTextView;
        TextView mBubbleTextView;
        TextView mTimeStampTextView;
        TextView audioBoxFileName;
        ImageView chatImage;
        ImageView videoThumb;
        VideoView chatVideo;
        RelativeLayout attachmentHoder;
        ProgressBar attachmentProgress;
        RelativeLayout audioHolder;

        public ChatViewHolder(View view) {
            super(view);
            mBubbleParentLinearLayout = (LinearLayout) view.findViewById(R.id.bubble_layout_parent);
            mBubbleLinearLayout = (RelativeLayout) view.findViewById(R.id.bubble_layout);
            mSenderTextView =  (TextView) view.findViewById(R.id.message_sender);
            mBubbleTextView = (TextView) view.findViewById(R.id.message_text);
            mTimeStampTextView = (TextView) view.findViewById(R.id.message_timestamp);
        }
    }
}
