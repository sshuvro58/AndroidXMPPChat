
package com.example.xmppchatsampleproject;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;



public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    TextView toolbarTitle;

    EditText eTxtMsg;
    static RecyclerView rvMsgList;

    public static boolean isOpen;//temp fix
    public static ChatAdapter mChatAdapter;
    private String user1 = "", user2 = ""; // hard coded
    private Random random;
    public static ArrayList<ChatMessage> chatlist;
    private LinearLayoutManager layoutManager;
    XMPPChat xmppChat;
    SharedPreferenceManager sessionManager;
    ImageButton btnChooser;
    public static ProgressBar chatProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);
        sessionManager = new SharedPreferenceManager(this);

        eTxtMsg = (EditText) findViewById(R.id.eTxtMsg);
        eTxtMsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    onSendClicked(v);
                }
                return true;
            }
        });
        //rvMsgList.scrollToPosition(chatlist.size()-1);

        rvMsgList = (RecyclerView) findViewById(R.id.rvMsgList);

        random = new Random();

        chatlist = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        rvMsgList.setLayoutManager(layoutManager);
        mChatAdapter = new ChatAdapter(this,chatlist);
        rvMsgList.setAdapter(mChatAdapter);
        //chat
        xmppChat = XMPPChat.getInstance(getApplicationContext());

        if (Utility.isNetworkAvailable(this) && sessionManager.isLoggedIn() && !xmppChat.isAuthenticated()) {
            if(sessionManager.getUsername() == null || sessionManager.getUserPass() == null) {
                finish();
            }else {
                Helper.loginToChat(this, sessionManager.getUsername(), sessionManager.getUserPass(),
                        sessionManager.getKeyChatroomHostName(), sessionManager.getKeyChatroomName());
            }
        }

        user1 = sessionManager.getUsername();
        user2 = sessionManager.getKeyChatroomName().trim();

        chatProgressBar = (ProgressBar) findViewById(R.id.chatProgressBar);
        chatProgressBar.setVisibility(View.GONE);

        // btnChooser.setOnClickListener(this);  //multimedia chat button

    }

    @Override
    protected void onResume() {
        super.onResume();
        isOpen = true;
        chatlist.clear();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        //temporary fix
        try{
            joinRoom(sessionManager.getKeyChatroomHostName(), sessionManager.getKeyChatroomName());
        }catch (Exception e) {
            Log.e("xmpp", "Failed to join room");
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(attchmentDownloaded,
                new IntentFilter("attachment_download"));

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(attchmentDownloaded);
        isOpen = false;
        if(chatlist != null && chatlist.size() != 0){
            sessionManager.setLastMucMessageAt(chatlist.get(chatlist.size()-1).getTimestamp());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //Toast.makeText(GroupChatActivity.this, "back pressed", Toast.LENGTH_SHORT).show();
            // should open the
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //going to the home activity
        Intent homeActivity = new Intent(GroupChatActivity.this, MainActivity.class);
        homeActivity.putExtra("updated", true);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(homeActivity);
        overridePendingTransition(0, 0);
        finish();
    }

    public void onSendClicked(View view){

//        if(GroupChatActivity.chatlist.size()>0)
//            sessionManager.setLastMucMessageAt(GroupChatActivity.chatlist.get(GroupChatActivity.chatlist.size() - 1).getTimestamp());

        String message = eTxtMsg.getEditableText().toString();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        if (!message.equalsIgnoreCase("")) {
            final ChatMessage chatMessage = new ChatMessage(user1, user2, "txt", message, true, false);
            eTxtMsg.setText("");
            // dont add the message to list now
            // send conference message

            // if offline add to db and send later
            if (Utility.isNetworkAvailable(this)) {
                xmppChat.sendConferenceMessage(chatMessage);
            }
        }
    }

    private void sendEcho(String message){
        //String message = "Reply from chatbot...";
        if (!message.equalsIgnoreCase("")) {
            final ChatMessage chatMessage = new ChatMessage(user1, user2, "txt", message, true, false);
            eTxtMsg.setText("");
            mChatAdapter.add(chatMessage);
            mChatAdapter.notifyDataSetChanged();
            rvMsgList.smoothScrollToPosition(mChatAdapter.getItemCount());
            //call the message send service here
            xmppChat.sendMessage(chatMessage, sessionManager.getKeyChatroomHostName());
        }
    }

    public static void scrollToLastMessage(){
        rvMsgList.smoothScrollToPosition(mChatAdapter.getItemCount());
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }





    private void joinRoom(final String hostname, final String roomName) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                xmppChat.leaveRoom(sessionManager.getKeyChatroomName(), sessionManager.getKeyChatroomHostName());
                xmppChat.joinRoom(hostname, roomName);
                return "done";
            }

        }.execute();
    }

    /**
     * Broadcast receiver for when attachment download is finished
     */
    private BroadcastReceiver attchmentDownloaded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mChatAdapter.notifyDataSetChanged();
            rvMsgList.invalidate();
            Log.d("FileDownloadService", " download finished");
        }
    };


}
