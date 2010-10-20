package com.manticore.swingui;

import java.util.logging.Logger;
import javax.swing.*;
import java.net.URL;

public class ToolBar extends JToolBar {

    static String i18n = "i18n_Action_";

    public ToolBar() {
        super(JToolBar.VERTICAL);
        String ButtonStr[] = {"exit", "print", "help", "--", "add", "edit", "cancel", "delete"};
        initToolBar(ButtonStr);
    }

    public ToolBar(String[] ButtonStr) {
        super(JToolBar.VERTICAL);
        initToolBar(ButtonStr);
    }

    private void initToolBar(String[] ButtonStr) {
        JButton b;
        URL ImageURL;
        for (int i = 0; i < ButtonStr.length; i++) {

            if (ButtonStr[i].equals("--")) {
                addSeparator();
            } else {
                b = new JButton();
                b.setToolTipText(GlobalProperties.getInstance().localizeStr(i18n + ButtonStr[i]));
                b.setActionCommand(ButtonStr[i]);
                b.addActionListener(GlobalProperties.getInstance().getActionListener());
                try {
                    ImageURL = getClass().getResource("/com/manticore/swingui/icons/" + ButtonStr[i] + ".png");
                    ImageIcon imageIcon = new ImageIcon(ImageURL);
                    b.setIcon(imageIcon);
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).warning("FIXME: ImageIcon " + "/com/manticore/swingui/icons/" + ButtonStr[i] + ".png not found.");
                }                
                add(b);
            }

        }
    }
}
