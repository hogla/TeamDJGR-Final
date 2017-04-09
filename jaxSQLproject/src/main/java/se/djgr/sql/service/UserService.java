package se.djgr.sql.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import se.djgr.sql.model.Status;
import se.djgr.sql.model.Team;
import se.djgr.sql.model.User;
import se.djgr.sql.model.WorkItem;
import se.djgr.sql.repositories.UserRepository;
import se.djgr.sql.repositories.WorkItemRepository;
import se.djgr.sql.sequrity.ServiceException;

@Component
public class UserService {

	private final UserRepository userRepository;
	private final WorkItemRepository workItemRepository;
	private final ServiceTransaction executor;

	@Autowired
	public UserService(UserRepository userRepository, WorkItemRepository workItemRepository, ServiceTransaction exeutor) {
		this.userRepository = userRepository;
		this.workItemRepository = workItemRepository;
		this.executor = exeutor;
	}

	public User createNewUser(User user) throws ServiceException {
		
		if (user.getUsername().length() < 10) {
			throw new ServiceException("Username is too short");
		}
		
		String userNumber = UUID.randomUUID().toString();
		try {
			User generatedUser = user.setRandomUserNumber(userNumber);
			return executor.execute(() -> userRepository.save(generatedUser));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not create user");
		}
	}

	public User updateUser(Long userId, String updateUsername) throws ServiceException {
		final User retrievedUser = userRepository.findOne(userId);

		if (retrievedUser == null) {
			throw new ServiceException("User does not exist");
		}

		if (updateUsername.length() < 10) {
			throw new ServiceException("Username too short");
		}

		retrievedUser.setUsername(updateUsername);

		try {
			return executor.execute(() -> userRepository.save(retrievedUser));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not update user");
		}
	}

	public User inactivateUser(User user) throws ServiceException {
		final User retrievedUser = userRepository.findOne(Long.valueOf(user.getId()));

		if (retrievedUser == null) {
			throw new ServiceException("User does not exist");
		}

		if (retrievedUser.getStatus().equals(Status.INACTIVE)) {
			throw new ServiceException("User is already Inactive");
		}

		final List<WorkItem> workItems = workItemRepository.findByUser(retrievedUser);

		for (WorkItem workItem : workItems) {
			workItem.setStatus(Status.UNSTARTED);
		}

		user.setStatus(Status.INACTIVE);

		try {
			return executor.execute(() -> {
				workItemRepository.save(workItems);
				return userRepository.save(user);
			});
		} catch (DataAccessException e) {
			throw new ServiceException("Could not inactivate user");
		}
	}

	public User getUserByUserNumber(String userNumber) {
		return userRepository.findByUserNumber(userNumber);
	}

	public User getUserByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public List<User> getUsersOfTeam(Team team) {
		return userRepository.findAllByTeam(team);
	}
	
	public WorkItem doneWithWorkItem(User user, WorkItem workItem) throws ServiceException {
		final User retrievedUser = userRepository.findOne(user.getId());
		
		if(retrievedUser.getStatus().equals(Status.INACTIVE)) {
			throw new ServiceException("User is not active");
		}
		
		final WorkItem retrievedWorkItem = workItemRepository.findOne(workItem.getId());
		
		if(!retrievedWorkItem.getUser().equals(retrievedUser)) {
			throw new ServiceException("This work item has not been assigned to user: " + user.getUsername());
		}
		
		try {
			return executor.execute(() -> {
				retrievedWorkItem.setStatus(Status.DONE);
				retrievedWorkItem.setDateDone(new Date());
				
				userRepository.save(retrievedUser);
				return workItemRepository.save(retrievedWorkItem);
			});
			
		} catch (DataAccessException e) {
			throw new ServiceException("Could not update work item: " + workItem.getId());
		}
		
	}

}
