package com.mytaskmanager.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.api.services.tasks.v1.model.Task;
import com.mytaskmanager.TaskEntry;

public class TaskExpandableListAdapter extends BaseExpandableListAdapter {
	/*// Sample data set.  children[i] contains the children (String[]) for groups[i].
	private String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
	private String[][] children = {
			{ "Arnold", "Barry", "Chuck", "David" },
			{ "Ace", "Bandit", "Cha-Cha", "Deuce" },
			{ "Fluffy", "Snuggles" },
			{ "Goldy", "Bubbles" }
	};*/
	
	private List<TaskEntry> taskEntryList = new ArrayList<TaskEntry>();
	
	private Context context;
	public TaskExpandableListAdapter(Context context, List<TaskEntry> taskEntryList){
		
		this.context = context;
		this.taskEntryList = taskEntryList;
	}

	public Object getChild(int groupPosition, int childPosition) {
		//return children[groupPosition][childPosition];
		//return subjectList.get(groupPosition).get(childPosition);
		return ((TaskEntry)taskEntryList.get(groupPosition)).getChildens().get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	public int getChildrenCount(int groupPosition) {
		//return children[groupPosition].length;
		return (taskEntryList.get(groupPosition)).getChildens().size();
	}

	public TextView getGenericView() {
		// Layout parameters for the ExpandableListView
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, 64);

		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		// Center the text vertically
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		// Set the text starting position
		textView.setPadding(36, 0, 0, 0);
		return textView;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		TextView textView = getGenericView();
		textView.setText(((Task)getChild(groupPosition, childPosition)).title);
		textView.setTag(getChildId(groupPosition, childPosition));
		return textView;
		/*ExpandableListView childExpandableListView = new ExpandableListView(context);
		childExpandableListView.setAdapter(new TaskExpandableListAdapter(context, ((TaskEntry)getChild(groupPosition, childPosition)).getChildens()));
		return childExpandableListView;*/
	}

	public Object getGroup(int groupPosition) {
		return (taskEntryList.get(groupPosition));
	}

	public int getGroupCount() {
		return taskEntryList.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		TextView textView = getGenericView();
		textView.setText(((TaskEntry)getGroup(groupPosition)).getTask().title);
		textView.setTag(getGroupId(groupPosition));
		return textView;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	

	public boolean hasStableIds() {
		return false;
	}
	
	
}


