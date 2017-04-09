package se.djgr.sql.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.djgr.sql.model.Status;
import se.djgr.sql.model.Team;
import se.djgr.sql.model.User;
import se.djgr.sql.model.WorkItem;

public interface WorkItemRepository extends CrudRepository<WorkItem, Long> {

	List<WorkItem> findByStatus(Status status);

	List<WorkItem> findByStatus(String status);

	List<WorkItem> findByUserTeam(Team team);

	@Query("select e from #{#entityName} e where e.user like :user")
	List<WorkItem> findByUser(@Param(value = "user") User user);

	@Query("select e from #{#entityName} e where e.task like :task")
	WorkItem findByTask(@Param(value = "task") String task);
	
	WorkItem findByWorkItemNumber(String workItemNumber);
	
}
