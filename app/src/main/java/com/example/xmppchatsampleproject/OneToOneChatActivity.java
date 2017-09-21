
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamElements;
import org.jivesoftware.smackx.rsm.packet.RSMSet;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class OneToOneChatActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    TextView toolbarTitle;

    EditText eTxtMsg;
    static RecyclerView singleChatListRecyclerView;

    public static boolean isOpen;//temp fix
    public static ChatAdapter singleChatAdapter;
    private String user1 = "", user2 = ""; // hard coded
    private Random random;
    public static ArrayList<ChatMessage> singleChatlist;
    private LinearLayoutManager layoutManager;
    XMPPChat xmppChat;
    SharedPreferenceManager sessionManager;
    ImageButton btnChooser;
    MamManager mamManager;
    public static String currentChatTo = "";
    ImageButton btnSend;
    ProgressBar singleChatProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_to_one_chat);

        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        singleChatProgressBar = (ProgressBar) findViewById(R.id.singleChatProgressBar);
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
        //singleChatListRecyclerView.scrollToPosition(chatlist.size()-1);

        singleChatListRecyclerView = (RecyclerView) findViewById(R.id.singleChatList);

        random = new Random();
        singleChatlist = new ArrayList<>();

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        singleChatListRecyclerView.setLayoutManager(layoutManager);
        singleChatAdapter = new ChatAdapter(this, singleChatlist);
        singleChatListRecyclerView.setAdapter(singleChatAdapter);
        //chat
        xmppChat = XMPPChat.getInstance(this);
        if (Utility.isNetworkAvailable(this) && sessionManager.isLoggedIn() && !xmppChat.isAuthenticated()) {
            Helper.loginToChat(this, sessionManager.getUsername(), sessionManager.getUserPass(),
                    sessionManager.getKeyChatroomHostName(), sessionManager.getKeyChatroomName());
        }

        user1 = sessionManager.getUsername();
        user2 = getIntent().getStringExtra("username");
        currentChatTo = user2;


    }

    @Override
    protected void onNewIntent(Intent intent) {
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSend) {

            String message = eTxtMsg.getEditableText().toString();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            if (!message.equalsIgnoreCase("")) {
                final ChatMessage chatMessage = new ChatMessage(user1, user2, "txt", message, true, false);
                eTxtMsg.setText("");
                if (Utility.isNetworkAvailable(this)) {
                    xmppChat.sendOnToOneMessgae(user2, chatMessage);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOpen = true;

        LocalBroadcastManager.getInstance(this).registerReceiver(xmpp_authenticated,
                new IntentFilter("xmpp_authenticated"));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(500);


        ArrayList<ChatMessage> chatMessages = new ArrayList<>();

        if (xmppChat.isAuthenticated()) {
            if (singleChatlist.isEmpty()) {
                requestHistory();
            }
        }
    }

    private void requestHistory() {
        try {
            mamManager = MamManager.getInstanceFor(xmppChat.getConnection());
            getArchivedMessages(mamManager, user2 + "@"+sessionManager.getKeyChatroomHostName(), 10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOpen = false;

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
        Intent homeActivity = new Intent(OneToOneChatActivity.this, OneToOneChatActivity.class);
        homeActivity.putExtra("updated", true);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(homeActivity);
        overridePendingTransition(0, 0);
        finish();
    }

    public void onSendClicked(View view) {

        String message = eTxtMsg.getEditableText().toString();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(500);
        if (!message.equalsIgnoreCase("")) {
            final ChatMessage chatMessage = new ChatMessage(user1, user2, "txt", message, true, false);
            eTxtMsg.setText("");
            if (Utility.isNetworkAvailable(this)) {
                xmppChat.sendOnToOneMessgae(user2, chatMessage);

            }
        }
    }

    public static void scrollToLastMessage() {
        singleChatListRecyclerView.smoothScrollToPosition(singleChatAdapter.getItemCount());

    }


    public synchronized void getArchivedMessages(final MamManager mamManager, final String jid, final int maxResults) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                singleChatProgressBar.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    DataForm form = new DataForm(DataForm.Type.submit);
                    FormField field = new FormField(FormField.FORM_TYPE);
                    field.setType(FormField.Type.hidden);
                    field.addValue(MamElements.NAMESPACE);
                    form.addField(field);

                    FormField formField = new FormField("with");
                    formField.addValue(jid);
                    form.addField(formField);

                    // "" empty string for before
                    RSMSet rsmSet = new RSMSet(maxResults, "", RSMSet.PageDirection.before);
                    MamManager.MamQueryResult mamQueryResult = mamManager.page(form, rsmSet);

                    List<Forwarded> forwardeds = mamQueryResult.forwardedMessages;
//
                    for (int i = 0; i < forwardeds.size(); i++) {
                        singleChatlist.add(ChatMessage.fromSingleMessagePacket((Message) forwardeds.get(i).getForwardedStanza(), user1));
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //btnSend.setActivated(true);
                singleChatAdapter.notifyDataSetChanged();
                scrollToLastMessage();
                singleChatProgressBar.setVisibility(View.GONE);
            }
        }.execute();

    }


    private BroadcastReceiver xmpp_authenticated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestHistory();
        }
    };
}
