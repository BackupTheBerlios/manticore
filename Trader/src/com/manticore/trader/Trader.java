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
package com.manticore.trader;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.*;

import com.manticore.chart.ChartPane;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.manticore.swingui.MenuBar;
import com.manticore.swingui.SwingUI;
import com.manticore.swingui.ToolBar;
import com.manticore.chartexport.ABLoadChartExport;
import com.manticore.database.DataBaseWizard;
import com.manticore.database.Quotes;
import com.manticore.foundation.MessageDialog;
import com.manticore.report.PerformanceReport;
import com.manticore.swingui.AboutDialog;
import com.manticore.ui.WorldTimePane;
import com.manticore.util.SettingsEditor;
import java.util.TimeZone;
import javax.swing.filechooser.FileFilter;
import org.joda.time.DateTimeZone;

public class Trader extends SwingUI {

    ChartPane realTimeChartPane;

    public Trader() {
        this.setTitle("manticore-trader");
        this.setSize(800, 600);
        realTimeChartPane = new ChartPane();
        initUI((JComponent) realTimeChartPane, new WorldTimePane());
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        DateTimeZone.setDefault(DateTimeZone.forID("Europe/Berlin"));

        //Quotes.getInstance().executeUpdate("delete from trader.tickdata where \"timestamp\">'2010-09-13 00:00:00.0';");

        //WebsiteParser.getInstance().writeTempXMLFiles=false;

        if (Quotes.getInstance().getConnection() != null) {
            DataBaseWizard.checkDataBase();

            Logger.getLogger(Trader.class.getName()).info("Here we go.");
            Trader trader = new Trader();
        } else {
            Logger.getAnonymousLogger().info("Not logged in.");
            System.exit(0);
        }
    }

    @Override
    public void initMenuBar() {
        String MenuStr[] = {"File", "Help"};
        String MenuItemStr[][] = {
            {"open", "update", "save", "report", "preferences", "exit"}, {"index", "search", "--", "about"}
        };

        menuBar = new MenuBar(this, MenuStr, MenuItemStr);
    }

    @Override
    public void initToolBar() {
        String ButtonStr[] = {"exit", "save", "export", "--", "addCanvas", "removeCanvas", "delete", "--", "refresh", "mouse", "crossHair", "trend", "pitchFork", "timeMarker", "resistance", "fibonacciRetracement", "gap", "arc", "rectangle", "arrow", "marker", "text"};
        toolBar = new ToolBar(ButtonStr);
    }

    @Override
    public void exit() {
        try {
            Quotes.getInstance().getConnection().close();
        } catch (SQLException ex) {
            Logger.getLogger(Trader.class.getName()).log(Level.SEVERE, null, ex);
        }
        dispose();
        System.exit(0);
    }

    public boolean dispatchActionCommand(String commandStr) {
        boolean dispatched = false;
        Method[] methodArray = getClass().getMethods();

        for (int i = 0; i < methodArray.length; i++) {
            Method method = methodArray[i];
            if (method.getName().equalsIgnoreCase(commandStr) && method.getParameterAnnotations().length == 0) {
                try {
                    method.invoke((Object) this);
                    dispatched = true;
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (!dispatched) {
            dispatched &= realTimeChartPane.dispatchActionCommand(commandStr);
        }

        return dispatched;
    }
    // implements ActionListener

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (dispatchActionCommand(actionCommand)) {
            //GlobalProperties.getInstance().showStatusMsg(actionCommand);
        }
    }

    public void export() {
        new ABLoadChartExport(realTimeChartPane.getCanvas().getRenderedImage()).start();
    }

    public void update() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setDialogTitle("Apply database update");
        jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setFileHidingEnabled(true);
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.canRead() && (f.isDirectory() || (f.isFile() && f.getName().startsWith("manticore-trader") && f.getName().endsWith("dbu")));
            }

            @Override
            public String getDescription() {
                return "manticore trader database update";
            }
        });

        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String filename;
            try {
                filename = jFileChooser.getSelectedFile().getCanonicalPath();
                DataBaseWizard.applyUpdateFromFile(filename);
            } catch (IOException ex) {
                Logger.getLogger(Trader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void about() {
        AboutDialog aboutDialog = new AboutDialog(this, "manticore-trader");
    }

    public void preferences() {
        SettingsEditor settingsEditor = new SettingsEditor(this, "manticore-trader");
        settingsEditor.setVisible(true);
    }

    public void report() {
        if (Quotes.getInstance().getPositionArrayList(false).isEmpty()) {
            MessageDialog.getInstance().showClean("There are no transactions yet.\nPlease come back when you made some trades.");
        } else {
            new PerformanceReport("trader").setVisible(true);
        }
    }
}


