package edu.gvsu.cis.cis656.clock;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Assert;

import jdk.nashorn.api.scripting.JSObject;

public class VectorClock implements Clock {

    // suggested data structure ...
    private Map<String,Integer> clock = new Hashtable<String,Integer>();


    @Override
    public void update(Clock other) {
    	VectorClock otherVC = (VectorClock) other;
    	Set<String> keys = otherVC.getClock().keySet();
    	Set<String> thiskeys = clock.keySet();
    	for(String k: keys) {
    		int i = Integer.parseInt(k);
    		//System.out.println("i=  " + i + " " +getTime(i) + " " + otherVC.getTime(i));
    		if(!thiskeys.contains(k)) {
    			addProcess(i, otherVC.getTime(i));
    		}
    		if(getTime(i) < otherVC.getTime(i))
    			addProcess(i, otherVC.getTime(i));
    		
    	}
    }

    @Override
    public void setClock(Clock other) {
    	for (int i = 0; i < clock.size(); i++) {
			addProcess(i, other.getTime(i));
		}
    }

    @Override
    public void tick(Integer pid) {
    	int tempVal = getTime(pid);
    	addProcess(pid, tempVal + 1);
    }

    @Override
    public boolean happenedBefore(Clock other) {
    	VectorClock otherVC = (VectorClock) other;
    	for (int i = 0; i < clock.size(); i++) {
    		if (i < otherVC.getClock().size()) {
    			if(getTime(i) > otherVC.getTime(i)) {
    				return false;
    			}
    		}
    	}
    	
    	return true;
    }

    public String toString() {
    	JSONObject obj = new JSONObject(clock);   	
        return obj.toString();
    }

    @Override
    public void setClockFromString(String clock) {
    	JSONObject map = new JSONObject(clock);
        Iterator<?> keys = map.keys();
        Hashtable<String, Integer> newClock = new Hashtable<>();
        while (keys.hasNext()) {
            String key = (String) keys.next();

            try {
                int value = Integer.parseInt(map.get(key).toString());
                newClock.put(key, value);
            } catch (Exception e) {
                return;
            }
        }
        this.clock = newClock;
    }

    @Override
    public int getTime(int p) {
        return clock.get(String.valueOf(p)) == null ? 0 : clock.get(String.valueOf(p));
    }

    @Override
    public void addProcess(int p, int c) {
    	clock.put(String.valueOf(p), c);
    }

	public Map<String, Integer> getClock() {
		return clock;
	}
    
	public Set<String> getKnownPids() {
		return clock.keySet();
	}
    
}
