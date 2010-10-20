/*
 *
 *  Copyright (C) 2010 Andreas Reichel <andreas@manticore-projects.com>
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package com.manticore.connection;

import com.manticore.foundation.TanReader;
import com.manticore.util.Settings;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class AccessDialog extends JDialog implements ActionListener {
    private JButton saveButton;
    private JButton loginButton;
    private JButton cancelButton;

    private JTextField username;
    private JTextField password;
    private JTextField tradingPassword;
    private static boolean loggedIn = false;

    public AccessDialog() {
        createControls();
        setVisible(true);
    }

    private void createControls() {
        loginButton = new JButton("Login");
        loginButton.setActionCommand("LOGIN");
        loginButton.setDefaultCapable(true);
        loginButton.addActionListener(this);

        saveButton = new JButton("Save");
        saveButton.setActionCommand("SAVE");
        saveButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);

        username = new JPasswordField("");
        password = new JPasswordField("");
        tradingPassword = new JPasswordField("");

        setModal(true);
        setAlwaysOnTop(true);
        setTitle("Broker Access Settings");

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        GridBagConstraints gridBagConstraints = new GridBagConstraints(0, 0, 1, 1, 0f, 0f, GridBagConstraints.BASELINE, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);

        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/com/manticore/connection/logo.png"));
        gridBagConstraints.gridwidth = 2;
        add(new JLabel(imageIcon), gridBagConstraints);

        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridy = 1;
        add(new JLabel("Account:"), gridBagConstraints);
        gridBagConstraints.gridy = 2;
        add(new JLabel("Password:"), gridBagConstraints);
        gridBagConstraints.gridy = 3;
        add(new JLabel("Trading-Password:"), gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1f;
        add(username, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        add(password, gridBagConstraints);
        gridBagConstraints.gridy = 3;
        add(tradingPassword, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1f;
        gridBagConstraints.weighty = 0f;


        JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        p.add(cancelButton);
        p.add(saveButton);
        p.add(loginButton);
        add(p, gridBagConstraints);

        password.requestFocus();
        pack();

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation( (tk.getScreenSize().width-getWidth()) / 2, (tk.getScreenSize().height-getHeight()) / 2);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("LOGIN")) {
            logIn();
        } else if (e.getActionCommand().equals("CANCEL")) {
            setVisible(false);
        } else if (e.getActionCommand().equals("SAVE")) {
            save();
        }
    }

    private void logIn() {
        loggedIn=true;
        setVisible(false);
    }

    private void save() {
        Settings.getInstance().set("manticore-trader", "Flatex", "accountID", username.getText());
        Settings.getInstance().set("manticore-trader", "Flatex", "password", password.getText());
        Settings.getInstance().set("manticore-trader", "Flatex", "tradingPassword", tradingPassword.getText());
        Settings.getInstance().writeToFile();
        loggedIn=true;
        setVisible(false);
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }


    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            logIn();
        }
    }

    public String getAccountID() { return username.getText(); }
    public String getPassword() { return password.getText(); }
    public String getTradingPassword() { return tradingPassword.getText(); }
}
