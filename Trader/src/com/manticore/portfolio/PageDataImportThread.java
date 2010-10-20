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
import com.manticore.foundation.Instrument;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import org.dom4j.Document;
import org.dom4j.Node;
import com.manticore.ui.ProgressPanel;

public class PageDataImportThread extends Thread {
    private ProgressPanel progressPanel;
    Semaphore semaphore;
    TreeMap<Instrument,Vector<String>> InstrumentTreeMap;
    Instrument instrument;
    Vector<Page> pageVector;
    int stringVectorSize;

    public PageDataImportThread(ProgressPanel progressPanel, Semaphore semaphore, TreeMap<Instrument,Vector<String>> instrumentTreeMap, Instrument instrument, Vector<Page> pageVector, int stringVectorSize) {
        this.progressPanel=progressPanel;
        this.semaphore=semaphore;
        this.InstrumentTreeMap=instrumentTreeMap;
        this.instrument=instrument;
        this.pageVector=pageVector;
        this.stringVectorSize=stringVectorSize;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            
            Vector<String> stringVector=new Vector(stringVectorSize);

            for (int i=0; i<pageVector.size(); i++) {
                Page page=pageVector.get(i);
                String urlStr=page.urlStr.concat(instrument.getWkn());

                Document document=XMLTools.parseHtml(urlStr);
                //XMLTools.writeToXML(document, "/tmp/" + page.name + ".xml");

                for (int k=0; k<page.size(); k++) {
                    Trigger trigger=page.triggerVector.get(k);

                    Node node=document.selectSingleNode(trigger.xpathStr);

                    if (node!=null) {
                        String string= node.getText();

                        if (string.length() > 0 && trigger.regexStr.length() > 0) {
                            Matcher m = trigger.pattern.matcher(string);
                            if (m.find()) {
                                string = m.group(1);
                            }
                        }

                        string=string.replaceAll("^[ \\s]+|\\s{2,}+|[ \\s]+$", "");

                        stringVector.add(string);
                    } else {
                        Logger.getLogger(this.getClass().getName()).warning("page " + page.name + ", trigger " + trigger.name + " failed!");
                    }
                }
            }

            InstrumentTreeMap.put(instrument, stringVector);
            progressPanel.raise();
        } catch (Exception ex) {
            Logger.getLogger(PageDataImportThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        semaphore.release();
    }
}
