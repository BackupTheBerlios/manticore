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

package com.manticore.report;

import com.manticore.database.Quotes;
import com.manticore.swingui.GridBagPane;
import com.manticore.swingui.MenuBar;
import com.manticore.swingui.SwingUI;
import com.manticore.swingui.ToolBar;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

public class PerformanceReport extends SwingUI {
    GridBagPane panel;
    PerformanceRatioPanel performanceRatioPanel;
    GridPanel gridPanel;
    TimeSeriesBarChartPanel panel1;
    TimeSeriesLineChartPanel panel2;
    CategoryChartPanel panel3;
    
    public PerformanceReport(String schema) {
        String sqlStr="select * from " + schema + ".performance_view;";
        String sqlStr2="select * from " + schema + ".performance_per_instrument_view;";
        String sqlStr3="select * from " + schema + ".performance_view;";

        panel=new GridBagPane();
        try {
            gridPanel=new GridPanel(Quotes.getInstance().getResultSet(sqlStr));

            performanceRatioPanel=new PerformanceRatioPanel(Quotes.getInstance().getResultSet(sqlStr3));
            panel.add(performanceRatioPanel, "gridx=0, gridy=0, fill=BOTH, weighty=1f, weightx=0.0f, gridwidth=1, gridheight=3");
            
            panel1=new TimeSeriesBarChartPanel();
            panel1.setTitle("Profit per day");
            panel2=new TimeSeriesLineChartPanel();
            panel2.setTitle("Profit total");
            ResultSet rs = Quotes.getInstance().getResultSet(sqlStr);
            while (rs.next()) {
                panel1.addData(rs.getTimestamp("timestamp_max").getTime(), rs.getFloat("profit"));
                panel2.addData(rs.getTimestamp("timestamp_max").getTime(), rs.getFloat("profit_total"));
            }

            panel.add(panel1, "gridx=1, gridy=0, fill=BOTH, weighty=0.33f, weightx=1f, gridwidth=1, gridheight=1");
            panel.add(panel2, "gridx=1, gridy=1, fill=BOTH, weighty=0.33f, weightx=1f, gridwidth=1, gridheight=1");

            panel3=new CategoryChartPanel();
            panel3.setTitle("Profit per instrument");
            rs = Quotes.getInstance().getResultSet(sqlStr2);
            while (rs.next()) {
                panel3.addData(rs.getString("description"), rs.getFloat("profit"));
            }

            panel.add(panel3, "gridx=1, gridy=2, fill=BOTH, weighty=0.33f, weightx=1f, gridwidth=1, gridheight=1");
        } catch (SQLException ex) {
            Logger.getLogger(PerformanceReport.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSplitPane splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, gridPanel, panel);
        splitPane.setDividerSize(2);
        splitPane.setResizeWeight(0.5f);

        pack();
        setSize(800,600);
        initUI((JComponent) splitPane);
    }

    @Override
    public void initMenuBar() {
        String MenuStr[] = {"File", "Help"};
        String MenuItemStr[][] = {
            {"open", "export", "exit"}, {"index", "search", "--", "about"}
        };

        menuBar = new MenuBar(this, MenuStr, MenuItemStr);
    }

    @Override
    public void initToolBar() {
        String ButtonStr[] = {"exit", "open", "export"};
        toolBar = new ToolBar(ButtonStr);
    }

    @Override
    public void exit() {
        dispose();
    }

    public void export() {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "export is not supported yet");
    }

    public void open() {
        PerformanceDistributionWindow performanceDistributionWindow=new PerformanceDistributionWindow();
        performanceDistributionWindow.setValues(Quotes.getInstance().getPositionProfits());
        performanceDistributionWindow.setVisible(true);
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
}
