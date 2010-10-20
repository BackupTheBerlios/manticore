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

import com.manticore.chart.*;
import com.manticore.util.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class ChaikinOscilatorDrawingThread extends DrawingThread {

    Graphics2D source;
    int n;

    public ChaikinOscilatorDrawingThread(ChartCanvas canvas, CandleArrayList candleVector) {
        super(canvas, candleVector);
    }

    @Override
    public void run() {
        double x1 = 0;
        double x2 = 0;
        double y1 = 0;
        double y2 = 0;

        if (candleArrayList.maxChaikinOscilator==null || candleArrayList.minChaikinOscilator==null) return;

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        g2.setColor(Settings.MANTICORE_DARK_GREY);
        g2.setStroke(tlstroke);

        g2.draw(new Rectangle2D.Float(0, 0, canvas.getChartWidth(), canvas.getIndicatorChartHeight()));
        g2.setColor(Settings.MANTICORE_DARK_BLUE);
        g2.setFont(Settings.SMALL_MANTICORE_FONT);
        g2.drawString("Chaikin Money Flow", canvas.getInset(), (int) (0.2 * canvas.getIndicatorChartHeight()));

        g2.setStroke(glstroke);
        g2.setColor(Settings.MANTICORE_LIGHT_GREY);


        double pxh = (double) canvas.getIndicatorChartHeight() / (candleArrayList.maxChaikinOscilator - candleArrayList.minChaikinOscilator);

        g2.draw(new Line2D.Double(0, 0.2 * canvas.getIndicatorChartHeight(), canvas.getChartWidth(), 0.2 * canvas.getIndicatorChartHeight()));
        g2.draw(new Line2D.Double(0, 0.5 * canvas.getIndicatorChartHeight(), canvas.getChartWidth(), 0.5 * canvas.getIndicatorChartHeight()));
        g2.draw(new Line2D.Double(0, 0.8 * canvas.getIndicatorChartHeight(), canvas.getChartWidth(), 0.8 * canvas.getIndicatorChartHeight()));

        g2.setStroke(tlstroke);
        for (int i = 0; i < candleArrayList.size(); i++) {
            Double chaikinOscilator = candleArrayList.get(i).getChaikainOszilator();

            if (!candleArrayList.get(i).isFirst() && chaikinOscilator != null) {
                x1 = x2;
                y1 = y2;

                //x2 = canvas.getInset() + ((double) i + 0.5f) * canvas.getScaleWidth();
                x2 = canvas.getScaleWidth() * (float) i;
                y2 = canvas.getIndicatorChartHeight() - (chaikinOscilator - candleArrayList.minChaikinOscilator) * pxh;

                if (x1 != 0) {
                    g2.setColor(Settings.MANTICORE_DARK_BLUE);
                    g2.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
        }
    }
}
