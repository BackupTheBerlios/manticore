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
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Ressistance extends Annotation {
    
    public Ressistance(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }
    
    public void paintFigure(Graphics2D g2) {
        if (selectedPoint != null) {
            if (selectedPoint.equals(points.get(0))) {
                points.get(1).y = points.get(0).y;
            } else if (selectedPoint.equals(points.get(1))) {
                points.get(0).y = points.get(1).y;
            }
        }
        Line2D.Double l1 = new Line2D.Double(points.get(0), points.get(1));
        g2.draw(l1);
        
        paintPriceMarker(g2, points.get(0));
    }

    @Override
    void addPoints() {
        Point2D p=realTimeChartCanvas.getChartingMidPoint();
        points.add(new Point2D.Double(p.getX() - 15, p.getY()));
        points.add(new Point2D.Double(p.getX() + 15, p.getY()));
    }

    @Override
    void duplicate() {
    }
}
