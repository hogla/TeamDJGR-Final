package se.djgr.sql.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class WorkItem {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable = false)
	private Date dateCreated;

	@Column
	private Date dateDone;
	
	@Column(unique = true)
	private String workItemNumber;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(nullable = false, unique = true)
	private String task;

	@ManyToOne
	private User user;

	@OneToOne
	@JoinColumn(unique = true)
	private Issue issue;

	protected WorkItem() {}

	public WorkItem(String workItemNumber, String task) {
		this.workItemNumber = workItemNumber;
		this.task = task;
		this.status = Status.UNSTARTED;
		this.dateCreated = new Date();
		this.dateDone = null;
		this.user = null;
	}

	public Long getId() {
		return id;
	}
	
	public String getWorkItemNumber() {
		return workItemNumber;
	}

	public Date getDateCreated() {
		return dateCreated;
	}
	
	public Date getDateDone() {
		return dateDone;
	}

	public Status getStatus() {
		return status;
	}

	public String getTask() {
		return task;
	}

	public User getUser() {
		return user;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}
	
	public void setDateDone(Date dateDone) {
		this.dateDone = dateDone;
	}
	
	public WorkItem setWorkItemNumber(String workItemNumber) {
		return new WorkItem(workItemNumber, task);
	}

	@Override
	public String toString() {
		return String.format("%s, %s, %s, %s, &s, %s", id, dateCreated, dateDone, status, user, task);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof WorkItem) {
			WorkItem otherWorkItem = (WorkItem) other;
			return task.equals(otherWorkItem.task);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 9;
		result += task.hashCode() * 37;

		return result;
	}

}
