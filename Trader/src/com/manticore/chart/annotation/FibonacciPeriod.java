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
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

public class FibonacciPeriod extends Annotation {
    public FibonacciPeriod(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public FibonacciPeriod(FibonacciPeriod fibonacciPeriod) {
        super(fibonacciPeriod);
    }
    
    public void paintFigure(Graphics2D g2) {
	//Line2D.Double l1 =new Line2D.Double(points.get(0),points.get(1));
	
	//g2.draw(l1);
        
        drawFibonacciLine(g2,1.0000f);
        drawFibonacciLine(g2,1.3820f);
        drawFibonacciLine(g2,1.6180f);
        drawFibonacciLine(g2,2.0000f);
        drawFibonacciLine(g2,2.6180f);
    }
    
    private void drawFibonacciLine(Graphics2D g2, float r) {
        Rectangle2D rect=realTimeChartCanvas.getChartingRect();
        
        double x=0;
        if (points.get(1).getX() > points.get(0).getX()) {
            x=points.get(0).getX() + ((points.get(1).getX()-points.get(0).getX()) * r);
        } else {
            x=points.get(1).getX() + (points.get(0).getX()-points.get(1).getX()) * r;
        }
        
        Point2D.Double p1=new Point2D.Double(x, rect.getMinY() );
        Point2D.Double p2=new Point2D.Double(x, rect.getMaxY() );
        
        Line2D.Double l=new Line2D.Double(p1,p2);
        g2.draw(l);
        
        //String s=DecimalFormat.getPercentInstance().format(r);
        //g2.drawString(s, (float) p1.x, (float) p1.y );
    }

    @Override
    void addPoints() {
        Point2D p=realTimeChartCanvas.getChartingMidPoint();
        
        points.add( new Point2D.Double(p.getX() +25, p.getY()-25));
	points.add( new Point2D.Double(p.getX() -25, p.getY() +25));
    }

    @Override
    void duplicate() {
        Annotation annotation=new FibonacciPeriod(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
