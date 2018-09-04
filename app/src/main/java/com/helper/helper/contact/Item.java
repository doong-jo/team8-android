package com.helper.helper.contact;

/**
 * A POJO that contains some properties to show in the list
 *
 * @author marvinlabs
 */
public class Item implements Comparable<Item> {

    private String m_name;
    private String m_phoneNumber;

    public Item(String name, String number) {
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
    public int compareTo(Item other) {
        if ( other.getPhoneNumber() == m_phoneNumber) { return 1; } else { return 0; }
    }
}
