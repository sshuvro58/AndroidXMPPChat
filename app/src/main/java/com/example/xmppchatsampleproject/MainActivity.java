package com.example.xmppchatsampleproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText username,password,roomName,chatDomain,chatRoomIp;
    Button login;
    RelativeLayout rootContainer;
    ProgressBar progressBar;
    SharedPreferenceManager sharedPreferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferenceManager = new SharedPreferenceManager(this);
        setContentView(R.layout.activity_main);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        roomName = (EditText) findViewById(R.id.roomName);
        chatDomain = (EditText) findViewById(R.id.chatDomain);
        chatRoomIp = (EditText) findViewById(R.id.chatRoomIp);

        login = (Button) findViewById(R.id.login);
        rootContainer = (RelativeLayout) findViewById(R.id.rootContainer);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(username.getText().toString()) && !TextUtils.isEmpty(password.getText().toString()) &&
                        !TextUtils.isEmpty(chatDomain.getText().toString()) && !TextUtils.isEmpty(chatRoomIp.getText().toString())){

                    sharedPreferenceManager.setChatroomIp(chatRoomIp.getText().toString().trim());
//                    sharedPreferenceManager.setChatroomDomain(chatDomain.getText().toString());
                    sharedPreferenceManager.setChatUserName(username.getText().toString().trim());
                    sharedPreferenceManager.setChatUserPass(password.getText().toString().trim());
                    sharedPreferenceManager.setChatroomName(roomName.getText().toString().trim());
                    sharedPreferenceManager.setChatroomHostName(chatDomain.getText().toString().trim());
                    sharedPreferenceManager.setChatroomHostName(chatDomain.getText().toString().trim());

                    if(Utility.isNetworkAvailable(MainActivity.this)) {
                        Helper.loginToChat(MainActivity.this,
                                username.getText().toString().trim(),
                                password.getText().toString().trim(),
                                chatDomain.getText().toString().trim(),
                                roomName.getText().toString().trim()
                        );
                    }

                }else {
                    Toast.makeText(MainActivity.this,"Username , Password and chatdomain are required field",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(xmmppLoginResponse,
                new IntentFilter("xmpp_authenticated"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(xmmppLoginResponse);
    }

    private BroadcastReceiver xmmppLoginResponse = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            progressBar.setVisibility(View.GONE);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String messageContent = extras.getString("xmpp_status");
                if(messageContent!=null && messageContent.equals("success")){
                    startActivity(new Intent(MainActivity.this,ChatOptionActivity.class));
                }else {
                    Toast.makeText(MainActivity.this,"Somthing went wrong. try again",Toast.LENGTH_SHORT).show();
                }
            }

        }
    };
}
