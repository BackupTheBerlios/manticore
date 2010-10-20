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

package com.manticore.database;

import com.manticore.parser.WebsiteParser;
import com.manticore.parser.WebsiteParser.Site;
import com.manticore.util.Settings;

/**
 *
 * @author are
 */
public class XID {
    private static String xid="";

    public static String getXID() {
        Settings.setProxy();

        //@ todo: test for expired XIDs directly from XIDs timestamp
        if (xid.length() == 0) {
//            try {
//                //https://www.cortalconsors.de/Finanzinfos/Aktien/java_charts_popup_desk
//                //<param name="XID" value="32373b1b-4894d8df-98441d5d2f405fbb"><param name="XID_EXPIRES" value=1217714399000>
//                String urlStr = "https://www.cortalconsors.de/Kurse-Maerkte/Aktien/java-charts-popup-desk";
//                String xPathStr = "//html:param[@name='XID']/@value";
//                Document doc = XMLTools.parseHtml(urlStr);
//
//                Node node = doc.selectSingleNode(xPathStr);
//                if (node != null) {
//                    xid = node.getText();
//                    System.out.println("found XID: ".concat(xid));
//                } else {
//                    Logger.getLogger(XID.class.getName()).warning("No XID found! Please check " + XMLTools.writeTempXMLFile(doc));
//                }

                Site site=WebsiteParser.getInstance().getSite("ConsorsApplet");
                if (site.hasNextNode()) {
                    xid=site.getString("XID");
                    System.out.println("found XID: ".concat(xid));
                }
                
//            } catch (Exception ex) {
//                Logger.getLogger(XID.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        return xid;
    }
}
