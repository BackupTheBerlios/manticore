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

public class TimeSeriesLineChartPanel extends TimeSeriesChartPanel {
    @Override
    public void drawPlotObjects(Graphics2D g2D, ChartObject chartObject, float dx, float dy) {
        g2D.setColor(Settings.MANTICORE_DARK_BLUE);
        Path2D.Float path = new Path2D.Float();
        path.moveTo(inset, getHeight() - inset + chartObject.offset * dy);
        for (int i = 0; i < chartObject.dataPoints.length; i++) {
            if (chartObject.dataPoints[i] != null) {
                path.lineTo(inset + i * dx + dx / 2, getHeight() - inset + (chartObject.offset - chartObject.dataPoints[i].value) * dy);
                
                if (showDataPointCaption)
                g2D.drawString(decimalFormat.format(chartObject.dataPoints[i].value), inset + i * dx + dx / 2, getHeight() - inset + (chartObject.offset - chartObject.dataPoints[i].value) * dy);
            }
        }
        g2D.draw(path);
    }
}
