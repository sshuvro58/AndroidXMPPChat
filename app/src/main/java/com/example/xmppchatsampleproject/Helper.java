package com.example.xmppchatsampleproject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamElements;
import org.jivesoftware.smackx.rsm.packet.RSMSet;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.List;

public class Helper {


    public static AsyncTask<String, Void, Integer> mTask;


    public static void loginToChat(final Context context, String username, String password, final String chtDomain, final String chatRoomName) {

        if (null != mTask && mTask.getStatus() != AsyncTask.Status.FINISHED) {
            return; //Returning as the current task execution  is not finished yet.
        }

        mTask = new AsyncTask<String, Void, Integer>() {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Integer doInBackground(String... strings) {
                return XMPPChat.getInstance(context).login(strings[0], strings[1], chtDomain, chatRoomName);
            }

            @Override
            protected void onPostExecute(Integer resultCode) {
                //pd.dismiss();
                if (resultCode == 1) {
                    //SUCCESS
                    Log.e("xmpp", "jabber login successful");
                } else if (resultCode == 2) {
                    Log.e("xmpp jabber", "result code 2");
                } else if (resultCode == 3) {
                    Log.e("xmpp error", "resultCode == " + resultCode);
                } else {
                    Log.e("xmpp error", "resultCode == " + resultCode);
                }
            }
        }.execute(username, password);
    }


    /**
     *
     * @param jid
     * @param context
     */
    public static void getArchivedMessages(final String jid, final Context context) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    XMPPChat xmppChat = XMPPChat.getInstance(context);
                    MamManager mamManager = MamManager.getInstanceFor(xmppChat.getConnection());
                    DataForm form = new DataForm(DataForm.Type.submit);
                    FormField field = new FormField(FormField.FORM_TYPE);
                    field.setType(FormField.Type.hidden);
                    field.addValue(MamElements.NAMESPACE);
                    form.addField(field);

                    FormField formField = new FormField("with");
                    formField.addValue(jid);
                    form.addField(formField);

                    // "" empty string for before
                    RSMSet rsmSet = new RSMSet(10, "", RSMSet.PageDirection.before);
                    MamManager.MamQueryResult mamQueryResult = mamManager.page(form, rsmSet);

                    List<Forwarded> forwardeds = mamQueryResult.forwardedMessages;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //btnSend.setActivated(true);
            }
        }.execute();

    }
}
