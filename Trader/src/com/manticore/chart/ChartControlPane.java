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
package com.manticore.chart;

import com.manticore.swingui.GridBagPane;
import com.manticore.swingui.FormatedTextField;
import com.manticore.database.Quotes;
import com.manticore.position.PositionGrid;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.StockExchange;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.joda.time.*;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

class ChartControlPane extends GridBagPane implements ActionListener, ListSelectionListener, TreeSelectionListener {
    private static ChartControlPane instance = null;
    public static final String MEDASEEKFORWARD = "/com/manticore/chart/media-seek-forward.png";
    public static final String MEDIASEEKBACKWARD = "/com/manticore/chart/media-seek-backward.png";
    public static final String MEDIASKIPBACKWWARD = "/com/manticore/chart/media-skip-backward.png";
    public static final String MEDIASKIPFORWARD = "/com/manticore/chart/media-skip-forward.png";
    private JButton previousDayButton;
    private JButton previousCandleButton;
    private JButton nextCandleButton;
    private JButton nextDayButton;
    private ChartCanvas chartCanvas;
    private PositionGrid positionGrid;
    private JTree instrumentTree;
    private JList modeList;
    private FormatedTextField reportDateTextField;
    
    

    private ChartControlPane() {

        MutableDateTime mutableDateTime = new MutableDateTime();
        mutableDateTime.setSecondOfMinute(0);
        mutableDateTime.setMinuteOfHour(0);
        mutableDateTime.setHourOfDay(22);

        reportDateTextField = new FormatedTextField(mutableDateTime.toDate(), FormatedTextField.DATETIME_FORMAT, true);

        previousCandleButton = new JButton(new ImageIcon(getClass().getResource(MEDIASEEKBACKWARD)));
        previousCandleButton.setActionCommand("previousCandle");
        previousCandleButton.addActionListener(this);

        previousDayButton = new JButton(new ImageIcon(getClass().getResource(MEDIASKIPBACKWWARD)));
        previousDayButton.setActionCommand("previousDay");
        previousDayButton.addActionListener(this);

        nextDayButton = new JButton(new ImageIcon(getClass().getResource(MEDIASKIPFORWARD)));
        nextDayButton.setActionCommand("nextDay");
        nextDayButton.addActionListener(this);

        nextCandleButton = new JButton(new ImageIcon(getClass().getResource(MEDASEEKFORWARD)));
        nextCandleButton.setActionCommand("nextCandle");
        nextCandleButton.addActionListener(this);

        ArrayList<PeriodSettings> periodSettingsArrayList = new ArrayList();
        //periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.hours(), 3, DurationFieldType.hours(), 1, DurationFieldType.minutes(), 15, DurationFieldType.minutes(), 1));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 1, DurationFieldType.days(), 1, DurationFieldType.minutes(), 15, DurationFieldType.minutes(), 3));

        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 2, DurationFieldType.days(), 1, DurationFieldType.hours(), 1, DurationFieldType.minutes(), 5));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 2, DurationFieldType.days(), 1, DurationFieldType.hours(), 1, DurationFieldType.minutes(), 10));

        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 3, DurationFieldType.days(), 1, DurationFieldType.hours(), 1, DurationFieldType.minutes(), 10));

        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 5, DurationFieldType.days(), 1, DurationFieldType.hours(), 2, DurationFieldType.minutes(), 15));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 5, DurationFieldType.days(), 1, DurationFieldType.hours(), 2, DurationFieldType.minutes(), 20));

        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 10, DurationFieldType.days(), 1, DurationFieldType.days(), 1, DurationFieldType.minutes(), 30));

        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 15, DurationFieldType.days(), 1, DurationFieldType.days(), 1, DurationFieldType.hours(), 1));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.days(), 20, DurationFieldType.days(), 1, DurationFieldType.days(), 1, DurationFieldType.hours(), 2));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.months(), 2, DurationFieldType.days(), 1, DurationFieldType.weeks(), 1, DurationFieldType.hours(), 2));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.months(), 3, DurationFieldType.days(), 1, DurationFieldType.weeks(), 1, DurationFieldType.hours(), 4));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.months(), 6, DurationFieldType.days(), 1, DurationFieldType.weeks(), 1, DurationFieldType.days(), 1));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.months(), 6, DurationFieldType.days(), 1, DurationFieldType.months(), 1, DurationFieldType.weeks(), 1));
        periodSettingsArrayList.add(new PeriodSettings(DurationFieldType.years(), 1, DurationFieldType.days(), 1, DurationFieldType.months(), 1, DurationFieldType.weeks(), 1));


        modeList = new JList(periodSettingsArrayList.toArray(new PeriodSettings[0]));
        modeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modeList.addListSelectionListener(this);

        instrumentTree = new JTree(Quotes.getInstance().getInstrumentTreeNode(), false);
        instrumentTree.setRootVisible(false);
        instrumentTree.setEditable(false);
        instrumentTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        instrumentTree.addTreeSelectionListener(this);

        add(new JScrollPane(instrumentTree), "nl, weightx=1, weighty=0.2, fill=BOTH, size=80 180");

        add(new JScrollPane(modeList), "nl, weighty=0.1, fill=BOTH");

        GridBagPane panel1 = new GridBagPane();
        panel1.add(previousDayButton, "size=24 24, fill=NONE, weightx=0, weighty=0");
        panel1.add(previousCandleButton, "size=24 24, fill=NONE, weightx=0, weighty=0");
        panel1.add(reportDateTextField, "size=64 24, fill=BOTH, weightx=1, weighty=0");
        panel1.add(nextCandleButton, "size=24 24, fill=NONE, weightx=0, weighty=0");
        panel1.add(nextDayButton, "size=24 24, fill=NONE, weightx=0, weighty=0");

        add(panel1, "nl,  weighty=0.025, fill=BOTH");

        positionGrid = new PositionGrid(Quotes.getInstance(), Quotes.getInstance());
        //positionGrid = new PositionGrid();
        add(positionGrid, "nl,  weighty=0.1, fill=BOTH");

        add(new NewsPanel(), "nl, weighty=0.6, fill=BOTH");

        revalidate();
    }

    public static ChartControlPane getInstance() {
        if (instance == null) {
            instance = new ChartControlPane();
        }
        return instance;
    }

    

    private DateTime getSelectedDateTimeTo() {
        Date date = null;
        try {
            date = reportDateTextField.getDateValue();
        } catch (ParseException ex) {
            Logger.getLogger(ChartControlPane.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new DateTime(date);
    }

    public PeriodSettings getSelectedPeriodSettings() {
        return (PeriodSettings) (modeList.isSelectionEmpty() ? modeList.getComponent(0) : modeList.getSelectedValue());
    }

    public ChartParameters getNewChartParameters() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) instrumentTree.getSelectionPath().getLastPathComponent();
        Instrument instrument = (Instrument) treeNode.getUserObject();
        StockExchange stockExchange = instrument.getStockExchangeArrayList().get(0);

        return new ChartParameters(getSelectedTreeRow(), instrument, stockExchange, getSelectedDateTimeTo(), getSelectedPeriodSettings());
    }

    private int getSelectedTreeRow() {
        int row = -1;
        if (instrumentTree.getSelectionCount() > 0) {
            row = instrumentTree.getSelectionRows()[0];
        }
        return row;
    }

    public void updateChartParameters() {
        ChartParameters chartParameters = getChartCanvas().getChartParameters();

        if (chartParameters != null) {
            reportDateTextField.setValue(chartParameters.getDateTimeTo().toDate());
            modeList.setEnabled(false);
            modeList.setSelectedValue(chartParameters.getPeriodSettings(), true);
            modeList.setEnabled(true);

            int row = chartParameters.getTreeItem();
            instrumentTree.setSelectionRow(row);
            instrumentTree.scrollRowToVisible(row);
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        JTree jTree = (JTree) e.getSource();

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) instrumentTree.getSelectionPath().getLastPathComponent();
        Instrument instrument = (Instrument) treeNode.getUserObject();
        StockExchange stockExchange = instrument.getStockExchangeArrayList().get(0);

        positionGrid.setInstrument(instrument);

        if (getChartCanvas() != null && getChartCanvas().getChartParameters() != null) {
            getChartCanvas().updateInstrument(instrument, stockExchange, getSelectedTreeRow());
            redrawChart();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        JList jList = (JList) e.getSource();

        if (!e.getValueIsAdjusting() && jList.equals(modeList) && getChartCanvas() != null && getChartCanvas().getChartParameters() != null) {
            getChartCanvas().getChartParameters().setPeriodSettings(getSelectedPeriodSettings());
            redrawChart();
        }
    }

    

    private void redrawChart() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getChartCanvas().updateChartSettings();
        getChartCanvas().drawChart();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals("nextDay")) {
                reportDateTextField.addDateTime(getSelectedPeriodSettings().getIntervalDurationFieldType(), 1);
            } else if (e.getActionCommand().equals("previousDay")) {
                reportDateTextField.addDateTime(getSelectedPeriodSettings().getIntervalDurationFieldType(), -1);
            } else if (e.getActionCommand().equals("nextCandle")) {
                reportDateTextField.addPeriod(getSelectedPeriodSettings().getCandlePeriod(), 1);
            } else if (e.getActionCommand().equals("previousCandle")) {
                reportDateTextField.addPeriod(getSelectedPeriodSettings().getCandlePeriod(), 1);
            }
            getChartCanvas().getChartParameters().setDateTimeTo(reportDateTextField.getDateTimeValue());
            redrawChart();
        } catch (ParseException ex) {
            Logger.getLogger(ChartControlPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the chartCanvas
     */
    public ChartCanvas getChartCanvas() {
        return chartCanvas;
    }

    /**
     * @param chartCanvas the chartCanvas to set
     */
    public void setChartCanvas(ChartCanvas chartCanvas) {
        this.chartCanvas = chartCanvas;
    }
}
