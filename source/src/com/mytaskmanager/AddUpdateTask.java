package com.mytaskmanager;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.services.tasks.v1.model.Task;
import com.mytaskmanager.R;
import com.mytaskmanager.controller.ApplicationController;
import com.mytaskmanager.util.Constant;

public class AddUpdateTask extends Activity{

	EditText title;
	EditText notes;
	EditText due;
	ImageView clearDate;
	ImageView setDate;

	private int RESULT_CODE_OK = 1;
	private int RESULT_CODE_CANCEL = 2;
	private int mode;
	private int selectedPosition;

	private static final int DATE_DIALOG_ID = 1;


	public ApplicationController controller = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnewtask);

		Button okButton =  (Button)findViewById(R.id.Ok);
		okButton.setOnClickListener(okClickListener);
		Button cancelButton =  (Button)findViewById(R.id.Cancel);
		cancelButton.setOnClickListener(cancelClickListener);

		title = (EditText)findViewById(R.id.Title);		
		notes = (EditText)findViewById(R.id.Notes);
		due = (EditText)findViewById(R.id.Due);
		
		clearDate = (ImageView)findViewById(R.id.ClearDate);
		clearDate.setOnClickListener(clearDateClickListener);

		setDate = (ImageView)findViewById(R.id.SetDate);
		setDate.setOnClickListener(setDateClickListener);

		controller = (ApplicationController)getApplication();

		mode = this.getIntent().getExtras().getInt(Constant.MODE);
		selectedPosition = this.getIntent().getExtras().getInt(Constant.PARENT_POSITION_IN_LIST, -1);

		if (mode == Constant.SHOW_TASK_MODE){
			this.setTitle(getResources().getString(R.string.title_task_details));
			fillDetails();
		}else{
			if (mode == Constant.ADD_TASK_MODE){
				this.setTitle(getResources().getString(R.string.title_add_task));
			}else{
				this.setTitle(getResources().getString(R.string.title_add_sub_task));
			}
			clearDetails();			
		}
		
	}

	private void fillDetails(){
		Task task = controller.selectedTask;
		title.setText(task.title);		
		notes.setText(task.notes);
		if (task.due != null && task.due.trim().length() > 0 && task.due.indexOf("T") > 0){
			due.setText(task.due.substring(0, task.due.indexOf("T")));
		}
	}

	private void clearDetails(){		
		title.setText("");		
		notes.setText("");  
		due.setText("");
	}

	private OnClickListener okClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			addNewTask();
		}
	};

	private OnClickListener clearDateClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			due.setText("");
		}
	};

	private OnClickListener setDateClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showDialog(DATE_DIALOG_ID);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {		
		switch (id) {
		case DATE_DIALOG_ID:
			final Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this,
					mDateSetListener,
					mYear, mMonth, mDay);	
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener =
		new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, 
				int monthOfYear, int dayOfMonth) {			
			updateDateDisplay(year, monthOfYear, dayOfMonth);
		}
	};

	private void updateDateDisplay(int mYear, 
			int mMonth, int mDay) {
		due.setText(
				new StringBuilder()
				.append(mYear).append("-")
				.append(pad(mMonth + 1)).append("-")
				.append(pad(mDay))
				);
	}
	private OnClickListener cancelClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			setResult(RESULT_CODE_CANCEL, null);
			finish();
		}
	};

	private void addNewTask(){

		Intent intent = new Intent();
		intent.putExtra(Constant.MODE, mode);

		if (title.getText().toString() == null || title.getText().toString().trim().length() == 0){
			Toast.makeText(getBaseContext(), "Task Title is mandatory", Toast.LENGTH_LONG).show();
		}else{
			Task task;
			if (mode == Constant.SHOW_TASK_MODE){
				task = controller.selectedTask;
			}else if (mode == Constant.ADD_SUB_TASK_MODE){
				task = new Task();
				task.parent = controller.selectedTask.id;
				intent.putExtra(Constant.PARENT_POSITION_IN_LIST, selectedPosition);
			}else{
				task = new Task();
			}
			task.title = title.getText().toString().trim();
			task.notes = notes.getText().toString().trim();
			if (due.getText() != null && due.getText().toString().trim().length() > 0){
				task.due = due.getText().toString().trim()+"T00:00:00.000Z";
			}else{
				task.due = null;
			}
						
			controller.selectedTask = task;

			setResult(RESULT_CODE_OK, intent);
			finish();
		}
	}
	
	private static String pad(int i) {
		String c = String.valueOf(i);
		if (c.length() == 2)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
}
