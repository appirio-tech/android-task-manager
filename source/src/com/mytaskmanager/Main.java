package com.mytaskmanager;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.Tasks.TasksOperations;
import com.google.api.services.tasks.v1.model.TaskList;
import com.mytaskmanager.R;
import com.mytaskmanager.adapter.TaskListAdapter;
import com.mytaskmanager.controller.ApplicationController;
import com.mytaskmanager.util.Constant;

public class Main extends Activity{

	private static Level LOGGING_LEVEL = Level.OFF;

	private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";

	private static final String TAG = "MyTaskManager";	
	
	private static final int REQUEST_AUTHENTICATE = 0;

	private static final int DIALOG_ACCOUNTS = 0;

	public ApplicationController controller = null;

	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();	

	String gsessionid;
	String authToken = "AIzaSyDUYGu5MtVQ-9ZKReCVOIW8NCWOyNJ7_y0";
	String accessKey = "AIzaSyDUYGu5MtVQ-9ZKReCVOIW8NCWOyNJ7_y0";	
	
	String accountName;

	static final String PREF = TAG;
	static final String PREF_ACCOUNT_NAME = "accountName";
	static final String PREF_AUTH_TOKEN = "authToken";
	static final String PREF_GSESSIONID = "gsessionid";

	GoogleAccountManager accountManager;
	SharedPreferences settings;

	final Tasks taskService = new Tasks("MyTaskManager", transport, new JacksonFactory());
	TasksOperations taskOperations = taskService.tasks;
	
	private TaskListAdapter adapter;


	private static final int RESULT_CODE_OK = 100;	
	private static final int RESULT_CODE_CANCEL = 200;
	
	private List<TaskList> taskLists;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {	    
		super.onCreate(savedInstanceState);
		accountManager = new GoogleAccountManager(this);
		controller = (ApplicationController)getApplication();
		controller.setTasks(taskService);
		Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);	



		gotAccount(false);
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			final Account[] accounts = accountManager.getAccounts();
			final int size = accounts.length;
			if (size > 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(this.getResources().getString(R.string.select_account));
			String[] names = new String[size];
			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gotAccount(accounts[which]);
				}
			});
			return builder.create();
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.app_name);				
				builder.setMessage(this.getResources().getString(R.string.no_account_found))
			       .setCancelable(false)
			       .setPositiveButton(this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.dismiss();
			           }
			       });
				return builder.create();
			}		
		}
		return null;
	}

	void gotAccount(boolean tokenExpired) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		String accountName = settings.getString("accountName", "com.google");
		Account account = accountManager.getAccountByName(accountName);
		if (account != null) {
			if (tokenExpired) {
				accountManager.invalidateAuthToken(authToken);
				authToken = null;
			}
			gotAccount(account);
			return;
		}
		showDialog(DIALOG_ACCOUNTS);
	}

	void gotAccount(final Account account) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accountName", account.name);
		editor.commit();
		accountManager.manager.getAuthToken(
				account, AUTH_TOKEN_TYPE, true, new AccountManagerCallback<Bundle>() {

					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bundle = future.getResult();
							if (bundle.containsKey(AccountManager.KEY_INTENT)) {
								Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
								intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent, REQUEST_AUTHENTICATE);
							} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
								authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
								onAuthToken();
							}
						} catch (Exception e) {
							handleException(e);
						}
					}
				}, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				gotAccount(false);
			} else {
				showDialog(DIALOG_ACCOUNTS);
			}
			break;
		case RESULT_CODE_OK:
			if (requestCode == resultCode){
				processTaskList(data);
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.SwitchAccount:
			showDialog(DIALOG_ACCOUNTS);
			return true;
		case R.id.About:
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;	
		}
		return false;
	}

	void handleException(Exception e) {
		e.printStackTrace();
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).response;
			int statusCode = response.statusCode;
			try {
				response.ignore();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
			if (statusCode == 401) {
				gotAccount(true);
				return;
			}
		}
		Log.e(TAG, e.getMessage(), e);
	}

	void onAuthToken() {
		new GoogleAccessProtectedResource(authToken) {

			@Override
			protected void onAccessToken(String accessToken) {
				gotAccount(true);
			}
		};

		taskService.setAccessToken(authToken);
		taskService.accessKey = accessKey; 
		Log.i(TAG, "Auth Token" + authToken);
		Log.i(TAG, "Access Key " + taskService.accessKey);
		Log.i(TAG, "Base Path " + taskService.basePath);
		Log.i(TAG, "Base Server " + taskService.baseServer);	    
		setContentView(R.layout.main);
		Button createNewButton = (Button)findViewById(R.id.CreateTaskList);
		createNewButton.setOnClickListener(createClickListener);
		final ListView activitiesListView = (ListView) findViewById(R.id.activities);
		
		registerForContextMenu(activitiesListView);
		activitiesListView.setOnItemClickListener(taskListClickListener);
		new LoadActivities().execute();
	}

	private static final String FIELDS_ACTIVITY = "object/content,updated,id";
	private static final String FIELDS_ACTIVITY_FEED = "items(" + FIELDS_ACTIVITY + ")";

	class LoadActivities extends AsyncTask<Void, Void, com.google.api.services.tasks.v1.model.TaskLists> {
		private final ProgressDialog dialog = new ProgressDialog(Main.this);

		@Override
		protected void onPreExecute() {
			dialog.setMessage(getBaseContext().getResources().getString(R.string.loading_tasklist));
			dialog.show();		
		}

		@Override
		protected com.google.api.services.tasks.v1.model.TaskLists doInBackground(Void... params) {
			
			try {
				return taskService.tasklists.list().execute();
			} catch (Exception e) {
				handleException(e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(com.google.api.services.tasks.v1.model.TaskLists feed) {
			dialog.dismiss();
			if (feed == null) {
				return;
			}				
			ListView activitiesListView = (ListView) findViewById(R.id.activities);
			taskLists = feed.items;
			adapter = new TaskListAdapter(getBaseContext(), R.layout.textview,taskLists);
			activitiesListView.setAdapter(adapter);
		}
	}

	private OnItemClickListener taskListClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			showContextMenu(arg2);
		}
	};
	
	private void showTasks(int selectedPositon){
		TaskList taskList = (TaskList)taskLists.get(selectedPositon);
		controller.setCurrentTasklist(taskList);
		Intent intent = new Intent(getBaseContext(), ShowTasks.class);
		startActivity(intent);
	}

	private OnClickListener createClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			addNewTaskList();
		}
	};

	private void addNewTaskList(){
		Intent intent = new Intent(this, AddUpdateTaskList.class);
		intent.putExtra(Constant.MODE, Constant.ADD_TASKLIST_MODE);	
		startActivityForResult(intent, RESULT_CODE_OK);
	}
	private void processTaskList(Intent data){		
		TaskList taskList = controller.selectedTaskList;
		ProgressDialog dialog = new ProgressDialog(Main.this);
		try {
			dialog.setMessage(this.getResources().getString(R.string.processing_tasklist));
			dialog.show();
			if (data.getExtras().getInt(Constant.MODE) == Constant.ADD_TASKLIST_MODE){
				taskList = taskService.tasklists.insert(taskList).execute();
				taskLists.add(taskList);
			}else{
				taskService.tasklists.update("@me", taskList).execute();
			}
			adapter.notifyDataSetChanged();
			dialog.dismiss();
		}catch (Exception e) {
			// TODO: handle exception
			dialog.dismiss();
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();			
		}
	}
	
	

	private void showContextMenu(final int selectedPosition){		
		final CharSequence[] items = getResources().getStringArray(R.array.tasklist_context_menu);		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getResources().getString(R.string.options));		
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				switch (item) {
				case 0:
					showTasks(selectedPosition);
					break;
				case 1:
					editTasklist(selectedPosition);
					break;
				case 2:
					deleteTasklistAlert(selectedPosition);
					break;
				}				

			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void editTasklist(int selectedPosition){
		TaskList taskList = taskLists.get(selectedPosition);
		controller.selectedTaskList = taskList;
		Intent intent = new Intent(this, AddUpdateTaskList.class);
		intent.putExtra(Constant.MODE, Constant.EDIT_TASKLLIST_MODE);		
		startActivityForResult(intent, RESULT_CODE_OK);
	}

	private void deleteTasklistAlert(final int selectedPosition){
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getResources().getString(R.string.alert_tasklist_delete))
		       .setCancelable(false)
		       .setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {		        	   
		                dialog.dismiss();
		                deleteTasklist(selectedPosition);
		           }
		       }).setNegativeButton(this.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {		        	   
		                dialog.dismiss();		                
		           }
		       });
			builder.create().show();
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	private void deleteTasklist(int selectedPosition){
		ProgressDialog dialog = new ProgressDialog(Main.this);
		try{
			dialog.setMessage(this.getResources().getString(R.string.processing_tasklist));
			dialog.show();
			TaskList taskList = taskLists.get(selectedPosition);
			taskService.tasklists.delete(taskList.id).execute();
			taskLists.remove(taskList);
			adapter.notifyDataSetChanged();			
			dialog.dismiss();
		}catch (Exception e) {
			// TODO: handle exception
			dialog.dismiss();
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
		}
	}
}