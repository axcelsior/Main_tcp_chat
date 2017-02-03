package Shared;
import java.io.Serializable;

import org.json.simple.JSONObject;

public class ChatMessage implements Serializable{
		
	private JSONObject obj = new JSONObject();
		
	public ChatMessage(String sender, String command, String parameters){
		obj.put("sender", sender);
		obj.put("command", command);
		obj.put("parameters", parameters);
		obj.put("timestamp", System.currentTimeMillis());
	}

	public String getCommand(){
		return (String)obj.get("command");	
	}
	public String getSender(){
		return (String)obj.get("sender");
	}
	public String getParameters(){
		return (String)obj.get("parameters");	
	}
	
	public String getTimeStamp(){
		return obj.get("timestamp").toString();
	}
}
