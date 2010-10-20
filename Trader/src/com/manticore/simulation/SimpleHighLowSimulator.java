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
package com.manticore.simulation;

import com.manticore.foundation.Candle;
import com.manticore.chart.CandleArrayList;
import com.manticore.chart.ChartParameters;
import com.manticore.foundation.Transaction;
import java.util.Vector;
import org.joda.time.DateTime;

public class SimpleHighLowSimulator extends Thread {
    public final static int MODE_SHORT=-1;
    public final static int MODE_NEUTRAL=0;
    public final static int MODE_LONG=1;

    private Vector<Transaction> transactionVector;

    CandleArrayList chartSettings;
    private Double TRIGGER_ENTER = 85d;
    private Double TRIGGER_EXIT = 0d;

    private int mode=MODE_NEUTRAL;
    private Float lastHigh;
    private Float lastLow;


    public SimpleHighLowSimulator(ChartParameters chartParameters) {
        chartSettings = new CandleArrayList(chartParameters);
        transactionVector=new Vector();
    }

    public void run() {
//        for (int i = 1; i < chartSettings.size() - 1; i++) {
//            Candle candle = chartSettings.get(i);
//            if (candle.getLow()!=null && candle.getHigh()!=null && candle.getOpening()!=null && candle.getClosing()!=null) {
//
//
//
//                if (triggersEnterLong(i) ) {
//                    DateTime timestamp=candle.getEnd();
//                    String assetType="C";
//                    Double price=candle.getClosing().doubleValue();
//                    Integer quantity=1;
//
//                    Transaction transaction=new Transaction();
//                    transaction.timestamp=timestamp.toDate();
//                    transaction.price=price;
//                    transaction.quantity=quantity;
//                    transaction.id_transaction_type=assetType;
//
//                    transactionVector.add(transaction);
//                } else if (triggersEnterShort(i)) {
//
//                    DateTime timestamp=candle.getEnd();
//                    String assetType="P";
//                    Double price=candle.getClosing().doubleValue();
//                    Integer quantity=1;
//
//                    Transaction transaction=new Transaction();
//                    transaction.timestamp=timestamp.toDate();
//                    transaction.price=price;
//                    transaction.quantity=quantity;
//                    transaction.id_transaction_type=assetType;
//
//                    transactionVector.add(transaction);
//                }
//
//                if (candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_LOW) {
//                    if (lastLow!=null && candle.getLow()<lastLow) {
//                            mode=MODE_SHORT;
//                    }
//                    lastLow=candle.getLow();
//                }
//
//                if (candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_HIGH) {
//                    if (lastHigh!=null && candle.getHigh()>lastHigh) {
//                            mode=MODE_LONG;
//                    }
//
//                    lastHigh=candle.getHigh();
//                }
//            }
//        }
    }

    private boolean triggersEnterLong(int i) {
        Candle candle = chartSettings.get(i-1);
        boolean trigger = (mode==MODE_LONG && candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_LOW);
        return trigger;
    }

    private boolean triggersEnterShort(int i) {
        Candle candle = chartSettings.get(i-1);
        boolean trigger = (mode==MODE_SHORT && candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_HIGH);
        return trigger;
    }

    public Vector<Transaction> getTransactionVector() {
        return transactionVector;
    }

    private long getInstrumentID() {
        return chartSettings.getInsrument().getId();
    }

    private long getStockExchangeID() {
        return chartSettings.getStockExchange().getId();
    }

    public DateTime getDateTimeFrom() {
        return chartSettings.getDateTimeFrom().toDateTime();
    }
}
