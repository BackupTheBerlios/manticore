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

public class CategoryChartPanel extends ChartPanel {

    @Override
    public ChartObject getChartObject() {
        ChartObject chartObject = new ChartObject();
        Float maxLineValue = null;
        Float minLineValue = null;
        chartObject.gridLinesCount = dataPointVector.size();
        chartObject.dataPoints = dataPointVector.toArray(new CategoryDataPoint[0]);;
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
}
