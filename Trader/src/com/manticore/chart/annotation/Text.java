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
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Text extends Annotation {
    float inset=2f;

    public Text(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
        text="insert text here";
    }

   
    public void paintFigure(Graphics2D g2) {
        Point2D p1=points.get(0);
        Point2D p2=points.get(1);

        g2.draw(new Line2D.Double(p1, p2));
        
        g2.setFont(Settings.SMALL_MANTICORE_FONT);
        Rectangle2D boundRect=g2.getFontMetrics().getStringBounds(text, g2);
        LineMetrics lineMetrics=g2.getFontMetrics().getLineMetrics(text, g2);
        float descent=lineMetrics.getDescent();

        Rectangle2D.Double rect=null;
        if (p1.getX()<=p2.getX() && p1.getY()<=p2.getY()) {
            rect=new Rectangle2D.Double(p2.getX()+inset, p2.getY()+inset, boundRect.getWidth()+2*inset, boundRect.getHeight()+2*inset);
        } else if (p1.getX()<=p2.getX() && p1.getY()>p2.getY()) {
            rect=new Rectangle2D.Double(p2.getX()+inset, p2.getY()-3 * inset - boundRect.getHeight(), boundRect.getWidth()+2*inset, boundRect.getHeight()+2*inset);
        } else if (p1.getX()>p2.getX() && p1.getY()>p2.getY()) {
            rect=new Rectangle2D.Double(p2.getX()-3*inset- boundRect.getWidth(), p2.getY()-3*inset - boundRect.getHeight(), boundRect.getWidth()+2*inset, boundRect.getHeight()+2*inset);
        } else if (p1.getX()>p2.getX() && p1.getY()<=p2.getY()) {
            rect=new Rectangle2D.Double(p2.getX()-3*inset- boundRect.getWidth(), p2.getY()+inset, boundRect.getWidth()+2*inset, boundRect.getHeight()+2*inset);
        }
        g2.setColor(Settings.MANTICORE_LIGHT_BLUE);
        g2.fill(rect);

        g2.setColor(Settings.MANTICORE_DARK_BLUE);
        g2.draw(rect);
        g2.drawString(text, (float) rect.getX()+inset, (float) rect.getMaxY()-inset-descent);
    }

    @Override
    void addPoints() {
        Point2D p = realTimeChartCanvas.getChartingMidPoint();
        points.add(new Point2D.Double(p.getX()-25, p.getY()));
        points.add(new Point2D.Double(p.getX(), p.getY()-25));
    }


}
