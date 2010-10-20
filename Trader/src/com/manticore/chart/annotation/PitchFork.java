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

public class PitchFork extends Annotation {

    public PitchFork(ChartCanvas realTimeChartCanvas) {
        super(realTimeChartCanvas);
    }

    public PitchFork(PitchFork pitchFork) {
        super(pitchFork);
    }

    /*
     * gegeben: A(a1,a2), B(b1,b2), C(c1,c2)

    Gerade durch AB:
    y = mx + r
    Steigung: m = (a2-b2)/(a1-b1)
    Einsetzen von A:
    a2 = (a2-b2)/(a1-b1) a1 + r
    r = a2 - a1 (a2-b2)/(a1-b1) = (a1 b2 - a2 b1) / (a1 - b1)
    y = ((a2-b2) x + a1 b2 - a2 b1)/(a1-b1)

    Senkrechte Gerade durch C:
    Steigung: m' = - 1/m = (a1-b1)/(b2-a2)

    Einsetzen von C liefert:
    c2 = (a1-b1)/(b2-a2) c1 + r
    r = (b2 c2 - a2 c2 - a1 c1 + b1 c1)/(b2-a2)

    y = ((a1 - b1) x + b2 c2 - a2 c2 - a1 c1 + b1 c1)/(b2-a2)

    Schnittpunkt bestimmen. => x und y wert

    Abstand zu C (= Abstand gerade Punkt): wurzel((c1-x)² + (c2-y)²)
     *
     *
     * schnittpunkt: x = ((c1*((b1^2) + (a1^2))) + (a1*((a2*(c2 - b2)) - (2*c1*b1) + (b2^2) - (b2*c2))) + (b1*(b2 - a2)*(c2 - a2)))/((a2^2) - (2*((a2*b2) + (b1*a1))) + (b1^2) + (b2^2) + (a1^2))
     *               y = ((c2*((a2^2) + (b2^2))) + (a2*((b1^2) - (2*c2*b2) + (a1*(c1 - b1)) - (b1*c1))) + (b2*(b1 - a1)*(c1 - a1)))/((a2^2) - (2*((a2*b2) + (b1*a1))) + (b1^2) + (b2^2) + (a1^2))
     */
    public void paintFigure(Graphics2D g2) {
        Point2D p1 =null;
        Point2D p2 = null;
        Point2D p3 = null;

        Point2D p6 = null;
        Point2D p7 = null;

        if (selectedPoint==null || selectedPoint.equals(points.get(0)) || selectedPoint.equals(points.get(1)) || selectedPoint.equals(points.get(2))) {
            p1 = points.get(0);
            p2 = points.get(1);
            p3 = points.get(2);

            double a1 = p1.getX();
            double a2 = p1.getY();
            double b1 = p2.getX();
            double b2 = p2.getY();
            double c1 = p3.getX();
            double c2 = p3.getY();


            // Schnittpunkt aus Normalenvektor und Parallele
            double mx = ((c1 * (Math.pow(b1, 2) + Math.pow(a1, 2))) + (a1 * ((a2 * (c2 - b2)) - (2 * c1 * b1) + Math.pow(b2, 2) - (b2 * c2))) + (b1 * (b2 - a2) * (c2 - a2))) / (Math.pow(a2, 2) - (2 * ((a2 * b2) + (b1 * a1))) + Math.pow(b1, 2) + Math.pow(b2, 2) + Math.pow(a1, 2));
            double my = ((c2 * (Math.pow(a2, 2) + Math.pow(b2, 2))) + (a2 * (Math.pow(b1, 2) - (2 * c2 * b2) + (a1 * (c1 - b1)) - (b1 * c1))) + (b2 * (b1 - a1) * (c1 - a1))) / (Math.pow(a2, 2) - (2 * ((a2 * b2) + (b1 * a1))) + Math.pow(b1, 2) + Math.pow(b2, 2) + Math.pow(a1, 2));

            // Abstand zwischen Schnittpunkt und P3
            double n = Math.sqrt(Math.pow(c1 - mx, 2) + Math.pow(c2 - my, 2));

            double distance = n / p2.distance(p1);
            double dy = p1.getX() - p2.getX();
            double dx = -(p1.getY() - p2.getY());


            points.get(3).setLocation(p1.getX() + distance * dx, p1.getY() + distance * dy);
            points.get(4).setLocation(p2.getX() + distance * dx, p2.getY() + distance * dy);

            p6 = new Point2D.Double(p1.getX() - distance * dx, p1.getY() - distance * dy);
            p7 = new Point2D.Double(p2.getX() - distance * dx, p2.getY() - distance * dy);
            //points.get(5).setLocation((p6.getX()+p7.getX())/2, (p6.getY()+p7.getY())/2);
            
            points.get(5).setLocation(0.5d * (p1.getX() + p2.getX()) - distance * dx, 0.5d * (p1.getY() + p2.getY()) - distance * dy);

        } else {
            p1=points.get(3);
            p2=points.get(4);
            p3=points.get(5);

            double a1 = p1.getX();
            double a2 = p1.getY();
            double b1 = p2.getX();
            double b2 = p2.getY();
            double c1 = p3.getX();
            double c2 = p3.getY();


            // Schnittpunkt aus Normalenvektor und Parallele
            double mx = ((c1 * (Math.pow(b1, 2) + Math.pow(a1, 2))) + (a1 * ((a2 * (c2 - b2)) - (2 * c1 * b1) + Math.pow(b2, 2) - (b2 * c2))) + (b1 * (b2 - a2) * (c2 - a2))) / (Math.pow(a2, 2) - (2 * ((a2 * b2) + (b1 * a1))) + Math.pow(b1, 2) + Math.pow(b2, 2) + Math.pow(a1, 2));
            double my = ((c2 * (Math.pow(a2, 2) + Math.pow(b2, 2))) + (a2 * (Math.pow(b1, 2) - (2 * c2 * b2) + (a1 * (c1 - b1)) - (b1 * c1))) + (b2 * (b1 - a1) * (c1 - a1))) / (Math.pow(a2, 2) - (2 * ((a2 * b2) + (b1 * a1))) + Math.pow(b1, 2) + Math.pow(b2, 2) + Math.pow(a1, 2));

            // Abstand zwischen Schnittpunkt und P3
            double n = Math.sqrt(Math.pow(c1 - mx, 2) + Math.pow(c2 - my, 2));

            double distance = n / p2.distance(p1);
            double dy = p1.getX() - p2.getX();
            double dx = -(p1.getY() - p2.getY());



            p6 = new Point2D.Double(p1.getX() - distance * dx, p1.getY() - distance * dy);
            p7 = new Point2D.Double(p2.getX() - distance * dx, p2.getY() - distance * dy);
            points.get(5).setLocation((p6.getX()+p7.getX())/2, (p6.getY()+p7.getY())/2);

            points.get(0).setLocation((p1.getX()+p6.getX())/2, (p1.getY()+p6.getY())/2);
            points.get(1).setLocation((p2.getX()+p7.getX())/2, (p2.getY()+p7.getY())/2);
            points.get(2).setLocation((p1.getX()+p2.getX())/2, (p1.getY()+p2.getY())/2);
        }

        Line2D.Double l1 = new Line2D.Double(points.get(0), points.get(1));
        Line2D.Double l3 = new Line2D.Double(points.get(3), points.get(4));
        Line2D.Double l4 = new Line2D.Double(p6, p7);

        g2.draw(l1);
        g2.draw(l3);
        g2.draw(l4);
    }

    @Override
    void addPoints() {
        Point2D p = realTimeChartCanvas.getChartingMidPoint();

        points.add(new Point2D.Double(p.getX(), p.getY() - 25));
        points.add(new Point2D.Double(p.getX() - 25, p.getY()));
        points.add(new Point2D.Double(p.getX() + 25, p.getY() + 5));

        points.add(new Point2D.Double(p.getX(), p.getY() - 25));
        points.add(new Point2D.Double(p.getX() - 25, p.getY()));
        points.add(new Point2D.Double(p.getX() + 25, p.getY() + 5));
    }

    @Override
    void duplicate() {
        Annotation annotation=new PitchFork(this);
        realTimeChartCanvas.addFigure(annotation);
    }
}
