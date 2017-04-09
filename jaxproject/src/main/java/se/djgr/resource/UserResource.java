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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import static se.djgr.model.UserParser.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import se.djgr.sql.model.Team;
import se.djgr.sql.model.User;
import se.djgr.sql.model.WorkItem;
import se.djgr.sql.sequrity.ServiceException;
import se.djgr.sql.service.TeamService;
import se.djgr.sql.service.UserService;
import se.djgr.sql.service.WorkItemService;

@Component
@Path("/users")
public final class UserResource {

	private static final String HEADERJSON = "application/json";
	private static final String HEADERACCEPT = "Accept";
	private static final String HEADERCONTENTTYPE = "Content-Type";
	private static final String usernameParameter = "username";
	private static final String jsonKeyInactivateUser = "inactivateUser";
	private static final String jsonKeyDoneWorkItem = "doneWithWorkItem";
	private UserService userService;
	private TeamService teamService;
	private WorkItemService workItemService;

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpHeaders headers;

	public UserResource(UserService userService, TeamService teamService, WorkItemService workItemService) {
		this.userService = userService;
		this.teamService = teamService;
		this.workItemService = workItemService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(String userString) throws ServiceException, JSONException {
		if (jsonHeaderContentType()) {
			User user = userService.createNewUser(fromJsonToUser(userString));
			return location(user);
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@GET
	@Path("{userNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("userNumber") String userNumber) {
		User user = userService.getUserByUserNumber(userNumber);

		if (jsonHeaderAccept()) {
			return user == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(user).build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
	}

	@GET
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByUsername() {
		Map<String, List<String>> parameters = uriInfo.getQueryParameters();

		for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
			if (parameter.getKey().equals(usernameParameter)) {
				if (parameter.getValue().get(0) != null) {
					User user = userService.getUserByUsername(parameter.getValue().get(0));
					if (jsonHeaderAccept()) {
						return user == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(user).build();
					} else {
						return Response.status(Status.NOT_ACCEPTABLE).build();
					}
				}
			}
		}

		return Response.status(Status.UNAUTHORIZED).build();
	}

	@GET
	@Path("team/{teamNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsersOfTeam(@PathParam("teamNumber") String teamNumber) throws JsonProcessingException {
		Team team = teamService.getByTeamNumber(teamNumber);
		List<User> usersOfTeam = userService.getUsersOfTeam(team);

		if (jsonHeaderAccept()) {
			String users = fromListToJson(usersOfTeam);
			return users == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(users).build();
		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@PUT
	@Path("update/{userNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@PathParam("userNumber") String userNumber, String userUpdateString)
			throws ServiceException, JSONException{
		User retrievedUser = userService.getUserByUserNumber(userNumber);

		if (jsonHeaderContentType()) {
			User user = userService.updateUser(retrievedUser.getId(), fromJsonToUpdatedUsername(userUpdateString));

			return Response.ok("Username updated from: " + retrievedUser.getUsername() + " to: " + user.getUsername()).build();
		} else {
			return Response.status(Status.UNAUTHORIZED).build();
		}

	}

	@PUT
	@Path("inactivate/{userNumber}")
	@Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	public Response inactivateUser(@PathParam("userNumber") String userNumber, String inactivateString) throws ServiceException, JSONException {
		User user = userService.getUserByUserNumber(userNumber);

		if (jsonHeaderContentType()) {
			boolean inactivate = fromJsonToBoolean(inactivateString, jsonKeyInactivateUser);
			if (inactivate) {
				userService.inactivateUser(user);

				return user == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(user.getUsername() + " inactivated").build();
			} else if (inactivate == false) {
				return Response.ok("No changes to user has been made").build();
			} else {
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
	}

	@PUT
	@Path("{userNumber}/workItem/{workItemNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doneWithWorkItem(@PathParam("userNumber") String userNumber, @PathParam("workItemNumber") String workItemNumber, String workItemString) throws ServiceException, JSONException {
		User user = userService.getUserByUserNumber(userNumber);
		WorkItem workItem = workItemService.getByWorkItemNumber(workItemNumber);

		if (jsonHeaderContentType()) {
			boolean workItemDone = fromJsonToBoolean(workItemString, jsonKeyDoneWorkItem);
			if (workItemDone) {
				userService.doneWithWorkItem(user, workItem);
				return Response.ok("WorkItem updated").build();
			} else {
				return Response.status(Status.UNAUTHORIZED).build();
			}
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}
	
	private Response location(User user) {
		URI location = uriInfo.getAbsolutePathBuilder().path(user.getUserNumber()).build();
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
