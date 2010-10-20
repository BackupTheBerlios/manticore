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
import java.awt.geom.GeneralPath;

public class BollingerBandsDrawingThread extends DrawingThread {
    public BollingerBandsDrawingThread(ChartCanvas canvas, CandleArrayList candleVector) {
        super(canvas, candleVector);
    }

    public void run() {
        double x1 = 0;
        double y1 = 0;
        double y2 = 0;
        double y3 = 0;
        boolean isStartPoint = true;
        GeneralPath pathUpperBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
        GeneralPath pathMiddleBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
        GeneralPath pathLowerBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());

        for (int i = 0; i < candleArrayList.size(); i++) {
            Double bbUpperBand = candleArrayList.get(i).getBbUpperBand();
            Double bbMiddleBand = candleArrayList.get(i).getBbMiddleBand();
            Double bbLowerBand = candleArrayList.get(i).getBbLowerBand();

            if ((candleArrayList.get(i).isFirst() || bbUpperBand == null) && !isStartPoint) {
                pathLowerBand.lineTo(x1, y1);
                pathUpperBand.append(pathLowerBand.getPathIterator(null), true);
                pathUpperBand.closePath();

                g2.setColor(Settings.MANTICORE_LIGHT_BLUE_TRANSPARENT);
                g2.fill(pathUpperBand);

                g2.setColor(Settings.MANTICORE_DARK_GREY);
                g2.setStroke(glstroke);
                g2.draw(pathMiddleBand);

                isStartPoint=true;

                pathUpperBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
                pathMiddleBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
                pathLowerBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
            }

            if (i>= candleArrayList.size()-1 && !isStartPoint) {
                pathLowerBand.lineTo(x1, y1);
                pathUpperBand.append(pathLowerBand.getPathIterator(null), true);
                pathUpperBand.closePath();

                g2.setColor(Settings.MANTICORE_LIGHT_BLUE_TRANSPARENT);
                g2.fill(pathUpperBand);

                g2.setColor(Settings.MANTICORE_DARK_GREY);
                g2.setStroke(glstroke);
                g2.draw(pathMiddleBand);

                isStartPoint=true;

                pathUpperBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
                pathMiddleBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
                pathLowerBand = new GeneralPath(GeneralPath.WIND_EVEN_ODD, candleArrayList.size());
            }

            if (!candleArrayList.get(i).isFirst() && bbUpperBand != null) {
                x1 = canvas.getInset() + canvas.getScaleWidth() * (float) i;
                y1 = canvas.getInset() + canvas.getChartHeight() - (bbUpperBand - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
                y2 = canvas.getInset() + canvas.getChartHeight() - (bbLowerBand - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();
                y3 = canvas.getInset() + canvas.getChartHeight() - (bbMiddleBand - canvas.getChartParameters().getAdjustedMinPrice()) * canvas.getScaleHeight();

                if (isStartPoint) {
                    pathUpperBand.moveTo(x1, y2);
                    pathUpperBand.lineTo(x1, y1);
                    pathLowerBand.moveTo(x1, y2);
                    pathMiddleBand.moveTo(x1, y3);
                    isStartPoint = false;
                } else {
                    pathUpperBand.lineTo(x1, y1);
                    pathLowerBand.lineTo(x1, y2);
                    pathMiddleBand.lineTo(x1, y3);
                }
            }
        }
    }
}
