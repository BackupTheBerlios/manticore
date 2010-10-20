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
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ListIterator;

public class CandleDrawingThread extends DrawingThread {
    private long lh = -1;
    private long ll = -1;

    public CandleDrawingThread(ChartCanvas canvas, CandleArrayList candleVector) {
        super(canvas, candleVector);
    }

    @Override
    public void run() {
        g2.setStroke(tlstroke);

        for (int i = 0; i < candleArrayList.size(); i++) {
            Candle candle = candleArrayList.get(i);
            if (candle.getClosing() != null) {
                drawCandle(g2, candle);

                if (candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_LOW) {
                    double x = canvas.getInset() + canvas.getScaleWidth() * (float) candle.getPosition();
                    double y = canvas.getInset() + canvas.getChartHeight() - (candle.getLow() - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
                    g2.draw(new Line2D.Double(x, y, x + 10, y));
                }

                if (candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_HIGH) {
                    double x = canvas.getInset() + canvas.getScaleWidth() * (float) candle.getPosition();
                    double y = canvas.getInset() + canvas.getChartHeight() - (candle.getHigh() - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
                    g2.draw(new Line2D.Double(x, y, x + 10, y));
                }
            }
        }
    }

    private void drawCandle(Graphics2D g2, Candle c) {
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
        double y3 = 0;
        double y4 = 0;

        x1 = canvas.getInset() + canvas.getScaleWidth() * (float) c.getPosition();
        x2 = 4;
        if (c.getOpening() >= c.getClosing()) {
            y1 = canvas.getInset() + canvas.getChartHeight() - (c.getOpening() - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
            y2 = (c.getOpening() - c.getClosing()) * canvas.getScaleHeight();
        } else {
            y1 = canvas.getInset() + canvas.getChartHeight() - (c.getClosing() - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
            y2 = (c.getClosing() - c.getOpening()) * canvas.getScaleHeight();
        }
        y3 = canvas.getInset() + canvas.getChartHeight() - (c.getHigh() - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
        y4 = canvas.getInset() + canvas.getChartHeight() - (c.getLow() - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();

        g2.setColor(c.getColor());
        g2.fill(new Rectangle2D.Double(x1 - x2 / 2, y1, x2, y2));

        g2.draw(new Line2D.Double(x1, y3, x1, y4));


    }
}
