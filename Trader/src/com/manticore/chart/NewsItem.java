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

import com.manticore.parser.WebsiteParser;
import com.manticore.parser.WebsiteParser.Site;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.xml.sax.Attributes;

public class NewsItem  {

    private String caption = "";
    private String timeStr = "";
    private String agency = "";
    private String id = "";
    private static final int ADD_NOTHING = 0;
    private static final int ADD_TIME = 1;
    private static final int ADD_CAPTION = 2;
    private static final int ADD_AGENCY = 3;
    private int mode = ADD_NOTHING;
    private int td = 0;
    private int div = 0;
    private String text = "";

    NewsItem(String date, String time, String caption, String href, String agency) {
        this.timeStr=time;
        this.caption=caption;
        this.id=href;
        this.agency=agency;
    }

    public String extractContent() throws Exception {
        File file=File.createTempFile("manticore-trader-news", ".html");
        StringBuffer htmlContent= new StringBuffer();

        Site site = WebsiteParser.getInstance().getSite("NewsItem","id", id);
        if (site.hasNextNode()) {
            htmlContent.append("<html><body>").append(site.getString("item")).append("</body></html>");

            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(htmlContent.toString());
            outputStreamWriter.flush();
            fileOutputStream.close();
        }
        return file.toURI().toURL().toExternalForm();
    }

    public String toString() {
        return new StringBuffer()
                .append(timeStr)
                .append(" ")
                .append(caption)
                .append(" ")
                .append(agency)
                .toString();
    }

}
