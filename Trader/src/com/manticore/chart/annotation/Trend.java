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

public class Trend extends Annotation {

    public Trend(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public Trend(Trend trend) {
        super(trend);
    }

   
    public void paintFigure(Graphics2D g2) {
        Point2D p1=points.get(0);
        Point2D p2=points.get(1);


        Line2D.Double l1 = new Line2D.Double(points.get(0), points.get(1));

        g2.draw(l1);
    }

    @Override
    void addPoints() {
        Point2D p = realTimeChartCanvas.getChartingMidPoint();
        points.add(new Point2D.Double(p.getX(), p.getY() - 25));
        points.add(new Point2D.Double(p.getX() - 25, p.getY()));
    }

    @Override
    void duplicate() {
        Annotation annotation=new Trend(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
