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

import com.manticore.database.Quotes;
import com.manticore.swingui.GridBagPane;
import com.manticore.stream.WatchDog;
import com.manticore.foundation.StockExchange;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;
import com.manticore.ui.TabbingPanel;
import com.manticore.ui.TimeMarkerForm;
import java.awt.Cursor;
import java.awt.event.MouseListener;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChartPane extends GridBagPane implements ChangeListener, ActionListener, MouseListener, ListSelectionListener {
    private ChartCanvas canvas= new ChartCanvas();
    private JToggleButton lastToggleButton=null;
    private JPopupMenu popupMenu;
    private JList list;

    ArrayList<ChartParameters> chartParametersArrayList;
    TabbingPanel  tabbingPanel;

    public ChartPane() {
        canvas= new ChartCanvas();
        canvas.setPreferredSize(new Dimension(600, 480));
        canvas.setMinimumSize(new Dimension(300, 240));
        canvas.setSize(600, 480);
        
        chartParametersArrayList=new ArrayList();

        tabbingPanel=new TabbingPanel(this, this);
        
        JPanel panel=new JPanel(new BorderLayout(), true);
        panel.add(tabbingPanel, BorderLayout.NORTH);
        panel.add(canvas, BorderLayout.CENTER);

        //tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        //tabbedPane.addChangeListener(this);

        JSplitPane sp = new JSplitPane();
        sp.setDividerSize(4);
        sp.setResizeWeight(0.96);
        sp.setLeftComponent(panel);
        sp.setRightComponent(ChartControlPane.getInstance());

        add(sp, "weightx=1, weighty=1, fill=BOTH");
        this.validate();

    }

    public ChartParameters getChartParameters() {
        return chartParametersArrayList.get( tabbingPanel.getSelectedIndex() );
    }

    private void addCanvas() {
        ChartParameters chartParameters=ChartControlPane.getInstance().getNewChartParameters();

        JToggleButton jToggleButton=tabbingPanel.add(chartParameters.getCaption());
        chartParameters.setToogleButton(jToggleButton);
        validate();

        
        chartParametersArrayList.add(chartParameters);

        canvas.setChartParameters(chartParameters);
        canvas.updateChartSettings();

        ChartControlPane.getInstance().setChartCanvas(canvas);

        //controlpane.updateArivaStreamQuote();

        Logger.getLogger(this.getClass().getName()).finer("add new Tab");
    }

    private void removeCanvas() {
        int index=tabbingPanel.getSelectedIndex();
        tabbingPanel.remove(index);
        chartParametersArrayList.remove(index);
        validate();

        if (index>0) tabbingPanel.setSelectedIndex(index);
    }

    private void refresh() {
        canvas.updateArivaStreamQuote();
        WatchDog.getInstance().restartAllStreams();
    }

    public void timeMarker() {
        TimeMarkerForm timeMarkerForm=new TimeMarkerForm();
        timeMarkerForm.setVisible(true);
    }

    public boolean dispatchActionCommand(String commandStr) {
        boolean dispatched=false;
        Method[] methodArray = getClass().getDeclaredMethods();

        for (int i = 0; i < methodArray.length; i++) {
            Method method = methodArray[i];
            if (method.getName().equalsIgnoreCase(commandStr)) {
                try {
                    method.invoke((Object) this);
                    dispatched=true;
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ChartPane.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(ChartPane.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(ChartPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (!dispatched) {
            dispatched &= canvas.dispatchActionCommand(commandStr);
        }

        return dispatched;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        //ChartParameters chartParameters=getChartParameters();
        //canvas.setChartParameters(chartParameters);
        //controlpane.setChartParameters(chartParameters);
    }

    /**
     * @return the canvas
     */
    public ChartCanvas getCanvas() {
        return canvas;
    }

    public void actionPerformed(ActionEvent e) {
        JToggleButton jToggleButton=(JToggleButton) e.getSource();

        if (jToggleButton.isSelected()) {
            if (jToggleButton.equals(lastToggleButton)) {
                System.out.println("show Exchange Dialog now!");
            } else {
                lastToggleButton=jToggleButton;
                int index=tabbingPanel.getSelectedIndex(jToggleButton);
                ChartParameters chartParameters=chartParametersArrayList.get(index);
                
                canvas.setChartParameters(chartParameters);
                ChartControlPane.getInstance().updateChartParameters();
            }
        
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {

            

            long id_instrument=getChartParameters().getInstrument().getId();
            list=new JList(Quotes.getInstance().getStockExchangeArrayListFromInstrumentID(id_instrument).toArray(new StockExchange[0]));
            list.addListSelectionListener(this);

            popupMenu=new JPopupMenu("Exchanges");
            popupMenu.add(new JScrollPane(list));

            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            StockExchange stockExchange=(StockExchange) list.getSelectedValue();
            Logger.getAnonymousLogger().info(stockExchange + "selected.");

            popupMenu.setVisible(false);
            popupMenu=null;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            canvas.getChartParameters().setStockExchange(stockExchange);
            canvas.updateChartSettings();
            canvas.drawChart();

            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
