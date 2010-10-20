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

public class FibonacciRectangle extends Annotation {
    public FibonacciRectangle(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public FibonacciRectangle(FibonacciRectangle fibonacciRectangle) {
        super(fibonacciRectangle);
    }
    
    public void paintFigure(Graphics2D g2) {
        Point2D p1=points.firstElement();
        Point2D p2=points.lastElement();
        
        if (selectedPoint != null) {
            if (selectedPoint.equals(p1)) {
                p1.setLocation( p1.getX(), p2.getY() + (p2.getX() - p1.getX()));
                
            } else if (selectedPoint.equals(p2)) {
                p2.setLocation( p2.getX(), p1.getY() + (p1.getX() - p2.getX()));
            }
        }
        
        Point2D p3=new Point2D.Double(p1.getX(), p2.getY());
        Point2D p4=new Point2D.Double(p2.getX(), p1.getY());
          
        drawLine(g2, p1, p2);
        drawLine(g2, p2, p1);
        drawLine(g2, p3, p4);
        drawLine(g2, p4, p3);
    }
    
    private void drawLine(Graphics2D g2, Point2D pointFrom, Point2D pointTo) {
        double x;
        double y;
        
        x=pointTo.getX();
        y=pointTo.getY()-(pointTo.getY()-pointFrom.getY()) * 0.3820;
        g2.draw( new Line2D.Double(pointFrom, new Point2D.Double(x,y)));
        
        x=pointTo.getX();
        y=pointTo.getY()-(pointTo.getY()-pointFrom.getY()) * 0.6180;
        g2.draw( new Line2D.Double(pointFrom, new Point2D.Double(x,y)));
        
        x=pointTo.getX();
        y=pointTo.getY()-(pointTo.getY()-pointFrom.getY()) * 1.000;
        g2.draw( new Line2D.Double(pointFrom, new Point2D.Double(x,y)));
        
        x=pointTo.getX()-(pointTo.getX()-pointFrom.getX())*0.3820;
        y=pointTo.getY();
        g2.draw( new Line2D.Double(pointFrom, new Point2D.Double(x,y)));
        
        x=pointTo.getX()-(pointTo.getX()-pointFrom.getX())*0.6180;
        y=pointTo.getY();
        g2.draw( new Line2D.Double(pointFrom, new Point2D.Double(x,y)));
        
        x=pointTo.getX()-(pointTo.getX()-pointFrom.getX())*1.000;
        y=pointTo.getY();
        g2.draw( new Line2D.Double(pointFrom, new Point2D.Double(x,y)));
    }

    @Override
    void addPoints() {
        Point2D p=realTimeChartCanvas.getChartingMidPoint();
        
        points.add( new Point2D.Double(p.getX() +25, p.getY()-25));
	points.add( new Point2D.Double(p.getX() -25, p.getY() +25));
        
    }

    @Override
    void duplicate() {
        Annotation annotation=new FibonacciRectangle(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
