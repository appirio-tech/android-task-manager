package com.mytaskmanager.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.api.services.tasks.v1.model.TaskList;
import com.mytaskmanager.R;

public class TaskListAdapter extends ArrayAdapter<TaskList>{
	private Context context;
	private int textViewResourceId;
	private List<TaskList> taskLists;

	public TaskListAdapter(Context context, int textViewResourceId, List<TaskList> taskLists) {
		super(context, textViewResourceId, taskLists);
		this.context = context;
		this.textViewResourceId = textViewResourceId; 
		this.taskLists = taskLists;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return taskLists.size();
	}

	@Override
	public TaskList getItem(int position) {
		// TODO Auto-generated method stub
		return taskLists.get(position);
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
		TaskList channel =  taskLists.get(position);

		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);

			holder = new ViewHolder();				

			holder.taskListName = (TextView)convertView.findViewById(R.id.TaskListName);

			convertView.setTag(holder);

		}else{			
			holder = (ViewHolder)convertView.getTag();		
		}
		if (channel != null){
			holder.taskListName.setText(channel.title);
		}

		return convertView;
	}	

	static class ViewHolder{
		TextView taskListName;
	}

}
