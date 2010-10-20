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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;

public class MainPane extends JSplitPane implements ChangeListener {

    ListPane listPane;
    CandleArrayList chartSettings;
    ChartParameters chartParameters;
    ChartCanvas chartCanvas;
    DataImportManager dataImportManager;

    public MainPane() {
        super(JSplitPane.VERTICAL_SPLIT, false);
        chartCanvas = new ChartCanvas();

        listPane = new ListPane();
        listPane.addChangeListener(this);

        //dataImportManager = new DataImportManager(listPane);
        //SwingUtilities.invokeLater(dataImportManager);

        this.setResizeWeight(0.9f);
        this.setTopComponent(chartCanvas);
        this.setBottomComponent(listPane);
    }

    private void testChart() {
        chartSettings = new CandleArrayList(chartParameters);

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
            //dispatched &= mainPane.dispatchActionCommand(commandStr);
        }

        return dispatched;
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
}
