package src;

import java.util.ArrayList;

public class Activity {
	
	
	public String _name;
	public ArrayList<String> actions,categories;
	
	public Activity(String ActivityName){
		_name = ActivityName;
		actions = new ArrayList<String>();
		categories = new ArrayList<String>();
	}
	
	public void addAction(String act){
		actions.add(act);
	}
	
	public void addCategory(String cat){
		categories.add(cat);
	}
}
