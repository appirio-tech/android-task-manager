package com.mytaskmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.Tasks.TasksOperations.Move;
import com.google.api.services.tasks.v1.model.Task;
import com.mytaskmanager.R;
import com.mytaskmanager.R.id;
import com.mytaskmanager.adapter.TaskIndentAdapter;
import com.mytaskmanager.controller.ApplicationController;
import com.mytaskmanager.util.Constant;


public class ShowTasks extends Activity{

	private static final String TAG = "MyTaskManager";

	private Tasks taskService;	
	private ApplicationController controller;
	private List<Task> tasksList;
	private final int RESULT_CODE_OK = 1;
	private final int RESULT_CODE_CLEAR_OK = 2;
	private ListView tasksListView;
	private TextView showMessageView;
	private List<TaskEntry> taskEntryList;

	private TreeMap<String, Integer> taskEntryMap;

	private TaskIndentAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		controller = (ApplicationController)getApplication();

		taskService = controller.getTasks();
		setContentView(R.layout.showtask);

		tasksListView = (ListView) findViewById(R.id.Tasks);
		tasksListView.setOnItemClickListener(taskItemClickListener);		

		showMessageView = (TextView) findViewById(R.id.ShowMessage);
		String dfNoteText = getResources().getString(R.string.message_notask);
		dfNoteText = dfNoteText.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		showMessageView.setText(Html.fromHtml(dfNoteText));
		showMessageView.setVisibility(TextView.INVISIBLE);
		tasksListView.setVisibility(TextView.INVISIBLE);

		Button addTask = (Button)findViewById(R.id.AddTask);
		addTask.setOnClickListener(addNewTaskClickListener);

		Button completedTask = (Button)findViewById(R.id.CompletedTask);
		completedTask.setOnClickListener(completedTaskClickListener);

		TextView appTitle = (TextView)findViewById(id.AppTitle);
		appTitle.setText(appTitle.getText() + " - " + controller.getCurrentTasklist().title);

		new LoadActivities().execute();
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
				//gotAccount(true);
				return;
			}
		}
		Log.e(TAG, e.getMessage(), e);
	}
	class LoadActivities extends AsyncTask<Void, Void, com.google.api.services.tasks.v1.model.Tasks> {
		private final ProgressDialog dialog = new ProgressDialog(ShowTasks.this);

		@Override
		protected void onPreExecute() {
			dialog.setMessage(getBaseContext().getResources().getString(R.string.loading_task));
			dialog.show();
		}

		@Override
		protected com.google.api.services.tasks.v1.model.Tasks doInBackground(Void... params) {			
			try {

				return taskService.tasks.list(controller.getCurrentTasklist().id).execute();
			} catch (Exception e) {				
				Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
				return null;
			}
		}

		@Override
		protected void onPostExecute(com.google.api.services.tasks.v1.model.Tasks feed) {
			dialog.dismiss();
			if (feed == null) {
				return;
			}			 
			taskEntryMap = new TreeMap<String, Integer>();
			taskEntryList = new ArrayList<TaskEntry>();
			if (feed.items != null && feed.items.size() > 0){
				for (Task task : feed.items) {				
					TaskEntry taskEntry = new TaskEntry();
					taskEntryList.add(taskEntry);
					taskEntry.setTask(task);				
					taskEntryMap.put(task.id, taskEntryList.size() - 1);
					if (task.parent != null && task.parent.trim().length() > 0){
						TaskEntry parent = taskEntryList.get(taskEntryMap.get(task.parent));
						taskEntry.setPosition(parent.getPosition() + 1);
						parent.addChildens(taskEntry);
						taskEntry.setParent(parent);
					}
				}
			}
			if (taskEntryList.size() > 0){
				showMessageView.setVisibility(TextView.INVISIBLE);
				tasksListView.setVisibility(TextView.VISIBLE);
			}else{
				showMessageView.setVisibility(TextView.VISIBLE);
				tasksListView.setVisibility(TextView.INVISIBLE);
			}
			adapter = new TaskIndentAdapter(ShowTasks.this, R.layout.indent_textview, taskEntryList, checkBoxClickListener, taskClickListener);
			tasksListView.setAdapter(adapter);
		}

	}

	private void addNewTask(){		
		Intent intent = new Intent(this, AddUpdateTask.class);
		intent.putExtra(Constant.MODE, Constant.ADD_TASK_MODE);		
		startActivityForResult(intent, RESULT_CODE_OK);
	}

	private void showTaskDetails(int selectedPosition){
		TaskEntry taskEntry = taskEntryList.get(selectedPosition);
		controller.selectedTask = taskEntry.getTask();
		Intent intent = new Intent(this, AddUpdateTask.class);
		intent.putExtra(Constant.MODE, Constant.SHOW_TASK_MODE);		
		startActivityForResult(intent, RESULT_CODE_OK);
	}

	private void addSubTask(int selectedPosition){
		TaskEntry taskEntry = taskEntryList.get(selectedPosition);
		controller.selectedTask = taskEntry.getTask();
		Intent intent = new Intent(this, AddUpdateTask.class);
		intent.putExtra(Constant.MODE, Constant.ADD_SUB_TASK_MODE);
		intent.putExtra(Constant.PARENT_POSITION_IN_LIST, selectedPosition);
		startActivityForResult(intent, RESULT_CODE_OK);
	}

	private void deleteTasklistAlert(final int selectedPosition){
		try{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getResources().getString(R.string.alert_task_delete))
			.setCancelable(false)
			.setPositiveButton(this.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {		        	   
					dialog.dismiss();
					deleteTask(selectedPosition);
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

	private void deleteTask(int selectedPosition){
		try{
			TaskEntry taskEntry = taskEntryList.get(selectedPosition);
			taskService.tasks.delete(controller.getCurrentTasklist().id, taskEntry.getTask().id).execute();
			new LoadActivities().execute();
			adapter.notifyDataSetChanged();
		}catch (Exception e) {
			// TODO: handle exception			 
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == resultCode){
			switch (resultCode) {
			case RESULT_CODE_OK:
				processTask(data);	
				break;
			case RESULT_CODE_CLEAR_OK:
				removeCompleteTask();	
				break;
			default:
				break;
			}

		}
	}

	private void processTask(Intent data){
		Task task = controller.selectedTask;
		ProgressDialog dialog = new ProgressDialog(ShowTasks.this);
		try {
			dialog.setMessage(this.getResources().getString(R.string.processing_task));
			dialog.show();
			if (data.getExtras().getInt(Constant.MODE) == Constant.ADD_TASK_MODE){
				task = taskService.tasks.insert(controller.getCurrentTasklist().id,task).execute();

			

			}else if (data.getExtras().getInt(Constant.MODE) == Constant.ADD_SUB_TASK_MODE){
				int position = data.getExtras().getInt(Constant.PARENT_POSITION_IN_LIST);

				TaskEntry parent = taskEntryList.get(position);
				task = taskService.tasks.insert(controller.getCurrentTasklist().id, task).execute();

				Move move = taskService.tasks.move(controller.getCurrentTasklist().id, task.id);
				move.parent = parent.getTask().id;
				if (parent.getChildens().size() > 0){
					move.previous = parent.getChildens().get(parent.getChildens().size() -1).getTask().id;
				}
				task = move.execute();

				
			}else{
				task = taskService.tasks.update(controller.getCurrentTasklist().id,task.id, task).execute();					
			}
			new LoadActivities().execute();
			//adapter.notifyDataSetChanged();
			dialog.dismiss();		 

		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			dialog.dismiss();
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
		}
	}

	private OnItemClickListener taskItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			showContextMenu(arg2);
		}
	};

	private OnClickListener taskClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showContextMenu((Integer)v.getTag());
		}
	};

	private void showContextMenu(final int selectedPosition){		
		final CharSequence[] items = getResources().getStringArray(R.array.task_context_menu);		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getResources().getString(R.string.options));		
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				switch (item) {
				case 0:
					showTaskDetails(selectedPosition);
					break;
				case 1:
					addSubTask(selectedPosition);
					break;
				case 2:
					deleteTasklistAlert(selectedPosition);
					break;
				case 3:
					sendSMS(selectedPosition);
					break;
				case 4:
					sendEmail(selectedPosition);
					break;	 
				}				

			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void sendSMS(int selectedPosition){
		TaskEntry taskEntry = taskEntryList.get(selectedPosition);
		Task task = taskEntry.getTask();
		String msg = task.title;
		if (task.notes != null && task.notes.trim().length() > 0){
			msg += " Notes: " + task.notes;
		}
		if (task.due != null && task.due.length()>0 && task.due.indexOf("T") > 0){
			msg += " Due: " + task.due.substring(0, task.due.indexOf("T")); 
		}
		Intent smsIntent  = new Intent(Intent.ACTION_VIEW);
		smsIntent.setType("vnd.android-dir/mms-sms");		
		smsIntent.putExtra("sms_body",msg);
		startActivity(smsIntent);
	}
	
	private void sendEmail(int selectedPosition){
		TaskEntry taskEntry = taskEntryList.get(selectedPosition);
		Task task = taskEntry.getTask();
		String msg = task.title;
		if (task.notes != null && task.notes.trim().length() > 0){
			msg += " Notes: " + task.notes;
		}
		if (task.due != null && task.due.length()>0 && task.due.indexOf("T") > 0){
			msg += " Due: " + task.due.substring(0, task.due.indexOf("T")); 
		}
		Intent emailIntent  = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("text/plain");
		String[] extra = new String[]{""};
		emailIntent.putExtra(Intent.EXTRA_EMAIL, extra);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
		emailIntent.putExtra(Intent.EXTRA_TEXT,msg);
		startActivity(emailIntent);
	}

	private void processTaskSetStatusForChild(TaskEntry taskEntry, String status){
		try{
			Task task = taskEntry.getTask();
			task.status = status;
			if (status == Constant.STATUS_REOPEN){
				task.completed = null;				
			}
			taskService.tasks.update(controller.getCurrentTasklist().id, task.id, task).execute();
			for (TaskEntry child : taskEntry.getChildens()) {
				processTaskSetStatusForChild(child, status);
			}			
		}catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
		}
	}

	private void processTaskSetStatusForParent(TaskEntry taskEntry, String status){
		try{
			TaskEntry parent = taskEntry.getParent();
			if (parent == null){
				return;
			}
			Task task = parent.getTask(); 
			task.status = status;
			if (status == Constant.STATUS_REOPEN){
				task.completed = null;				
			}
			taskService.tasks.update(controller.getCurrentTasklist().id, task.id, task).execute();
			processTaskSetStatusForParent(parent, status);						
		}catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
		}
	}


	private OnClickListener checkBoxClickListener =  new OnClickListener() {

		@Override
		public void onClick(View v) {			
			ProgressDialog dialog = new ProgressDialog(ShowTasks.this);
			dialog.setMessage(getBaseContext().getResources().getString(R.string.processing_task));
			dialog.show();
			String status = Constant.STATUS_REOPEN;
			if (((CheckBox) v).isChecked()) {
				status = Constant.STATUS_COMPLETED; 
			} else {
				processTaskSetStatusForParent((TaskEntry)v.getTag(), status);
			}
			processTaskSetStatusForChild((TaskEntry)v.getTag(),status);
			adapter.notifyDataSetChanged();
			dialog.dismiss();
		}
	};



	public OnClickListener addNewTaskClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {				
				addNewTask();
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
			}
		}
	};

	public OnClickListener completedTaskClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			List<TaskEntry> completedTasks = new ArrayList<TaskEntry>();			
			List<String> completedTaskTitles = new ArrayList<String>();
			for (TaskEntry taskEntry : taskEntryList) {
				if (taskEntry.getTask().status.equalsIgnoreCase(Constant.STATUS_COMPLETED)){
					completedTaskTitles.add(taskEntry.getTask().title);
					completedTasks.add(taskEntry);
				}
			}
			controller.completedTasks = completedTasks;
			controller.completedTaskTitles = completedTaskTitles;

			Intent intent = new Intent(getBaseContext(),ShowCompletedTask.class);
			startActivityForResult(intent, RESULT_CODE_CLEAR_OK);

		}
	};

	private void removeCompleteTask(){
		try {
			taskService.tasks.clear(controller.getCurrentTasklist().id).execute();
			//taskEntryList.removeAll(controller.completedTasks);
			new LoadActivities().execute();
			adapter.notifyDataSetChanged();
		} catch (Exception e) {
			Toast.makeText(this, this.getResources().getString(R.string.error_processing), Toast.LENGTH_LONG).show();
		}
	}	 
}
