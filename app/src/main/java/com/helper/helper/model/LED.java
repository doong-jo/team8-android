/*
 * Copyright (c) 10/11/18 10:32 AM
 * Written by Sungdong Jo
 * Description: LED data class
 */

package com.helper.helper.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/** Compatible Collection **/
public class LED {
    public static final String LED_TYPE_FREE = "free";
    public static final String LED_TYPE_PREMIUM = "premium";

    public static final String KEY_INDEX = "index";
    public static final String KEY_NAME = "name";
    public static final String KEY_CREATOR = "creator";
    public static final String KEY_CREATE_TIME = "create_time";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_DOWNLOADCNT = "downloadcnt";
    public static final String KEY_TYPE = "type";

    private String m_index;
    private String m_name;
    private String m_creator;
    private Date m_createDate;
    private String m_category;
    private int m_downloadCnt;
    private boolean m_bookmared; // not include collection only use app
    private String m_type;

    public static class Builder {
        private String m_builderIndex;
        private String m_builderName;
        private String m_builderCreator;
        private Date m_builderCreateDate;
        private String m_builderCategory;
        private int m_builderDownloadCnt;
        private boolean m_builderBookmarked;
        private String m_type;

        public Builder() {
            m_builderIndex = "";
            m_builderName = "";
            m_builderCreator = "";
            m_builderCreateDate = null;
            m_builderCategory = "";
            m_builderDownloadCnt = 0;
            m_builderBookmarked = false;
            m_type = LED_TYPE_FREE;
        }

        public Builder index(String indexStr) {
            this.m_builderIndex = indexStr;
            return this;
        }

        public Builder name(String nameStr) {
            this.m_builderName = nameStr;
            return this;
        }

        public Builder createDate(Date date) {
            this.m_builderCreateDate = date;
            return this;
        }

        public Builder creator(String creatorStr) {
            this.m_builderCreator = creatorStr;
            return this;
        }

        public Builder category(String categoryStr) {
            this.m_builderCategory = categoryStr;
            return this;
        }

        public Builder downloadCnt(int donwloadCnt) {
            this.m_builderDownloadCnt = donwloadCnt;
            return this;
        }

        public Builder bookmarked(boolean isbookmarked) {
            this.m_builderBookmarked = isbookmarked;
            return this;
        }

        public Builder type(String type) {
            this.m_type = type;
            return this;
        }

        public LED build() {
            return new LED(this);
        }
    }

    public LED(Builder builder) {
        m_index = builder.m_builderIndex;
        m_name = builder.m_builderName;
        m_creator = builder.m_builderCreator;
        m_createDate = builder.m_builderCreateDate;
        m_category = builder.m_builderCategory;
        m_downloadCnt = builder.m_builderDownloadCnt;
        m_bookmared = builder.m_builderBookmarked;
        m_type = builder.m_type;
    }

    public void setIndex(String ledIndex) { m_index = ledIndex; }

    public void setBookmarked(boolean isBookmarked) {
        m_bookmared = isBookmarked;
    }

    public void setDownloadCount(int downloadCnt) {
        m_downloadCnt = downloadCnt;
    }

    public void setName(String nameStr) { m_name = nameStr; }

    public void setCategory(String categoryStr) { m_category = categoryStr; }

    public void setCreator(String creatorStr) { m_creator = creatorStr; }

    public void setType(String typeStr) { m_type = typeStr; }

    public void setCreateDate(Date date) { m_createDate = date; }

    public String getIndex() {
        return m_index;
    }

    public String getName() { return m_name; }

    public String getCreator() {
        return m_creator;
    }

    public String getCategory() { return m_category; }

    public String getType() { return m_type; }

    public int getDownloadCnt() {
        return m_downloadCnt;
    }

    public boolean getBookmarked() {
        return m_bookmared;
    }

    public Date getCreateDate() { return m_createDate; }

    public JSONObject getTransformLEDToJSON() {
        JSONObject obj = new JSONObject();

        try {
            if( !m_index.equals("")) {
                obj.put(LED.KEY_INDEX, m_index);
            }
            if( !m_name.equals("")) {
                obj.put(LED.KEY_NAME, m_name);
            }
            if( !m_category.equals("") ) {
                obj.put(LED.KEY_CATEGORY, m_category);
            }
            if( !m_creator.equals("")) {
                obj.put(LED.KEY_CREATOR, m_creator);
            }
            obj.put(LED.KEY_DOWNLOADCNT, m_downloadCnt);

            if( !m_type.equals("")) {
                obj.put(LED.KEY_TYPE, m_type);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
