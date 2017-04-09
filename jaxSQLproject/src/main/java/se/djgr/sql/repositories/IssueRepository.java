package se.djgr.sql.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.djgr.sql.model.Issue;
import se.djgr.sql.model.WorkItem;

public interface IssueRepository extends CrudRepository<Issue, Long> {

	@Query("select w from WorkItem w join fetch w.issue")
	List<WorkItem> getWorkItemsWithIssues();

	@Query("select e from #{#entityName} e where e.issueNumber like :iNumber")
	Issue findByIssueNumber(@Param(value = "iNumber")String issueNumber);

}
