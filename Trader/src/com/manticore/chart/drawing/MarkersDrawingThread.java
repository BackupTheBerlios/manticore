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
package com.manticore.chart.drawing;

import com.manticore.util.Settings;
import com.manticore.foundation.Candle;
import com.manticore.chart.CandleArrayList;
import com.manticore.chart.ChartCanvas;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.text.NumberFormat;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class MarkersDrawingThread extends DrawingThread {

    public MarkersDrawingThread(ChartCanvas canvas, CandleArrayList candleArrayList) {
        super(canvas, candleArrayList);
    }

    public void run() {
        FontRenderContext frc = g2.getFontRenderContext();
        for (int i = 0; i <= 10; i++) {
            g2.setColor(Settings.MANTICORE_LIGHT_GREY);
            g2.setStroke(glstroke);
            g2.draw(new Line2D.Float(canvas.getInset(), canvas.getInset() + i * canvas.getChartHeight() / 10, canvas.getInset() + canvas.getChartWidth(), canvas.getInset() + i * canvas.getChartHeight() / 10));

            g2.setColor(Settings.MANTICORE_DARK_GREY);
            g2.setStroke(tlstroke);
            float p = canvas.getChartParameters().getAdjustedMaxPrice() + i * (canvas.getChartParameters().getAdjustedMinPrice() - canvas.getChartParameters().getAdjustedMaxPrice()) / 10;
            String s = NumberFormat.getInstance().format(p);

            TextLayout layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
            if (i>0 && i<10)  layout.draw(g2, (float) (canvas.getInset() + canvas.getChartWidth() - layout.getBounds().getWidth()) - 10, (float) canvas.getInset() + i * canvas.getChartHeight() / 10);

            p = (p / candleArrayList.firstElement().getOpening() - 1f) * 100f;

            s = NumberFormat.getInstance().format(p);


            layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
            //layout.draw(g2, (float) (canvas.getInset() - layout.getBounds().getWidth()) - 2, (float) canvas.getInset() + i * canvas.getChartHeight() / 10);
            if (i>0 && i<10) layout.draw(g2, (float) (2 * canvas.getInset()) + 10 , (float) canvas.getInset() + i * canvas.getChartHeight() / 10);
        }

        TextLayout layout;

        DateTimeFormatter dateTimeFormatter = getDateTimeFormatter(candleArrayList.getPeriodSettings().getReportMarkerDurationFieldType());

        MutableDateTime mdt = candleArrayList.getDateTimeFrom().toMutableDateTime();
        g2.rotate(-0.5f * Math.PI, 0, canvas.getChartHeight() + 2 * canvas.getInset());


        int k = 0;

        for (int i = 0; i < candleArrayList.size(); i++) {
            Candle candle = candleArrayList.get(i);

            if (mdt.isBefore(candle.getStart())) {
                mdt = candle.getStart().toMutableDateTime();
            }

            if (mdt.isEqual(candle.getStart())) {
                float y = (float) (canvas.getChartHeight() + 3 * canvas.getInset() + (float) i * canvas.getScaleWidth());
                g2.setColor(Settings.MANTICORE_LIGHT_GREY);
                g2.setStroke(glstroke);
                g2.draw(new Line2D.Float(canvas.getInset(), y, canvas.getInset() + canvas.getChartHeight(), y));

                String s = dateTimeFormatter.print(mdt);
                layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
                g2.setColor(Settings.MANTICORE_DARK_GREY);
                g2.setStroke(tlstroke);
                if (i>0.1 * candleArrayList.size() && i<0.9*candleArrayList.size()) layout.draw(g2, canvas.getInset() + 10, y + (float) layout.getAscent());

                mdt.add(candleArrayList.getPeriodSettings().getReportMarkerDurationFieldType(), candleArrayList.getPeriodSettings().getReportMarkerDurationFieldValue());
            }


            if (candleArrayList.getPeriodSettings().getCandlePeriod().toDurationFrom(new DateTime()).getMillis() < 1800000) {
                if (k < candleArrayList.getTimeMarkerArrayList().size() && candle.containsDateTime(candleArrayList.getTimeMarkerArrayList().get(k).getTimestamp())) {
                    g2.setColor(Settings.MANTICORE_LIGHT_BLUE);
                    g2.setStroke(BOLD_STROKE);

                    //double x1 = canvas.getInset() + canvas.getScaleWidth() * (float) i;
                    float y1 = (float) (canvas.getChartHeight() + 3 * canvas.getInset() + (float) i * canvas.getScaleWidth());
                    g2.draw(new Line2D.Double(0, y1, canvas.getChartHeight(), y1));

                    g2.setStroke(tlstroke);
                    String s = dateTimeFormatter.print(candleArrayList.getTimeMarkerArrayList().get(k).getTimestamp());
                    layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
                    float x1 = (float) layout.getBounds().getWidth();

                    s = s.concat(" ").concat(candleArrayList.getTimeMarkerArrayList().get(k).getShortDescription());
                    layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
                    g2.setColor(Settings.MANTICORE_DARK_GREY);
                    g2.setStroke(tlstroke);
                    layout.draw(g2, 24 + canvas.getInset() + x1 , y1 + (float) layout.getAscent());

                    if (k < candleArrayList.getTimeMarkerArrayList().size() - 1) {
                        k++;
                    }
                }
            }
        }
        g2.rotate(0.5f * Math.PI, 0, canvas.getChartHeight() + 2 * canvas.getInset());


        //source.drawImage(bufferedImage, 0, 0, null);

    }

    private DateTimeFormatter getDateTimeFormatter(DurationFieldType ft) {
        DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();

        if (ft.equals(DurationFieldType.years())) {
            b.appendYear(0, 0);
        } else if (ft.equals(DurationFieldType.months())) {
            b.appendMonthOfYearShortText();
        } else if (ft.equals(DurationFieldType.weeks())) {
            b.appendPattern("dd.MM.");
        } else if (ft.equals(DurationFieldType.days())) {
            b.appendPattern("dd.MM.");
        } else {
            b.appendPattern("HH:mm");
        }
        return b.toFormatter();
    }
}
