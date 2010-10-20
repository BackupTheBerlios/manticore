package com.manticore.swingui;

import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.ResourceBundle;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.MessageFormat;


import javax.swing.JProgressBar;


public class GlobalProperties {

    private static GlobalProperties instance;
    public ActionListener AL;
    public ResourceBundle i18n;
    public StatusBar SB;
    public DecimalFormat DecimalF;
    public DateFormat DateF;
    public SimpleDateFormat SimpleDateF;
    public Dimension minD;
    public Dimension minFieldD;
    public Dimension minButtonD;
    public Dimension minFrameD;
    public JProgressBar PB;
    public ThreadGroup TG;
    public boolean stopApplication = false;
    static String PathStr = "com.manticore.swingui.i18n";

    public GlobalProperties() {
        minD = new Dimension(96, 24);
        minFieldD = new Dimension(80, 24);
        minButtonD = new Dimension(16, 24);
        minFrameD = new Dimension(640, 240);

        System.out.println("init Globalproperties");
    }

    public static GlobalProperties getInstance() {
        if (instance == null) {
            instance = new GlobalProperties();
        }
        return instance;
    }

    public void setActionListener(ActionListener NewActionListener) {
        this.AL = NewActionListener;
    }

    public ActionListener getActionListener() {
        return AL;
    }

    public ResourceBundle getResourceBundle() {
        if (i18n == null) {
            i18n = ResourceBundle.getBundle(PathStr);
        }
        return i18n;
    }

    public String localizeStr(String s) {
        if (s.startsWith("i18n_") && getResourceBundle().containsKey(s) ) {
            return getResourceBundle().getString(s);
        } else {
            return s;
        }
    }

    public StatusBar getStatusBar() {
        if (SB == null) {
            SB = new StatusBar();
        }
        return SB;
    }

    public void showStatusMsg(String s) {
        getStatusBar().setText(s);
    }

    public void showStatusMsg(String Key, Object[] Args) {
        String s = MessageFormat.format(getResourceBundle().getString(Key), Args);
        getStatusBar().setText(s);
    }

    public void showMissingFeature(String s) {
        //String s = MessageFormat.format( getResourceBundle().getString("MissingFeature"), Args);
        getStatusBar().setText("Missing Feature: " + s);
    }

    public Dimension getMinDim() {
        return minD;
    }

    public Dimension getFieldDim() {
        return minFieldD;
    }

    public Dimension getButtonDim() {
        return minButtonD;
    }

    public Dimension getFrameDim() {
        return minFrameD;
    }

    public JProgressBar getProgressBar() {

        if (PB == null) {
            PB = new JProgressBar();
            PB.setMinimum(0);
            PB.setMaximum(0);
            PB.setStringPainted(true);
        }
        return PB;
    }

    public ThreadGroup getThreadGroup() {
        if (TG == null) {
            TG = new ThreadGroup("DataSource");
        }
        return TG;
    }

 
}
