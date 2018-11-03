package com.helper.helper.controller;

public class ViewStateManager {
    private static int m_tabPos;
    private static int m_bottomNavPos;

    public static void saveTabPosition(int position) {
        m_tabPos = position;
    }

    public static int getSavedTabPosition() {
        return m_tabPos;
    }

    public static void saveBottomNavigationPosition(int position) {
        m_bottomNavPos = position;
    }

    public static int getSavedBottomNavPosition() {
        return m_bottomNavPos;
    }
}
