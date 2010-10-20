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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


public class TanDialog extends JDialog implements ActionListener, TanReader {
    private JButton okButton;
    private JButton cancelButton;

    private JTextField tan;
    private JLabel label;

    public TanDialog() {
        createControls();
    }

    private void createControls() {
        okButton = new JButton("ok");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);

        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);

        tan = new JPasswordField("");
        label= new JLabel("Insert TAN for ");

        setModal(true);
        //setAlwaysOnTop(true);
        setTitle("Broker is asking for TAN");

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        GridBagConstraints gridBagConstraints = new GridBagConstraints(0, 0, 1, 1, 0f, 0f, GridBagConstraints.BASELINE, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);

        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/com/manticore/connection/logo.png"));
        gridBagConstraints.gridwidth = 2;
        add(new JLabel(imageIcon), gridBagConstraints);

        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridy = 1;
        add(label, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1f;
        add(tan, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1f;
        gridBagConstraints.weighty = 0f;


        JPanel p = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        p.add(cancelButton);
        p.add(okButton);
        add(p, gridBagConstraints);

        tan.requestFocus();
        pack();

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation( (tk.getScreenSize().width-getWidth()) / 2, (tk.getScreenSize().height-getHeight()) / 2);
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }


    public String getTan(long id_account, String key) {
        label.setText(key);
        pack();
        setVisible(true);
        return tan.getText();
    }
}
