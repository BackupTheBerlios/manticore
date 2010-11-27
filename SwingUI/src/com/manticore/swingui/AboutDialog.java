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

package com.manticore.swingui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AboutDialog extends JDialog implements ActionListener {
    JButton closeButton;

    public AboutDialog(Frame owner, String programName) {
        super(owner, "About " + programName);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        setLocationByPlatform(true);
        setLayout(new BorderLayout(2, 2));

        ImageIcon imageIcon=new ImageIcon(getClass().getResource("/com/manticore/swingui/logo.png"), "manticore Logo");
        JLabel logoLabel=new JLabel(imageIcon);
        add(logoLabel, BorderLayout.NORTH);

        JTextArea textArea=new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setText("Copyright (C) 2010 Andreas Reichel <andreas@manticore-projects.com>\n\n"
                + programName + " is based on software from manticore-projects.com, which has been licensed under the GNU General Public License version 2. For more information, please visit www.manticore-projects.com.");
        add(textArea, BorderLayout.CENTER);


        closeButton=new JButton("close");
        closeButton.setActionCommand("CLOSE");
        closeButton.setToolTipText("Close that dialog.");
        closeButton.addActionListener(this);
        JPanel panel=new JPanel(new FlowLayout(FlowLayout.TRAILING, 2, 2));
        panel.add(closeButton);
        add(panel,BorderLayout.SOUTH);

        setSize(imageIcon.getIconWidth(), imageIcon.getIconWidth()*3/4);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("CLOSE")) dispose();
    }
}
