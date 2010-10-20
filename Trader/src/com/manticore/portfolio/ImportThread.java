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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.Node;

public class ImportThread extends Thread {
    String isin;
    String symbol;
    Semaphore semaphore;

    public ImportThread(String isin, Semaphore semaphore) {
        this.isin=isin;
        this.semaphore=semaphore;
    }
    
    public void run() {
        String xpathStr="/html:html/html:body[1]/html:center[1]/html:table/html:tr/html:td/html:table[4]/html:tr/html:td/html:center/html:table[position()>3]/html:tr[1]/html:td/html:table/html:tr/html:td[2][@class='yfnc_h']";
        String urlStr = "http://de.finsearch.yahoo.com/de/?nm=".concat(isin);

        try {
            semaphore.acquire();

            Document document = XMLTools.parseHtml(urlStr);
            Node node=document.selectSingleNode(xpathStr);

            if (node==null) {
                //XMLTools.writeToXML(document, "/tmp/isin" + XMLTools.getCurrentTimeStampStr() + ".xml");
                Logger.getLogger(this.getClass().getName()).severe("could not extract symbol for ".concat(isin));
            } else {
                symbol=node.getText().trim();

                Logger.getLogger(this.getClass().getName()).info("found symbol:" + symbol);
                importFromCSV();
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        semaphore.release();
    }

    public void importFromCSV() throws FileNotFoundException, IOException, SQLException {
        long i=0;
        String urlStr="http://ichart.yahoo.com/table.csv?&d=1&e=31&f=2020&g=d&a=12&b=1&c=1990&ignore=.csv&s="+symbol;
        String line="";

        String sql = "INSERT INTO quotes_eod VALUES ";
        StringBuffer strb = new StringBuffer(sql);

        Logger.getLogger(this.getClass().getName()).info("download:" + urlStr);
        
        URL url=new URL(urlStr);

        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(url.openStream()));
        if ((line=bufferedReader.readLine())!=null ) {
            while ((line=bufferedReader.readLine())!=null ) {
                //@todo: remove that ugly date-quoting hack!
                line=line.replaceFirst(",", "',");

                strb.append("('").append(isin).append("', '").append(line).append("),");
            }
        }
        strb.setLength(strb.length()-1);
        strb.append(";");
        int r = Quotes.getInstance().executeUpdate( strb.toString() );

        bufferedReader.close();
    }
}
