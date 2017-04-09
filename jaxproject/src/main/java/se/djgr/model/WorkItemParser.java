package se.djgr.model;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.djgr.sql.model.WorkItem;

public final class WorkItemParser {
	
	public static WorkItem fromJsonToWorkItem(String jsonBody) throws JSONException {
		JSONObject json = new JSONObject(jsonBody);
		String workItemNumber = json.getString("workItemNumber");
		String task = json.getString("task");

		return new WorkItem(workItemNumber, task);
	}
	
	public static String fromListToJson(List<WorkItem> workItems) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(workItems);
		
		return json;
	}
	
	public static String fromJsonToChangeStatus(String statusBody) throws JSONException {
		JSONObject json = new JSONObject(statusBody);
		String status = json.getString("status");
		
		return status;
	}
	
	public static boolean fromJsonToBoolean(String assignBody, String jsonKey) throws JSONException {
		JSONObject json = new JSONObject(assignBody);
		boolean jsonValue = json.getBoolean(jsonKey);
		
		return jsonValue;
	}
	
}