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
package com.manticore.portfolio;

import com.manticore.chart.CandleArrayList;
import com.manticore.chart.PeriodSettings;
import com.manticore.chart.ChartCanvas;
import com.manticore.chart.ChartParameters;
import com.manticore.database.Quotes;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.StockExchange;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import com.manticore.swingui.MenuBar;
import com.manticore.swingui.SwingUI;
import com.manticore.swingui.ToolBar;

class PortfolioView extends SwingUI implements ChangeListener, TreeSelectionListener {

    TreePane treePane;
    ListPane listPane;
    CandleArrayList chartSettings;
    ChartParameters chartParameters;
    ChartCanvas chartCanvas;
    DataImportManager dataImportManager;

    public PortfolioView() {
        this.setTitle("PortfolioView");

        treePane = new TreePane(this);
        chartCanvas = new ChartCanvas();
        listPane = new ListPane();
        listPane.addChangeListener(this);

        JSplitPane splitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        splitPane1.setResizeWeight(0.9f);
        splitPane1.setLeftComponent(chartCanvas);
        splitPane1.setRightComponent(treePane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false);
        splitPane.setResizeWeight(0.9f);
        splitPane.setTopComponent(splitPane1);
        splitPane.setBottomComponent(listPane);
        initUI((JComponent) splitPane);
    }

    public void initMenuBar() {
        String MenuStr[] = {"File", "Help"};
        String MenuItemStr[][] = {
            {"open", "importData", "save", "print", "preferences", "exit"}, {"index", "search", "--", "about"}
        };

        menuBar = new MenuBar(this, MenuStr, MenuItemStr);
    }

    @Override
    public void initToolBar() {
        String ButtonStr[] = {"exit", "importData", "save", "export", "print", "preferences", "--", "addCanvas", "removeCanvas", "--", "arrow", "crossHair", "trend", "pitchFork", "timeMarker", "resistance", "fibonacciRetracement", "fibonacciRectangle", "fibonacciPeriod", "fibonacciFanLine", "gap", "delete", "--", "zoomIn", "zoomOut"};
        toolBar = new ToolBar(ButtonStr);
    }

    public static void main(String args[]) {
        new PortfolioView();
    }

    public boolean dispatchActionCommand(String commandStr) {
        boolean dispatched = false;
        Method[] methodArray = getClass().getMethods();

        for (int i = 0; i < methodArray.length; i++) {
            Method method = methodArray[i];
            if (method.getName().equalsIgnoreCase(commandStr)) {
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
            dispatched &= chartCanvas.dispatchActionCommand(commandStr);
        }

        return dispatched;
    }
    // implements ActionListener

    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (dispatchActionCommand(actionCommand)) {
            //GlobalProperties.getInstance().showStatusMsg(actionCommand);
        }
    }

    public void export() {
        //new ChartExportFreeImageHostingNet( realTimeChartPane.getCanvas().getRenderedImage() );
    }

    public void zoomIn() {
        listPane.zoomIn();
    }

    public void zoomOut() {
        listPane.zoomOut();
    }

    public void importData() {
        Logger.getLogger(this.getClass().getName()).finest("start data import");
        dataImportManager = new DataImportManager(listPane);
        SwingUtilities.invokeLater(dataImportManager);
        Logger.getLogger(this.getClass().getName()).finest("finished data import");
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(listPane)) {
            Instrument instrument = listPane.getSelectedInstrument();

            updateChartCanvas(instrument);
        }
    }

    private void updateChartCanvas(Instrument instrument) {
        StockExchange stockExchange = Quotes.getInstance().getStockExchange("ETR");
        PeriodSettings periodSettings = new PeriodSettings(DurationFieldType.years(), 1, DurationFieldType.days(), 1, DurationFieldType.months(), 1, DurationFieldType.weeks(), 1);
        DateTime dateTimeTo = new DateTime();
        chartParameters = new ChartParameters(1, instrument, stockExchange, dateTimeTo, periodSettings);
        chartParameters.setEod(true);

        chartCanvas.setChartParameters(chartParameters);
        chartCanvas.updateChartSettings();
        chartCanvas.drawChart();
    }

    private void testChart() {
        chartSettings = new CandleArrayList(chartParameters);

    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
        System.out.println(treeNode.getUserObject().getClass().getName());
        System.out.println(treeNode.getUserObject().toString());

        if (treeNode.getUserObject() instanceof Instrument) {
            Instrument instrument = (Instrument) treeNode.getUserObject();
            StockExchange stockExchange = Quotes.getInstance().getStockExchange("ETR");
            PeriodSettings periodSettings = new PeriodSettings(DurationFieldType.years(), 1, DurationFieldType.days(), 1, DurationFieldType.months(), 1, DurationFieldType.weeks(), 1);
            DateTime dateTimeTo = new DateTime();
            chartParameters = new ChartParameters(1, instrument, stockExchange, dateTimeTo, periodSettings);
            chartParameters.setEod(true);
            chartCanvas.setChartParameters(chartParameters);
            chartCanvas.updateChartSettings();
            chartCanvas.drawChart();
        }
    }
}


