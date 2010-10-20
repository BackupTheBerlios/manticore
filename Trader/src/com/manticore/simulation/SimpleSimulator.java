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

public class SimpleSimulator extends Thread {
    public final static int SHORT=-1;
    public final static int NEUTRAL=0;
    public final static int LONG=1;

    private Vector<Transaction> transactionVector;

    CandleArrayList chartSettings;
    private Double TRIGGER_ENTER = 85d;
    private Double TRIGGER_EXIT = 0d;

    public SimpleSimulator(ChartParameters chartParameters) {
        chartSettings = new CandleArrayList(chartParameters);
        transactionVector=new Vector();
    }

    public void run() {
//        for (int i = 1; i < chartSettings.size() - 1; i++) {
//            Candle candle = chartSettings.get(i);
//            if (candle.getLow() > 0 && candle.getHigh() > 0 && candle.getOpening() > 0 && candle.getClosing() > 0) {
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
//                   Transaction transaction=new Transaction();
//                    transaction.timestamp=timestamp.toDate();
//                    transaction.price=price;
//                    transaction.quantity=quantity;
//                    transaction.id_transaction_type=assetType;
//                    transactionVector.add(transaction);
//                }
//            }
//        }
    }

    private boolean triggersEnterLong(int i) {
        Candle candle0 = chartSettings.get(i - 1);
        Candle candle1 = chartSettings.get(i);
        boolean trigger = (candle0.getAroonOszilator() != null && candle1.getAroonOszilator() != null && candle1.getAroonOszilator() > -TRIGGER_ENTER && candle0.getAroonOszilator() < -TRIGGER_ENTER);
        //trigger &= (chartSettings.get(i).getStart().getMinuteOfDay() > (9 * 60) && chartSettings.get(i).getStart().getMinuteOfDay() < (20 * 60));
        return trigger;
    }

    private boolean triggersEnterShort(int i) {
        Candle candle0 = chartSettings.get(i - 1);
        Candle candle1 = chartSettings.get(i);
        boolean trigger = (candle0.getAroonOszilator() != null && candle1.getAroonOszilator() != null && candle1.getAroonOszilator() < TRIGGER_ENTER && candle0.getAroonOszilator() > TRIGGER_ENTER);
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
