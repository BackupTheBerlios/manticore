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
import com.manticore.chart.drawing.DrawingThread;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class VolumeDrawingThread extends DrawingThread {
    private final static Font SMALL_STANDARD_FONT = new Font("Courier New", Font.PLAIN, 11);
    public final static Color GRID_FOREGROUND_COLOR = new Color(0.75f, 0.75f, 0.75f, 1.0f);
    public final static Color FRAME_FOREGROUND_COLOR = new Color(0.55f, 0.55f, 0.55f, 1.0f);

    Graphics2D source;
    int n;

    public VolumeDrawingThread(ChartCanvas canvas, CandleArrayList candleVector) {
        super(canvas, candleVector);
    }

    @Override
    public void run() {
        double x1 = 0;
        double y2 = 0;
        double y1 = 0;
        double x2 = 0;


        if (candleArrayList.getMaxquantity()==null) {
            return;
        }

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        g2.setColor(FRAME_FOREGROUND_COLOR);
        g2.setStroke(tlstroke);

        g2.draw(new Rectangle2D.Float(0, 0, canvas.getChartWidth(), canvas.getIndicatorChartHeight()));

        g2.setFont(SMALL_STANDARD_FONT);
        g2.drawString("Volume", canvas.getInset(), (int) (0.2 * canvas.getIndicatorChartHeight()));

        g2.setStroke(glstroke);
        g2.setColor(GRID_FOREGROUND_COLOR);

        double pxh = (double) canvas.getIndicatorChartHeight() / (double) candleArrayList.getMaxquantity().doubleValue();
        for (int i = 0; i < candleArrayList.size(); i++) {
            Candle t = candleArrayList.get(i);

            y1 = canvas.getIndicatorChartHeight();

            x2 = canvas.getScaleWidth() * (float) i;
            y2 = canvas.getIndicatorChartHeight() - (t.getQuantity()) * pxh;

            g2.setColor(t.getColor());
            g2.draw(new Line2D.Double(x2, y1, x2, y2));
        }
        

        //source.drawImage(bufferedImage, canvas.getInset(), h + n * (canvas.getInset() + canvas.getIndicatorChartHeight()), null);
    }
}
