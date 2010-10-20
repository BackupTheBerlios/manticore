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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class Arrow extends Annotation {

    public Arrow(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public Arrow(Arrow arrow) {
        super(arrow);
    }

   
    public void paintFigure(Graphics2D g2) {
        Path2D.Double path=new Path2D.Double();
        path.moveTo(points.get(0).getX(), points.get(0).getY());
        for (int i=1; i< points.size(); i++) {
            path.lineTo(points.get(i).getX(), points.get(i).getY());
        }
        g2.draw(path);
    }

    @Override
    void addPoints() {
        Point2D p = realTimeChartCanvas.getChartingMidPoint();
        points.add(new Point2D.Double(p.getX()-50, p.getY()));
        points.add(new Point2D.Double(p.getX() - 25, p.getY()+25));
        points.add(new Point2D.Double(p.getX(), p.getY()));
        points.add(new Point2D.Double(p.getX() +25, p.getY()+25));
         points.add(new Point2D.Double(p.getX()+50, p.getY()));
    }

    @Override
    void duplicate() {
        Annotation annotation=new Arrow(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
