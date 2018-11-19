/*
 * Copyright (c) 10/15/18 2:01 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.model;

public class ContactItem {

    private String m_name;
    private String m_phoneNumber;

    public ContactItem(String name, String number) {
        super();
        this.m_name = name;
        this.m_phoneNumber = number;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        this.m_name = name;
    }

    public String getPhoneNumber() {
        return m_phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.m_phoneNumber = phoneNumber;
    }
}
