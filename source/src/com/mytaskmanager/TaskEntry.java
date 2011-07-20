package com.mytaskmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.tasks.v1.model.Task;

public class TaskEntry implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Task task;
	private List<TaskEntry> childens = new ArrayList<TaskEntry>();
	private int position = 0;
	private TaskEntry parent;
	
	
	/**
	 * @return the task
	 */
	public Task getTask() {
		return task;
	}
	/**
	 * @param task the task to set
	 */
	public void setTask(Task task) {
		this.task = task;
	}
	/**
	 * @return the childens
	 */
	public List<TaskEntry> getChildens() {
		return childens;
	}
	/**
	 * @param childens the childens to set
	 */
	/*public void setChildens(List<Task> childens) {
		this.childens = childens;
	}*/
	public void addChildens(TaskEntry taskEntry) {
		this.childens.add(taskEntry);
	}
	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	/**
	 * @return the parent
	 */
	public TaskEntry getParent() {
		return parent;
	}
	/**
	 * @param parent the parent to set
	 */
	public void setParent(TaskEntry parent) {
		this.parent = parent;
	}
}
