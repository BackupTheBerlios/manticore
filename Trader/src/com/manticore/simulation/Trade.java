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

import com.manticore.foundation.Tick;
import com.manticore.foundation.Transaction;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Trade extends Thread {

    public final static String TYPE_CALL = "C";
    public final static String TYPE_PUT = "P";
    public final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
    private ArrayList<Tick> tickArrayList;
    private DateTime buyDateTime;
    private DateTime sellDateTime;
    private String assetType;
    private Float buyPrice;
    private Float sellPrice;
    private Float initialStoppLoss;
    private Float trailingStoppLoss;
    Float stopLossPrice=null;
    private Float maximumDrawDown;
    private Float result = 0f;
    private boolean stop = false;

    public Trade(ArrayList<Tick> tickArrayList, String typeStr, String buyDateTimeStr, Float initialStoppLoss, Float trailingStoppLoss, Float maximumDrawDown) {
        this.tickArrayList = tickArrayList;
        assetType = typeStr;
        buyDateTime = DATE_TIME_FORMATTER.parseDateTime(buyDateTimeStr);
        this.initialStoppLoss = initialStoppLoss;
        this.trailingStoppLoss = trailingStoppLoss;
        this.maximumDrawDown = maximumDrawDown;

    }

    public Trade(ArrayList<Tick> tickArrayList, String assetType, DateTime buyDateTime, Float buyPrice, Float initialStoppLoss, Float trailingStoppLoss, Float maximumDrawDown) {
        this.tickArrayList = tickArrayList;
        this.assetType = assetType;
        this.buyDateTime = buyDateTime;
        this.initialStoppLoss = initialStoppLoss;
        this.trailingStoppLoss = trailingStoppLoss;
        this.maximumDrawDown = maximumDrawDown;

    }

    public Trade(ArrayList<Tick> tickArrayList, Transaction transaction, Float initialStoppLoss, Float trailingStoppLoss, Float maximumDrawDown) {
        this.tickArrayList = tickArrayList;
        this.assetType = transaction.id_transaction_type;
        this.buyDateTime = new DateTime(transaction.timestamp);
        this.buyPrice=transaction.price.floatValue();
        this.initialStoppLoss = initialStoppLoss;
        this.trailingStoppLoss = trailingStoppLoss;
        this.maximumDrawDown = maximumDrawDown;
        stopLossPrice = (assetType.equals(TYPE_CALL)) ? buyPrice - (buyPrice*initialStoppLoss) : buyPrice + (buyPrice*initialStoppLoss);
    }

    public void run() {
        for (int i = 0; i < tickArrayList.size() & !stop; i++) {
            Tick tick = tickArrayList.get(i);

            if (buyPrice == null && tick.getDateTime().isAfter(buyDateTime)) {
                buyPrice = tick.getPrice();
                //System.out.println(DecimalFormat.getInstance().format( buyPrice));
                buyDateTime = tick.getDateTime();

                stopLossPrice = (assetType.equals(TYPE_CALL)) ? buyPrice - (buyPrice*initialStoppLoss) : buyPrice + (buyPrice*initialStoppLoss);
            } else if (buyPrice != null && tick.getDateTime().isAfter(buyDateTime)) {
                sellPrice = tick.getPrice();
                sellDateTime = tick.getDateTime();

                //System.out.println(DecimalFormat.getInstance().format( sellPrice));

                Float currentResult = (assetType.equals(TYPE_CALL)) ? sellPrice - buyPrice : buyPrice - sellPrice;

                // trailing Stopp Loss
                stop =  (assetType.equals(TYPE_CALL)) ? ((buyPrice + result)*(1-trailingStoppLoss) > sellPrice ) : ((buyPrice - result)*(1+trailingStoppLoss) < sellPrice );

                // initial Stopp Loss
                stop |= (assetType.equals(TYPE_CALL)) ? (sellPrice < stopLossPrice) : (sellPrice > stopLossPrice);

                // end of day
                stop |= (i==tickArrayList.size()) || ( tick.getDateTime().getDayOfMonth() < tickArrayList.get(i+1).getDateTime().getDayOfMonth());

                if (currentResult > result || stop) {
                    result = currentResult;
                }
            }
        }
    }

    /**
     * @return the result
     */
    public Float getResult() {
        return result;
    }

    /**
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * @return the buyDateTime
     */
    public DateTime getBuyDateTime() {
        return buyDateTime;
    }

    /**
     * @return the sellDateTime
     */
    public DateTime getSellDateTime() {
        return sellDateTime;
    }

    /**
     * @return the buyPrice
     */
    public Float getBuyPrice() {
        return buyPrice;
    }

    /**
     * @return the sellPrice
     */
    public Float getSellPrice() {
        return sellPrice;
    }

    /**
     * @param sellPrice the sellPrice to set
     */
    public void setSellPrice(Float sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Duration getDuration() {
        return new Duration (buyDateTime, sellDateTime);
    }

    /**
     * @return the type
     */
    public String getType() {
        return assetType;
    }
}
