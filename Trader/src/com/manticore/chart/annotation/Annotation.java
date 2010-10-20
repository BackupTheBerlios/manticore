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

import com.manticore.util.Settings;
import com.manticore.chart.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public abstract class Annotation implements ActionListener {

    Vector<Point2D.Double> points = new Vector();
//    private Point2D.Double selectedPoint = null;
    static final int SNAP_DISTANCE = 7;
    static final double ELLIPSE_RADIUS = 2d;
    public Point2D.Double selectedPoint = null;
    private boolean selected=false;
    public ChartCanvas realTimeChartCanvas;
    private JPopupMenu popupMenu=null;
    public String text=null;
    public Color color=null;
    public Color fillColor=null;
    public Font font=null;
    
    public Annotation(ChartCanvas realTimeChartCanvas) {
        this.realTimeChartCanvas=realTimeChartCanvas;
        color=Settings.MANTICORE_DARK_BLUE;
        font=Settings.SMALL_MANTICORE_FONT;
        fillColor=Settings.MANTICORE_LIGHT_BLUE_TRANSPARENT;
        buildPopupMenu();
        addPoints();
    }

    public Annotation(Annotation annotation) {
        for (int i = 0; i < annotation.points.size(); i++) {
            points.add(new Point2D.Double(annotation.points.get(i).x + 15, annotation.points.get(i).y + 15));
            realTimeChartCanvas = annotation.realTimeChartCanvas;
            color=annotation.color;
            fillColor=annotation.fillColor;
            font=annotation.font;

            buildPopupMenu();
        }
    }

    private void buildPopupMenu() {
        popupMenu=new JPopupMenu("properties");
        JMenuItem menuItem=new JMenuItem("text");
        menuItem.addActionListener(this);
        menuItem.setActionCommand("SET_TEXT");
        popupMenu.add(menuItem);

        menuItem=new JMenuItem("color");
        menuItem.setActionCommand("SET_COLOR");
        menuItem.addActionListener(this);
        popupMenu.add(menuItem);

        menuItem=new JMenuItem("fill color");
        menuItem.setActionCommand("SET_FILL_COLOR");
        menuItem.addActionListener(this);
        popupMenu.add(menuItem);

        menuItem=new JMenuItem("font");
        menuItem.setActionCommand("SET_FONT");
        menuItem.addActionListener(this);
        popupMenu.add(menuItem);

        menuItem=new JMenuItem("duplicate");
        menuItem.setActionCommand("DUPLICATE");
        menuItem.addActionListener(this);
        popupMenu.add(menuItem);
    }

    abstract void addPoints();
    
    public Point2D getSelectionPoint(Point2D clickedPoint) {
       selectedPoint=null;
       for (int i = 0; i < points.size() && selectedPoint==null; i++) {
            if ( getSnapRect( points.get(i) ).contains(clickedPoint)) {
                selectedPoint=points.get(i);
                setSelected(true);
            }
        } 
       return selectedPoint;
    }
    
    private Rectangle2D.Double getPointRect(Point2D.Double p) {
        return new Rectangle2D.Double(p.getX() - ELLIPSE_RADIUS, p.getY() - ELLIPSE_RADIUS, 2 * ELLIPSE_RADIUS, 2 * ELLIPSE_RADIUS);
    }

    private Rectangle2D.Double getSnapRect(Point2D.Double p) {
        return new Rectangle2D.Double(p.getX() - ELLIPSE_RADIUS, p.getY() - SNAP_DISTANCE, 2 * SNAP_DISTANCE, 2 * SNAP_DISTANCE);
    }
    
    final public void paint(Graphics2D g2) {
        g2.setColor( isSelected() ? Settings.MANTICORE_ORANGE : color);
        g2.setFont(font);
        
        for (int i = 0; i < points.size(); i++) {
            Rectangle2D.Double rectangle = getPointRect( points.get(i) );
            g2.draw(rectangle);
        }
        paintFigure(g2);
    };

    private DecimalFormat getNumberFormat(Double price) {
        DecimalFormat decimalFormat=(DecimalFormat) DecimalFormat.getNumberInstance();
        int digits=Double.valueOf(Math.log10(100000 / Math.pow(10, Math.ceil(Math.log10(price))))).intValue();

        decimalFormat.setMaximumFractionDigits(digits);
        return decimalFormat;
    }

    public void paintPriceMarker(Graphics2D g2, Point2D p) {
        Double price=realTimeChartCanvas.getPriceFromY(p.getY());
        g2.drawString(getNumberFormat(price).format(price) , (long) p.getX() + 5 , (long) p.getY() - 5);
    }
    
    public void paintPriceMarker(Graphics2D g2, Point2D p, double x) {
        Double price=realTimeChartCanvas.getPriceFromY(p.getY());
        g2.drawString(getNumberFormat(price).format(price) , (long) x , (long) p.getY() - 5);
    }
    
    public void paintTimeMarker(Graphics2D g2, Point2D p) {
        DateTime dateTime=realTimeChartCanvas.getDateTimeFromX( p.getX() );
        //g2.rotate(-0.5f * Math.PI, 0, realTimeChartCanvas.h);
        g2.drawString(DateTimeFormat.mediumTime().print(dateTime) , (long) p.getX() + 5 , (long) p.getY() - 5);
    }
    
    final public void move(double x, double y) {
        for (int i = 0; i < points.size(); i++) {
            Point2D.Double p = points.get(i);
            p.x += x;
            p.y += y;
        }
    }
    ;

    abstract public void paintFigure(Graphics2D g2);

    public Vector<Point2D.Double> getPoints() {
        return points;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    /**
     * @return the popupMenu
     */
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("SET_TEXT")) {
            text=TextDialog.getTextFromDialog(text);
        } else if (e.getActionCommand().equalsIgnoreCase("SET_COLOR")) {
            color = JColorChooser.showDialog(
                     realTimeChartCanvas,
                     "Choose Line Color",
                     color);

        } else if (e.getActionCommand().equalsIgnoreCase("SET_FILL_COLOR")) {
            fillColor = JColorChooser.showDialog(
                     realTimeChartCanvas,
                     "Choose Fill Color",
                     fillColor);

        }else if (e.getActionCommand().equalsIgnoreCase("SET_FONT")) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else if (e.getActionCommand().equalsIgnoreCase("DUPLICATE")) {
            duplicate();
        }
    }

    void duplicate() {
        Logger.getLogger(getClass().getName()).info("class does not support duplication");
    }
}
