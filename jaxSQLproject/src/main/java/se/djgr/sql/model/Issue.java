package se.djgr.sql.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Issue {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column
	private String issueNumber;

	@Column(nullable = false)
	private String comments;

	@OneToOne(mappedBy = "issue")
	private WorkItem workItem;

	protected Issue() {}

	public Issue(String issueNumber, String comments) {
		this.issueNumber = issueNumber;
		this.comments = comments;
	}

	public Long getId() {
		return id;
	}
	
	public String getIssueNumber() {
		return issueNumber;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public Issue setRandomIssueNumber(String issueNumber) {
		return new Issue(issueNumber, comments);
	}

	@Override
	public String toString() {
		return String.format("%s, %s", id, comments);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof Issue) {
			Issue otherIssue = (Issue) other;
			return comments.equals(otherIssue.comments);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 9;
		result += comments.hashCode() * 37;

		return result;
	}

}
