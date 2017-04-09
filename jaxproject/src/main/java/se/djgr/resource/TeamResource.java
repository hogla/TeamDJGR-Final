package se.djgr.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.djgr.sql.model.Team;
import se.djgr.sql.model.User;
import se.djgr.sql.sequrity.ServiceException;
import se.djgr.sql.service.TeamService;
import se.djgr.sql.service.UserService;

import static se.djgr.model.TeamParser.*;

import java.net.URI;
import java.util.List;

@Component
@Path("/teams")
public final class TeamResource {

	private final static String HEADERJSON = "application/json";
	private final static String HEADERACCEPT = "Accept";
	private final static String HEADERCONTENTTYPE = "Content-Type";
	private final static String jsonKeyInactivateTeam = "inactivateTeam";
	private final static String jsonKeyAddUser = "addUser";
	private final TeamService teamService;
	private final UserService userService;

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpHeaders headers;

	public TeamResource(TeamService teamService, UserService userService) {
		this.teamService = teamService;
		this.userService = userService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTeam(String teamString) throws ServiceException, JSONException {
		if (jsonHeaderContentType()) {
			Team team = teamService.createNewTeam(fromJsonToTeam(teamString));
			return location(team);
		} else {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTeams() throws JsonProcessingException {
		List<Team> teams = teamService.getAllTeams();

		if (jsonHeaderAccept()) {
			String jsonString = fromListToJson(teams);

			return teams == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(jsonString).build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@GET
	@Path("{teamNumber}")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	public Response getTeam(@PathParam("teamNumber") String teamNumber) {
		Team team = teamService.getByTeamNumber(teamNumber);

		if (jsonHeaderAccept()) {
			return team == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(team).build();
		} else {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	@PUT
	@Path("{teamNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTeam(@PathParam("teamNumber") String teamNumber, String updateTeamString)
			throws ServiceException, JSONException {
		if (jsonHeaderContentType()) {
			Team team = teamService.getByTeamNumber(teamNumber);
			teamService.updateTeam(team.getId(), fromJsonToUpdatedTeamName(updateTeamString));

			return team == null ? Response.status(Status.NOT_FOUND).build() : Response.ok("Team name updated").build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
	}

	@PUT
	@Path("inactivate/{teamNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response inactivate(@PathParam("teamNumber") String teamNumber, String inactivateString)
			throws ServiceException, JSONException {
		Team team = teamService.getByTeamNumber(teamNumber);

		if (jsonHeaderContentType()) {
			boolean inactivate = fromJsonToBoolean(inactivateString, jsonKeyInactivateTeam);
			if (inactivate) {
				Team inactivatedTeam = teamService.inactivateTeam(team);
				return Response.ok(inactivatedTeam.getTeamName() + " inactivated").build();
			} else if (inactivate == false) {
				return Response.ok("No changes has been made").build();
			} else {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@PUT
	@Path("{teamNumber}/activateUser/{userNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUserToTeam(@PathParam("teamNumber") String teamNumber,
			@PathParam("userNumber") String userNumber, String activateBody) throws ServiceException, JSONException {
		Team team = teamService.getByTeamNumber(teamNumber);
		User user = userService.getUserByUserNumber(userNumber);

		if (jsonHeaderContentType()) {
			boolean addUser = fromJsonToBoolean(activateBody, jsonKeyAddUser);
			if (addUser) {
				teamService.addUserToTeam(user, team);
				return Response.ok("User: " + user.getUsername() + " added to Team: " + team.getTeamName()).build();
			} else if (addUser == false) {
				return Response.ok("No user added").build();
			} else {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
		} else {
			return Response.status(Status.UNAUTHORIZED).build();
		}
	}

	private Response location(Team team) {
		URI location = uriInfo.getAbsolutePathBuilder().path(team.getTeamNumber()).build();
		return Response.created(location).build();
	}

	private boolean jsonHeaderContentType() {
		boolean isJsonContentType = HEADERJSON.equals(headers.getHeaderString(HEADERCONTENTTYPE));
		return isJsonContentType;
	}

	private boolean jsonHeaderAccept() {
		boolean isJsonAccept = HEADERJSON.equals(headers.getHeaderString(HEADERACCEPT));
		return isJsonAccept;
	}

}
