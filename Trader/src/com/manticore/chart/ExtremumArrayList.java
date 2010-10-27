/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.chart;

import com.manticore.foundation.Extremum;
import java.util.ArrayList;
import org.joda.time.DateTime;

/**
 *
 * @author are
 */
public class ExtremumArrayList extends ArrayList<Extremum> {
    public void adjustExtremum() {
        Extremum low=null;
        Extremum high=null;

        for (int i=2; i<size()-2; i++) {
            if (get(i).getType()<Extremum.TYPE_EXTREMUM_NONE) {
                if (get(i-2).getPrice()>get(i).getPrice() && get(i).getPrice()<=get(i+2).getPrice()) {
                    get(i).setType(Extremum.TYPE_EXTREMUM_LOW);
                }
            }

            if (get(i).getType()>Extremum.TYPE_EXTREMUM_NONE) {
                if (get(i-2).getPrice()<get(i).getPrice() && get(i).getPrice()>=get(i+2).getPrice()) {
                    get(i).setType(Extremum.TYPE_EXTREMUM_HIGH);
                }
            }
        }
    }

    public Extremum getLastTmpLow(DateTime dateTime) {
        Extremum extremum=null;

        for (int i=0; i<size() && get(i).getDateTime().isBefore(dateTime); i++) {
            if (get(i).getType()<Extremum.TYPE_EXTREMUM_NONE) extremum=get(i);
        }

        return extremum;
    }

    public Extremum getPreviousTmpLow(DateTime dateTime) {
        Extremum extremum=getLastTmpLow(dateTime);

        if (extremum!=null) {
            extremum=getLastTmpLow(extremum.getDateTime());
        } else {
            extremum=null;
        }

        return extremum;
    }

    public Extremum getLastLow(DateTime dateTime) {
        Extremum extremum=null;

        for (int i=0; i<size() && get(i).getDateTime().isBefore(dateTime); i++) {
            if (get(i).getType()==Extremum.TYPE_EXTREMUM_LOW) extremum=get(i);
        }

        return extremum;
    }

    public Extremum getPreviousLow(DateTime dateTime) {
        Extremum extremum=getLastLow(dateTime);

        if (extremum!=null) {
            extremum=getLastLow(extremum.getDateTime());
        } else {
            extremum=null;
        }

        return extremum;
    }

    public Extremum getLastTmpHigh(DateTime dateTime) {
        Extremum extremum=null;

        for (int i=0; i<size() && get(i).getDateTime().isBefore(dateTime); i++) {
            if (get(i).getType()>Extremum.TYPE_EXTREMUM_NONE) extremum=get(i);
        }

        return extremum;
    }

    public Extremum getPreviousTmpHigh(DateTime dateTime) {
        Extremum extremum=getLastTmpHigh(dateTime);

        if (extremum!=null) {
            extremum=getLastTmpHigh(extremum.getDateTime());
        } else {
            extremum=null;
        }

        return extremum;
    }

    public Extremum getLastHigh(DateTime dateTime) {
        Extremum extremum=null;

        for (int i=0; i<size() && get(i).getDateTime().isBefore(dateTime); i++) {
            if (get(i).getType()==Extremum.TYPE_EXTREMUM_HIGH) extremum=get(i);
        }
        
        return extremum;
    }

    public Extremum getPreviousHigh(DateTime dateTime) {
        Extremum extremum=getLastHigh(dateTime);

        if (extremum!=null) {
            extremum=getLastHigh(extremum.getDateTime());
        } else {
            extremum=null;
        }

        return extremum;
    }

    public void println() {
        for (int i=0; i<size(); i++) {
            System.out.println(get(i).getType() + " " + get(i).getDateTime() + " " + get(i).getPrice());
        }
    }
}
