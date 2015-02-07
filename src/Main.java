package src;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main {

	static ArrayList <AppAndroidManifest> manifests;
	static ArrayList <Packages> packageNames;
	
	public static void main(String[] args) {
		manifests = new ArrayList<AppAndroidManifest>();
		packageNames = new ArrayList<Packages>();
		
		File[] files = new File(
				"E:/GitHub/AndroSec/VersionControlExtractor/datarepo/datascan_2_3_2015/mainOutput")
				.listFiles();
		int cnt = 0, cnt2 = 0, totalCommits = 0;
		for (File file : files) {
			if (file.isDirectory()) {
				System.out.println("Directory " + cnt + ": " + file.getName());
				cnt++;
				cnt2 = 0;

				System.out.println("Directory: " + file.getAbsolutePath());

				File[] subDir = new File(file.getAbsolutePath()).listFiles();
				for (File subDirs : subDir) {
					if (file.isDirectory()) {
						cnt2++;
						totalCommits++;
						System.out.println("-- Commit Directory " + cnt2
								+ " : " + subDirs.getName());
						// get AndroidManifest.xml
						parseXML(subDirs.getAbsoluteFile());
					}
				}
				System.out.println("Commit Directories: " + cnt2);

				if (cnt > 5)break; // limit size for testing
			}
		}
		System.out.println("Total Commits to Analyze: " + totalCommits);
		System.out.println("Average Commits per App: " + ((double)totalCommits / cnt));
		System.out.println("Total Manifests Saved: " + manifests.size());
		
		saveManifestsToDB();
		
	}

	static void parseXML(File xmlFile) {
		try {

			File fXmlFile = new File(xmlFile.getAbsolutePath()
					+ "/AndroidManifest.xml");

			System.out.println("Current File: " + fXmlFile.getAbsolutePath());
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			System.out.println("Commit: " + xmlFile.getName());
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());
			System.out.println("package : "
					+ doc.getDocumentElement().getAttribute("package"));
			
			String strPackageName = doc.getDocumentElement().getAttribute("package");
			
			addPackageNameToRunningList(strPackageName);
			
			System.out.println("android:versionCode : "
					+ doc.getDocumentElement().getAttribute(
							"android:versionCode"));
			System.out.println("android:versionName : "
					+ doc.getDocumentElement().getAttribute(
							"android:versionName"));
			
			AppAndroidManifest myManifest = new AppAndroidManifest("", 
																	doc.getDocumentElement().getAttribute("android:versionCode"), 
																	doc.getDocumentElement().getAttribute("android:versionName"), 
																	strPackageName,
																	xmlFile.getName()
																	);

			NodeList nList = doc.getElementsByTagName("uses-permission");
			System.out.println("Total Permissions : " + nList.getLength());

			System.out
					.println("*****\n---------------------------- PERMISSIONS START");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					myManifest.addPermission(eElement.getAttribute("android:name"));
					System.out.println("Permission : "
							+ eElement.getAttribute("android:name"));
				}
			}
			System.out.println("---------------------------- PERMISSIONS END");

			System.out.println("*****\n---------------------------- ACTIVITY START ");
			
			nList = doc.getElementsByTagName("activity");
			System.out.println("Total Activities : " + nList.getLength());
		
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);		

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
								
					Activity myActivity = new Activity(eElement.getAttribute("android:name"));
					System.out.println("Activity Name: " + myActivity._name);
					
					// process actions
					NodeList ActivityChildren = eElement.getChildNodes();
					for (int x=0; x < ActivityChildren.getLength(); x++){
						Node anNode = ActivityChildren.item(x);		

						if (anNode.getNodeType() == Node.ELEMENT_NODE) {
							Element aElement = (Element) anNode; // this is our intent
							
							NodeList intentChildren = aElement.getChildNodes();
							System.out.println("Number of Actions and Categories: " + intentChildren.getLength());
							
							for (int y=0; y< intentChildren.getLength(); y++){
								Node bNode = intentChildren.item(y);
								if (bNode.getNodeType() == Node.ELEMENT_NODE) {
									Element bElement = (Element) bNode; // this is our intent
									System.out.println("#### NAME: " + bElement.getNodeName() + " : " + bElement.getAttribute("android:name"));
									
									String str = bElement.getAttribute("android:name");
									if (bElement.getNodeName().equals("category")){
										myActivity.addCategory(str);
									} else if (bElement.getNodeName().equals("action")){
										myActivity.addAction(str);
									} else if (bElement.getNodeName().equals("data")){
										// do nothing
									}
								}
							}
						}	
					}
					myManifest.addActivity(myActivity);
					
					//System.out.println("Activity : "+ eElement.getAttribute("android:name"));
				}
			}
			
			System.out.println("---------------------------- ACTIVITY END");
			
			manifests.add(myManifest);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void saveManifestsToDB(){
		String DBLocation="E:/GitHub/AndroSec/VersionControlExtractor/db/AndrosecDatabase.sqlite";
	    Connection c = null;
	    Statement stmt = null;

	    
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:" + DBLocation);
	      System.out.println("Opened database successfully");
	      
	      // START - INSERT ONLY UNIQUE PACKAGE NAMES TO GET UNIQUE APP ID
	      
	      	for (int x=0; x < packageNames.size(); x++){
	      		
	      		Packages myPackage = packageNames.get(x);
	      		
	      		// **** THIS NEEDS TO BE CHANGED TO PULL THE ALREADY EXISTING APPID'S FROM THE ANDROID_MANIFEST_APPINFO TABLE - CURRENTLY EMPTY.
	      		String strSQL = "INSERT INTO Android_Manifest_AppInfo (AppName) VALUES ('" + myPackage.packageName + "');";
				stmt = c.createStatement();
			
			      //stmt.executeUpdate(strSQL);
				PreparedStatement statement = c.prepareStatement(strSQL, Statement.RETURN_GENERATED_KEYS);
	
			    int affectedRows = statement.executeUpdate();
	
			    if (affectedRows == 0) {
			    	throw new SQLException("Creating user failed, no rows affected.");
			    }
	
			    ResultSet generatedKeys = statement.getGeneratedKeys();
			    if (generatedKeys.next()) {
			    	myPackage.appID = (int) generatedKeys.getLong(1);
			    	System.out.println("dbAppID " + myPackage.appID   + " generated for " + myPackage.packageName);
		        } else {
		        	throw new SQLException("Creating user failed, no ID obtained.");
			    }
			  	
			    stmt.close();
	      	}
	      
	      // END - INSERT ONLY UNIQUE PACKAGE NAMES TO GET UNIQUE APP ID	     
	      	
	      	// START - reconcile package names and app id
	      	
	      		for (int x=0; x < manifests.size(); x++){
	      			AppAndroidManifest myManifest = manifests.get(x);
	      			
	      			for (int y=0; y < packageNames.size(); y++){
	      				Packages myPackage = packageNames.get(y);
	      				
	      				if (myManifest._package.equals(myPackage.packageName)){
	      					myManifest.dbAppID = myPackage.appID;
	      					break;
	      				}
	      			}
	      		}
	      	
	      		// dispose of package name list
	      		packageNames = null;
	      		
	      	// END - reconcile package names and app id
	      	
	    
			/*for (int x=0; x < manifests.size(); x++){
				AppAndroidManifest myManifest = manifests.get(x);
				
				//String strSQL = "INSERT INTO Android_Manifest_CommitInfo (Commit_val, Author_name, Author_email";
				String strSQL = "INSERT INTO Android_Manifest_AppInfo (AppName) VALUES ('" + myManifest._package + "');";
				stmt = c.createStatement();
			
			      //stmt.executeUpdate(strSQL);
				PreparedStatement statement = c.prepareStatement(strSQL, Statement.RETURN_GENERATED_KEYS);
	
			    int affectedRows = statement.executeUpdate();
	
			    if (affectedRows == 0) {
			    	throw new SQLException("Creating user failed, no rows affected.");
			    }
	
			    ResultSet generatedKeys = statement.getGeneratedKeys();
			    if (generatedKeys.next()) {
			    	myManifest.dbAppID = (int) generatedKeys.getLong(1);
			    	System.out.println("dbAppID " + myManifest.dbAppID  + " generated for " + myManifest._package);
		        } else {
		        	throw new SQLException("Creating user failed, no ID obtained.");
			    }
			  	
			    stmt.close();
				//c.commit();
				 
			}*/
		c.close();
        } catch(Exception e){
        	System.out.println("ERROR.  Exiting (1)");
        	e.printStackTrace();
        	System.exit(1);
        	// hard exit - something is wrong with the db insert
        }
	}
	
	static void addPackageNameToRunningList(String strPackageName){
		boolean foundFlag=false;
		for (int x=0;x<packageNames.size();x++){
			if (packageNames.get(x).packageName.equals(strPackageName)){
				foundFlag = true;
				break;
			}
		}
		
		if (!foundFlag){
			packageNames.add(new Packages(strPackageName, -1));
		}
	}
	
}
