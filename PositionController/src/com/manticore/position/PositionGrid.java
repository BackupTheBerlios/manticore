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
package com.manticore.position;

import com.manticore.connection.Flatex;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.Position;
import com.manticore.foundation.PositionDataStorage;
import com.manticore.foundation.TanReader;
import com.manticore.swingui.GridBagPane;
import com.manticore.util.Settings;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

public class PositionGrid extends GridBagPane implements ActionListener, ChangeListener {
    private static final String[] columnLabel={"Asset", "T", "L", "#", "Price", "P/L", "Code"};

    private static final String LIST_ADD_PNG = "/com/manticore/position/list-add.png";
    private static final String LIST_REMOVE_PNG = "/com/manticore/position/list-remove.png";
    JTable table;
    TableModel tableModel;
    JButton addButton;
    JButton removeButton;
    private ArrayList<PositionControler> positionControlerArrayList;
    private Instrument instrument;
    private TanReader tanReader;
    private PositionDataStorage positionDataStorage;

    public PositionGrid(PositionDataStorage positionDataStorage, TanReader tanReader) {
        this.tanReader = tanReader;
        this.positionDataStorage = positionDataStorage;
        buildControls();
        
        //find open positions
        ArrayList<Position> positionArrayList = positionDataStorage.getPositionArrayList(true);
        if (positionArrayList.size() > 0) {
            Flatex.getInstance(tanReader);
        }

        Iterator<Position> iterator = positionArrayList.iterator();
        while (iterator.hasNext()) {
            addPositionControler(iterator.next());
        }
    }

    public boolean hasOpenPosition() {
        ArrayList<Position> positionArrayList = positionDataStorage.getPositionArrayList(true);
        return positionArrayList.size() > 0;
    }

    public boolean hasOpenLongPosition() {
        boolean found=false;
        Iterator<Position> iterator = positionDataStorage.getPositionArrayList(true).iterator();
        while (iterator.hasNext() && !found) {
            found |= iterator.next().isLong();
        }

        return found;
    }

    public boolean hasOpenShortPosition() {
        boolean found=false;
        Iterator<Position> iterator = positionDataStorage.getPositionArrayList(true).iterator();
        while (iterator.hasNext() && !found) {
            found |= iterator.next().isShort();
        }

        return found;
    }

    private void buildControls() {
        positionControlerArrayList = new ArrayList();
        tableModel=new TableModel();
        table = new JTable();
        table.setDoubleBuffered(true);
        table.setModel(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFont(Settings.SMALL_MANTICORE_FONT.deriveFont(6));
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row= table.getSelectedRow();
                    PositionView positionView=getPositionControlerArrayList().get(row).getPositionView();
                    positionView.setVisible(!positionView.isVisible());
                }
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMaxWidth(30);
        table.getColumnModel().getColumn(3).setMaxWidth(35);
        table.getColumnModel().getColumn(4).setMaxWidth(45);
        table.getColumnModel().getColumn(5).setMaxWidth(45);

        addButton = new JButton(new ImageIcon(this.getClass().getResource(LIST_ADD_PNG)));
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        addButton.setIconTextGap(0);
        addButton.setFocusPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setMargin(new Insets(1, 1, 1, 1));
        removeButton = new JButton(new ImageIcon(this.getClass().getResource(LIST_REMOVE_PNG)));
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);
        removeButton.setIconTextGap(0);
        removeButton.setFocusPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setMargin(new Insets(1, 1, 1, 1));
        add(new JScrollPane(table), "fill=BOTH, weightx=1f, weighty=1f, gridheight=2");
        add(addButton, "size=16 16, fill=NONE, weightx=0f, weighty=0f, gridheight=1");
        add(removeButton, "size=16 16, gridx=1, gridy=1, fill=NONE, weightx=0f, weighty=0f, gridheight=1");
        validate();
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("add")) {
            addPositionControler();
        } else if (e.getActionCommand().equals("remove")) {
            removePositionControler();
        }
    }

    public PositionControler addPositionControler(Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
        PositionControler positionControler=null;
        if (instrument!=null) {
            positionControler = new PositionControler(positionDataStorage, tanReader, instrument,stopLoss, entry, stopBuy, takeProfit, isin);
            positionControler.addStreamChangeListener(this);
            positionControlerArrayList.add(positionControler);
            tableModel.fireTableDataChanged();
        } else {
            Logger.getLogger(getClass().getName()).warning("Can not open new position, as no instrument was selected yet!");
        }

        return positionControler;
    }

    private void addPositionControler() {
        if (instrument!=null) {
        PositionControler positionControler = new PositionControler(positionDataStorage, tanReader, instrument);
        positionControler.addStreamChangeListener(this);
        positionControlerArrayList.add(positionControler);
        tableModel.fireTableDataChanged();
        showPositionControllerFrame(positionControler);
        } else {
            Logger.getLogger(getClass().getName()).warning("Can not open new position, as no instrument was selected yet!");
        }
    }

    private void addPositionControler(Position position) {
        PositionControler positionControler = new PositionControler(positionDataStorage, tanReader, position);
        positionControler.addStreamChangeListener(this);
        positionControlerArrayList.add(positionControler);
        tableModel.fireTableDataChanged();
        showPositionControllerFrame(positionControler);
    }

    private void showPositionControllerFrame(PositionControler positionControler) throws SecurityException, HeadlessException {
        positionControler.getPositionView().setVisible(true);
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    private void removePositionControler() {
        int row = table.getSelectedRow();
        PositionControler positionControler=positionControlerArrayList.get(row);
        if (positionControler.close()) {
            positionControlerArrayList.remove(positionControler);
            tableModel.fireTableDataChanged();
        }
    }

    /**
     * @return the tanReader
     */
    public TanReader getTanReader() {
        return tanReader;
    }

    /**
     * @param tanReader the tanReader to set
     */
    public void setTanReader(TanReader tanReader) {
        this.tanReader = tanReader;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        tableModel.fireTableDataChanged();
    }

    /**
     * @return the positionControlerArrayList
     */
    public ArrayList<PositionControler> getPositionControlerArrayList() {
        return positionControlerArrayList;
    }

    private class TableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return getPositionControlerArrayList().size();
        }

        @Override
        public int getColumnCount() {
            return columnLabel.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ArrayList<String> data=getPositionControlerArrayList().get(rowIndex).getData();
            String s="";

            if (data.size()>columnIndex) s=data.get(columnIndex);
            return s;
        }

        @Override
        public String getColumnName(int column) {
            return columnLabel[column];
        }

        @Override
        public Class getColumnClass(int column) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
