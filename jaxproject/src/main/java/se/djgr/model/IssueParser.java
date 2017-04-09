package se.djgr.model;

import org.json.JSONException;
import org.json.JSONObject;

import se.djgr.sql.model.Issue;

public final class IssueParser {
	
	public static String fromJsonToUpdatedIssue(String updateString) throws JSONException {
		JSONObject json = new JSONObject(updateString);
		String comments = json.getString("comments");

		return comments;
	}
	
	public static Issue fromJsonToIssue(String jsonBody) throws JSONException {
		JSONObject json = new JSONObject(jsonBody);
		String issueNumber = json.getString("issueNumber");
		String comments = json.getString("comments");

		return new Issue(issueNumber, comments);
	}
	
	public static boolean fromJsonToAssignWorkItem(String assignBody) throws JSONException {
		JSONObject json = new JSONObject(assignBody);
		boolean assignIssue = json.getBoolean("assignIssue");
		
		return assignIssue;
	}
}