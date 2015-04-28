package com.es.hello.chat.ui.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.es.hello.chat.PlayServicesHelper;
import com.es.hello.chat.StaticFunction;
import com.es.hello.chat.consts.FontTypeUtils;
import com.es.hello.chat.consts.SharePrefsHelper;
import com.es.hello.chat.customobject.ObjectSearch4;
import com.es.hello.chat.customobject.Object_SearchExpandChild;
import com.es.hello.chat.customobject.Object_SearchExpandGroup;
import com.es.hello.chat.ui.adapters.ExpandableListAdapter;
import com.es.hello.chat.ui.adapters.Search4Adapter;
import com.es.hello.chat.ui.adapters.TrendAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lat.hello.chat.R;
import com.quickblox.users.model.QBUser;

// search screen
public class Activity_Search extends ActionBarActivity
{

    public int BackPressCount = 0;

    private DrawerLayout mDrawerLayout;

    // private LinearLayout mDrawerList;

    private PullToRefreshListView listSearch4;

    List<ObjectSearch4> listDataSearch = new ArrayList<ObjectSearch4>();

    List<String> listTrendTags = new ArrayList<String>();

    ExpandableListAdapter listAdapter;

    ExpandableListView expListView;

    List<Object_SearchExpandGroup> listObjectGroup;

    // private int listViewIndex;

    // private int listViewTop;

    ListView listivewTrendTags;

    Button btn1;

    Button btn2;

    Button btn3;

    Button btn4;

    private RelativeLayout progressBar;

    // private RelativeLayout progressBarRelative;

    TextView txtTrendingTittle;

    public PlayServicesHelper playServicesHelper;

    RelativeLayout leftLayout;

    LinearLayout contentLayout;

    RelativeLayout listBelowLayout;

    TrendAdapter adapter;

    LinearLayout btnMoveDown;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_search);

	setupUI(findViewById(R.id.drawer_layout));

	QBUser currentLoginUser = SharePrefsHelper.getCurrentLoginUser(Activity_Search.this);

	playServicesHelper = new PlayServicesHelper(this, currentLoginUser.getLogin());

	Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
	setSupportActionBar(toolbar);

	StaticFunction.initActionToolBar(this, toolbar, "SEARCH", false);

	// getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	txtTrendingTittle = (TextView) findViewById(R.id.txtTittleTrending);
	txtTrendingTittle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 29);
	txtTrendingTittle.setTypeface(FontTypeUtils.getFont_Myriad_Pro_Conden(Activity_Search.this), Typeface.BOLD);

	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
	listSearch4 = (PullToRefreshListView) findViewById(R.id.listviewHashtags);

	btnMoveDown = (LinearLayout) findViewById(R.id.btnMoveDown);
	btnMoveDown.setVisibility(View.GONE);
	btnMoveDown.setOnTouchListener(new OnTouchListener()
	{

	    @SuppressLint("ClickableViewAccessibility")
	    @Override
	    public boolean onTouch(View v, MotionEvent event)
	    {

		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{

		    scrollListViewToBottom();
		    Log.e("ACTION_DOWN", "ACTION_DOWN");

		}

		// }

		if (event.getAction() == MotionEvent.ACTION_UP)
		{
		    Log.e("ACTION_UP", "ACTION_UP");
		    StopScrollListView();

		}

		return true;

	    }
	});

	leftLayout = (RelativeLayout) findViewById(R.id.left_drawer);
	leftLayout.setOnFocusChangeListener(new View.OnFocusChangeListener()
	{

	    @Override
	    public void onFocusChange(View v, boolean hasFocus)
	    {

		if (hasFocus)
		{
		    hideKeyboard(v);
		}
	    }
	});

	listBelowLayout = (RelativeLayout) findViewById(R.id.expanListBelowContainer);
	listBelowLayout.setOnFocusChangeListener(new View.OnFocusChangeListener()
	{

	    @Override
	    public void onFocusChange(View v, boolean hasFocus)
	    {

		if (hasFocus)
		{
		    hideKeyboard(v);
		}
	    }
	});

	contentLayout = (LinearLayout) findViewById(R.id.content_frame);
	contentLayout.setOnFocusChangeListener(new View.OnFocusChangeListener()
	{

	    @Override
	    public void onFocusChange(View v, boolean hasFocus)
	    {

		if (hasFocus)
		{
		    hideKeyboard(v);
		}
	    }
	});

	expListView = (ExpandableListView) findViewById(R.id.expanListBelow);

	expListView.setOnFocusChangeListener(new View.OnFocusChangeListener()
	{

	    @Override
	    public void onFocusChange(View v, boolean hasFocus)
	    {

		if (hasFocus)
		{
		    hideKeyboard(v);
		}
	    }
	});

	Field mDragger;
	try
	{
	    mDragger = mDrawerLayout.getClass().getDeclaredField("mRightDragger"); // "mLeftDragger"
										   // for
										   // left
										   // ;
										   // "mRightDragger"
										   // for
										   // right
										   // drawer
										   // navigation

	    mDragger.setAccessible(true);
	    ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(mDrawerLayout);

	    Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
	    mEdgeSize.setAccessible(true);
	    int edge = mEdgeSize.getInt(draggerObj);

	    mEdgeSize.setInt(draggerObj, edge * 6);
	    //

	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	progressBar = (RelativeLayout) findViewById(R.id.progressBarRelative);
	// progressBarRelative = (RelativeLayout)
	// findViewById(R.id.progressBarRelative);
	progressBar.setVisibility(View.GONE);
	// progressBar.setVisibility(View.GONE);

	initPullListSearchTags();

	int numOfApp = SharePrefsHelper.getNumOfAppUsedForInstructionToSharePrefs(Activity_Search.this);

	if (numOfApp < 3)
	{

	    contentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener()
	    {

		public void onGlobalLayout()
		{

		    contentLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

		    int[] locations = new int[2];
		    listBelowLayout.getLocationOnScreen(locations);
		    // int x = locations[0];
		    int y = locations[1];

		    showPopupInstruction(Activity_Search.this, R.layout.dialog_menu_instruction, y + 35);
		}
	    });

	}

	initTrendList(0);

	btn1 = (Button) findViewById(R.id.btnHello);
	btn1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	btn1.setTypeface(FontTypeUtils.getFont_Myriad_Pro_Regular(getApplicationContext()));

	btn1.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		initTrendList(0);

	    }
	});

	btn2 = (Button) findViewById(R.id.btnTweeter);
	btn2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	btn2.setTypeface(FontTypeUtils.getFont_Myriad_Pro_Regular(getApplicationContext()));

	btn2.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		initTrendList(1);

	    }
	});

	btn3 = (Button) findViewById(R.id.btnInstagram);
	btn3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	btn3.setTypeface(FontTypeUtils.getFont_Myriad_Pro_Regular(getApplicationContext()));

	btn3.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		initTrendList(2);

	    }
	});

	btn4 = (Button) findViewById(R.id.btnMyFavou);
	btn4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
	btn4.setTypeface(FontTypeUtils.getFont_Myriad_Pro_Regular(getApplicationContext()));

	btn4.setOnClickListener(new OnClickListener()
	{

	    @Override
	    public void onClick(View v)
	    {

		initTrendList(3);

	    }
	});

    }

    @Override
    public void onBackPressed()
    {

	BackPressCount++;
	if (BackPressCount == 2)
	{
	    BackPressCount = 0;
	    // java.lang.System.exit(0);
	    super.onBackPressed();
	}
	else
	{
	    Toast.makeText(Activity_Search.this, "press BACK 1 more time to close app!", Toast.LENGTH_LONG).show();
	}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.menu_search, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

	int id = item.getItemId();

	if (id == android.R.id.home)
	{
	    // this.finish();
	    return true;
	}

	if (id == R.id.action_1)
	{

	    Intent intent = new Intent(Activity_Search.this, Activity_Setting.class);
	    startActivity(intent);
	    // ApplicationSingleton.queueActivities.enqueue(Activity_Search.class);
	    // finish();

	}

	if (id == R.id.action_2)
	{

	    Intent intent = new Intent(Activity_Search.this, DialogsActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    startActivity(intent);
	    // ApplicationSingleton.queueActivities.enqueue(Activity_Search.class);
	    // finish();

	}

	if (id == R.id.action_3)
	{

	    // progressBar.setVisibility(View.VISIBLE);
	    progressBar.setVisibility(View.VISIBLE);

	    expListView.setEnabled(false);
	    expListView.setAlpha(0.3f);

	    startProgress(progressBar);

	}
	return super.onOptionsItemSelected(item);
    }

    public void startProgress(View view)
    {

	// do something long
	Runnable runnable = new Runnable()
	{

	    @Override
	    public void run()
	    {

		for (int i = 0; i < 1; i++)
		{
		    doFakeWork();
		    progressBar.post(new Runnable()
		    {

			@Override
			public void run()
			{

			    initExpandList();
			    // progressBar.setVisibility(View.GONE);
			    progressBar.setVisibility(View.GONE);

			    expListView.setEnabled(true);
			    expListView.setAlpha(1f);

			}
		    });
		}
	    }
	};
	new Thread(runnable).start();
    }

    // simulate the progress of finding friend
    // do nothing, just load the progress dialog
    private void doFakeWork()
    {

	try
	{
	    Thread.sleep(2000);

	}
	catch (InterruptedException e)
	{
	    e.printStackTrace();
	}
    }

    // create dummy data as found friends
    private List<Object_SearchExpandGroup> getDummyData()
    {

	List<Object_SearchExpandGroup> objGroups = new ArrayList<Object_SearchExpandGroup>();

	Object_SearchExpandGroup objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = true;
	objGroup.NumOfUserFound = 5;

	Object_SearchExpandChild objChild = new Object_SearchExpandChild();
	objChild.Name = "Allice88";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "HippieDude12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Catagaya123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Sinbadtwo2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Lazadacocacola";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 7;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Allice88";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "HippieDude12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Catagaya123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Sinbadtwo2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Lazadacocacola";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "pepsi123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "cocojambo00";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 8;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Loi18";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Dakao12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Catagaya123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Sinbadtwo2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Madacata";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "pepsi123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "cocojambo00";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Hehehe00";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 4;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Loi18";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Dakao12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Catagaya123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Sinbadtwo2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 5;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Shark1234";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Spiderman12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Hulk123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Ironman2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Captain2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 3;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Hulk123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Ironman2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Captain2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 5;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Loki4";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Superman12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Hulk123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Ironman2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Captain2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 5;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Shark1234";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Dilolphin12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Acacdfd123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Suleman2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Captain2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================
	objGroup = new Object_SearchExpandGroup();
	objGroup.IsTag1Found = true;
	objGroup.IsTag2Found = true;
	objGroup.IsTag3Found = true;
	objGroup.IsTag4Found = false;
	objGroup.NumOfUserFound = 5;

	objChild = new Object_SearchExpandChild();
	objChild.Name = "Chelsea1234";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Spiderman12";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Manchester123";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Ironman2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);
	objChild = new Object_SearchExpandChild();
	objChild.Name = "Suno2";
	objChild.Status = "Looking for festival buddies!!";
	objChild.HowFar = 10;
	objGroup.ListChilds.add(objChild);

	objGroups.add(objGroup);
	// ===============================================

	return objGroups;

    }

    // Trending list on the right panel
    private void initTrendList(int type)
    {

	listTrendTags = new ArrayList<String>();

	String typee = "";

	if (type == 0)
	{
	    typee = "#ello trending";
	}
	else if (type == 1)
	{
	    typee = "twitter trending";
	}
	else if (type == 2)
	{
	    typee = "instagram trending";
	}
	else if (type == 3)
	{
	    typee = "favourite trending";
	}

	listivewTrendTags = (ListView) findViewById(R.id.listviewBelowTrend);

	listTrendTags.add("1. " + typee);
	listTrendTags.add("2. " + typee);
	listTrendTags.add("3. " + typee);
	listTrendTags.add("4. " + typee);
	listTrendTags.add("5. " + typee);
	listTrendTags.add("6. " + typee);
	listTrendTags.add("7. " + typee);
	listTrendTags.add("8. " + typee);
	listTrendTags.add("9. " + typee);
	listTrendTags.add("10. " + typee);
	listTrendTags.add("11. " + typee);
	listTrendTags.add("12. " + typee);
	listTrendTags.add("13. " + typee);
	listTrendTags.add("14. " + typee);
	listTrendTags.add("15. " + typee);
	listTrendTags.add("16. " + typee);

	adapter = new TrendAdapter(listTrendTags, Activity_Search.this);

	listivewTrendTags.setAdapter(adapter);

	if (adapter.getCount() > 0)
	{
	    btnMoveDown.setVisibility(View.VISIBLE);
	}

    }

    private void initExpandList()
    {

	listObjectGroup = getDummyData();
	listAdapter = new ExpandableListAdapter(this, listObjectGroup);
	expListView.setAdapter(listAdapter);
	getSupportActionBar().setTitle("MATCHES");
	expListView.setOnChildClickListener(new OnChildClickListener()
	{

	    @Override
	    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
	    {

		Intent intent = new Intent(Activity_Search.this, Activity_Opponent_Profile.class);
		intent.putExtra("IsTag1Found", listObjectGroup.get(groupPosition).IsTag1Found);
		intent.putExtra("IsTag2Found", listObjectGroup.get(groupPosition).IsTag2Found);
		intent.putExtra("IsTag3Found", listObjectGroup.get(groupPosition).IsTag3Found);
		intent.putExtra("IsTag4Found", listObjectGroup.get(groupPosition).IsTag4Found);
		startActivity(intent);
		// ApplicationSingleton.queueActivities.enqueue(Activity_Search.class);
		return false;
	    }
	});

    }

    private void initPullListSearchTags()
    {

	listSearch4.setOnFocusChangeListener(new View.OnFocusChangeListener()
	{

	    @Override
	    public void onFocusChange(View v, boolean hasFocus)
	    {

		if (!hasFocus)
		{
		    hideKeyboard(v);
		}
	    }
	});

	ObjectSearch4 obj = new ObjectSearch4();
	obj.strTag = "";

	listDataSearch.add(obj);
	listDataSearch.add(obj);
	listDataSearch.add(obj);
	listDataSearch.add(obj);

	Search4Adapter adapter = new Search4Adapter(listDataSearch, Activity_Search.this);

	listSearch4.setAdapter(adapter);

	listSearch4.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>()
	{

	    @Override
	    public void onRefresh(PullToRefreshBase<ListView> refreshView)
	    {

		// progressBar.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		expListView.setEnabled(false);
		expListView.setAlpha(0.3f);

		new Handler().post(new Runnable()
		{

		    @Override
		    public void run()
		    {

			listSearch4.onRefreshComplete();

		    }
		});

		startProgress(progressBar);

	    }
	});

    }

    public void showPopupInstruction(Context context, int ResourceID, int y)
    {

	// custom dialog
	final Dialog dialog = new Dialog(context);

	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	dialog.setCanceledOnTouchOutside(false);
	dialog.setContentView(ResourceID);

	final ImageView imgView = (ImageView) dialog.findViewById(R.id.imgInstruction);

	imgView.setOnTouchListener(new OnTouchListener()
	{

	    @Override
	    public boolean onTouch(View v, MotionEvent event)
	    {

		int width = imgView.getWidth();
		// int height = imgView.getHeight();

		float x = event.getX();
		float y = event.getY();

		if (x >= (width - 96) && y <= (96))
		{
		    dialog.dismiss();

		    int numOfApp = SharePrefsHelper.getNumOfAppUsedForInstructionToSharePrefs(Activity_Search.this);

		    SharePrefsHelper.saveNumOfAppUsedForInstructionToSharePrefs(numOfApp + 1, Activity_Search.this);
		}

		return false;
	    }
	});

	WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
	wmlp.gravity = Gravity.TOP;
	wmlp.y = y;

	dialog.show();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {

	super.onWindowFocusChanged(hasFocus);
	if (hasFocus)
	{
	    int top = listBelowLayout.getTop();
	}
    }

    
    public void hideKeyboard(View view)
    {

	InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
	inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // setup hide keyboard when touch outside
    public void setupUI(View view)
    {

	// Set up touch listener for non-text box views to hide keyboard.
	if (!(view instanceof EditText))
	{

	    view.setOnTouchListener(new OnTouchListener()
	    {

		public boolean onTouch(View v, MotionEvent event)
		{

		    StaticFunction.hideSoftKeyboard(Activity_Search.this);
		    getWindow().getDecorView().clearFocus();
		    return false;
		}

	    });
	}

	// If a layout container, iterate over children and seed recursion.
	if (view instanceof ViewGroup)
	{

	    for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
	    {

		View innerView = ((ViewGroup) view).getChildAt(i);

		setupUI(innerView);
	    }
	}
    }

    
    // when user press scroll button -> scroll down
    private void scrollListViewToBottom()
    {

	if (listivewTrendTags != null)
	{
	    listivewTrendTags.post(new Runnable()
	    {

		@Override
		public void run()
		{

		    if (adapter != null)
		    {
			int size = adapter.getCount();
			int totalHeight = 0;

			for (int i = 0; i < size; i++)
			{
			    View mView = adapter.getView(i, null, listivewTrendTags);

			    mView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),

			    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			    totalHeight += mView.getMeasuredHeight();

			}

			listivewTrendTags.smoothScrollBy(totalHeight, totalHeight * 10);

		    }

		}
	    });

	}

    }

 // when user unpress scroll button ->stop scroll
    private void StopScrollListView()
    {

	if (listivewTrendTags != null)
	{
	    listivewTrendTags.post(new Runnable()
	    {

		@Override
		public void run()
		{

		    listivewTrendTags.smoothScrollBy(0, 0);

		}
	    });
	}

    }

}
