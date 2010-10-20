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

import com.manticore.swingui.FormatedTextField;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GridPanel extends JScrollPane {
    public final static String[] _columnNames={"id", "underlying", "entry on", "exit on", "profit", "total", "steps", "amount", "fee", "performance"};
    private JTable table;
    private DefaultTableModel tableModel;
    private DefaultTableColumnModel columnModel;

    public GridPanel(ResultSet rs) {
        super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableModel=new DefaultTableModel();
        setTableModel(rs);
        table=new JTable(tableModel);
        table.setAutoCreateColumnsFromModel(false);
        //table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setShowVerticalLines(false);

        FormatedTextField tf0 = new FormatedTextField();
        tf0.setFormat(FormatedTextField.TEXT_FORMAT);
        tf0.setHorizontalAlignment(FormatedTextField.LEADING);

        FormatedTextField tf1 = new FormatedTextField();
        tf1.setFormat(FormatedTextField.INTEGER_FORMAT);

        FormatedTextField tf2 = new FormatedTextField();
        tf2.setFormat(FormatedTextField.DATETIME_FORMAT);

        FormatedTextField tf3 = new FormatedTextField();
        tf3.setFormat(FormatedTextField.DECIMAL_FORMAT);

        FormatedTextField tf4 = new FormatedTextField();
        tf4.setFormat(FormatedTextField.PERCENT_FORMAT);


        columnModel = new DefaultTableColumnModel();
        addTableColumn("id", 48, tf1);
        addTableColumn("underlying", 160, tf0);
        addTableColumn("entry on", 160, tf2);
        addTableColumn("exit on", 160, tf2);
        addTableColumn("profit", 72, tf3);
        addTableColumn("total", 72, tf3);
        addTableColumn("steps", 48, tf1);
        addTableColumn("amount", 96, tf3);
        addTableColumn("fee", 72, tf3);
        addTableColumn("performance", 96, tf4);

        table.setColumnModel(columnModel);
        setViewportView(table);
    }

    public void addTableColumn(String headerValue, int width, TableCellRenderer renderer) {
        TableColumn tableColumn=new TableColumn(0, width, renderer, null);
        tableColumn.setHeaderValue(headerValue);
        columnModel.addColumn(tableColumn);
    }

    public void setTableModel(ResultSet rs) {
        Vector<Vector> data=new Vector();
        try {
            while (rs.next()) {
                Vector row=new Vector();
                row.add(new Long(rs.getLong("id_position")));
                row.add(rs.getString("description"));
                row.add(rs.getTimestamp("timestamp_min"));
                row.add(rs.getTimestamp("timestamp_max"));
                row.add(new Float(rs.getFloat("profit")));
                row.add(new Float(rs.getFloat("profit_total")));
                row.add(new Integer(rs.getInt("transaction_count")));
                row.add(new Float(rs.getFloat("amount")));
                row.add(new Float(rs.getFloat("fee")));
                row.add(new Float(rs.getFloat("profit_rel")));
                data.add(row);
            }
            Vector<String> columnNames=new Vector(_columnNames.length);
            for (int i=0; i<_columnNames.length; i++) columnNames.add(_columnNames[i]);

            tableModel.setDataVector(data, columnNames);

        } catch (SQLException ex) {
            Logger.getLogger(GridPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

}
