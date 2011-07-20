package com.mytaskmanager.adapter;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytaskmanager.R;
import com.mytaskmanager.TaskEntry;
import com.mytaskmanager.util.Constant;


public class TaskIndentAdapter extends ArrayAdapter<TaskEntry>{
	
	private Context context;
	private int textViewResourceId;
	private List<TaskEntry> tasks;
	private OnClickListener checkBoxClickListener;
	private OnClickListener taskClickListener;	

	public TaskIndentAdapter(Context context, int textViewResourceId, List<TaskEntry> tasks, OnClickListener checkBoxClickListener, OnClickListener taskClickListener) {
		super(context, textViewResourceId, tasks);
		this.context = context;
		this.textViewResourceId = textViewResourceId; 
		this.tasks = tasks;
		this.checkBoxClickListener = checkBoxClickListener;
		this.taskClickListener = taskClickListener;
	}
	
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return tasks.size();
	}

	@Override
	public TaskEntry getItem(int position) {
		// TODO Auto-generated method stub
		return tasks.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub		
		ViewHolder holder;
		TaskEntry taskEntry =  tasks.get(position);

		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);

			holder = new ViewHolder();				
			holder.taskContent = (RelativeLayout)convertView.findViewById(R.id.TaskContent);
			holder.taskTitle = (TextView)convertView.findViewById(R.id.TaskListName);
			holder.taskContent.setOnClickListener(taskClickListener);
			
			holder.complete = (CheckBox)convertView.findViewById(R.id.Complete);
			holder.complete.setOnClickListener(checkBoxClickListener);
				
			holder.due = (TextView)convertView.findViewById(R.id.Due);
			holder.notes = (TextView)convertView.findViewById(R.id.Notes);
		
			convertView.setTag(holder);

		}else{			
			holder = (ViewHolder)convertView.getTag();		
		}
		if (taskEntry != null){
			holder.taskTitle.setText(taskEntry.getTask().title);
			if (taskEntry.getTask().due != null && taskEntry.getTask().due.length() > 0 && taskEntry.getTask().due.indexOf("T") > 0){
				holder.due.setVisibility(TextView.VISIBLE);		
				String due = taskEntry.getTask().due.substring(0, taskEntry.getTask().due.indexOf("T"));				
				holder.due.setText(due);			
			}else{
				holder.due.setVisibility(TextView.GONE);
			}
			if (taskEntry.getTask().notes != null && taskEntry.getTask().notes.length() > 0){
				holder.notes.setVisibility(TextView.VISIBLE);
				holder.notes.setText(taskEntry.getTask().notes);			
			}else{
				holder.notes.setVisibility(TextView.GONE);
			}
			if(taskEntry.getTask().status.equals("completed")){
				holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);				
				holder.notes.setPaintFlags(holder.notes.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				holder.due.setPaintFlags(holder.due.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				holder.complete.setChecked(true);
			}else{
				holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & Paint.LINEAR_TEXT_FLAG);
				holder.notes.setPaintFlags(holder.notes.getPaintFlags() & Paint.LINEAR_TEXT_FLAG);
				holder.due.setPaintFlags(holder.due.getPaintFlags() & Paint.LINEAR_TEXT_FLAG);
				holder.complete.setChecked(false);
			}
			
			ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams)holder.complete.getLayoutParams();
			marginLayoutParams.setMargins(taskEntry.getPosition() * Constant.INDENT_WIDTH, marginLayoutParams.topMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
			holder.complete.setLayoutParams(marginLayoutParams);
			holder.taskContent.setTag(position);
			holder.complete.setTag(taskEntry);
		}
		return convertView;
	}	

	static class ViewHolder{
		RelativeLayout taskContent;
		TextView taskTitle;
		TextView due;
		TextView notes;
		CheckBox complete;
	}


}
