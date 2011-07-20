package com.mytaskmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.services.tasks.v1.model.TaskList;
import com.mytaskmanager.R;
import com.mytaskmanager.controller.ApplicationController;
import com.mytaskmanager.util.Constant;

public class AddUpdateTaskList extends Activity{

	EditText title;
	EditText notes;
	EditText due;
	private int RESULT_CODE_OK = 100;
	private int RESULT_CODE_CANCEL = 200;
			
	private int mode;
	
	public ApplicationController controller = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnewtasklist);
		
		controller = (ApplicationController)getApplication();
		
		Button okButton =  (Button)findViewById(R.id.Ok);
		okButton.setOnClickListener(okClickListener);
		Button cancelButton =  (Button)findViewById(R.id.Cancel);
		cancelButton.setOnClickListener(cancelClickListener);
		
		title = (EditText)findViewById(R.id.Title);
		
		mode = this.getIntent().getExtras().getInt(Constant.MODE, Constant.ADD_TASKLIST_MODE);
		if (mode == Constant.EDIT_TASKLLIST_MODE){
			this.setTitle(getResources().getString(R.string.title_edit_tasklist));
			fillDetails();	
		}else{
			this.setTitle(getResources().getString(R.string.title_create_tasklist));
			clearDetails();
		}
		
	}
	private void fillDetails(){
		TaskList taskList = controller.selectedTaskList;
		title.setText(taskList.title);		
	}

	private void clearDetails(){		
		title.setText("");		
	}
	
	private OnClickListener okClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			addNewTaskList();
		}
	};
	
	private OnClickListener cancelClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			setResult(RESULT_CODE_CANCEL, null);
			finish();
		}
	};
	
	private void addNewTaskList(){
		
		Intent intent = new Intent();
		intent.putExtra(Constant.MODE, mode);
		
		if (title.getText().toString() == null || title.getText().toString().trim().length() == 0){
			Toast.makeText(getBaseContext(), "Tasklist Title is mandatory", Toast.LENGTH_LONG).show();
		}else{			
			TaskList taskList;
			if (mode == Constant.EDIT_TASKLLIST_MODE){
				taskList = controller.selectedTaskList;
			}else{
				taskList = new TaskList();
			}
			taskList.title = title.getText().toString().trim();
			controller.selectedTaskList = taskList;
			setResult(RESULT_CODE_OK, intent);
			finish();
		}
	}
}
