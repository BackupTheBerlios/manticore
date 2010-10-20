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
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

public class Arc extends Annotation {

    public Arc(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public Arc(Arc arc) {
        super(arc);
    }

   
    public void paintFigure(Graphics2D g2) {
        Point2D.Double p0=points.get(0);
        Point2D.Double p1=points.get(1);
        Arc2D arc=null;
        if (p0.x <=p1.x && p0.y<=p1.y) {
            arc=new Arc2D.Double(p0.x, 2* p0.y - p1.y, 2*(p1.x-p0.x), 2*(p1.y-p0.y), 180, 180, Arc2D.OPEN);
        } else if (p0.x > p1.x && p0.y<=p1.y) {
            arc=new Arc2D.Double(2*p1.x-p0.x, 2* p0.y - p1.y, 2*(p0.x-p1.x), 2*(p1.y-p0.y), 180, 180, Arc2D.OPEN);
        } else if (p0.x <= p1.x && p0.y>p1.y) {
            arc=new Arc2D.Double(p0.x, p1.y, 2*(p1.x-p0.x), 2*(p0.y-p1.y), 0, 180, Arc2D.OPEN);
        } else {
            arc=new Arc2D.Double(2*p1.x-p0.x, p1.y, 2*(p0.x-p1.x), 2*(p0.y-p1.y), 0, 180, Arc2D.OPEN);
        }
        g2.draw(arc);
    }

    @Override
    void addPoints() {
        Point2D p = realTimeChartCanvas.getChartingMidPoint();
        points.add(new Point2D.Double(p.getX()-50, p.getY()));
        points.add(new Point2D.Double(p.getX(), p.getY()-50));
    }

    @Override
    void duplicate() {
        Annotation annotation=new Arc(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
