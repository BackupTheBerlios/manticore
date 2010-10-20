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

import com.manticore.util.XMLTools;
import com.manticore.database.Quotes;
import com.manticore.foundation.Instrument;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import com.manticore.ui.ProgressPanel;
import com.manticore.util.ThreadArrayList;

public class DataImportManager extends Thread  {
    public final static int MAX_CONECTIONS=5;
    private ArrayList<Page> pageArrayList;
    private TreeMap<Instrument,ArrayList<String>> instrumentTreeMap;
    private ListPane listPane;
    private ProgressPanel progressPanel;

    public DataImportManager(ListPane listPane) {
        
        instrumentTreeMap=new TreeMap<Instrument, ArrayList<String>>();
        this.listPane=listPane;
    }

    public void run() {
        int stringArrayListSize=0;
        try {
            ArrayList<Instrument> instrumentArrayList = Quotes.getInstance().getInstrumentArrayList();
            ThreadArrayList threadArrayList = new ThreadArrayList(instrumentArrayList.size());
            progressPanel=new ProgressPanel("Importing data from internet", instrumentArrayList.size());
            Semaphore semaphore = new Semaphore(MAX_CONECTIONS, true);

            //@fixme: fix that path
            Document document = XMLTools.readXML("/home/are/src/WaveTrader/etc/triggers.xml");
            List<Node> pageNodeList=document.selectNodes("/pages/page");
            pageArrayList=new ArrayList(pageNodeList.size());
            for (int i=0; i<pageNodeList.size(); i++) {
                Page page=new Page( (Element) pageNodeList.get(i) );


                stringArrayListSize+=page.size();
                pageArrayList.add(page);
            }

            for (int i = 0; i < instrumentArrayList.size()  && i<10; i++) {
                //PageDataImportThread pageDataImportThread=new PageDataImportThread(progressPanel, semaphore, instrumentTreeMap, instrumentArrayList.get(i), pageArrayList, stringArrayListSize);
                //threadArrayList.addThread(pageDataImportThread);
            }

            threadArrayList.join();

            /*
            Iterator<Instrument> iterator=instrumentTreeMap.navigableKeySet().iterator();
            while (iterator.hasNext()) {

                Instrument security=iterator.next();
                ArrayList<String> stringArrayList=instrumentTreeMap.get(security);

                StringBuffer stringBuffer=new StringBuffer(security.getName());

                for (int i=0; i<stringArrayList.size();i++) {
                    stringBuffer.append( "; ").append(stringArrayList.get(i));
                }

                System.out.println(stringBuffer.toString());
            }
            */

            //listPane.setInstrumentTreeMap(instrumentTreeMap);
            progressPanel.setVisible(false);
        }
        catch (Exception ex) {
            Logger.getLogger(DataImportManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
