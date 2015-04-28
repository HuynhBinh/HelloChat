package com.es.hello.chat.core;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;

import android.util.Log;
import android.view.View;

import com.es.hello.chat.StaticFunction;
import com.es.hello.chat.sugarobject.Sugar_Message;
import com.es.hello.chat.ui.activities.ChatActivity;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBIsTypingListener;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBMessage;
import com.quickblox.users.model.QBUser;

public class PrivateChatManagerImpl extends QBMessageListenerImpl<QBPrivateChat> implements ChatManager, QBPrivateChatManagerListener, ChatActivity.MyTypingCallback
{

    private static final String TAG = "PrivateChatManagerImpl";

    private ChatActivity chatActivity;

    private QBPrivateChatManager privateChatManager;

    private QBPrivateChat privateChat;

    private String DialogID = "";

    // private Integer currentOppenentID;

    public PrivateChatManagerImpl(ChatActivity chatActivity, final Integer opponentID, String dialogID)
    {

	this.chatActivity = chatActivity;

	this.DialogID = dialogID;

	this.chatActivity.mTypingCallback = this;

	// this.currentOppenentID = opponentID;

	privateChatManager = QBChatService.getInstance().getPrivateChatManager();

	privateChatManager.removePrivateChatManagerListener(this);

	privateChatManager.addPrivateChatManagerListener(this);

	// init private chat
	//
	privateChat = privateChatManager.getChat(opponentID);
	if (privateChat == null)
	{
	    privateChat = privateChatManager.createChat(opponentID, this);
	}
	else
	{
	    privateChat.removeMessageListener(this);
	    privateChat.addMessageListener(this);
	}

	// listener for opponent typing status //user is typing...
	QBIsTypingListener<QBPrivateChat> privateChatIsTypingListener = new QBIsTypingListener<QBPrivateChat>()
	{

	    @Override
	    public void processUserIsTyping(QBPrivateChat privateChat)
	    {

		PrivateChatManagerImpl.this.chatActivity.runOnUiThread(new Runnable()
		{

		    @Override
		    public void run()
		    {

			PrivateChatManagerImpl.this.chatActivity.typingStatusContainer.setVisibility(View.VISIBLE);

			if (PrivateChatManagerImpl.this.chatActivity.txtTypingStatus.getVisibility() == View.GONE)
			{
			    PrivateChatManagerImpl.this.chatActivity.txtTypingStatus.setVisibility(View.VISIBLE);
			}

			QBUser opponentUser = StaticFunction.getUserById(opponentID);

			String opponentLogin = opponentUser.getLogin();

			PrivateChatManagerImpl.this.chatActivity.txtTypingStatus.setText(opponentLogin + " is typing...");

		    }
		});

	    }

	    @Override
	    public void processUserStopTyping(QBPrivateChat privateChat)
	    {

		PrivateChatManagerImpl.this.chatActivity.runOnUiThread(new Runnable()
		{

		    @Override
		    public void run()
		    {

			PrivateChatManagerImpl.this.chatActivity.typingStatusContainer.setVisibility(View.GONE);
			if (PrivateChatManagerImpl.this.chatActivity.txtTypingStatus.getVisibility() == View.VISIBLE)
			{
			    PrivateChatManagerImpl.this.chatActivity.txtTypingStatus.setVisibility(View.GONE);
			}
			PrivateChatManagerImpl.this.chatActivity.txtTypingStatus.setText("");

		    }
		});
	    }
	};

	privateChat.addIsTypingListener(privateChatIsTypingListener);

    }

    // send message in 1:1 chat
    @Override
    public void sendMessage(QBChatMessage message) throws Exception
    {

	String dia = message.getProperty("dialog_id");
	String sender = message.getProperty("sender_id");
	String recipient = message.getProperty("recipient_id");
	String messBody = message.getBody();
	String messID = message.getId();

	privateChat.sendMessage(message);
	StaticFunction.saveMessageToDB(message, messID, Integer.parseInt(sender), Integer.parseInt(recipient), dia, messBody, false);

    }

    // release the room when user back to list chat
    @Override
    public void release()
    {

	Log.w(TAG, "release private chat");
	privateChat.removeMessageListener(this);
	privateChatManager.removePrivateChatManagerListener(this);
	Log.w(TAG, "release private chat SUCCESSSSSSSSSSSSSS");
    }

    // this is called when message is dilivered to the opponent
    @Override
    public void processMessageDelivered(QBPrivateChat sender, final String messageID)
    {

	if (!PrivateChatManagerImpl.this.chatActivity.isFinishing())
	{
	    PrivateChatManagerImpl.this.chatActivity.runOnUiThread(new Runnable()
	    {

		@Override
		public void run()
		{

		    if (PrivateChatManagerImpl.this != null)
		    {
			if (PrivateChatManagerImpl.this.chatActivity != null)
			{
			    if (PrivateChatManagerImpl.this.chatActivity.adapter != null)
			    {
				if (PrivateChatManagerImpl.this.chatActivity.adapter.chatMessages != null)
				{
				    for (QBMessage mess : PrivateChatManagerImpl.this.chatActivity.adapter.chatMessages)
				    {
					if (mess.getId().equalsIgnoreCase(messageID))
					{
					    Sugar_Message sMess = StaticFunction.findMessageByID(mess.getId());
					    if (sMess != null)
					    {
						sMess.messRead = 1;
						sMess.save();
					    }
					    try
					    {
						((QBChatMessage) mess).setProperty("mess_status", "sent");
					    }
					    catch (Exception ex)
					    {

					    }
					    break;
					}

				    }

				    PrivateChatManagerImpl.this.chatActivity.adapter.notifyDataSetChanged();
				    PrivateChatManagerImpl.this.chatActivity.scrollDown();
				}
			    }
			}
		    }

		}
	    });
	}

	super.processMessageDelivered(sender, messageID);
    }

    // this is called when opponent have read the message
    @Override
    public void processMessageRead(QBPrivateChat sender, final String messageID)
    {

	if (!PrivateChatManagerImpl.this.chatActivity.isFinishing())
	{
	    PrivateChatManagerImpl.this.chatActivity.runOnUiThread(new Runnable()
	    {

		@Override
		public void run()
		{

		    if (PrivateChatManagerImpl.this != null)
		    {
			if (PrivateChatManagerImpl.this.chatActivity != null)
			{
			    if (PrivateChatManagerImpl.this.chatActivity.adapter != null)
			    {
				if (PrivateChatManagerImpl.this.chatActivity.adapter.chatMessages != null)
				{

				    for (QBMessage mess : PrivateChatManagerImpl.this.chatActivity.adapter.chatMessages)
				    {
					if (mess.getId().equalsIgnoreCase(messageID))
					{

					    Sugar_Message sMess = StaticFunction.findMessageByID(mess.getId());
					    if (sMess != null)
					    {
						sMess.messRead = 2;
						sMess.save();
					    }

					    try
					    {
						((QBChatMessage) mess).setProperty("mess_status", "seen");
					    }
					    catch (Exception ex)
					    {

					    }

					    break;

					}

				    }

				    PrivateChatManagerImpl.this.chatActivity.adapter.notifyDataSetChanged();
				    PrivateChatManagerImpl.this.chatActivity.scrollDown();
				}
			    }
			}
		    }

		}
	    });
	}
	super.processMessageRead(sender, messageID);
    }

    // this is called when user receive message from opponent
    @Override
    public void processMessage(QBPrivateChat chat, QBChatMessage message)
    {

	if (message != null)
	{
	    if (message.getProperty("dialog_id") != null)
	    {
		String dialodID = message.getProperty("dialog_id");

		if (dialodID.equalsIgnoreCase(this.DialogID))
		{

		    Log.e("SAVE message", "Save messageeeeeeeeeeeee");

		    // save mess to DB
		    StaticFunction.saveMessageToDB(message);
		    // save mess to DB

		    //

		    Sugar_Message sMess = StaticFunction.findMessageByID(message.getId());

		    if (sMess != null)
		    {

			if (sMess.messRecipientId == -999)
			{
			    message.setProperty("mess_status", "fail");
			}

			if (sMess.isShowDate.equalsIgnoreCase("true"))
			{
			    message.setProperty("is_set_time_bar", "true");
			}
			else
			{
			    message.setProperty("is_set_time_bar", "false");
			}

		    }

		    //

		    chatActivity.showMessageOnReceiveOnly(message);

		    try
		    {
			privateChat.readMessage(message.getId());
		    }
		    catch (Exception e)
		    {

		    }
		}

	    }
	}

    }

    @Override
    public void processError(QBPrivateChat chat, QBChatException error, QBChatMessage originChatMessage)
    {

	Log.e("processError", error.toString() + "---" + originChatMessage.getBody());

    }

    @Override
    public void chatCreated(QBPrivateChat incomingPrivateChat, boolean createdLocally)
    {

	/*if (!createdLocally)
	{
	    privateChat = incomingPrivateChat;
	    privateChat.addMessageListener(PrivateChatManagerImpl.this);
	}

	Log.w(TAG, "private chat created: " + incomingPrivateChat.getParticipant() + ", createdLocally:" + createdLocally);*/
    }

    // typing status callback
    @Override
    public void onSenderTyping()
    {

	try
	{
	    privateChat.sendIsTypingNotification();
	}
	catch (NotConnectedException e)
	{

	    e.printStackTrace();
	}
	catch (XMPPException e)
	{

	    e.printStackTrace();
	}

    }

    // typing status callback
    @Override
    public void onSenderStopTyping()
    {

	try
	{
	    privateChat.sendStopTypingNotification();
	}
	catch (NotConnectedException e)
	{

	    e.printStackTrace();
	}
	catch (XMPPException e)
	{

	    e.printStackTrace();
	}

    }

}
