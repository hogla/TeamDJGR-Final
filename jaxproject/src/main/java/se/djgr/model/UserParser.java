package se.djgr.model;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.djgr.sql.model.User;

public final class UserParser {

	public static User fromJsonToUser(String jsonBody) throws JSONException {
		JSONObject json = new JSONObject(jsonBody);
		String userNumber = json.getString("userNumber");
		String username = json.getString("username");

		return new User(userNumber, username);
	}

	public static String fromJsonToUpdatedUsername(String updateString) throws JSONException {
		JSONObject json = new JSONObject(updateString);
		String username = json.getString("username");

		return username;
	}

	public static String fromListToJson(List<User> users) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(users);

		return json;
	}

	public static boolean fromJsonToBoolean(String body, String jsonKey) throws JSONException {
		JSONObject json = new JSONObject(body);
		boolean jsonValue = json.getBoolean(jsonKey);

		return jsonValue;
	}

}
