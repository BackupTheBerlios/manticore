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

package com.manticore.util;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadArrayList extends ArrayList<Thread> {
    public ThreadArrayList(int size) {
        super(size);
    }
    
    public void addThread(Thread newThread) {
        this.add(newThread);
        newThread.start();
    }

    public boolean join() {
        for (int i=0; i<size(); i++) {
            try {
                Thread thread=get(i);
                if (thread!=null) {
                    get(i).join();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadArrayList.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }
}
