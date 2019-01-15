/*
 * Copyright (c) 10/15/18 1:50 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.helper.helper.interfaces.ValidateCallback;
import com.helper.helper.model.Accident;
import com.helper.helper.model.ContactItem;
import com.helper.helper.model.LEDCategory;
import com.helper.helper.model.Member;
import com.helper.helper.model.MemberList;
import com.helper.helper.model.TrackingData;
import com.helper.helper.model.User;
import com.snatik.storage.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final String TAG = FileManager.class.getSimpleName() + "/DEV";
    private static final String DIR_NAME = "user_data";

    private static final String CATEGORY_XML_NAME = "category_info.xml";

    private static final String CATEGORY_XML_ELEM_ROOT = "categories";
    private static final String CATEGORY_XML_ELEM = "category";
    private static final String CATEGORY_XML_ELEM_ATTR_NAME = "name";
    private static final String CATEGORY_XML_ELEM_ATTR_BKG = "background_color";
    private static final String CATEGORY_XML_ELEM_ATTR_NOTICE = "notice";
    private static final String CATEGORY_XML_ELEM_ATTR_CHARACTER = "character";

    private static final String EMERGENCY_CONTACTS_XML_NAME = "emergency_contacts.xml";

    private static final String EMERGENCY_CONTACTS_XML_ELEM_ROOT = "contacts";
    private static final String EMERGENCY_CONTACTS_XML_ELEM_CONTACT = "contact";
    private static final String EMERGENCY_CONTACTS_XML_ELEM_ATTR_NAME = "name";
    private static final String EMERGENCY_CONTACTS_XML_ELEM_ATTR_PHONE = "phone";

    private static final String ACCIDENT_XML_NAME = "accident_info.xml";
    private static final String ACCIDENT_XML_ELEM_ROOT = "accidents";
    private static final String ACCIDENT_XML_ELEM = "accident";
    private static final String ACCIDENT_XML_ELEM_ATTR_RIDING_TYPE = "riding_type";
    private static final String ACCIDENT_XML_ELEM_ATTR_HAS_ALERTED="has_alerted";
    private static final String ACCIDENT_XML_ELEM_ATTR_OCCURED_DATE="occured_date";
    private static final String ACCIDENT_XML_ELEM_ATTR_POSITION="position";
    private static final String ACCIDENT_XML_ELEM_ATTR_POSITION_ATTR_LATITUDE="latitude";
    private static final String ACCIDENT_XML_ELEM_ATTR_POSITION_ATTR_LONGITUTDE="longitude";


    private static final String USER_XML_NAME = "user_info.xml";

    private static final String USER_INFO_XML_ELEM_ROOT = "info";
    private static final String USER_INFO_XML_ELEM_USER = "user";
    private static final String USER_INFO_XML_ELEM_ATTR_EMAIL = "email";
    private static final String USER_INFO_XML_ELEM_ATTR_NAME = "name";
    private static final String USER_INFO_XML_ELEM_ATTR_RIDING_TYPE = "riding_type";
    private static final String USER_INFO_XML_ELEM_ATTR_LED_INDICIES = "ledIndicies";
    private static final String USER_INFO_XML_ELEM_ATTR_LED_BOOKMARKED = "ledBookmarked";
    private static final String USER_INFO_XML_ELEM_ATTR_ACC_ENABLED = "acc_enabled";
    private static final String USER_INFO_XML_ELEM_ATTR_ACC_LEVEL = "acc_level";

    private static final String PROFILE_IMG_NAME = "_profile.jpg";
    private static final String PROFILE_IMG_DIR_NAME = "profile_image";

    /** Category **/
    public static void writeXmlCategory(Context context, List<LEDCategory> categoriesData) throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath =  dir + File.separator + CATEGORY_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
//            if( fileExists ) {
//                doc = docBuilder.parse(new File(xmlFilePath));
//                rootElement = (Element) doc.getDocumentElement();
//            } else {
//                rootElement = doc.createElement(EMERGENCY_CONTACTS_XML_ELEM_ROOT);
//            }

            rootElement = doc.createElement(CATEGORY_XML_ELEM_ROOT);

            /* Make elements start */


            for (LEDCategory data :
                    categoriesData) {
                Element contactElement = doc.createElement(CATEGORY_XML_ELEM);
                /* Make elements end */

                /* Define attributes start */
                contactElement.setAttribute(CATEGORY_XML_ELEM_ATTR_NAME, data.getName());
                contactElement.setAttribute(CATEGORY_XML_ELEM_ATTR_BKG, data.getBkgColor());
                contactElement.setAttribute(CATEGORY_XML_ELEM_ATTR_NOTICE, data.getNotice());
                contactElement.setAttribute(CATEGORY_XML_ELEM_ATTR_CHARACTER, data.getCharacter());

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

    public static List<LEDCategory> readXmlCategory(Context context) throws IOException {
        List<LEDCategory> categoriesList = null;
        try {
            categoriesList = new ArrayList<>();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath = dir + File.separator + CATEGORY_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if (fileExists) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                return null;
            }

            NodeList categories = doc.getElementsByTagName(CATEGORY_XML_ELEM);

            for (int i = 0; i < categories.getLength(); i++) {
                Node map = categories.item(i);

                String name = map.getAttributes().getNamedItem(CATEGORY_XML_ELEM_ATTR_NAME).getNodeValue();
                String bkgColor = map.getAttributes().getNamedItem(CATEGORY_XML_ELEM_ATTR_BKG).getNodeValue();
                String notice = map.getAttributes().getNamedItem(CATEGORY_XML_ELEM_ATTR_NOTICE).getNodeValue();
                String character = map.getAttributes().getNamedItem(CATEGORY_XML_ELEM_ATTR_CHARACTER).getNodeValue();

                categoriesList.add(new LEDCategory(name, bkgColor, notice, character));
            }

        } catch (ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }

        return categoriesList;
    }

    /** Accident **/
    public static void writeXmlAccident(Context context, List<Accident> accidentData) throws IOException{
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath =  dir + File.separator + ACCIDENT_XML_NAME;

            Document doc = docBuilder.newDocument();

            Element rootElement;
            rootElement = doc.createElement(ACCIDENT_XML_ELEM_ROOT);

            for (Accident data : accidentData) {
                Element contactElement = doc.createElement(ACCIDENT_XML_ELEM);
                contactElement.setAttribute(ACCIDENT_XML_ELEM_ATTR_RIDING_TYPE, data.getRidingType());
                contactElement.setAttribute(ACCIDENT_XML_ELEM_ATTR_HAS_ALERTED, Boolean.toString(data.getHasAlerted()));
                contactElement.setAttribute(ACCIDENT_XML_ELEM_ATTR_OCCURED_DATE, data.getOccuredDate().toString());

                Element positionElement = doc.createElement(ACCIDENT_XML_ELEM_ATTR_POSITION);
                positionElement.setAttribute(ACCIDENT_XML_ELEM_ATTR_POSITION_ATTR_LATITUDE, Double.toString(data.getPosition().latitude));
                positionElement.setAttribute(ACCIDENT_XML_ELEM_ATTR_POSITION_ATTR_LONGITUTDE, Double.toString(data.getPosition().longitude));
                contactElement.appendChild(positionElement);

                rootElement.appendChild(contactElement);
            }
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
            Log.d(TAG, "writeXmlAccident: \n" + source.getNode().getTextContent());
        }
        catch (ParserConfigurationException | TransformerException pce)
        {
            pce.printStackTrace();
        }
    }

    public static List<Accident> readXmlAccident(Context context) throws IOException{
        List<Accident> accidentList= null;
        try {
            accidentList = new ArrayList<>();
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath = dir + File.separator + ACCIDENT_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            if (fileExists) {
                doc = docBuilder.parse(new File(xmlFilePath));
                rootElement = (Element) doc.getDocumentElement();
            } else {
                return null;
            }

            NodeList accidents = doc.getElementsByTagName(ACCIDENT_XML_ELEM);

            for (int i = 0; i < accidents.getLength(); i++) {
                Node map = accidents.item(i);

                String riding_type = map.getAttributes().getNamedItem(ACCIDENT_XML_ELEM_ATTR_RIDING_TYPE).getNodeValue();
                String occured_date = map.getAttributes().getNamedItem(ACCIDENT_XML_ELEM_ATTR_OCCURED_DATE).getNodeValue();
                Boolean has_alerted = Boolean.getBoolean(map.getAttributes().getNamedItem(ACCIDENT_XML_ELEM_ATTR_HAS_ALERTED).getNodeValue());

                Node mapPosition = map.getChildNodes().item(1);
                LatLng position = new LatLng( Double.parseDouble(mapPosition.getAttributes().getNamedItem(ACCIDENT_XML_ELEM_ATTR_POSITION_ATTR_LATITUDE).getNodeValue()),
                        Double.parseDouble(mapPosition.getAttributes().getNamedItem(ACCIDENT_XML_ELEM_ATTR_POSITION_ATTR_LONGITUTDE).getNodeValue()));

                accidentList.add(Accident.builder()
                        .m_ridingType(riding_type)
                        .m_hasAlerted(has_alerted)
                        .m_occuredDate(occured_date)
                        .m_position(position)
                        .build());
            }

        } catch (ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }

        return accidentList;
    }
    /** Emergency Contacts **/
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

            for (int i = 0; i < contacts.getLength(); i++) {
                Node map = contacts.item(i);

                String name = map.getAttributes().getNamedItem(EMERGENCY_CONTACTS_XML_ELEM_ATTR_NAME).getNodeValue();
                String phoneNumber = map.getAttributes().getNamedItem(EMERGENCY_CONTACTS_XML_ELEM_ATTR_PHONE).getNodeValue();

                contactItems.add(new ContactItem(name, phoneNumber));
            }

        } catch (ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }

        return contactItems;
    }



    /** User **/
    // TODO: 29/10/2018 Insert Profile Bitmap Image in Server
    public static void writeUserProfile(Context context, Bitmap bitmap, String userName) {
        Storage internalStorage = new Storage(context);

        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DIR_NAME + File.separator + PROFILE_IMG_DIR_NAME;
        String imgdir = dir + File.separator +  userName + PROFILE_IMG_NAME;

        File file = new File(dir);
        if(!file.exists()) {
            file.mkdirs();
        }


        File fileCacheItem = new File(imgdir);
        OutputStream out = null;

        try
        {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void writeXmlUserInfo(Context context, User user) throws IOException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Storage internalStorage = new Storage(context);

            String path = internalStorage.getInternalFilesDirectory();
            String dir = path + File.separator + DIR_NAME;
            String xmlFilePath =  dir + File.separator + USER_XML_NAME;

            boolean fileExists = internalStorage.isFileExist(xmlFilePath);

            Document doc = docBuilder.newDocument();

            Element rootElement;
            rootElement = doc.createElement(USER_INFO_XML_ELEM_ROOT);

            Element userElement = doc.createElement(USER_INFO_XML_ELEM_USER);

            /* Define attributes start */
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_EMAIL, user.getUserEmail());
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_NAME, user.getUserName());
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_RIDING_TYPE, user.getUserRidingType());
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_LED_INDICIES, user.getUserLEDIndicies());
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_LED_BOOKMARKED, user.getUserBookmarked());
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_ACC_ENABLED, user.getUserAccEnabled().toString());
            userElement.setAttribute(USER_INFO_XML_ELEM_ATTR_ACC_LEVEL, user.getUserAccLevel());

            rootElement.appendChild(userElement);

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
        }
        catch (ParserConfigurationException | TransformerException pce)
        {
            pce.printStackTrace();
        }
    }

    public static User readXmlUserInfo(Context context) throws  IOException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Storage internalStorage = new Storage(context);
        String path = internalStorage.getInternalFilesDirectory();
        String dir = path + File.separator + DIR_NAME;
        String xmlFilePath = dir + File.separator + USER_XML_NAME;
        boolean fileExists = internalStorage.isFileExist(xmlFilePath);
        Document doc = docBuilder.newDocument();
        Element rootElement;
        if (fileExists) {
            try {
                doc = docBuilder.parse(new File(xmlFilePath)); } catch (SAXException e) {
                e.printStackTrace(); }rootElement = (Element) doc.getDocumentElement(); } else {
            return null; }NodeList userInfo = doc.getElementsByTagName(USER_INFO_XML_ELEM_USER);
        Node map = userInfo.item(0);
        String email = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_EMAIL).getNodeValue();
        String name = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_NAME).getNodeValue();
        String riding_type = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_RIDING_TYPE).getNodeValue();
        String led_indicies = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_LED_INDICIES).getNodeValue();
        String led_bookmarked = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_LED_BOOKMARKED).getNodeValue();
        String acc_enabled = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_ACC_ENABLED).getNodeValue();
        String acc_level = map.getAttributes().getNamedItem(USER_INFO_XML_ELEM_ATTR_ACC_LEVEL).getNodeValue();

        JSONArray led_indices_jarr = null;
        JSONArray led_bookmarked_jarr = null;
        try {
            led_indices_jarr = new JSONArray(led_indicies);
            led_bookmarked_jarr = new JSONArray(led_bookmarked);
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        User user = new User.Builder()
                .email(email)
                .name(name)
                .ridingType(riding_type)
                .ledIndicies(led_indices_jarr)
                .ledBookmarked(led_bookmarked_jarr)
                .accEnabled(acc_enabled)
                .accLevel(acc_level)
                .build();


        return user;
    }

    public static void writeXmlGroupRoom(Context context, List<MemberList> memberData) {

    }

    public static List<MemberList> readXmlGroupRoom(Context context) throws IOException {
        List<MemberList> memberListItems = null;

        return memberListItems;
    }

    public static void writeXmlMembers(Context context, List<Member> memberData) {

    }

    public static List<Member> readXmlMembers(Context context) {
        List<Member> memberItems = null;

        return memberItems;
    }
}
