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
package com.manticore.report;

import com.manticore.util.Settings;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JPanel;
import org.joda.time.DurationFieldType;

abstract class ChartPanel extends JPanel implements MouseMotionListener, ComponentListener {

    public TreeSet<DataPoint> dataPointVector;
    public TreeSet<DataPoint> dataPointVector2;
    public final static int inset = 12;
    public final static int gridlines = 5;
    public final static DurationFieldType durationFieldType = DurationFieldType.days();
    private int chartMode = LINE;
    public String title;
    public boolean showDataPointCaption=false;
    public final static int LINE = 0;
    public final static int BAR = 1;
    public BufferedImage bufferedImage = null;
    public DecimalFormat decimalFormat;

    public Vector<String> stringVector;

    public ChartPanel() {
        this.title = "";
        dataPointVector = new TreeSet<DataPoint>();
        dataPointVector2 = new TreeSet<DataPoint>();
        decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setGroupingUsed(true);

        addComponentListener(this);
        stringVector=new Vector<String>();
    }

    public ChartPanel(String title) {
        this.title = title;
        dataPointVector = new TreeSet<DataPoint>();
        decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setGroupingUsed(true);

        addComponentListener(this);
        stringVector=new Vector<String>();
    }

    public void addData(String key, Float value) {
        CategoryDataPoint dataPoint = new CategoryDataPoint(key, value);
        dataPointVector.add(dataPoint);
    }

    public void addData(long key, Float value) {
        TimeDataPoint dataPoint = new TimeDataPoint(new Date(key), value);
        dataPointVector.add(dataPoint);
    }

    public void addData(int key, Float value) {
        SlotDataPoint dataPoint = new SlotDataPoint(key, value);
        dataPointVector.add(dataPoint);
    }

    public void addData2(int key, Float value) {
        SlotDataPoint dataPoint = new SlotDataPoint(key, value);
        dataPointVector2.add(dataPoint);
    }

    public abstract ChartObject getChartObject();

    public void drawPlotObjects(Graphics2D g2D, ChartObject chartObject, float dx, float dy) {
        for (int i = 0; i < chartObject.dataPoints.length; i++) {
            if (chartObject.dataPoints[i] != null) {
                drawBar(g2D, i, dx, chartObject.offset, dy, chartObject.dataPoints[i].value);
            }

        }
    }

    public void drawChart() {
        if (bufferedImage == null && this.getGraphicsConfiguration() != null) {
            bufferedImage = this.getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight());
        }

        if (bufferedImage != null) {
            Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2D.setColor(Color.WHITE);
            g2D.fillRect(0, 0, getWidth(), getHeight());
            ChartObject chartObject = getChartObject();

            float dx = (float) (getWidth() - 2 * inset) / (float) chartObject.gridLinesCount;
            float dy = ((float) (getHeight() - 2 * inset)) / (chartObject.range);

            g2D.setColor(Settings.MANTICORE_DARK_GREY);
            for (int i = 0; i < gridlines; i++) {
                g2D.draw(new Line2D.Float(inset, getHeight() - inset + (chartObject.offset - chartObject.dy2 * i) * dy, getWidth() - inset, getHeight() - inset + (chartObject.offset - chartObject.dy2 * i) * dy));
                g2D.drawString(decimalFormat.format(chartObject.dy2*i), inset, getHeight() - inset + (chartObject.offset - chartObject.dy2 * i) * dy);

                g2D.draw(new Line2D.Float(inset, getHeight() - inset + (chartObject.offset + chartObject.dy2 * i) * dy, getWidth() - inset, getHeight() - inset + (chartObject.offset + chartObject.dy2 * i) * dy));
                g2D.drawString(decimalFormat.format(-chartObject.dy2*i), inset, getHeight() - inset + (chartObject.offset + chartObject.dy2 * i) * dy);
            }

            for (int i = 0; i < chartObject.gridLinesCount; i++) {
                g2D.draw(new Line2D.Float(inset + i * dx + dx / 2, inset, inset + i * dx + dx / 2, getHeight() - inset));
            }

            dx = (float) (getWidth() - 2 * inset) / (float) chartObject.dataPoints.length;
            dy = ((float) (getHeight() - 2 * inset)) / chartObject.range;

            //draw average if exists
            if (chartObject.average != 0f) {
                g2D.setColor(Settings.MANTICORE_ORANGE);
                g2D.draw(new Line2D.Float(inset, getHeight() - inset + (chartObject.offset - chartObject.average) * dy, getWidth() - inset, getHeight() - inset + (chartObject.offset - chartObject.average) * dy));
            }

            drawPlotObjects(g2D, chartObject, dx, dy);

            if (title.length() > 0) {
                g2D.setFont(Settings.MEDIUM_MANTICORE_FONT);
                g2D.setColor(Settings.MANTICORE_DARK_BLUE);
                g2D.drawString(title, inset, inset);
            }

            Iterator<String> stringIterator=stringVector.iterator();
            float x=10f;
            float y=10f;
            while (stringIterator.hasNext()) {
                g2D.drawString(stringIterator.next(), x, y);
                y+=10f;
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(bufferedImage, 0, 0, this);
    }

    public void drawBar(Graphics2D g2D, int i, float dx, float offset, float dy, float value) {
        Rectangle2D.Float rect = null;
        if (value < 0) {
            g2D.setColor(Settings.MANTICORE_ORANGE);
            rect = new Rectangle2D.Float(inset + i * dx + dx / 2 - 2, getHeight() - inset + (offset) * dy, 4, -value * dy);
        } else {
            g2D.setColor(Settings.MANTICORE_DARK_BLUE);
            rect = new Rectangle2D.Float(inset + i * dx + dx / 2 - 2, getHeight() - inset + (offset - value) * dy, 4, value * dy);
        }
        
        if (showDataPointCaption)
        g2D.drawString(decimalFormat.format(value), inset + i * dx + dx / 2, getHeight() - inset + (offset - value) * dy);

        g2D.fill(rect);
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        CategoryDataPoint dataPoint = getNearestPoint(e.getPoint());
        String s = dataPoint.getDate() + " " + dataPoint.value;
        setToolTipText(dataPoint.getDate() + " " + dataPoint.value);
    }

    private CategoryDataPoint getNearestPoint(Point2D point) {
        CategoryDataPoint dataPoint = null;
//        double minDistance=Double.MAX_VALUE;
//
//        for (int i=0; i< dataPoints.length;i++) {
//            double distance=dataPoints[i].point.distance(point);
//
//            if (distance<minDistance) {
//                minDistance=distance;
//                dataPoint=dataPoints[i];
//            }
//        }
        return dataPoint;
    }

    public void componentResized(ComponentEvent e) {
        bufferedImage = this.getGraphicsConfiguration().createCompatibleImage(getWidth(), getHeight());
        drawChart();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public class ChartObject {

        Float offset;
        Float range;
        Float dy2;
        float average = 0f;
        int gridLinesCount;
        DataPoint[] dataPoints;

        public void setRange(Float maxValue, Float minValue) {
            double t1 = Math.ceil(Math.log10(Math.abs(maxValue))) - 1;
            double t2 = Math.ceil(Math.log10(Math.abs(minValue))) - 1;
            double t = Math.max(t1, t2);

            maxValue = (float) (Math.ceil(Math.abs(maxValue) / Math.pow(10d, t)) * Math.pow(10d, t)) * Math.signum(maxValue);
            minValue = (float) (Math.ceil(Math.abs(minValue) / Math.pow(10d, t)) * Math.pow(10d, t)) * Math.signum(minValue);

            if (minValue < 0 && maxValue <= 0) {
                offset = minValue;
                range = -minValue;
            } else if (minValue < 0 && maxValue > 0) {
                offset = minValue;
                range = (maxValue - minValue);
            } else {
                offset = 0f;
                range = maxValue;
            }

            dy2 = (float) maxValue / (float) gridlines;
        }
    }
}
