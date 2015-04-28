package com.es.hello.chat.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.WindowManager;

import com.es.hello.chat.consts.SharePrefsHelper;
import com.es.hello.chat.ui.activities.Activity_Dialog_Logout;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.server.BaseService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

// This class hanlde all the backgroud process for the app
// Mainly this will maintain a connection from client to server for quickly app restart
// and also manage the error 
public class HelloMainService extends Service
{

    // app QB API token and key
    public static final String APP_ID = "19378";

    public static final String AUTH_KEY = "DsHtgV73sW4Qj9T";

    public static final String AUTH_SECRET = "8UY55pPXyrVZFPR";

    // app QB API token and key

    WakeLock wakeLock = null;

    public int RECONECT_FAIL_COUNT = 0;

    public interface ConnectionInterfaceCallback
    {

	public void onReconnectFail3Times();

	public void onConnectionClosedOnError();

	public void onConnectionClosedDuetoUserLoginToOtherDevice();

	public void onConnectedSuccess();

	public void onReconnecting();

	public void onConnectFail();

    }

    public static ConnectionInterfaceCallback ConnectionCallback;

    // listener for connection
    ConnectionListener connectionListener = new ConnectionListener()
    {

	@Override
	public void connected(XMPPConnection connection)
	{

	    Log.e("XMPPConnection", "connected");
	}

	@Override
	public void authenticated(XMPPConnection connection)
	{

	    Log.e("XMPPConnection", "authenticated");
	}

	@Override
	public void connectionClosed()
	{

	    Log.e("XMPPConnection", "connectionClosed");

	    if (ConnectionCallback != null)
		ConnectionCallback.onConnectionClosedOnError();
	}

	@Override
	public void connectionClosedOnError(Exception e)
	{

	    String cause = e.getMessage();

	    if (cause.trim().equalsIgnoreCase("stream:error (conflict)"))
	    {

		Intent dialogIntent = new Intent(getBaseContext(), Activity_Dialog_Logout.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		HelloMainService.this.startActivity(dialogIntent);

	    }
	    else
	    {

		Log.e("XMPPConnection", "connectionClosedOnError");

		if (ConnectionCallback != null)
		    ConnectionCallback.onConnectionClosedOnError();

	    }

	}

	@Override
	public void reconnectingIn(int seconds)
	{

	    Log.e("XMPPConnection", "reconnectingIn  " + seconds);
	    if (ConnectionCallback != null)
		ConnectionCallback.onReconnecting();

	}

	@Override
	public void reconnectionSuccessful()
	{

	    Log.e("XMPPConnection", "reconnectionSuccessful");
	    if (ConnectionCallback != null)
		ConnectionCallback.onConnectedSuccess();

	}

	@Override
	public void reconnectionFailed(Exception e)
	{

	    Log.e("XMPPConnection", "reconnectionFailed");
	    if (ConnectionCallback != null)
		ConnectionCallback.onConnectFail();

	    RECONECT_FAIL_COUNT++;

	    if (RECONECT_FAIL_COUNT > 5)
	    {
		RECONECT_FAIL_COUNT = 0;
		ConnectionCallback.onReconnectFail3Times();
	    }

	}
    };

    public static final String TAG = "HelloMainServiceTag";

    public interface ServiceInterfaceCallback
    {

	public void onLoginSuccess();

	public void onCreateSessionError(String error);

	public void onLoginError(String error);

	public void onCreateSessionErrorNeedToResetAll();

    }

    public static ServiceInterfaceCallback Callback;

    public static QBChatService chatService;

    public static String ACTION_START = "com.hello.action.START";

    public static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    @Override
    public void onCreate()
    {

	super.onCreate();
	Log.e("SERVICE IS CREATEDDDDDDD", "SERVICE IS CREATEDDDDDDD onCreate");
    }

    public HelloMainService()
    {

	Log.e("SERVICE IS CREATEDDDDDDD", "Constructor");
    }

    @Override
    public IBinder onBind(Intent intent)
    {

	return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

	initChat();
	Log.e("SERVICE IS onStartCommand", "SERVICE IS onStartCommand");
	return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {

	Log.e("SERVICE IS onUnbind", "SERVICE IS onUnbind");
	return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {

	if (wakeLock != null)
	{
	    wakeLock.release();
	}
	super.onDestroy();
	Log.e("SERVICE IS DESTROYED", "SERVICE IS DESTROYED");
    }

    // here we init something that need to the app to start
    // 1. init QB API
    // 2. session
    // 3. user login
    public void initChat()
    {

	PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
	if (wakeLock == null)
	{
	    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
	}

	wakeLock.acquire();

	// QBChatService.setDebugEnabled(true);

	QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);

	if (!QBChatService.isInitialized())
	{

	    QBChatService.init(HelloMainService.this);

	    QBChatService.getInstance().addConnectionListener(connectionListener);

	}

	if (HelloMainService.chatService == null)
	{

	    HelloMainService.chatService = QBChatService.getInstance();

	    String token = null;

	    try
	    {
		token = BaseService.getBaseService().getToken();
	    }
	    catch (BaseServiceException e)
	    {

		e.printStackTrace();
	    }

	    if (token == null)
	    {
		QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
		createSession(user);
	    }
	    else
	    {

		try
		{
		    Date currentDate = new Date(System.currentTimeMillis());
		    Date expirationDate = BaseService.getBaseService().getTokenExpirationDate();

		    if (expirationDate.before(currentDate))
		    {
			QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
			createSession(user);
		    }
		    else
		    {
			if (HelloMainService.chatService.isLoggedIn() == false)
			{
			    QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
			    int userID = SharePrefsHelper.getCurrentLoginUserID(HelloMainService.this);
			    if (userID != -1)
			    {
				user.setId(userID);
				loginToChat(user);
			    }
			    else
			    {
				if (Callback != null)
				    Callback.onLoginError("Error When login, please try again!");
			    }
			}
			else
			{
			    if (Callback != null)
			    {
				Callback.onLoginSuccess();

				try
				{
				    HelloMainService.chatService.setReconnectionAllowed(false);
				    HelloMainService.chatService.enableCarbons();
				    HelloMainService.chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);

				}
				catch (Exception e)
				{
				    e.printStackTrace();
				}
			    }
			}

		    }

		}
		catch (Exception ex)
		{
		    QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
		    createSession(user);

		}

	    }
	}
	else
	{
	    String token = null;

	    try
	    {
		token = BaseService.getBaseService().getToken();
	    }
	    catch (BaseServiceException e)
	    {

		e.printStackTrace();
	    }

	    if (token == null)
	    {
		QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
		createSession(user);
	    }
	    else
	    {

		try
		{
		    Date currentDate = new Date(System.currentTimeMillis());
		    Date expirationDate = BaseService.getBaseService().getTokenExpirationDate();

		    if (expirationDate.before(currentDate))
		    {
			QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
			createSession(user);
		    }
		    else
		    {
			if (HelloMainService.chatService.isLoggedIn() == false)
			{
			    QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
			    int userID = SharePrefsHelper.getCurrentLoginUserID(HelloMainService.this);
			    if (userID != -1)
			    {
				user.setId(userID);
				loginToChat(user);
			    }
			    else
			    {
				if (Callback != null)
				    Callback.onLoginError("Error When login, please try again!");
			    }
			}
			else
			{
			    if (Callback != null)
			    {
				Callback.onLoginSuccess();

				try
				{
				    HelloMainService.chatService.setReconnectionAllowed(false);
				    HelloMainService.chatService.enableCarbons();
				    HelloMainService.chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);

				}
				catch (Exception e)
				{
				    e.printStackTrace();
				}
			    }
			}

		    }

		}
		catch (Exception ex)
		{
		    QBUser user = SharePrefsHelper.getCurrentLoginUser(HelloMainService.this);
		    createSession(user);

		}

	    }
	}

    }

    // not use anymore
    private void createAlarmToReConnectWhenSessionTimeout()
    {

	// alarm 2 hour later to re-create session
	AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	Intent i = new Intent(HelloMainService.this, AlarmReceiver.class); // explicit
	// intent
	PendingIntent intentExecuted = PendingIntent.getBroadcast(HelloMainService.this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
	Calendar now = Calendar.getInstance();
	now.setTimeInMillis(System.currentTimeMillis());
	now.add(Calendar.SECOND, 7200);

	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), 120 * 60 * 1000, intentExecuted);

	// Date expirationDate =
	// BaseService.getBaseService().getTokenExpirationDate();

    }

    // create a new session
    public void createSession(QBUser currentUser)
    {

	if (currentUser != null)
	{
	    final QBUser user = currentUser;
	    QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>()
	    {

		@Override
		public void onSuccess(QBSession session, Bundle args)
		{

		    // createAlarmToReConnectWhenSessionTimeout();

		    if (session != null)
		    {
			user.setId(session.getUserId());
			SharePrefsHelper.saveUserIDToSharePrefs(session.getUserId(), HelloMainService.this);

			// if (HelloMainService.chatService.isLoggedIn() ==
			// true)
			// {
			// if (Callback != null)
			// {
			// Callback.onLoginSuccess();
			// }
			// }
			// else
			// {
			loginToChat(user);
			// }
		    }

		}

		@Override
		public void onError(List<String> errors)
		{

		    if (errors.toString().trim().equalsIgnoreCase("[]"))
		    {
			// notify
			if (Callback != null)
			{
			    // String strError =
			    // "Cannot connect to server. Please try again!";
			    Callback.onCreateSessionErrorNeedToResetAll();
			}
		    }
		    else
		    {
			// notify
			if (Callback != null)
			{
			    String strError = errors.toString();
			    Callback.onCreateSessionError(strError);
			}
		    }

		}
	    });
	}

    }

    // login to chat app
    @SuppressWarnings("rawtypes")
    private void loginToChat(final QBUser user)
    {

	HelloMainService.chatService.login(user, "reshelchat", new QBEntityCallbackImpl()
	{

	    @Override
	    public void onSuccess()
	    {

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable()
		{

		    @Override
		    public void run()
		    {

			List<Integer> usersIDs = new ArrayList<Integer>();
			usersIDs.add(user.getId());

			QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
			requestBuilder.setPage(1);
			requestBuilder.setPerPage(usersIDs.size()); //

			QBUsers.getUsersByIDs(usersIDs, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>()
			{

			    @Override
			    public void onSuccess(ArrayList<QBUser> users, Bundle params)
			    {

				// ApplicationSingleton.setCurrentUser(users.get(0));
				// notify
				if (Callback != null)
				{
				    Callback.onLoginSuccess();
				}
			    }

			    @Override
			    public void onError(List<String> errors)
			    {

				// notify
				if (Callback != null)
				{
				    String strError = errors.toString();
				    Callback.onLoginError(strError);
				}
			    }

			});
		    }
		});

		try
		{
		    HelloMainService.chatService.setReconnectionAllowed(false);
		    HelloMainService.chatService.enableCarbons();
		    HelloMainService.chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);

		}
		catch (Exception e)
		{
		    e.printStackTrace();
		}

	    }

	    @Override
	    public void onError(List errors)
	    {

		if (errors.toString().equalsIgnoreCase("[You have already logged in chat]"))
		{
		    if (Callback != null)
		    {
			Callback.onLoginSuccess();

			try
			{
			    HelloMainService.chatService.setReconnectionAllowed(false);
			    HelloMainService.chatService.enableCarbons();
			    HelloMainService.chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);

			}
			catch (Exception e)
			{
			    e.printStackTrace();
			}

		    }
		}
		else
		{

		    // notify
		    if (Callback != null)
		    {
			String strError = errors.toString();
			Callback.onLoginError(strError);
		    }
		}
	    }
	});
    }

    
    // to show error in case there is error
    public void showDialogError(Context context, String dialogMessage)
    {

	AlertDialog.Builder builder = new AlertDialog.Builder(HelloMainService.this);
	builder.setMessage("sadasd").setPositiveButton("OK", new DialogInterface.OnClickListener()
	{

	    public void onClick(DialogInterface dialog, int id)
	    {

	    }
	}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	{

	    public void onClick(DialogInterface dialog, int id)
	    {

	    }
	});
	try
	{
	    AlertDialog dialog = builder.create();
	    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	    dialog.show();
	}
	catch (Exception ex)
	{
	    Log.e("", "");
	}

    }

}
