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
package com.manticore.chart.drawing;

import com.manticore.foundation.Candle;
import com.manticore.chart.*;
import com.manticore.foundation.Transaction;
import com.manticore.util.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.Vector;
import org.joda.time.Interval;

public class TransactionMarkerDrawingThread extends DrawingThread {

    Graphics2D source;

    public TransactionMarkerDrawingThread(ChartCanvas canvas, CandleArrayList candleVector) {
        super(canvas, candleVector);
    }

    @Override
    public void run() {
//        Vector<Transaction> transactionVector=candleVector.getTransactionMarkerVector();
//        if (transactionVector!=null) {
//        int k = 0;
//        for (int i = 0; i < candleVector.size(); i++) {
//            Candle candle = candleVector.get(i);
//            Interval interval=candle.getInterval();
//            while (k < transactionVector.size() && interval.contains(transactionVector.get(k).timestamp.getTime())) {
//                drawCandle(g2, candle, transactionVector.get(k));
//                k++;
//            }
//        }
//        }
    }

    private void drawCandle(Graphics2D g2, Candle c, Transaction transactionMarker) {
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double y3 = 0;
        double y4 = 0;

        x1 = canvas.getInset() + canvas.getScaleWidth() * (float) c.getPosition();
        y1 = canvas.getInset() + canvas.getChartHeight() - (transactionMarker.underlying_entry - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();

        y3 = (transactionMarker.id_transaction_type.equals("C") ? +8 : -8);
        y3 *= (transactionMarker.quantity > 0 ? 1 : -1);
        y2 = y1 + y3;

        g2.setColor(transactionMarker.quantity > 0 ? Settings.MANTICORE_DARK_BLUE : Settings.MANTICORE_ORANGE);

        GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD,4);
        generalPath.moveTo(x1, y1);
        generalPath.lineTo(x1 - 8, y2);
        generalPath.lineTo(x1 + 8, y2);
        generalPath.lineTo(x1, y1);
        generalPath.closePath();

        g2.fill(generalPath);
    }
}
