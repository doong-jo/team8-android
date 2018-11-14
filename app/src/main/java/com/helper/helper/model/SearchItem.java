package com.helper.helper.model;

public class SearchItem {
    private String m_title;
    private String m_type;

    public SearchItem() {
        super();
    }

    public SearchItem(String m_title, String m_type){
        super();
        this.m_title = m_title;
        this.m_type = m_type;
    }

    public String getTitle(){
        return m_title;
    }

    public void setTitle(String m_title){
        this.m_title = m_title;
    }

    public String getType(){
        return m_type;
    }

    public void setType(String m_type){
        this.m_type = m_type;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
