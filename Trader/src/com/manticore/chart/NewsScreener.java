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
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.JList;

public class NewsScreener extends TimerTask {

    private static final long TIMER_PERIOD = 300000;
    private static final long TIMER_DELAY = 0;
    private JList newsList;
    
    public NewsScreener(JList newsList) {
        this.newsList = newsList;
        Timer timer = new Timer(true);
        timer.schedule(this, TIMER_DELAY, TIMER_PERIOD);
    }

    @Override
    public void run() {
            Vector<NewsItem> listData = new Vector();
            Site site = WebsiteParser.getInstance().getSite("NewsList");
            while (site!=null && site.hasNextNode()) {
                String time=site.getString("time");
                String date=site.getString("date");
                String href=site.getString("href");
                String caption=site.getString("caption");
                String agency=site.getString("agency");

                NewsItem newsItem = new NewsItem(date, time, caption, href, agency);
                listData.add(newsItem);
            }
            newsList.setListData(listData);
    }

    

}


