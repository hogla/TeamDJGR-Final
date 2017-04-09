package se.djgr.sql.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.djgr.sql.model.Team;

public interface TeamRepository extends CrudRepository<Team, Long> {

	@Query("select e from #{#entityName} e where e.teamName like :teamName")
	Team getByTeamName(@Param(value = "teamName") String teamName);
	
	@Query("select e from #{#entityName} e where e.teamNumber like :tNumber ")
	Team getByTeamNumber(@Param(value = "tNumber") String teamNumber);

}

