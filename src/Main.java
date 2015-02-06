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

import org.w3c.dom.Document;

public class Main {

	public static void main(String[] args) {
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

				if (cnt > 3)
					break;
			} else {
				System.out.println("File: " + file.getName());
			}
		}
		System.out.println("Total Commits to Analyze: " + totalCommits);

	}

	static void parseXML(File xmlFile) {
		try {

			File fXmlFile = new File(xmlFile.getAbsolutePath()
					+ "/AndroidManifest.xml");// new
												// File("/Users/mkyong/staff.xml");

			System.out.println("Current File: " + fXmlFile.getAbsolutePath());
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
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

			NodeList nList = doc.getElementsByTagName("uses-permission");
			System.out.println("Total Permissions : " + nList.getLength());

			System.out
					.println("*****\n---------------------------- PERMISSIONS START");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					System.out.println("android:name : "
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
					System.out.println("android:name : "+ eElement.getAttribute("android:name"));
				}
			}
			
			System.out.println("---------------------------- ACTIVITY END");
			
			System.out.println("*****\n---------------------------- ACTION START ");
			
			nList = doc.getElementsByTagName("activity");
			System.out.println("Total Activity : " + nList.getLength());
			
			for (int i=0; i<nList.getLength();i++){
				NodeList iList = nList.item(i).getChildNodes();
				System.out.println("Total Children : " + iList.getLength());
				//
				for (int c=0;c<iList.getLength();c++){
					Node nNode = iList.item(c);	
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						NodeList myIntents = eElement.getElementsByTagName("intent-filter");
						System.out.println("android:name : "+ eElement.getElementsByTagName("intent-filter").item(0).getTextContent());
					}
				}
			}
		
			System.out.println("---------------------------- ACTION END");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
