package se.djgr.model;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.djgr.sql.model.Team;

public final class TeamParser {
	
	public static Team fromJsonToTeam(String jsonBody) throws JSONException {
		JSONObject json = new JSONObject(jsonBody);
		String teamNumber = json.getString("teamNumber");
		String teamName = json.getString("teamName");
		
		return new Team(teamNumber, teamName);
	}
	
	public static String fromJsonToUpdatedTeamName(String updateString) throws JSONException {
		JSONObject json = new JSONObject(updateString);
		String teamName = json.getString("teamName");
		
		return teamName;
	}
	
	public static boolean fromJsonToBoolean(String jsonBody, String jsonKey) throws JSONException {
		JSONObject json = new JSONObject(jsonBody);
		boolean jsonValue = json.getBoolean(jsonKey);
		
		return jsonValue;
	}
	
	public static String fromListToJson(List<Team> teams) throws JsonProcessingException  {
		 ObjectMapper mapper = new ObjectMapper();
		 String json = mapper.writeValueAsString(teams);
		 
		 return json;
	 }
}
