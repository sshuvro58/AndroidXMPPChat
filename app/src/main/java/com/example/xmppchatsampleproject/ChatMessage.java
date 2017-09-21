
package com.example.xmppchatsampleproject;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;


public class ChatMessage {

    private String sender, receiver;
    private boolean isFromMe;
    private String messageBody;
    private long timestamp;
    private String type;

    private boolean isAttachmentUploading;

    public ChatMessage(){

    }

    public ChatMessage(String sender, String receiver, String type, String messageBody, boolean isFromMe, boolean isAttachmentUploading) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.messageBody = messageBody;
        this.isFromMe = isFromMe;
        this.isAttachmentUploading = isAttachmentUploading;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public boolean isFromMe() {
        return isFromMe;
    }

    public void setFromMe(boolean fromMe) {
        isFromMe = fromMe;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public String getMsgType() {
        return type;
    }

    public void setMsgType(String type) {
        this.type = type;
    }

    public boolean isAttachmentUploading() {
        return isAttachmentUploading;
    }

    public void setAttachmentUploading(boolean attachmentUploading) {
        isAttachmentUploading = attachmentUploading;
    }


    public static ChatMessage fromMessagePacket(Message message){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(message.getFrom().asBareJid().getLocalpartOrNull().toString());
        chatMessage.setReceiver(message.getTo().asBareJid().getLocalpartOrNull().toString());
        chatMessage.setMessageBody(message.getBody());
        chatMessage.setFromMe(false);
        return chatMessage;
    }


    public static ChatMessage fromSingleMessagePacket(Message message, String currentUserNmae) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(message.getFrom().asBareJid().getLocalpartOrNull().toString());
        chatMessage.setReceiver(message.getTo().asBareJid().getLocalpartOrNull().toString());
        chatMessage.setMessageBody(message.getBody());
        if (message.getFrom().asBareJid().getLocalpartOrNull().toString().equalsIgnoreCase(currentUserNmae)) {
            chatMessage.setFromMe(true);
        } else {
            chatMessage.setFromMe(false);
        }

        long timeStamp = 0L;
        String type;
        DelayInformation delayInformation = DelayInformation.from(message);
        if (delayInformation != null) {
            timeStamp = delayInformation.getStamp().getTime();
        }
        if (new Long(timeStamp).equals(new Long(0L))) {
            timeStamp = JivePropertiesManager.getProperty(message, "timestamp") == null ? 0L : ((Long) JivePropertiesManager.getProperty(message, "timestamp")).longValue();
        }

        if (JivePropertiesManager.getProperty(message, "resourcetype") != null) {
            chatMessage.setMsgType(String.valueOf(JivePropertiesManager.getProperty(message, "resourcetype")));
        } else {
            chatMessage.setMsgType("txt");
        }
        chatMessage.setTimestamp(timeStamp);
        return chatMessage;
    }

    public static ChatMessage fromMucMessagePacket(Message message){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(message.getFrom().getResourceOrNull().toString());
        chatMessage.setReceiver(message.getTo().asBareJid().getLocalpartOrNull().toString());
        chatMessage.setMessageBody(message.getBody());

        chatMessage.setFromMe(false);
        long timeStamp = 0L;
        String type;
        DelayInformation delayInformation = DelayInformation.from(message);
        if(delayInformation != null ){
            timeStamp = delayInformation.getStamp().getTime();
        }
        if(new Long(timeStamp).equals(new Long(0L))){
            timeStamp = JivePropertiesManager.getProperty(message, "timestamp") == null ?0L: ((Long)JivePropertiesManager.getProperty(message, "timestamp")).longValue();
        }

        if (JivePropertiesManager.getProperty(message, "resourcetype") != null) {
            chatMessage.setMsgType(String.valueOf(JivePropertiesManager.getProperty(message, "resourcetype")));
        } else {
            chatMessage.setMsgType("txt");
        }
        chatMessage.setTimestamp(timeStamp);
        return chatMessage;
    }

}
