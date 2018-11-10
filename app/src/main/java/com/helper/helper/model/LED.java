/*
 * Copyright (c) 10/11/18 10:32 AM
 * Written by Sungdong Jo
 * Description: LED data class
 */

package com.helper.helper.model;

import org.json.JSONException;
import org.json.JSONObject;


public class LED {
    public static final String LED_TYPE_FREE = "free";
    public static final String LED_TYPE_PREMIUM = "premium";

    private String m_name;
    private String m_creator;
    private int m_downloadCnt;
    private boolean m_bookmared;
    private String m_type;

    public static class Builder {
        private String m_builderName;
        private String m_builderCreator;
        private int m_builderDownloadCnt;
        private boolean m_builderBookmarked;
        private String m_type;

        public Builder() {
            m_builderName = "";
            m_builderCreator = "";
            m_builderDownloadCnt = 0;
            m_builderBookmarked = false;
            m_type = LED_TYPE_FREE;
        }

        public Builder name(String nameStr) {
            this.m_builderName = nameStr;
            return this;
        }

        public Builder creator(String creatorStr) {
            this.m_builderCreator = creatorStr;
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
        m_name = builder.m_builderName;
        m_creator = builder.m_builderCreator;
        m_downloadCnt = builder.m_builderDownloadCnt;
        m_bookmared = builder.m_builderBookmarked;
        m_type = builder.m_type;
    }

    public void setBookmarked(boolean isBookmarked) {
        m_bookmared = isBookmarked;
    }

    public void setDownloadCount(int downloadCnt) {
        m_downloadCnt = downloadCnt;
    }

    public String getName() {
        return m_name;
    }

    public String getCreator() {
        return m_creator;
    }

    public int getDownloadCnt() {
        return m_downloadCnt;
    }

    public boolean getBookmarked() {
        return m_bookmared;
    }

    public JSONObject getTransformLEDToJSON() {
        JSONObject obj = new JSONObject();

        try {
            if( !m_name.equals("")) {
                obj.put("name", m_name);
            }
            if( !m_creator.equals("")) {
                obj.put("creator", m_creator);
            }
            obj.put("downloadcnt", m_downloadCnt);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
