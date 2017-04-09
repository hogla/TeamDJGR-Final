package se.djgr.sql.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Team {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String teamNumber;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(nullable = false, unique = true)
	private String teamName;

	@OneToMany(mappedBy = "team")
	private Collection<User> users;

	protected Team() {
	}

	public Team(String teamNumber, String teamName) {
		this.status = Status.ACTIVE;
		this.teamNumber = teamNumber;
		this.teamName = teamName;
		this.users = new ArrayList<>();
	}

	public Long getId() {
		return id;
	}

	public Status getStatus() {
		return status;
	}

	public String getTeamNumber() {
		return teamNumber;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public Team setRandomTeamNumber(String teamNumber) {
		return new Team(teamNumber, teamName);
	}

	@Override
	public String toString() {
		return String.join(",", id.toString(), status.toString(), teamNumber, teamName);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other instanceof Team) {
			Team otherTeam = (Team) other;
			return teamName.equals(otherTeam.teamName);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 9;
		result += teamName.hashCode() * 37;

		return result;
	}

}
