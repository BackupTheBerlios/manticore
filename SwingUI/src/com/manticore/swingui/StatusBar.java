package com.manticore.swingui;

import java.awt.GridLayout;
import java.util.logging.Logger;
import javax.swing.*;

public class StatusBar extends JPanel {
    private static StatusBar instance;
	JTextField textField;
	
	public StatusBar() {
        setLayout(new GridLayout(1,1, 0, 0));
        textField=new JTextField();
		add(textField);
	}

    public static StatusBar getInstance() {
        if (instance==null) instance=new StatusBar();
        return instance;
    }


	public void setText(String s) {
        Logger.getAnonymousLogger().fine( s );
		textField.setText(s);
	}
}
