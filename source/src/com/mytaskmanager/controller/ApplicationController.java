package com.mytaskmanager.controller;

import java.util.List;

import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.TaskList;
import com.mytaskmanager.TaskEntry;

import android.app.Application;

public class ApplicationController extends Application{

	private TaskList currentTasklist;
	private Tasks tasks;
	
	public TaskList selectedTaskList;
	public Task selectedTask;
	
	public List<TaskEntry> completedTasks;
	public List<String> completedTaskTitles;

	/**
	 * @return the currentTasklist
	 */
	public TaskList getCurrentTasklist() {
		return currentTasklist;
	}

	/**
	 * @param currentTasklist the currentTasklist to set
	 */
	public void setCurrentTasklist(TaskList currentTasklist) {
		this.currentTasklist = currentTasklist;
	}

	/**
	 * @return the tasks
	 */
	public Tasks getTasks() {
		return tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(Tasks tasks) {
		this.tasks = tasks;
	}
}
