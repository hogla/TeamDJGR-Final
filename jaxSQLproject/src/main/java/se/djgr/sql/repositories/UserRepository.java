package se.djgr.sql.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import se.djgr.sql.model.Team;
import se.djgr.sql.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

	@Query("select e from #{#entityName} e where e.username like :username")
	User findByUsername(@Param(value = "username") String username);

	@Query("select e from #{#entityName} e where e.userNumber like :uNumber ")
	User findByUserNumber(@Param(value = "uNumber") String userNumber);

	@Query("select e from #{#entityName} e where e.team like :team")
	List<User> findAllByTeam(@Param(value = "team") Team team);

}
