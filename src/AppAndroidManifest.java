package src;

import java.util.ArrayList;

public class AppAndroidManifest {

	public String _appName,_verName, _package, _ver, _commit;
	public int dbAppID;
	public ArrayList<String> Permissions;
	public ArrayList<Activity> Activities;
	
	
	public AppAndroidManifest (String appName, String ver, String verName, String packageName, String commitDirectory){
		_appName = appName;
		_ver = ver;
		_verName = verName;
		_package = packageName;
		dbAppID = -1;
		_commit = commitDirectory;
		
		Activities = new ArrayList<Activity>();
		//IntentActions  = new ArrayList<String>();
		//IntentCategories  = new ArrayList<String>();
		Permissions = new ArrayList<String>();
	}
	
	public void addPermission(String per){
		Permissions.add(per);
	}
	
	public void addActivity(Activity act){
		Activities.add(act);
	}
	
	/*public void addIntentActions(String act){
		IntentActions.add(act);
	}
	
	public void addIntentCategories(String cat){
		IntentCategories.add(cat);
	}
	*/
	
}
