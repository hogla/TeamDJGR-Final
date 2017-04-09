package se.djgr.sql.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import se.djgr.sql.model.Status;
import se.djgr.sql.model.Team;
import se.djgr.sql.model.User;
import se.djgr.sql.repositories.TeamRepository;
import se.djgr.sql.repositories.UserRepository;
import se.djgr.sql.sequrity.ServiceException;

@Component
public class TeamService {

	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private final ServiceTransaction executor;

	@Autowired
	public TeamService(TeamRepository teamRepository, UserRepository userRepository, ServiceTransaction exectutor) {
		this.teamRepository = teamRepository;
		this.userRepository = userRepository;
		this.executor = exectutor;
	}

	public Team createNewTeam(Team team) throws ServiceException {
		if (team.getTeamName().length() < 6) {
			throw new ServiceException("Team name is too short");
		}
		
		String teamNumber = UUID.randomUUID().toString();

		try {
			Team generatedTeam = team.setRandomTeamNumber(teamNumber);
			return executor.execute(() -> teamRepository.save(generatedTeam));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not create team: " + team.getTeamName());
		}
	}

	public Team updateTeam(Long teamId, String teamName) throws ServiceException {
		final Team team = teamRepository.findOne(teamId);

		if (teamName.length() < 6) {
			throw new ServiceException("Team name too short");
		}

		team.setTeamName(teamName);

		try {
			return executor.execute(() -> teamRepository.save(team));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not update team: " + team.getTeamName());
		}
	}

	public Team inactivateTeam(Team team) throws ServiceException {
		final Team retrievedTeam = teamRepository.findOne(team.getId());

		if (retrievedTeam.getStatus().equals(Status.INACTIVE)) {
			throw new ServiceException("Team is already inactive");
		}

		retrievedTeam.setStatus(Status.INACTIVE);

		try {
			return executor.execute(() -> teamRepository.save(retrievedTeam));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not inactivate team: " + team.getTeamName());
		}
	}

	public List<Team> getAllTeams() {
		return (List<Team>) teamRepository.findAll();
	}

	public Team getTeam(Long teamId) {
		return teamRepository.findOne(teamId);
	}

	public void addUserToTeam(User user, Team team) throws ServiceException {
		final User retrievedUser = userRepository.findOne(user.getId());

		if (retrievedUser.getTeam() == team | retrievedUser.getTeam() != null) {
			throw new ServiceException("User is already a member of a team");
		}

		final Team retrievedTeam = teamRepository.findOne(team.getId());

		if (!retrievedTeam.getStatus().equals(Status.ACTIVE)) {
			throw new ServiceException("Team must be active");
		}

		final List<User> usersOfTeam = userRepository.findAllByTeam(team);

		if (usersOfTeam.size() >= 10) {
			throw new ServiceException("Team is full. Max 10 users");
		}

		retrievedUser.setTeam(team);
		retrievedUser.setStatus(Status.ACTIVE);

		try {
			executor.execute(() -> userRepository.save(retrievedUser));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not add user to team: " + team.getTeamName());
		}
	}
	
	
	public Team getTeamByName(String teamName) {
		return teamRepository.getByTeamName(teamName);
	}
	
	public Team getByTeamNumber(String teamNumber) {
		return teamRepository.getByTeamNumber(teamNumber);
	}
}
