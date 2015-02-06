package src;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.Document;

public class Main {

	static ArrayList <AppAndroidManifest> manifests;
	
	public static void main(String[] args) {
		manifests = new ArrayList<AppAndroidManifest>();
		
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

				if (cnt > 30)break;
			} else {
				System.out.println("File: " + file.getName());
			}
		}
		System.out.println("Total Commits to Analyze: " + totalCommits);
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

			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());
			System.out.println("package : "
					+ doc.getDocumentElement().getAttribute("package"));
			System.out.println("android:versionCode : "
					+ doc.getDocumentElement().getAttribute(
							"android:versionCode"));
			System.out.println("android:versionName : "
					+ doc.getDocumentElement().getAttribute(
							"android:versionName"));
			
			AppAndroidManifest myManifest = new AppAndroidManifest("", doc.getDocumentElement().getAttribute(
					"android:versionCode"), doc.getDocumentElement().getAttribute(
							"android:versionName"), doc.getDocumentElement().getAttribute(
									"package"));

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
									if (bElement.getNodeName() == "category"){
										myActivity.addCategory(str);
									} else if (bElement.getNodeName() == "action"){
										myActivity.addAction(str);
									} else if (bElement.getNodeName() == "data"){
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
		
	}
	
}
