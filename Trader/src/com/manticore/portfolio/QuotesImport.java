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

import com.manticore.database.Quotes;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import com.manticore.util.ThreadArrayList;

public class QuotesImport {

    public final static int MAX_CONECTIONS = 5;

    public QuotesImport() {
        ArrayList<String> isinArrayList = Quotes.getInstance().getIsinArrayList();
        ThreadArrayList threadArrayList = new ThreadArrayList(isinArrayList.size());
        Semaphore semaphore = new Semaphore(MAX_CONECTIONS, true);

        //@todo: is it possible only to append the new one?
        Quotes.getInstance().executeUpdate("DELETE FROM quotes_eod;");
        for (int i = 0; i < isinArrayList.size(); i++) {
            threadArrayList.addThread(new ImportThread(isinArrayList.get(i), semaphore));
        }
        threadArrayList.join();


        System.exit(0);
    }

    public static void main(String args[]) {
        new QuotesImport();
    }
}
