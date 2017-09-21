package com.example.xmppchatsampleproject;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {


    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "XMPPSahmple";
    private static final String IS_LOGIN = "isLoggedIn";
    private static final String KEY_USER_PASS = "userPass";
    private static final String KEY_CHATROOM_NAME = "chatRoomName";
    private static final String KEY_CHATROOM_HOST = "chatRoomHostName";
    private static final String KEY_CHATROOM_IP = "chatRoomIp";
    public static final String KEY_USERNAME = "username";
    private static final String KEY_LAST_MUC_MESSAGE_AT = "lastMUCMessageAt";
    private static final String KEY_LAST_SINGLE_MESSAGE_AT = "lastSingleMessageAt";

    // Constructor
    public SharedPreferenceManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void createSession(String username,String roomName,String chatHostName,String chatIp,String pass,String chatDomain){
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_PASS, pass);
        editor.putString(KEY_CHATROOM_NAME, roomName);
        editor.putString(KEY_CHATROOM_HOST, chatHostName);
        editor.putString(KEY_CHATROOM_IP, chatIp);
        editor.putBoolean(IS_LOGIN,true);
        editor.commit();

    }


    public void setChatroomIp(String ip){
        editor.putString(KEY_CHATROOM_IP, ip);
        editor.commit();
    }

    public void setIsAuthenticated(Boolean isAuthenticated){
        editor.putBoolean(IS_LOGIN, isAuthenticated);
        editor.commit();
    }

    public void setChatroomName(String chatroomName){
        editor.putString(KEY_CHATROOM_NAME, chatroomName);
        editor.commit();
    }

    public void setChatroomHostName(String hostName){
        editor.putString(KEY_CHATROOM_HOST, hostName);
        editor.commit();
    }




    public void setChatUserName(String name){
        editor.putString(KEY_USERNAME, name);
        editor.commit();
    }


    public void setChatUserPass(String pass){
        editor.putString(KEY_USER_PASS, pass);
        editor.commit();
    }


    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }


    public String getKeyChatroomName() {
        return pref.getString(KEY_CHATROOM_NAME, null);
    }

    public String getKeyChatroomHostName() {
        return pref.getString(KEY_CHATROOM_HOST, null);
    }

    public String getKeyChatroomIp() {
        return pref.getString(KEY_CHATROOM_IP, null);
    }
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }
    public String getUserPass() {
        return pref.getString(KEY_USER_PASS,null);
    }

    public long getLastMucMessageAt(){return pref.getLong(KEY_LAST_MUC_MESSAGE_AT, 0);}

    public long getLastSingleMessageAt() {
        return pref.getLong(KEY_LAST_SINGLE_MESSAGE_AT, 0);
    }
    public void setLastMucMessageAt( long time ){
        editor.putLong(KEY_LAST_MUC_MESSAGE_AT, time);
        editor.commit();
    }

}
