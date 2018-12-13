package edu.gvsu.cis.cis656.client;

import java.util.ArrayList;

public class OutputWriter {

	ArrayList<String> events;
    String username;

    public OutputWriter(String username) {
        this.events = new ArrayList<>();
        this.username = username;
        render();
    }

    private String buildPrompt() {
        String prompt = username;
        prompt += ": ";

        return prompt;
    }

    public void addEvent(String event) {
        events.add(event);
        render();
    }

    private void render() {
 
    	if(events.size() > 0)
    		System.out.println(events.get(events.size()-1));
        System.out.print(buildPrompt());
    }
}
