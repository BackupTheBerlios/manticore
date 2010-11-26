/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manticore.foundation;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author are
 */
public class MessageDialog extends JDialog implements ActionListener {
    public String title="manticore-trader messages";
    public String welcome="Welcome to manticore-trader.";

    private static MessageDialog instance = null;
    private JTextArea textPane;
    private JButton closeButton;

    public static MessageDialog getInstance() {
        if (instance == null) {
            instance = new MessageDialog();
        }
        return instance;
    }

    public static MessageDialog getInstance(String title, String welcome) {
        if (instance == null) {
            instance = new MessageDialog(title, welcome);
        }
        return instance;
    }

    private MessageDialog(String title, String welcome) {
        this.title=title;
        this.welcome=welcome;
        buildDialog();
    }

    public MessageDialog() {
        buildDialog();
    }

    private void buildDialog() {
        setLayout(new BorderLayout(6, 6));
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle(title);
        setSize(new Dimension(480, 320));
        ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/com/manticore/foundation/logo.png"));
        add(new JLabel(imageIcon), BorderLayout.NORTH);
        textPane = new JTextArea(welcome);
        textPane.setEditable(false);
        textPane.setAutoscrolls(true);
        textPane.setDoubleBuffered(true);
        textPane.setLineWrap(true);
        textPane.setWrapStyleWord(true);
        closeButton = new JButton("close");
        closeButton.setActionCommand("CLOSE");
        closeButton.setToolTipText("Close this message dialog.");
        closeButton.addActionListener(this);
        add(new JScrollPane(textPane), BorderLayout.CENTER);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 24, 6));
        panel.add(closeButton);
        add(panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    public void showAndLock(final String message) {
        textPane.append(message);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getWidth()) / 2, (tk.getScreenSize().height - getHeight()) / 2);

        setVisible(true);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MessageDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void show(final String message) {
        textPane.append(message);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getWidth()) / 2, (tk.getScreenSize().height - getHeight()) / 2);

        setVisible(true);
    }

    public void showClean(final String message) {
        textPane.setText(message);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getWidth()) / 2, (tk.getScreenSize().height - getHeight()) / 2);

        setVisible(true);
    }

    public void release(String message) {
        textPane.append(message);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MessageDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setVisible(false);
    }
}
