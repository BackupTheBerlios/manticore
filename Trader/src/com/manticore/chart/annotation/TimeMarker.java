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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TimeMarker extends Annotation {
    public final static Color TIMEMARKER_BACKGROUND_COLOR=new Color(0.80f, 0.70f, 0.70f, 0.45f);
    public final static Color TIMEMARKER_FOREGROUND_COLOR=new Color(1.00f, 0.70f, 0.70f, 1.00f);
    
    public TimeMarker(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }
    
    public void paintFigure(Graphics2D g2) {
        Rectangle2D rect=realTimeChartCanvas.getChartingRect();
        
        Rectangle2D rect1=new Rectangle2D.Double(points.get(0).getX()-25, rect.getMaxY(), points.get(0).getX()+25, rect.getMinY());
        g2.setColor(TIMEMARKER_BACKGROUND_COLOR);
        g2.fill(rect1);
        
        Line2D line=new Line2D.Double(points.get(0).getX(), rect.getMaxY(), points.get(0).getX(), rect.getMinY());
        g2.setColor(TIMEMARKER_FOREGROUND_COLOR);
        g2.draw(line);
    }

    @Override
    void addPoints() {
        Point2D p=realTimeChartCanvas.getChartingMidPoint();
        points.add( new Point2D.Double(p.getX() +25, p.getY()-25));
    }
}
