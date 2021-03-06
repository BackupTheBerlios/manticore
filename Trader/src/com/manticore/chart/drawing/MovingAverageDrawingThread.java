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

import com.manticore.util.Settings;
import com.manticore.chart.CandleArrayList;
import com.manticore.chart.ChartCanvas;
import java.awt.geom.Line2D;

public class MovingAverageDrawingThread extends DrawingThread {

    public MovingAverageDrawingThread(ChartCanvas canvas, CandleArrayList candleVector) {
        super(canvas, candleVector);
    }

    public void run() {
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;

        for (int i = 0; i < candleArrayList.size(); i++) {
            Double indicatorValue = candleArrayList.get(i).getMovingAverage();
            if (!candleArrayList.get(i).isFirst() && indicatorValue != null) {
                x1 = x2;
                y1 = y2;

                x2 = canvas.getInset() + canvas.getScaleWidth() * (float) i;
                y2 = canvas.getInset() + canvas.getChartHeight() - (indicatorValue - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
                if (x1 != 0) {
                    g2.setStroke(tlstroke);
                    g2.setColor(Settings.MANTICORE_ORANGE);
                    g2.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
        }

        //source.drawImage(bufferedImage, 0, 0, null);
    }
}
