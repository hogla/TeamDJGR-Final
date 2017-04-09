package se.djgr.sql.service;

import java.util.ArrayList;
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
public class WorkItemService {

	private final WorkItemRepository workItemRepository;
	private final UserRepository userRepository;
	private final ServiceTransaction executor;

	@Autowired
	public WorkItemService(WorkItemRepository workItemRepository, UserRepository userRepository,
			ServiceTransaction executor) {
		this.workItemRepository = workItemRepository;
		this.userRepository = userRepository;
		this.executor = executor;
	}

	public WorkItem createWorkItem(WorkItem workItem) throws ServiceException {
		if (workItem.getTask() == null || workItem.getTask().equals("")) {
			throw new ServiceException("Task text must not be empty");
		}

		String workItemNumber = UUID.randomUUID().toString();

		try {
			WorkItem generatedWorkItem = workItem.setWorkItemNumber(workItemNumber);
			return executor.execute(() -> workItemRepository.save(generatedWorkItem));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not create work item");
		}

	}

	public List<WorkItem> getWorkItemsBasedOnStatus(Status status) throws ServiceException {
		if (!(status.equals(Status.ACTIVE) | status.equals(Status.INACTIVE) | status.equals(Status.UNSTARTED))) {
			throw new ServiceException("Invalid status");
		}

		final List<WorkItem> workItems = workItemRepository.findByStatus(status);

		return workItems;
	}

	public List<WorkItem> getWorkItemsBasedOnStatus(String status) throws ServiceException {
		List<WorkItem> workItems = new ArrayList<>();

		if (status.equalsIgnoreCase("Unstarted")) {
			workItems = workItemRepository.findByStatus(Status.UNSTARTED);
		} else if (status.equalsIgnoreCase("Started")) {
			workItems = workItemRepository.findByStatus(Status.STARTED);
		} else if (status.equalsIgnoreCase("Done")) {
			workItems = workItemRepository.findByStatus(Status.DONE);
		} else {
			throw new ServiceException("Invalid status");
		}

		return workItems;
	}

	public List<WorkItem> getWorkItemsOfTeam(Team team) throws ServiceException {
		if (team.getStatus().equals(Status.INACTIVE)) {
			throw new ServiceException("Team is inactive");
		}

		final List<WorkItem> workItemsOfTeam = workItemRepository.findByUserTeam(team);

		return workItemsOfTeam;
	}

	public List<WorkItem> getWorkItemsOfUser(User user) {
		final List<WorkItem> workItemOfUser = workItemRepository.findByUser(user);

		return workItemOfUser;
	}

	public WorkItem getWorkItemBasedOnTask(String text) {
		final WorkItem workItem = workItemRepository.findByTask(text);

		return workItem;
	}

	public void changeStatusOnWorkItem(WorkItem workItem, Status status) throws ServiceException {
		if (!(status.equals(Status.DONE) | status.equals(Status.STARTED) | status.equals(Status.UNSTARTED))) {
			throw new ServiceException("Invalid status");
		}

		final WorkItem retrievedWorkItem = workItemRepository.findOne(workItem.getId());

		retrievedWorkItem.setStatus(status);

		try {
			executor.execute(() -> workItemRepository.save(retrievedWorkItem));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not change status on work item");
		}
	}

	public void deleteWorkItem(WorkItem workItem) throws ServiceException {
		final WorkItem workItemToDelete = workItemRepository.findOne(workItem.getId());

		workItemToDelete.setUser(null);
		workItemToDelete.setIssue(null);

		try {
			workItemRepository.delete(workItem);
		} catch (DataAccessException e) {
			throw new ServiceException("Could not delete work item");
		}
	}

	public void assignWorkItemToUser(WorkItem workItem, User user) throws ServiceException {
		final WorkItem retrievedWorkItem = workItemRepository.findOne(workItem.getId());
		final User retrievedUser = userRepository.findOne(Long.valueOf(user.getId()));

		if (workItem.getTask().equals("") || workItem.getTask() == null) {
			throw new ServiceException("Task text must not be empty");
		}

		if (retrievedUser.getStatus().equals(Status.INACTIVE)) {
			throw new ServiceException("Can not assign work items to users that are inactive");
		}

		if (workItemRepository.findByUser(retrievedUser).size() >= 5) {
			throw new ServiceException("User can not have more than five work items");
		}

		retrievedWorkItem.setUser(retrievedUser);

		try {
			executor.execute(() -> workItemRepository.save(retrievedWorkItem));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not assign this work item");
		}
	}

	public WorkItem getByWorkItemNumber(String workItemNumber) {
		return workItemRepository.findByWorkItemNumber(workItemNumber);
	}

	public void changeStatusOnWorkItem(WorkItem workItem, String status) throws ServiceException {

		switch (status) {
		case "done":
			changeStatusOnWorkItem(workItem, Status.DONE);
			break;
		case "started":
			changeStatusOnWorkItem(workItem, Status.STARTED);
			break;

		case "unstarted":
			changeStatusOnWorkItem(workItem, Status.UNSTARTED);
			break;
		default:
			throw new ServiceException("Invalid status");
		}

	}

}
