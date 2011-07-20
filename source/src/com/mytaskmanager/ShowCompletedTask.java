package com.mytaskmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.mytaskmanager.R;
import com.mytaskmanager.adapter.CompletedTaskAdapter;
import com.mytaskmanager.controller.ApplicationController;

public class ShowCompletedTask extends Activity{

	private ApplicationController controller;

	private int RESULT_CODE_CLEAR_OK = 2;
	private int RESULT_CODE_CANCEL = 3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_completedtask);
		controller = (ApplicationController)getApplication();
		List<String> completedTaskTitles = new ArrayList<String>();
		completedTaskTitles = controller.completedTaskTitles;		

		ListView completedTasksView = (ListView)findViewById(R.id.Tasks);		
		completedTasksView.setAdapter(new CompletedTaskAdapter(this, R.layout.textview, completedTaskTitles));
		
		Button removeCompletedTask = (Button)findViewById(R.id.RemoveCompletedTask);
		removeCompletedTask.setOnClickListener(removeCompletedTaskClickListener);

		Button cancel = (Button)findViewById(R.id.Cancel);
		cancel.setOnClickListener(cancelClickListener);
		
		if (completedTaskTitles.size() == 0){
			removeCompletedTask.setEnabled(false);
		}
		
		this.setTitle(getResources().getString(R.string.title_completed_tasks));
	}

	private OnClickListener removeCompletedTaskClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub			
			setResult(RESULT_CODE_CLEAR_OK, null);
			finish();
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
}
