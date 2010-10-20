/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.chart.annotation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author are
 */
public class TextDialog extends JDialog implements ActionListener {
    JTextField textField;
    JButton button;

    public TextDialog(String s) {
        setTitle("Insert text here");
        setLayout(new BorderLayout(6, 6));
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        textField=new JTextField(s);
        add(textField, BorderLayout.CENTER);

        button=new JButton("ok");
        button.addActionListener(this);
        JPanel panel=new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 0));
        panel.add(button);
        add(panel, BorderLayout.SOUTH);
        pack();
        setSize(320, 120);

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getWidth()) / 2, (tk.getScreenSize().height - getHeight()) / 2);

        setModal(true);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    public String getText() {
        return textField.getText();
    }

    public static String getTextFromDialog(String s) {
        TextDialog dialog=new TextDialog(s);
        s=dialog.getText();
        dialog.dispose();

        return s;
    }
}
