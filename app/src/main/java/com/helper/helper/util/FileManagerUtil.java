package com.helper.helper.util;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.R;
import com.helper.helper.tracking.TrackingData;
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

public class FileManagerUtil {

    public static void writeTrackingDataInternalStorage(Context context, TrackingData trackingData) throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + R.string.internal_directory;
            String xmlFilePath =  dir + File.separator + R.string.file_name_tracking_data;

            String elemTracking = String.format("%s", R.string.tracking_xml_elem_root);
            String elemMap = String.format("%s", R.string.tracking_xml_elem_map);
            String elemLocation = String.format("%s", R.string.tracking_xml_elem_location);
            String elemLatitude = String.format("%s", R.string.tracking_xml_elem_latitude);
            String elemLongitude = String.format("%s", R.string.tracking_xml_elem_longitude);
            String attrDate = String.format("%s", R.string.tracking_xml_elem_attr_date);
            String attrStartTime = String.format("%s", R.string.tracking_xml_elem_attr_start_time);
            String attrEndTime = String.format("%s", R.string.tracking_xml_elem_attr_end_time);
            String attrDistance = String.format("%s", R.string.tracking_xml_elem_attr_distance);

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if( fileExists ) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                rootElement = doc.createElement(elemTracking);
            }

            /* Make elements start */
            Element mapElement = doc.createElement(elemMap);
            /* Make elements end */

            /* Define attributes start */
            mapElement.setAttribute(attrDate, trackingData.getDate());
            mapElement.setAttribute(attrStartTime, trackingData.getStartTime());
            mapElement.setAttribute(attrEndTime, trackingData.getEndTime());
            mapElement.setAttribute(attrDistance, trackingData.getDistance());
            /* Define attributes end */

            if( trackingData.getLocationData().size() <= 0 ) {
                return;
            }

            for(LatLng currentLat : trackingData.getLocationData()) {
                Element locationElement = doc.createElement(elemLocation);

                Element latitudeElement = doc.createElement(elemLatitude);
                latitudeElement.appendChild(doc.createTextNode(String.format("%f", currentLat.latitude)));

                Element longitudeElement = doc.createElement(elemLongitude);
                longitudeElement.appendChild(doc.createTextNode(String.format("%f", currentLat.longitude)));

                locationElement.appendChild(latitudeElement);
                locationElement.appendChild(longitudeElement);

                mapElement.appendChild(locationElement);
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
        }
        catch (ParserConfigurationException | TransformerException | SAXException pce)
        {
            pce.printStackTrace();
        }
    }

    public static List<TrackingData> readMapDataXML (Context context) throws IOException {
        List<TrackingData> lTrackingData = null;
        try {
            lTrackingData = new ArrayList<>();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + "user_data";
            String xmlFilePath = dir + File.separator + "tracking.xml";

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if (fileExists) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                rootElement = doc.createElement("tracking");
            }

            NodeList maps = doc.getElementsByTagName("map");

            String date = "";
            String startTime = "";
            String endTime = "";
            String distance = "";
            List<LatLng> locations = new ArrayList<LatLng>();

            for (int i = 0; i < maps.getLength(); i++) {
                Node map = maps.item(i);

                date = map.getAttributes().getNamedItem("date").getNodeValue();
                startTime = map.getAttributes().getNamedItem("start_time").getNodeValue();
                endTime = map.getAttributes().getNamedItem("end_time").getNodeValue();
                distance = map.getAttributes().getNamedItem("distance").getNodeValue();

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
