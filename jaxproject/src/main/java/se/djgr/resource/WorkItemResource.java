package se.djgr.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import se.djgr.sql.model.WorkItem;
import se.djgr.sql.sequrity.ServiceException;
import se.djgr.sql.service.TeamService;
import se.djgr.sql.service.UserService;
import se.djgr.sql.service.WorkItemService;

import static se.djgr.model.WorkItemParser.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Path("/workItems")
public final class WorkItemResource {
	
	private static final String HEADERJSON = "application/json";
	private static final String HEADERACCEPT = "Accept";
	private static final String HEADERCONTENTTYPE = "Content-Type";
	private static final String statusParameter =  "status";
	private static final String taskParameter = "task";
	private static final String jsonKeyAssignWorkItem = "assignWorkItem";
	private static final String jsonKeyDeleteWorkItem = "deleteWorkItem";
	private WorkItemService workItemService;
	private TeamService teamService;
	private UserService userService;

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpHeaders headers;

	public WorkItemResource(WorkItemService workItemService, TeamService teamService, UserService userService) {
		this.workItemService = workItemService;
		this.teamService = teamService;
		this.userService = userService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createWorkItem(String workItemString) throws ServiceException, JSONException {

		if (jsonHeaderContentType()) {
			WorkItem workItem = workItemService.createWorkItem(fromJsonToWorkItem(workItemString));

			return location(workItem);
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{workItemNumber}")
	public Response getByWorkItemNumber(@PathParam("workItemNumber") String workItemNumber) {
		WorkItem workItem = workItemService.getByWorkItemNumber(workItemNumber);
		
		return checkFormat(workItem);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("search")
	public Response getBasedOnStatus() throws ServiceException, JsonProcessingException {

		Map<String, List<String>> parameters = uriInfo.getQueryParameters();
		List<WorkItem> workItemsStatus = new ArrayList<>();

		for (Map.Entry<String, List<String>> parameter : parameters.entrySet()) {
			if (parameter.getKey().equals(statusParameter)) {
				if (parameter.getValue().get(0) != null) {
					workItemsStatus = workItemService.getWorkItemsBasedOnStatus(parameter.getValue().get(0));
					
					return requestedFormat(workItemsStatus);
				}
			}

			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.status(Status.BAD_REQUEST).build();
	}

	@GET
	@Path("team/{teamNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkItemsOfTeam(@PathParam("teamNumber") String teamNumber) throws ServiceException, JsonProcessingException {
		Team team = teamService.getByTeamNumber(teamNumber);
		List<WorkItem> workItems = workItemService.getWorkItemsOfTeam(team);

		return requestedFormat(workItems);
	}

	@GET
	@Path("user/{userNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkItemsOfUser(@PathParam("userNumber") String userNumber)
			throws ServiceException, JsonProcessingException {
		User user = userService.getUserByUserNumber(userNumber);
		List<WorkItem> workItems = workItemService.getWorkItemsOfUser(user);

		return requestedFormat(workItems);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkItemBasedOnTask() {
		Map<String, List<String>> parameters = uriInfo.getQueryParameters();

		for (Map.Entry<String, List<String>> param : parameters.entrySet()) {
			if (param.getKey().equals(taskParameter)) {
				if (param.getValue().get(0) != null) {
					String paramVal = param.getValue().get(0);
					WorkItem workItem = workItemService.getWorkItemBasedOnTask(paramVal);
					return checkFormat(workItem);
				}

			}
		}

		return Response.status(Status.BAD_REQUEST).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{workItemNumber}/assignTo/{userNumber}")
	public Response assignWorkItemToUser(@PathParam("workItemNumber") String workItemNumber, @PathParam("userNumber") String userNumber, String assignBody) throws ServiceException, JSONException {
		WorkItem workItem = workItemService.getByWorkItemNumber(workItemNumber);
		User user = userService.getUserByUserNumber(userNumber);

		if (jsonHeaderContentType()) {
			boolean assignWorkItem = fromJsonToBoolean(assignBody, jsonKeyAssignWorkItem);
			if (assignWorkItem) {
				workItemService.assignWorkItemToUser(workItem, user);
				return Response.ok("Assigned workitem to user").build();
			} else if (assignWorkItem == false) {
				return Response.ok("No changes has been made").build();
			} else {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("changeStatus/{workItemNumber}")
	public Response changeStatusOnWorkItem(@PathParam("workItemNumber") String workItemNumber, String statusBody)
			throws ServiceException, JSONException {
		WorkItem workItem = workItemService.getByWorkItemNumber(workItemNumber);
		if (jsonHeaderContentType()) {
			String status = fromJsonToChangeStatus(statusBody);
			workItemService.changeStatusOnWorkItem(workItem, status);
			return Response.ok("Status on work item has been changed").build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("delete/{workItemNumber}")
	public Response deleteWorkItem(@PathParam("workItemNumber") String workItemNumber, String deleteBody) throws ServiceException, JSONException {
		if (jsonHeaderContentType()) {
			boolean deleteWorkItem = fromJsonToBoolean(deleteBody, jsonKeyDeleteWorkItem);
			
			if(deleteWorkItem) {				
				WorkItem workItem = workItemService.getByWorkItemNumber(workItemNumber);
				workItemService.deleteWorkItem(workItem);
				return Response.ok("Work item deleted").build();
			} else if(deleteWorkItem == false) {
				return Response.ok("No changes has been made").build();
			} else {				
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
		} else {
			return Response.status(Status.UNAUTHORIZED).build();
		}

	}


	private Response checkFormat(WorkItem workItem) {
		if (jsonHeaderAccept()) {
			return workItem == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(workItem).build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	private Response requestedFormat(List<WorkItem> workItems) throws JsonProcessingException {
		if (jsonHeaderAccept()) {
			String jsonList = fromListToJson(workItems);
			return workItems == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(jsonList).build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
	}

	private Response location(WorkItem workItem) {
		URI location = uriInfo.getAbsolutePathBuilder().path(workItem.getWorkItemNumber()).build();
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