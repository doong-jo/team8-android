/*
 * Copyright (c) 10/15/18 1:50 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.model.ContactItem;
import com.helper.helper.model.TrackingData;
import com.snatik.storage.Storage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FileManager {

    private static final String DIR_NAME = "user_data";

    private static final String EMERGENCY_CONTACTS_XML_NAME = "emergency_contacts.xml";

    private static final String EMERGENCY_CONTACTS_XML_ELEM_ROOT = "contacts";
    private static final String EMERGENCY_CONTACTS_XML_ELEM_CONTACT = "contact";
    private static final String EMERGENCY_CONTACTS_XML_ELEM_ATTR_NAME = "name";
    private static final String EMERGENCY_CONTACTS_XML_ELEM_ATTR_PHONE = "phone";


    private static final String TRACKING_XML_NAME = "tracking.xml";

    private static final String TRACKING_XML_ELEM_ROOT = "tracking";
    private static final String TRACKING_XML_ELEM_MAP = "map";
    private static final String TRACKING_XML_ELEM_ATTR_DATE = "date";
    private static final String TRACKING_XML_ELEM_ATTR_START_TIME = "start_time";
    private static final String TRACKING_XML_ELEM_ATTR_END_TIME = "end_time";
    private static final String TRACKING_XML_ELEM_ATTR_DISTANCE = "distance";
    private static final String TRACKING_XML_ELEM_LOCATION = "location";
    private static final String TRACKING_XML_ELEM_LATITUDE = "latitude";
    private static final String TRACKING_XML_ELEM_LONGITUDE = "longitude";
    private static final String TAG = FileManager.class.getSimpleName() + "/DEV";


    public static void writeXmlEmergencyContacts(Context context, List<ContactItem> contactsData) throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath =  dir + File.separator + EMERGENCY_CONTACTS_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
//            if( fileExists ) {
//                doc = docBuilder.parse(new File(xmlFilePath));
//                rootElement = (Element) doc.getDocumentElement();
//            } else {
//                rootElement = doc.createElement(EMERGENCY_CONTACTS_XML_ELEM_ROOT);
//            }

            rootElement = doc.createElement(EMERGENCY_CONTACTS_XML_ELEM_ROOT);

            /* Make elements start */


            for (ContactItem data :
                    contactsData) {
                Element contactElement = doc.createElement(EMERGENCY_CONTACTS_XML_ELEM_CONTACT);
                /* Make elements end */

                /* Define attributes start */
                contactElement.setAttribute(EMERGENCY_CONTACTS_XML_ELEM_ATTR_NAME, data.getName());
                contactElement.setAttribute(EMERGENCY_CONTACTS_XML_ELEM_ATTR_PHONE, data.getPhoneNumber());

                rootElement.appendChild(contactElement);
            }

//            if( !fileExists ) {
//                doc.appendChild(rootElement);
//            }
            doc.appendChild(rootElement);

            // XML 파일로 쓰기
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            final boolean dirExists = internalStorage.isDirectoryExists(dir);

            if( !dirExists ) {
                internalStorage.createDirectory(dir);
            }

            StreamResult result = new StreamResult(new FileOutputStream(new File(xmlFilePath), false));

            transformer.transform(source, result);
            Log.d(TAG, "writeXmlEmergencyContacts: \n" + source.getNode().getTextContent());


        }
        catch (ParserConfigurationException | TransformerException pce)
        {
            pce.printStackTrace();
        }
    }

    public static List<ContactItem> readXmlEmergencyContacts(Context context) throws IOException {
        List<ContactItem> contactItems = null;
        try {
            contactItems = new ArrayList<>();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath = dir + File.separator + EMERGENCY_CONTACTS_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if (fileExists) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                return null;
            }

            NodeList contacts = doc.getElementsByTagName(EMERGENCY_CONTACTS_XML_ELEM_CONTACT);

            String name;
            String phoneNumber;

            for (int i = 0; i < contacts.getLength(); i++) {
                Node map = contacts.item(i);

                name = map.getAttributes().getNamedItem(EMERGENCY_CONTACTS_XML_ELEM_ATTR_NAME).getNodeValue();
                phoneNumber = map.getAttributes().getNamedItem(EMERGENCY_CONTACTS_XML_ELEM_ATTR_PHONE).getNodeValue();

                contactItems.add(new ContactItem(name, phoneNumber));
            }

        } catch (ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }

        return contactItems;
    }

    public static void writeXmlTrackingData(Context context, TrackingData trackingData) throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath =  dir + File.separator + TRACKING_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if( fileExists ) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                rootElement = doc.createElement(TRACKING_XML_ELEM_ROOT);
            }

            /* Make elements start */
            Element mapElement = doc.createElement(TRACKING_XML_ELEM_MAP);
            /* Make elements end */

            /* Define attributes start */
            mapElement.setAttribute(TRACKING_XML_ELEM_ATTR_DATE, trackingData.getDate());
            mapElement.setAttribute(TRACKING_XML_ELEM_ATTR_START_TIME, trackingData.getStartTime());
            mapElement.setAttribute(TRACKING_XML_ELEM_ATTR_END_TIME, trackingData.getEndTime());
            mapElement.setAttribute(TRACKING_XML_ELEM_ATTR_DISTANCE, trackingData.getDistance());
            /* Define attributes end */

            if( trackingData.getLocationData().size() <= 0 ) {
                return;
//                Element locationElement = doc.createElement(TRACKING_XML_ELEM_LOCATION);
//
//                Element latitudeElement = doc.createElement(TRACKING_XML_ELEM_LATITUDE);
//                latitudeElement.appendChild(doc.createTextNode(String.format("%f", 36.500881)));
//
//                Element longitudeElement = doc.createElement(TRACKING_XML_ELEM_LATITUDE);
//                longitudeElement.appendChild(doc.createTextNode(String.format("%f", 127.269924)));
//
//                locationElement.appendChild(latitudeElement);
//                locationElement.appendChild(longitudeElement);
//
//                mapElement.appendChild(locationElement);
            }
            else {
                for(LatLng currentLat : trackingData.getLocationData()) {
                    Element locationElement = doc.createElement(TRACKING_XML_ELEM_LOCATION);

                    Element latitudeElement = doc.createElement(TRACKING_XML_ELEM_LATITUDE);
                    latitudeElement.appendChild(doc.createTextNode(String.format("%f", currentLat.latitude)));

                    Element longitudeElement = doc.createElement(TRACKING_XML_ELEM_LONGITUDE);
                    longitudeElement.appendChild(doc.createTextNode(String.format("%f", currentLat.longitude)));

                    locationElement.appendChild(latitudeElement);
                    locationElement.appendChild(longitudeElement);

                    mapElement.appendChild(locationElement);
                }
            }



            rootElement.appendChild(mapElement);

            if( !fileExists ) {
                doc.appendChild(rootElement);
            }

            // XML 파일로 쓰기
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            final boolean dirExists = internalStorage.isDirectoryExists(dir);

            if( !dirExists ) {
                internalStorage.createDirectory(dir);
            }

            StreamResult result = new StreamResult(new FileOutputStream(new File(xmlFilePath), false));

            transformer.transform(source, result);
            Log.d(TAG, "writeXmlTrackingData: \n" + source.getNode().getTextContent());
        }
        catch (ParserConfigurationException | TransformerException | SAXException pce)
        {
            pce.printStackTrace();
        }
    }

    public static List<TrackingData> readXMLTrackingData(Context context) throws IOException {
        List<TrackingData> lTrackingData = null;
        try {
            lTrackingData = new ArrayList<>();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath = dir + File.separator + TRACKING_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if (fileExists) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                rootElement = doc.createElement(TRACKING_XML_ELEM_ROOT);
            }

            NodeList maps = doc.getElementsByTagName(TRACKING_XML_ELEM_MAP);

            String date = "";
            String startTime = "";
            String endTime = "";
            String distance = "";
            List<LatLng> locations = new ArrayList<LatLng>();

            for (int i = 0; i < maps.getLength(); i++) {
                Node map = maps.item(i);

                date = map.getAttributes().getNamedItem(TRACKING_XML_ELEM_ATTR_DATE).getNodeValue();
                startTime = map.getAttributes().getNamedItem(TRACKING_XML_ELEM_ATTR_START_TIME).getNodeValue();
                endTime = map.getAttributes().getNamedItem(TRACKING_XML_ELEM_ATTR_END_TIME).getNodeValue();
                distance = map.getAttributes().getNamedItem(TRACKING_XML_ELEM_ATTR_DISTANCE).getNodeValue();

                NodeList locationList = map.getChildNodes();

                int locationInd = 1;
                String lat = "";
                String log = "";

                Node location;

                for (int j = 1; j < locationList.getLength(); j += 2) {
                    location = locationList.item(j);

                    lat = location.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
                    log = location.getChildNodes().item(3).getChildNodes().item(0).getNodeValue();
                }

                if (locationList.getLength() > 0) {
                    locations.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(log)));
                }

                lTrackingData.add(new TrackingData(date, startTime, endTime, distance, locations));
            }

        } catch (ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }

        return lTrackingData;
    }
}
