package com.es.hello.chat.ui.activities;

import com.lat.hello.chat.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class Activity_Pre_Login extends Activity
{

    ImageView imgButtonExistingUser;

    ImageView imgButtonNewUser;

    ImageView imgButtonLoginFb;

    ImageView imgButtonLoginGGPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_pre_login);

	imgButtonExistingUser = (ImageView) findViewById(R.id.imgExistingUser);
	imgButtonExistingUser.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		Intent intent = new Intent(Activity_Pre_Login.this, Activity_Login_Hello.class);
		startActivity(intent);
		finish();

	    }
	});

	imgButtonNewUser = (ImageView) findViewById(R.id.imgNewUser);
	imgButtonNewUser.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		Intent intent = new Intent(Activity_Pre_Login.this, Activity_SignUp.class);
		startActivity(intent);
		finish();

	    }
	});

	imgButtonLoginFb = (ImageView) findViewById(R.id.imgLoginFb);
	imgButtonLoginFb.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		// TODO Auto-generated method stub

	    }
	});

	imgButtonLoginGGPlus = (ImageView) findViewById(R.id.imgLoginGplus);
	imgButtonLoginGGPlus.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		// TODO Auto-generated method stub

	    }
	});

    }

}
