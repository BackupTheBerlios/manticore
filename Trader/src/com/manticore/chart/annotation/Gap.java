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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Gap extends Annotation {
    public Gap(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public Gap(Gap gap) {
        super(gap);
    }

    public void paintFigure(Graphics2D g2) {
        Rectangle2D rect=realTimeChartCanvas.getChartingRect();

        Point2D.Double p0=points.get(0);
        Point2D.Double p1=points.get(1);

        double w=rect.getMaxX();
        double h=(double) (points.get(1).getY()-points.get(0).getY());
        Rectangle2D rect1=null;
        if (p0.y<=p1.y && p0.x<=p1.x) {
            w-=p0.x;
            h=p1.y-p0.y;
            rect1=new Rectangle2D.Double(p0.x, p0.y, w, h );
        } else if (p0.y>p1.y && p0.x<=p1.x) {
            w-=p0.x;
            h=p0.y-p1.y;
            rect1=new Rectangle2D.Double(p0.x, p1.y, w, h );
        } else if (p0.y<=p1.y && p0.x>p1.x) {
            w-=p1.x;
            h=p1.y-p0.y;
            rect1=new Rectangle2D.Double(p1.x, p0.y, w, h );
        } else {
            w-=p1.x;
            h=p0.y-p1.y;
            rect1=new Rectangle2D.Double(p1.x, p1.y, w, h );
        }

        g2.setColor(fillColor);
        g2.fill(rect1);
        
        g2.setColor(color);
        g2.draw(rect1);
    }

    @Override
    void addPoints() {
        Point2D p=realTimeChartCanvas.getChartingMidPoint();
        points.add( new Point2D.Double(p.getX(), p.getY()-25));
        points.add( new Point2D.Double(p.getX(), p.getY()+25));
    }

    @Override
    void duplicate() {
        Annotation annotation=new Gap(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
