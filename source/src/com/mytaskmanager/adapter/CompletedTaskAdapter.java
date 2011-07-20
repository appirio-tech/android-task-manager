package com.mytaskmanager.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mytaskmanager.R;


public class CompletedTaskAdapter extends ArrayAdapter<String>{
	
	private Context context;
	private int textViewResourceId;
	private List<String> taskTitles;

	public CompletedTaskAdapter(Context context, int textViewResourceId, List<String> taskTitles) {
		super(context, textViewResourceId, taskTitles);
		this.context = context;
		this.textViewResourceId = textViewResourceId; 
		this.taskTitles = taskTitles;
		
	}
	
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return taskTitles.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return taskTitles.get(position);
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
		String taskTitle =  taskTitles.get(position);

		if (convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(textViewResourceId, null);

			holder = new ViewHolder();				

			holder.taskTitle = (TextView)convertView.findViewById(R.id.TaskListName);
			
			convertView.setTag(holder);

		}else{			
			holder = (ViewHolder)convertView.getTag();		
		}
		if (taskTitle != null){
			holder.taskTitle.setText(taskTitle);			
			holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
		return convertView;
	}	

	static class ViewHolder{		
		TextView taskTitle;		
	}


}
