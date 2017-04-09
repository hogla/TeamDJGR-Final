package se.djgr.sql.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import se.djgr.sql.model.Issue;
import se.djgr.sql.model.Status;
import se.djgr.sql.model.WorkItem;
import se.djgr.sql.repositories.IssueRepository;
import se.djgr.sql.repositories.WorkItemRepository;
import se.djgr.sql.sequrity.ServiceException;

@Component
public class IssueService {

	private final IssueRepository issueRepository;
	private final WorkItemRepository workItemRepository;
	private final ServiceTransaction exectutor;

	@Autowired
	public IssueService(IssueRepository issueRepository, WorkItemRepository workItemRepository, ServiceTransaction executor) {
		this.issueRepository = issueRepository;
		this.workItemRepository = workItemRepository;
		this.exectutor = executor;
	}

	public Issue createIssue(Issue issue) throws ServiceException {
		if (issue.getComments() == null || issue.getComments().equals("")) {
			throw new ServiceException("Comments must not be null");
		}
		
		String issueNumber = UUID.randomUUID().toString();

		try {
			Issue generatedIssue = issue.setRandomIssueNumber(issueNumber);
			return exectutor.execute(() -> issueRepository.save(generatedIssue));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not create issue");
		}
	}

	public Issue assignIssueToWorkItem(Issue issue, WorkItem workItem) throws ServiceException {
		final WorkItem retrievedWorkItem = workItemRepository.findOne(workItem.getId());
		final Issue retrievedIssue = issueRepository.findOne(issue.getId());

		if (!retrievedWorkItem.getStatus().equals(Status.DONE)) {
			throw new ServiceException("Workitem is not done");
		}

		if (issue.getComments() == null || issue.getComments().equals("")) {
			throw new ServiceException("Issue comments must not be null");
		}

		retrievedWorkItem.setIssue(retrievedIssue);
		retrievedWorkItem.setStatus(Status.UNSTARTED);

		try {
			return exectutor.execute(() -> {
				workItemRepository.save(retrievedWorkItem);
				return issueRepository.save(retrievedIssue);
			});
		} catch (DataAccessException e) {
			throw new ServiceException("Could not assign issue to work item");
		}

	}

	public Issue updateIssue(Long id, String comments) throws ServiceException {
		if (comments == null || comments.equals("")) {
			throw new ServiceException("Comments must not be empty");
		}

		final Issue updatingIssue = issueRepository.findOne(id);

		updatingIssue.setComments(comments);

		try {
			return exectutor.execute(() -> issueRepository.save(updatingIssue));
		} catch (DataAccessException e) {
			throw new ServiceException("Could not update issue");
		}
	}

	public List<WorkItem> getAllWorkItemsWithIssues() {
		final List<WorkItem> workItemsWithIssues = issueRepository.getWorkItemsWithIssues();

		return workItemsWithIssues;
	}

	public Issue getByIssueNumber(String issueNumber) {
		return issueRepository.findByIssueNumber(issueNumber);
	}

}
