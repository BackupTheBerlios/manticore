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

package com.manticore.ui;

import com.manticore.database.Quotes;
import com.manticore.foundation.MessageDialog;
import com.manticore.report.PerformanceDistributionWindow;
import com.manticore.report.PerformanceReport;
import com.manticore.swingui.MenuBar;
import com.manticore.swingui.SwingUI;
import com.manticore.swingui.ToolBar;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 *
 * @author are
 */
public class TradeSystemApplication extends SwingUI {
	 private JTabbedPane tabbedPane;
	 private JTextArea textArea;

	 public TradeSystemApplication() {
		  tabbedPane=new JTabbedPane(JTabbedPane.TOP);
		  addTradeSystemPanel();

		  textArea=new JTextArea();
		  JScrollPane scrollPane=new JScrollPane(textArea);

		  setSize(800, 600);
		  validateTree();
		  initUI(tabbedPane, scrollPane);
	 }

	 public final void addTradeSystemPanel() {
		  TradeSystemPanel tradeSystemPanel=new TradeSystemPanel();
		  tabbedPane.addTab(tradeSystemPanel.getTitle(), tradeSystemPanel);
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
        String ButtonStr[] = {"exit", "open", "save", "report"};
        toolBar = new ToolBar(ButtonStr);
    }

    @Override
    public void exit() {
        dispose();
		  System.exit(0);
    }

    public void export() {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "export is not supported yet");
    }

    public void open() {
        TradeSystemPanel tradeSystemPanel=(TradeSystemPanel) tabbedPane.getSelectedComponent();
		  tradeSystemPanel.openScript();
    }

	 public void report() {
		  TradeSystemPanel tradeSystemPanel=(TradeSystemPanel) tabbedPane.getSelectedComponent();
		  String schema=tradeSystemPanel.isSimulation() ? "simulation" : "trader";

		  if (Quotes.getInstance().getPositionArrayList(schema, false).isEmpty()) {
            MessageDialog.getInstance().showClean("There are no transactions yet.\nPlease come back when you made some trades.");
        } else {
            new PerformanceReport(schema).setVisible(true);
        }
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

	 @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (dispatchActionCommand(actionCommand)) {
            //GlobalProperties.getInstance().showStatusMsg(actionCommand);
        }
    }
}
