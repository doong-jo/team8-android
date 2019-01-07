/*
 * Copyright (c) 1/3/19 8:14 PM
 * Written By Sungdong Jo
 */

package com.helper.helper.model;


import java.util.List;

public class MemberList {

    public static final String KEY_INDEX = "index";
    public static final String KEY_MEMBERS = "members";
    public static final String KEY_MASTER = "master";

    private List<String> m_names;
    private String m_masterMemberName;
    private String m_index;

    public MemberList(List<String> m_names, String m_masterMemberName, String index) {
        this.m_names = m_names;
        this.m_masterMemberName = m_masterMemberName;
        this.m_index = index;
    }

    public void setMembers(List<String> m_members) {
        this.m_names = m_members;
    }

    public void setMasterMemberName(String m_masterMemberName) {
        this.m_masterMemberName = m_masterMemberName;
    }

    public List<String> getNames() {
        return m_names;
    }

    public String getMasterMemberName() {
        return m_masterMemberName;
    }

    public String getIndex() { return m_index; }
}
