package com.example.xmppchatsampleproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChatTypeActivity extends AppCompatActivity {

    Button oneTwoOne,group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_type);

        oneTwoOne = (Button) findViewById(R.id.oneTwoOne);
        group = (Button) findViewById(R.id.group);

        oneTwoOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText taskEditText = new EditText(ChatTypeActivity.this);
                AlertDialog dialog = new AlertDialog.Builder(ChatTypeActivity.this)
                        .setTitle("Chat receipent ")
                        .setView(taskEditText)
                        .setPositiveButton("start chat", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String rp = String.valueOf(taskEditText.getText().toString().trim());
                                if(rp!=null && !rp.equals("")){
                                    Intent intent = new Intent(ChatTypeActivity.this, OneToOneChatActivity.class);
                                    intent.putExtra("username", rp);
                                    startActivity(intent);
                                }


                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
            }
        });

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatTypeActivity.this, GroupChatActivity.class);
                startActivity(intent);
            }
        });
    }
}
