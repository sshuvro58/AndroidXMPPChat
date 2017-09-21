
package com.example.xmppchatsampleproject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Asif on 12/27/2016.
 */

public class XMPPChat {


    public static final int PORT = 5222;
    public static final int CONNECT_TIME_OUT = 80000;
    public static final String RESOURCE = "Smack";


    private Context context;
    private static XMPPChat xmppChat;
    public static XMPPTCPConnection connection;
    private SingleChatMessageListener chatMessageListener;
    private ChatManagerListener chatManagerListener;
    private ChatManager chatManager;
    private MamManager mamManager;
    // Get the MultiUserChatManager
    MultiUserChatManager mucManager;
    // Create a MultiUserChat using an XMPPConnection for a room
    MultiUserChat muChat;
    //public org.jivesoftware.smack.chat.Chat thisChat;
    public Chat thisChat;

    //session manager
    SharedPreferenceManager sessionManager;

    MucMessageListener mucMessageListener;

    private boolean chatCreated = false;


    private XMPPChat(final Context context){
        this.context = context;
        sessionManager = new SharedPreferenceManager(context);

 //       final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 //       final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        mucMessageListener = new MucMessageListener();
        chatMessageListener = new SingleChatMessageListener();

        chatManagerListener = new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(chatMessageListener);
            }
        };

        initConnection(sessionManager);

    }

    /***
     * Initialize XMPP chat connection
     */
    private void initConnection(SharedPreferenceManager sessionManager) {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setHost(sessionManager.getKeyChatroomIp());
        //configBuilder.setHost("35.161.244.20");
        configBuilder.setPort(PORT);
        configBuilder.setCompressionEnabled(false);
        configBuilder.setConnectTimeout(CONNECT_TIME_OUT);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        try {
            DomainBareJid serviceName = JidCreate.domainBareFrom(sessionManager.getKeyChatroomHostName());
            //DomainBareJid serviceName = JidCreate.domainBareFrom("familytracker.com");
            configBuilder.setXmppDomain(serviceName);
            configBuilder.setResource(RESOURCE);
        } catch (XmppStringprepException e) {
            Log.e("XMPPChat", ""+e.getMessage());
        }
        configBuilder.build();
        connection = new XMPPTCPConnection(configBuilder.build());
        connection.setPacketReplyTimeout(20000);
        //manager
        chatManager = ChatManager.getInstanceFor(connection);

        mamManager = MamManager.getInstanceFor(connection);
        //connection listener
        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connected(XMPPConnection connection) {
                Log.d("xmpp-connection", "connected");
                chatManager.addChatListener(chatManagerListener);
            }

            @Override
            public void authenticated(XMPPConnection connection, boolean resumed) {
                Log.d("xmpp-connection", "connection-authenticated");
                Intent intent = new Intent("xmpp_authenticated");
                intent.putExtra("xmpp_status", "success");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                //Helper.pingChatWithUser(context);
            }

            @Override
            public void connectionClosed() {
                Log.d("xmpp-connection", "connection-closed");//login();
                // login(sessionManager.getUsername(), sessionManager.getUserPass());
                Intent intent = new Intent("xmpp_authenticated");
                intent.putExtra("xmpp_status", "fail");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.d("xmpp-connection", "connection-closed-on-error");//login();
                //login(sessionManager.getUsername(), sessionManager.getUserPass());
                Intent intent = new Intent("xmpp_authenticated");
                intent.putExtra("xmpp_status", "fail");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                logoutRelogin();
            }

            @Override
            public void reconnectionSuccessful() {
                Log.d("xmpp-connection", "reconnectionSuccessful");
            }

            @Override
            public void reconnectingIn(int seconds) {
                Log.d("xmpp-connection", "reconnecting " + seconds);
            }

            @Override
            public void reconnectionFailed(Exception e) {
                Log.d("xmpp-connection", "reconnectionFailed" + e.getMessage());
                logoutRelogin();
            }
        });
        ServerPingWithAlarmManager.onCreate(context);
        ReconnectionManager.getInstanceFor(connection).enableAutomaticReconnection();
        ServerPingWithAlarmManager.getInstanceFor(connection).setEnabled(true);

        //multi user chat
        mucManager = MultiUserChatManager.getInstanceFor(connection);

    }

    public static XMPPChat getInstance(Context context) {
        if(xmppChat == null) {
            xmppChat = new XMPPChat(context);
        }
        return xmppChat;
    }

    public XMPPTCPConnection getConnection() throws Exception{
        if(connection == null) {
            throw new Exception("XMPPTCPConnection not initialized.");
        }
        return connection;
    }

    public boolean isAuthenticated() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    public Integer login(String username, String password, String chatDomain, String chatRoomname) {
        try {
            if(!connection.isConnected()) {
                Log.d("xmpp", "connection is not connected try connecting");
                try {
                    connection.connect();
                    Thread.sleep(100);
                }catch (NullPointerException ne){
                    if(sessionManager == null){
                        sessionManager = new SharedPreferenceManager(context);
                    }
                    initConnection(sessionManager);
                    connection.connect();
                    Thread.sleep(100);
                }
            }
            if(connection.isConnected()) {
                Log.d("xmpp", "connection   isConnected ");
                ServerPingWithAlarmManager.getInstanceFor(connection).setEnabled(true);
                if(!connection.isAuthenticated()) {
                    Log.d("xmpp", "connection is not authenticated try authenticating");
                    connection.login(username,password);
                }
                if(connection.isAuthenticated()) {
                    Log.d("xmpp", "connection is  isAuthenticated ");
                    Presence presence = new Presence(Presence.Type.available);
                    presence.setStatus("Available");
                    connection.sendStanza(presence);
                    joinRoom(chatDomain, chatRoomname); //joining the room
                    return 1;
                } else {
                    return 3;
                }

            } else {
                return 2;
            }
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return 0;
    }

    public void sendMessage(ChatMessage chatMessage, String chatDomain) {
        try {
            thisChat = chatManager.createChat(JidCreate.entityBareFrom(chatMessage.getReceiver() + "@" + chatDomain.trim()));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        Message newMessage = new Message();
        newMessage.setBody(chatMessage.getMessageBody());
        newMessage.setType(Message.Type.chat);
        try {
            if(connection.isConnected() && connection.isAuthenticated()) {
                thisChat.sendMessage(newMessage);
            }else{
                //user need to login
                Log.e("xmpp", "user not logged in");
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /***
     * Join room
     */
    public synchronized void joinRoom(String chatDomain, String chatRooom) {

        if(connection == null || !connection.isAuthenticated()){
            Helper.loginToChat(context, sessionManager.getUsername(), sessionManager.getUserPass()
                    , sessionManager.getKeyChatroomHostName(), sessionManager.getKeyChatroomName());
            return;
        }
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        try {
            EntityBareJid roomJid = JidCreate.entityBareFrom(chatRooom.trim() + "@conference." + chatDomain.trim()); // hard coded
            muChat = mucManager.getMultiUserChat(roomJid);
            if( !muChat.isJoined() ){
                Log.d("xmpp", "joined is not joined room");
                try {
                    Resourcepart nickname = Resourcepart.from(sessionManager.getUsername()); // Create the nickname.
                    MucEnterConfiguration.Builder enterConfigurationBuilder = muChat.getEnterConfigurationBuilder(nickname);
                    //enterConfigurationBuilder.withPassword(sessionManager.getUserPass());
                    //enterConfigurationBuilder.requestMaxStanzasHistory(100);
                    enterConfigurationBuilder.requestHistorySince(5*24*60*60); // 5 days message
                    Log.d("xmpp", "trying joining room");
                    muChat.join( enterConfigurationBuilder.build() );
                    Log.d("xmpp", "joined in room");
                    //muChat.join(nickname, sessionManager.getUserPass());
                    //GroupChatActivity.chatProgressBar.setVisibility(View.GONE);
                    muChat.addMessageListener(mucMessageListener);
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                    Log.d("xmpp", "joinRoom.XMPPErrorException  " + e.getMessage());
                    logoutRelogin();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d("xmpp", "joinRoom.InterruptedException  " + e.getMessage());
                    logoutRelogin();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                    Log.d("xmpp", "joinRoom SmackException.NoResponseException  " + e.getMessage());
                    logoutRelogin();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    logoutRelogin();
                } catch (MultiUserChatException.NotAMucServiceException e) {
                    e.printStackTrace();
                    Log.d("xmpp", "joinRoom MultiUserChatException.NotAMucServiceException  " + e.getMessage());
                    logoutRelogin();
                }
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

    }

    public class MucMessageListener implements MessageListener {
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        @Override
        public void processMessage(Message message) {

            if( message.getType() == Message.Type.groupchat ){
                ChatMessage muMessage = ChatMessage.fromMucMessagePacket(message);
                if( message.getFrom().getResourceOrNull().equals(muChat.getNickname()) ){
                    muMessage.setFromMe(true);
                }
                if(GroupChatActivity.isOpen){ // Activity is open
//                    GroupChatActivity.chatProgressBar.setVisibility(View.VISIBLE);
                    processMucMessage(muMessage);
                }else{
                    // Activity not running show notification if user has chat permission
                        Log.d("XMPP", sessionManager.getLastMucMessageAt()+" XMPP chat");
                        if (!muMessage.isFromMe() && (Long.valueOf(muMessage.getTimestamp()).compareTo(sessionManager.getLastMucMessageAt()) > 0)) {
                            NotificationCompat.Builder builder =
                                    new NotificationCompat.Builder(context)
                                            .setSound(uri)
                                            .setAutoCancel(true);
                            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            //chat in notification
                            builder.setContentTitle("Group chat");

                            builder.setContentText(message.getBody());

                            Intent contentIntent = new Intent(context, GroupChatActivity.class);
                            //contentIntent.putExtra("new_msg", true);
                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    context,
                                    0,
                                    contentIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                            builder.setContentIntent(pendingIntent);
                            manager.notify(0, builder.build());
                        }

                }

            }

            Log.e("group-chat", message.getFrom().getResourceOrNull() + " Message " + message.getBody() +" Type "+ message.getType() );

        }



    }

    private void processMucMessage(ChatMessage chatMessage){
        GroupChatActivity.chatlist.add(chatMessage);
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                GroupChatActivity.mChatAdapter.notifyDataSetChanged();
                GroupChatActivity.scrollToLastMessage();
            }
        });
    }



    public class SingleChatMessageListener implements ChatMessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            ChatMessage singleChat = ChatMessage.fromSingleMessagePacket(message, sessionManager.getUsername());
            if (OneToOneChatActivity.isOpen && message.getBody() != null && OneToOneChatActivity.currentChatTo.equalsIgnoreCase(singleChat.getSender())) {
                processOneToOneMessage(singleChat);
            } else if (OneToOneChatActivity.isOpen && !OneToOneChatActivity.currentChatTo.equalsIgnoreCase(singleChat.getSender()) && !singleChat.isFromMe()) {
                singleChatNotification(message, uri, singleChat, true);
            } else {
                if (!singleChat.isFromMe()) {
                    singleChatNotification(message, uri, singleChat, false);
                }

            }
        }
    }

    private void singleChatNotification(Message message, Uri uri, ChatMessage singleChat, boolean reoPen) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSound(uri)
                        .setAutoCancel(true);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //chat in notification
        builder.setContentTitle("Message from" + " " + singleChat.getSender());

        builder.setContentText(message.getBody());

        Intent contentIntent = new Intent(context, OneToOneChatActivity.class);
        if (reoPen) {

            contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        //contentIntent.putExtra("username", message.getFrom().asBareJid().getLocalpartOrNull().toString());
        Bundle bundle = new Bundle();
        bundle.putString("username", message.getFrom().asBareJid().getLocalpartOrNull().toString());
        contentIntent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                singleChat.hashCode(),
                contentIntent,
                PendingIntent.FLAG_ONE_SHOT
        );

        builder.setContentIntent(pendingIntent);
        manager.notify(500, builder.build());
    }

    public void leaveRoom(String chatRoomName, String chatDomainName) {
        if( connection == null || !connection.isAuthenticated() ){
            return;
        }
        EntityBareJid roomJid = null; // hard coded
        try {
            roomJid = JidCreate.entityBareFrom(chatRoomName + "@conference." + chatDomainName.trim()
            );
            muChat = mucManager.getMultiUserChat(roomJid);
            if(muChat.isJoined()){
                muChat.removeMessageListener(mucMessageListener);
                muChat.leave();
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            Log.d("xmpp", "leaveRoom XmppStringprepException " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("xmpp", "leaveRoom InterruptedException " + e.getMessage());
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            Log.d("xmpp", "SmackException NotConnectedException " + e.getMessage());
        }
    }
    /***
     * Multi user chat
     * @param chatMessage
     */
    public void sendConferenceMessage(ChatMessage chatMessage){
        try {
            if(connection.isConnected() && connection.isAuthenticated()) {
                Message newMessage = new Message();
                JivePropertiesExtension jpe = new JivePropertiesExtension();
                Date now = new Date();
                //Calendar calendar = Calendar.getInstance();
                //calendar.setTimeInMillis(now.getTime());
                jpe.setProperty("timestamp", now.getTime());
                jpe.setProperty("resourcetype", chatMessage.getMsgType());
                newMessage.addExtension(jpe);
                newMessage.setBody(chatMessage.getMessageBody());
                muChat.sendMessage(newMessage);
            }

        } catch (SmackException.NotConnectedException e) {
            Log.d("xmpp", "sendConferenceMessage SmackException.NotConnectedExceptio " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.d("xmpp", "sendConferenceMessage InterruptedException " + e.getMessage());
            e.printStackTrace();
        }
    }

    /***
     * Retrieve chat history between the sender and the other user
     * @param user
     * @return
     */
    public ArrayList<ChatMessage> loadChatHistoryUserTo(Jid user){
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
        try {
            MamManager.MamQueryResult history = mamManager.queryArchive(20, null, null, user, null);// 20 messages //mamManager.queryArchive(JidCreate.from(user+"@"+CHAT_DOMAIN));////
            // Get forwarded messages
            List<Forwarded> forwardedMessages = history.forwardedMessages;
            for(Forwarded forwarded: forwardedMessages){
                chatMessages.add(ChatMessage.fromMessagePacket((Message) forwarded.getForwardedStanza()));
            }
            // Get fin IQ
            //MamFinIQ mamFinIQ = history.mamFin;
            //Log.e("history", mamFinIQ.toXML().toString());
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        }
        return chatMessages;
    }

    /***
     * Retrieve chat history between the sender and the other user
     * @param user
     * @return
     */
    public ArrayList<ChatMessage> loadGroupChatHistory(Jid user){
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
        try {
            MamManager.MamQueryResult history = mamManager.queryArchive(user);//////mamManager.queryArchive(20, null, null, user, null);// 20 messages //
            // Get forwarded messages
            List<Forwarded> forwardedMessages = history.forwardedMessages;
            for(Forwarded forwarded: forwardedMessages){
                chatMessages.add(ChatMessage.fromMucMessagePacket((Message) forwarded.getForwardedStanza()));
            }
            // Get fin IQ
            //MamFinIQ mamFinIQ = history.mamFin;
            //Log.e("history", mamFinIQ.toXML().toString());
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        }
        return chatMessages;
    }

    // need to add a connection listener that will check if it is connected with ejabberd
    public void logout(){
        ServerPingWithAlarmManager.getInstanceFor(connection).setEnabled(false);
        connection.disconnect();
        Log.e("xmpp-logout", "logged out");
    }


    /* need to add a connection listener that will check if it is connected with ejabberd
        added this because sometimes it has problem in joining room when network changed, hence trying logged out and relogin*/
    public void logoutRelogin() {
        xmppChat.logout();
        Helper.loginToChat(context, sessionManager.getUsername(), sessionManager.getUserPass()
                , sessionManager.getKeyChatroomHostName(), sessionManager.getKeyChatroomName());

//        Helper.loginToChat(context, sessionManager.getUsername(), sessionManager.getUserPass()
//                , "familytracker.com", "carbon");
    }


    /**
     * @param from
     * @param chatMessage
     */
    public void sendOnToOneMessgae(String from, ChatMessage chatMessage) {

        Message newMessage = new Message();
        JivePropertiesExtension jpe = new JivePropertiesExtension();
        Date now = new Date();
        jpe.setProperty("timestamp", now.getTime());
        jpe.setProperty("resourcetype", chatMessage.getMsgType());
        chatMessage.setTimestamp(now.getTime());
        newMessage.addExtension(jpe);
        newMessage.setBody(chatMessage.getMessageBody());


        try {
            Chat newChat = chatManager.createChat(JidCreate.entityBareFrom(from + "@"+sessionManager.getKeyChatroomHostName()));
            if (connection.isConnected() && connection.isAuthenticated()) {
                newChat.sendMessage(newMessage);
                OneToOneChatActivity.singleChatlist.add(chatMessage);
                OneToOneChatActivity.singleChatAdapter.notifyDataSetChanged();
                OneToOneChatActivity.scrollToLastMessage();
            }
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    public void processOneToOneMessage(ChatMessage chatMessage) {
        //OneToOneChatActivity.singleChatlist.add(chatMessage);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                OneToOneChatActivity.singleChatAdapter.notifyDataSetChanged();
                OneToOneChatActivity.scrollToLastMessage();
                //OneToOneChatActivity.chatProgressBar.setVisibility(View.GONE);
                //sessionManager.setLastMucMessageAt(GroupChatActivity.chatlist.get(GroupChatActivity.chatlist.size() - 1).getTimestamp());
            }
        });
    }




    public void getOldMessages(MultiUserChat multiUserChat) {
        List<Message> oldMessages = new ArrayList<>();
        try {
            Message message = multiUserChat.nextMessage();
            int i = 0;
            while (i < 20) {
                //oldMessages.add(message);
                message = multiUserChat.nextMessage();
                ChatMessage muMessage = ChatMessage.fromMucMessagePacket(message);
                if (message.getFrom().getResourceOrNull().equals(muChat.getNickname())) {
                    muMessage.setFromMe(true);
                }
                processMucMessage(muMessage);
                i++;
            }
        } catch (MultiUserChatException.MucNotJoinedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public class SingleCHatMangerListener implements ChatManagerListener {

        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            chat.addMessageListener(chatMessageListener);
        }
    }

}

