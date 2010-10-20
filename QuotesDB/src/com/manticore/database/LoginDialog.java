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
package com.manticore.database;

import com.manticore.util.Settings;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

public class LoginDialog extends JDialog implements ActionListener {

    private JButton loginButton;
    private JButton cancelButton;
    private JTextField connectionUrl;
    private JComboBox driverClassName;
    private JTextField username;
    private JTextField password;
    private static Connection con;
    private static boolean loggedIn = false;

    public LoginDialog() {
        createControls();
        setVisible(true);
    }

    private void createControls() {
        String connectionStr=Settings.getInstance().get("manticore-trader", "Quotes", "connectionUrlStr");
        String homeDir = System.getProperty("user.home");
        if (!homeDir.endsWith(File.separator)) {
            homeDir+=File.separator;
        }
        connectionUrl = new JTextField(connectionStr.replace("${user.home}", homeDir));

        driverClassName = new JComboBox();
        Enumeration<Driver> e = DriverManager.getDrivers();
        while (e.hasMoreElements()) {
            driverClassName.addItem(e.nextElement().getClass().getName());
        }
        driverClassName.setSelectedItem(Settings.getInstance().get("manticore-trader", "Quotes", "className"));

        loginButton = new JButton("Login");
        loginButton.setActionCommand("LOGIN");
        loginButton.setDefaultCapable(true);
        loginButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);

        username = new JTextField(Settings.getInstance().get("manticore-trader", "Quotes", "username"));
        password = new JPasswordField(Settings.getInstance().get("manticore-trader", "Quotes", "password"));

        setModal(true);
        setAlwaysOnTop(true);
        setTitle("Database connection settings");

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        GridBagConstraints gridBagConstraints = new GridBagConstraints(0, 0, 1, 1, 0f, 0f, GridBagConstraints.BASELINE, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);

        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/com/manticore/connection/logo.png"));
        gridBagConstraints.gridwidth = 2;
        add(new JLabel(imageIcon), gridBagConstraints);

        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridy = 1;
        JLabel hostLabel=new JLabel("Host:");
        hostLabel.setHorizontalAlignment(JLabel.TRAILING);
        add(hostLabel, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        JLabel driverLabel=new JLabel("Driver:");
        driverLabel.setHorizontalAlignment(JLabel.TRAILING);
        add(driverLabel, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        JLabel loginLabel=new JLabel("Login:");
        loginLabel.setHorizontalAlignment(JLabel.TRAILING);
        add(loginLabel, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        JLabel passwordLabel=new JLabel("Password:");
        passwordLabel.setHorizontalAlignment(JLabel.TRAILING);
        add(passwordLabel, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1f;
        add(connectionUrl, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        add(driverClassName, gridBagConstraints);
        gridBagConstraints.gridy = 3;
        add(username, gridBagConstraints);
        gridBagConstraints.gridy = 4;
        add(password, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1f;
        gridBagConstraints.weighty = 0f;

        JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        p.add(cancelButton);
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
            if (isLoggedIn() == true) {
                setVisible(false);
            }
        } else if (e.getActionCommand().equals("CANCEL")) {
            setVisible(false);
        }
    }

    private void logIn() {
        Properties props = new Properties();
        props.setProperty("user", username.getText());
        props.setProperty("password", password.getText());
        //props.setProperty("ssl","true");
        props.setProperty("data compression", "true");
        props.setProperty("compress", "true");
        try {
            con = DriverManager.getConnection(connectionUrl.getText(), props);
            loggedIn = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return (loggedIn);
    }

    public Connection getConnection() {
        return (con);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            logIn();
        }
    }
}
