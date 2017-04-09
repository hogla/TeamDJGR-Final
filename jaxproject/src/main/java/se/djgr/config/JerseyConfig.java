package se.djgr.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import se.djgr.resource.IssueResource;
import se.djgr.resource.TeamResource;
import se.djgr.resource.UserResource;
import se.djgr.resource.WorkItemResource;

@Component
public final class JerseyConfig extends ResourceConfig {
	
	public JerseyConfig() {
		register(UserResource.class);
		register(TeamResource.class);
		register(WorkItemResource.class);
		register(IssueResource.class);
	}
	
}
