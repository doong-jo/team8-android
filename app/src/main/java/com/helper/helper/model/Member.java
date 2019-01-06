/*
 * Copyright (c) 1/3/19 8:14 PM
 * Written By Sungdong Jo
 */

package com.helper.helper.model;


public class Member {

    private String m_ridingType;
    private String m_name;

    public Member(String type, String name) {
        super();
        this.m_name = name;
        this.m_ridingType = type;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    public String getType() { return m_ridingType; }

    public void setType(String type) { this.m_ridingType = type; }
}
