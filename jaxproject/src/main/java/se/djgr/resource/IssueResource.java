package se.djgr.resource;

import static se.djgr.model.IssueParser.*;

import java.net.URI;
import java.util.List;

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

import se.djgr.sql.model.Issue;
import se.djgr.sql.model.WorkItem;
import se.djgr.sql.sequrity.ServiceException;
import se.djgr.sql.service.IssueService;
import se.djgr.sql.service.WorkItemService;

@Component
@Path("/issues")
public final class IssueResource {

	private static final String HEADERJSON = "application/json";
	private static final String HEADERACCEPT = "Accept";
	private static final String HEADERCONTENTTYPE = "Content-Type";
	private IssueService issueService;
	private WorkItemService workItemService;

	@Context
	private UriInfo uriInfo;

	@Context
	private HttpHeaders headers;

	public IssueResource(IssueService issueService, WorkItemService workItemService) {
		this.issueService = issueService;
		this.workItemService = workItemService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createIssue(String issueString) throws ServiceException, JSONException {
		if (jsonHeaderContentType()) {
			Issue issue = issueService.createIssue(fromJsonToIssue(issueString));
			return location(issue);

		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
	}

	@GET
	@Path("{issueNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getByIssueNumber(@PathParam("issueNumber") String issueNumber) {
		Issue issue = issueService.getByIssueNumber(issueNumber);

		return checkFormat(issue);
	}

	@GET
	@Path("workItems")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorkItemsWithIssue() {
		if (jsonHeaderAccept()) {
			List<WorkItem> workItemsWithIssue = issueService.getAllWorkItemsWithIssues();

			return workItemsWithIssue == null ? Response.status(Status.NOT_FOUND).build()
					: Response.ok(workItemsWithIssue).build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@PUT
	@Path("update/{issueNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateIssue(@PathParam("issueNumber") String issueNumber, String customerUpdateString)
			throws ServiceException, JSONException {
		Issue retrievedIssue = issueService.getByIssueNumber(issueNumber);

		if (jsonHeaderContentType()) {
			issueService.updateIssue(retrievedIssue.getId(), fromJsonToUpdatedIssue(customerUpdateString));

			return Response.ok("Issue comments updated").build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	@PUT
	@Path("{issueNumber}/assignWorkItem/{workItemNumber}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response assignIssueToWorkItem(@PathParam("issueNumber") String issueNumber,
			@PathParam("workItemNumber") String workItemNumber, String assignBody)
			throws ServiceException, JSONException {
		Issue issue = issueService.getByIssueNumber(issueNumber);
		WorkItem workItem = workItemService.getByWorkItemNumber(workItemNumber);

		if (jsonHeaderContentType()) {
			boolean assignIssue = fromJsonToAssignWorkItem(assignBody);

			if (assignIssue) {
				issueService.assignIssueToWorkItem(issue, workItem);
				return Response.ok("Issue added to workItem").build();
			} else if (assignIssue == false) {
				return Response.ok("No changes has been made").build();
			} else {
				return Response.status(Status.UNAUTHORIZED).build();
			}

		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}

	}

	private Response location(Issue issue) {
		URI location = uriInfo.getAbsolutePathBuilder().path(issue.getIssueNumber()).build();
		return Response.created(location).build();
	}

	private Response checkFormat(Issue issue) {
		if (jsonHeaderAccept()) {
			return issue == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(issue).build();
		} else {
			return Response.status(Status.NOT_ACCEPTABLE).build();
		}
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
