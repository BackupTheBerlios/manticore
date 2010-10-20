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
import java.text.DecimalFormat;

public class FibonacciRetracement extends Annotation {
    public FibonacciRetracement(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public FibonacciRetracement(FibonacciRetracement fibonacciRetracement) {
        super(fibonacciRetracement);
    }
    
    public void paintFigure(Graphics2D g2) {
        drawFibonacciLine(g2,1.0000f);
        drawFibonacciLine(g2,0.6180f);
        drawFibonacciLine(g2,0.5000f);
        drawFibonacciLine(g2,0.3820f);
        drawFibonacciLine(g2,0.0000f);
    }
    
    private void drawFibonacciLine(Graphics2D g2, float r) {
        double y=0;
        if (points.get(1).y > points.get(0).y) {
            y=points.get(0).y + ((points.get(1).y-points.get(0).y) * r);
        } else {
            y=points.get(1).y + (points.get(0).y-points.get(1).y) * r;
        }
        
        Point2D.Double p1=new Point2D.Double(points.get(0).x, y );
        Point2D.Double p2=new Point2D.Double(points.get(1).x, y );
        
        Line2D.Double l=new Line2D.Double(p1,p2);
        g2.draw(l);
        
        String s=DecimalFormat.getPercentInstance().format(r);
        g2.drawString(s, (float) p1.x, (float) p1.y );
        
        paintPriceMarker(g2, p2);
    }

    @Override
    void addPoints() {
        Point2D p=realTimeChartCanvas.getChartingMidPoint();
        
        points.add( new Point2D.Double(p.getX() +25, p.getY()-25));
	points.add( new Point2D.Double(p.getX() -25, p.getY() +25));
    }

    @Override
    void duplicate() {
        Annotation annotation=new FibonacciRetracement(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
