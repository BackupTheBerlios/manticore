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

package com.manticore.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class SettingsEditor extends JDialog implements ActionListener {
    String programName;
    JTabbedPane tabbedPane;
    JButton saveButton;
    JButton cancelButton;
    JButton applyButton;
    public SettingsEditor(Frame owner, String programName) {
        super(owner);
        setLayout(new BorderLayout(2, 2));
        setModal(true);
        setAlwaysOnTop(true);
        setLocationRelativeTo(owner);
        setLocationByPlatform(true);
        setTitle("Edit settings for " + programName);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.programName=programName;
        buildTabbedPane(programName);

        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        saveButton=new JButton("save");
        saveButton.setActionCommand("SAVE");
        saveButton.setToolTipText("Save all changes and close the dialog.");
        saveButton.setDefaultCapable(true);
        saveButton.addActionListener(this);

        applyButton=new JButton("apply");
        applyButton.setActionCommand("APPLY");
        applyButton.setToolTipText("Save all changes without leaving the dialog");
        applyButton.addActionListener(this);

        cancelButton=new JButton("cancel");
        cancelButton.setActionCommand("CANCEL");
        cancelButton.setToolTipText("Close the dialog without saving the changes");
        cancelButton.addActionListener(this);

        JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 2));
        buttonPanel.add(cancelButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(saveButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(320, 480));
        validate();
        pack();
    }

    private void buildTabbedPane(String programName) {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        Vector<JPanel> panelVector = buildPanelVector(programName);
        Iterator<JPanel> panelIterator = panelVector.iterator();
        while (panelIterator.hasNext()) {
            JPanel panel = panelIterator.next();
            tabbedPane.addTab(panel.getName(), new JScrollPane(panel));
        }
    }

    private Vector<JPanel> buildPanelVector(String programName) {
        Vector<JPanel> panelVector = new Vector<JPanel>();
        HashMap<String, HashMap<String, String>> settingsHashMap = Settings.getInstance().getSettingsHashMap(programName);
        Iterator<String> moduleIterator = settingsHashMap.keySet().iterator();
        while (moduleIterator.hasNext()) {
            String moduleName = moduleIterator.next();
            JPanel panel = buildPanelForModule(moduleName, settingsHashMap.get(moduleName));
            panelVector.add(panel);
        }

        return panelVector;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("SAVE")) {
            saveSettings();
            setVisible(false);
        }
        else if (e.getActionCommand().equalsIgnoreCase("APPLY")) saveSettings();
        else if (e.getActionCommand().equalsIgnoreCase("CANCEL")) setVisible(false);
    }

    private void saveSettings() {
        HashMap<String, HashMap<String, String>> settingsHashMap=new HashMap<String, HashMap<String, String>>();
        for (int i=0; i<tabbedPane.getComponentCount(); i++) {
            JScrollPane scrollPane=(JScrollPane) tabbedPane.getComponentAt(i);
            JPanel panel=(JPanel) scrollPane.getViewport().getComponent(0);
            HashMap<String, String> moduleHashMap=new HashMap<String, String>();

            for (int k=0; k<panel.getComponentCount(); k++) {
                if (panel.getComponent(k) instanceof JTextField) {
                    JTextField textField=(JTextField) panel.getComponent(k);
                    moduleHashMap.put(textField.getName(), textField.getText().trim());
                }
            }
            settingsHashMap.put(panel.getName(), moduleHashMap);
        }

        Settings.getInstance().updateDocumentFromHashMap(programName, settingsHashMap);
    }

    private JPanel buildPanelForModule(String moduleName, HashMap<String, String> optionHashMap) {
        GridBagConstraints gridBagConstraints=new GridBagConstraints(0, 0, 1, 1, 0f, 0f,
                GridBagConstraints.ABOVE_BASELINE_TRAILING,
                GridBagConstraints.HORIZONTAL,
                new Insets(2,2,2,2),
                0, 0);
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setName(moduleName);

        Iterator<Entry<String, String>> optionIterator=optionHashMap.entrySet().iterator();
        while (optionIterator.hasNext()) {

            Entry <String, String> optionEntry=optionIterator.next();

            JLabel label=new JLabel(optionEntry.getKey());
            label.setHorizontalAlignment(JLabel.TRAILING);
            gridBagConstraints.gridx=0;
            gridBagConstraints.weightx=0;
            gridBagConstraints.anchor=GridBagConstraints.FIRST_LINE_END;
            panel.add(label, gridBagConstraints);

            JTextField textField=new JTextField(optionEntry.getValue());
            textField.setName(optionEntry.getKey());
            textField.setHorizontalAlignment(JTextField.LEADING);
            gridBagConstraints.gridx=1;
            gridBagConstraints.weightx=1f;
            gridBagConstraints.anchor=GridBagConstraints.FIRST_LINE_START;
            panel.add(textField, gridBagConstraints);

            gridBagConstraints.gridy++;
        }
        return panel;
    }



}
