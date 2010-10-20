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
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Iterator;

public class SlotChartPanel extends ChartPanel {

    @Override
    public ChartObject getChartObject() {
        ChartObject chartObject = new ChartObject();
        Float maxLineValue = null;
        Float minLineValue = null;
        chartObject.gridLinesCount = dataPointVector.size();
        chartObject.dataPoints = dataPointVector.toArray(new SlotDataPoint[0]);;
        for (int i = 0; i < chartObject.dataPoints.length; i++) {
            if (chartObject.dataPoints[i] != null) {
                if (maxLineValue == null || maxLineValue < chartObject.dataPoints[i].value) {
                    maxLineValue = chartObject.dataPoints[i].value;
                }
                if (minLineValue == null || minLineValue > chartObject.dataPoints[i].value) {
                    minLineValue = chartObject.dataPoints[i].value;
                }
            }
        }
        chartObject.setRange(maxLineValue, minLineValue);
        return chartObject;
    }

    public void drawPlotObjects(Graphics2D g2D, ChartObject chartObject, float dx, float dy) {
        dy=((float) (getHeight()-2*inset)) / 1f;
        for (int i=0; i< chartObject.dataPoints.length; i++) {
            if (chartObject.dataPoints[i]!=null) {
                drawBar(g2D, i, dx, chartObject.offset, dy, chartObject.dataPoints[i].value);
            }

        }

        float dx2=(float) (getWidth()-2*inset) / (float) dataPointVector2.size();

        g2D.setColor(Settings.MANTICORE_ORANGE);
        Path2D.Float path = new Path2D.Float();
        path.moveTo(inset, getHeight() - inset + chartObject.offset * dy);

        Iterator<DataPoint> dataPointIterator = dataPointVector2.iterator();
        int i=0;
        while (dataPointIterator.hasNext()) {
            DataPoint p=dataPointIterator.next();
            path.lineTo(inset + i * dx2 + dx2 / 2, getHeight() - inset + (chartObject.offset - p.value) * dy);
            //g2D.drawString(String.valueOf(p.value), inset + i * dx + dx / 2, getHeight() - inset + (chartObject.offset - chartObject.dataPoints[i].value) * dy);
            i++;
        }
        g2D.draw(path);

    }
}
