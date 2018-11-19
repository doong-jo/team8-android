package com.helper.helper.model;

/** Compatible Collection **/
public class LEDCategory {
    private String m_name;
    private String m_bkgColor;
    private String m_notice;
    private String m_character;

    public static final String KEY_NAME = "name";
    public static final String KEY_BKGCOLOR = "backgroundColor";
    public static final String KEY_NOTICE = "notice";
    public static final String KEY_CHARACTER = "character";

    public LEDCategory(String name, String bkgColor, String notice, String character) {
        super();
        this.m_name = name;
        this.m_bkgColor = bkgColor;
        this.m_notice = notice;
        this.m_character = character;
    }

    public String getName() {
        return m_name;
    }

    public String getBkgColor() {
        return m_bkgColor;
    }

    public String getNotice() {
        return m_notice;
    }

    public String getCharacter() {
        return m_character;
    }
}
