package com.manticore.swingui;

import java.awt.event.ActionListener;
import javax.swing.*;

public class MenuBar extends JMenuBar {

    static String i18n_Menu = "i18n_Menu_";
    static String i18n_Action = "i18n_Action_";

    public MenuBar(ActionListener actionListener) {
        String MenuStr[] = {"File", "Record", "Help"};
        String MenuItemStr[][] = {
            {"new", "open", "print", "exit"}, {"add", "edit", "cancel", "delete"}, {"index", "search", "--", "about"}
        };
        initMenuBar(actionListener, MenuStr, MenuItemStr);
    }

    public MenuBar(ActionListener actionListener, String[] MenuStr, String[][] MenuItemStr) {
        initMenuBar(actionListener, MenuStr, MenuItemStr);
    }

    private void initMenuBar(ActionListener actionListener, String[] MenuStr, String[][] MenuItemStr) {

        JMenu m[] = new JMenu[MenuStr.length];
        JMenuItem mi;

        for (int c = 0; c < MenuStr.length; c++) {
            m[c] = new JMenu(GlobalProperties.getInstance().localizeStr(i18n_Menu + MenuStr[c]));

            for (int r = 0; r < MenuItemStr[c].length; r++) {

                if (MenuItemStr[c][r].equals("--")) {
                    m[c].addSeparator();
                } else {
                    mi = m[c].add(GlobalProperties.getInstance().localizeStr(i18n_Action + MenuItemStr[c][r]));
                    mi.addActionListener(actionListener);
                    mi.setActionCommand(MenuItemStr[c][r]);
                }
            }

            add(m[c]);
        }

    }
}
