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
package com.manticore.chart.annotation;

import com.manticore.chart.*;
import com.manticore.util.Settings;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Marker extends Annotation {

    public Marker(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
        text="A";
    }

   
    public void paintFigure(Graphics2D g2) {
        Point2D p1=points.get(0);


        Ellipse2D.Double ellipse=new Ellipse2D.Double(p1.getX()-12, p1.getY()-12, 24, 24);
        g2.fill(ellipse);

        g2.setColor(color.white);
        g2.setFont(Settings.MEDIUM_MANTICORE_FONT.deriveFont(Font.BOLD));

        Rectangle2D rect=g2.getFontMetrics().getStringBounds(text, g2);
        LineMetrics lineMetrics=g2.getFontMetrics().getLineMetrics(text, g2);
        float descent=lineMetrics.getDescent();
        g2.drawString(text , (float) p1.getX()-(float) rect.getWidth()*0.5f, (float) p1.getY()+(float) rect.getHeight()*0.5f-descent);
    }

    @Override
    void addPoints() {
        Point2D p = realTimeChartCanvas.getChartingMidPoint();
        points.add(new Point2D.Double(p.getX(), p.getY()));
    }

    @Override
    void duplicate() {
    }
}
