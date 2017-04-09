package se.djgr.sql.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class User {

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true)
	private String userNumber;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(nullable = false, unique = true)
	private String username;

	@ManyToOne
	private Team team;

	@OneToMany(mappedBy = "user")
	private Collection<WorkItem> workItems;

	protected User() {}

	public User(String userNumber, String username) {
		this.userNumber = userNumber;
		this.username = username;
		this.status = Status.INACTIVE;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}
	
	public String getUserNumber() {
		return userNumber;
	}

	public Status getStatus() {
		return status;
	}

	public Team getTeam() {
		return team;
	}
	
	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public User setRandomUserNumber(String userNumber) {
		return new User(userNumber, username);
	}

	@Override
	public String toString() {
		return String.join(",", id.toString(), userNumber, username, status.toString());
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof User) {
			User otherUser = (User) other;
			return userNumber.equals(otherUser.userNumber) && username.equals(otherUser.username);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 9;
		result += userNumber.hashCode() * 37;
		result += username.hashCode() * 37;

		return result;
	}

}
