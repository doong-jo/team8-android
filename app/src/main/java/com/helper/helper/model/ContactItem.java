/*
 * Copyright (c) 10/15/18 2:01 PM
 * Written by Sungdong Jo
 * Description:
 */

package com.helper.helper.model;

/**
 * A POJO that contains some properties to show in the list
 *
 * @author marvinlabs
 */
public class ContactItem implements Comparable<ContactItem> {

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

    /**
     * Comparable interface implementation
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ContactItem other) {
        if ( other.getPhoneNumber() == m_phoneNumber) { return 1; } else { return 0; }
    }
}
