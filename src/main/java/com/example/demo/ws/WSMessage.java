package com.example.demo.ws;

import org.json.JSONObject;

public class WSMessage {

    private String jsonString = null;
    private String command = null;
    private String body = null;

    public WSMessage(String command, String body) {
        this.command = command;
        this.body = body;
    }

    public WSMessage(String jsonString) {
        this.jsonString = jsonString;

        JSONObject o = new JSONObject(jsonString);
        command = o.optString("command");
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getCommand() {
        return command;
    }

    public String getBody() {
        return body;
    }

    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        o.put("command",command);
        o.put("body",body);
        return o;
    }

    public String toJSONString(){
        return toJSON().toString();
    }
}
