package fr.enseeiht.superjumpingsumokart.application.autopilot;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Xml;


// import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MovementRegistry {

    // Status if the movements are recorded
    private static boolean isRecording = false;
    
    // Filename where movements are stored
    private static String fileName;

    public static void startRecord() {
        isRecording = true;
    }

    public static void stopRecord() {
        isRecording = false;
    }

    public static boolean getRecordingStatus() {
        return isRecording;
    }

    public static void setFileName(String fn) {
        fileName = fn;
    }


    // Function that create file to save movements
    public static void createFileXML(){
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.TRUE);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "root").text("").endTag(null, "root");
            serializer.flush();
            fos.close();

        } catch (Exception e) {
            Log.d("log_main_error_initFile", e.getMessage());
        }
    }

    public static void saveMovement(String mvmt) {
        String timestamp = null;
        try {

            long tsLong = System.currentTimeMillis() / 1000;
            timestamp = Long.toString(tsLong);

            File inputFile = new File(fileName);

            // finding the root tag to add movement inside
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(inputFile);
            Node root = document.getElementsByTagName("root").item(0); // only one root

            // creating the movement
            Element movement = document.createElement("movement");

            Element time = document.createElement("timestamp");
            time.appendChild(document.createTextNode(timestamp));

            // adding the mv value to the movement
            Element mv = document.createElement("mv");
            mv.appendChild(document.createTextNode(mvmt));

            movement.appendChild(time);
            movement.appendChild(mv);

            // adding the movement to the root element
            root.appendChild(movement);

            // finalisation to save the file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);

        } catch (Exception e) {
            Log.d("log_MODIFS_err_addMov", e.getMessage());
        }
        Log.d("log_MODIFS_addMv", "mvmt added : " + mvmt + "--" + timestamp);


    }

    // Function that read the file and return the movements list
    public static ArrayList<String> getMovementList() { 
        ArrayList<String> mvmtList = new ArrayList<>();

        File inputFile = new File(fileName);

        // finding the root tag to add measure inside
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(inputFile);
            Node root = document.getElementsByTagName("root").item(0); // only one root
            NodeList movements = root.getChildNodes();

            for (int i = 0; i < movements.getLength(); i++) {
                NodeList currM = movements.item(i).getChildNodes();

                NodeList timestamp = currM.item(0).getChildNodes();
                NodeList mvmt = currM.item(1).getChildNodes();

                String timeVal = timestamp.item(0).getNodeValue();
                String mvmtVal = mvmt.item(0).getNodeValue();

                String mouvementString = timeVal + "-" + mvmtVal;

                mvmtList.add(mouvementString);
            }

        } catch (Exception e) {
            Log.d("log_MODIFS_err_getList", e.getMessage());
        }
        Log.d("log_MODIFS_list", String.valueOf(mvmtList));

        return mvmtList;
    }

}
