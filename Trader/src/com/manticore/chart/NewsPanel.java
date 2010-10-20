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

import java.awt.GridLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class NewsPanel extends JPanel implements ListSelectionListener {
    private JList newsList;
    private JEditorPane editorPane;
    
    public NewsPanel() {
        
        newsList=new JList();
        newsList.addListSelectionListener(this);
        
        JScrollPane scrollPane1=new JScrollPane(newsList);
        
        editorPane=new JEditorPane();
        editorPane.setEditable(false);
        
        //editorPane.getFont().deriveFont(8);
        //editorPane.setFont( editorPane.getFont().deriveFont(8) );
        JScrollPane scrollPane2=new JScrollPane(editorPane);
        
        setLayout(new GridLayout(2,1));
        add(scrollPane1);
        add(scrollPane2);
        
        NewsScreener newsScreener=new NewsScreener(newsList);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            //System.out.println("New News selected");
            try {
                NewsItem newsItem = (NewsItem) newsList.getSelectedValue();
                if (newsItem !=null) {
                    String fileName=newsItem.extractContent();

                    editorPane.setPage( fileName );
                    File file=new File(fileName);
                    file.delete();
                }
            } catch (Exception ex) {
                Logger.getLogger(NewsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
}
