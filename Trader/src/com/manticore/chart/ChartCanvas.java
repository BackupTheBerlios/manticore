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
package com.manticore.chart;

import com.manticore.util.Settings;
import com.manticore.chart.drawing.ADXRDrawingThread;
import com.manticore.chart.drawing.ATRDrawingThread;
import com.manticore.chart.drawing.BollingerBandsDrawingThread;
import com.manticore.chart.drawing.CandleDrawingThread;
import com.manticore.chart.drawing.ChaikinOscilatorDrawingThread;
import com.manticore.chart.drawing.DrawingThread;
import com.manticore.chart.drawing.MACDDrawingThread;
import com.manticore.chart.drawing.MarkersDrawingThread;
import com.manticore.chart.drawing.MovingAverageDrawingThread;
import com.manticore.chart.drawing.StochasticRSIDrawingThread;
import com.manticore.chart.drawing.TransactionMarkerDrawingThread;
import com.manticore.stream.ArivaQuoteStream;
import com.manticore.chart.annotation.Annotation;
import com.manticore.chart.annotation.Arc;
import com.manticore.chart.annotation.Arrow;
import com.manticore.chart.annotation.FibonacciPeriod;
import com.manticore.chart.annotation.FibonacciRetracement;
import com.manticore.chart.annotation.FibonacciRectangle;
import com.manticore.chart.annotation.Gap;
import com.manticore.chart.annotation.Marker;
import com.manticore.chart.annotation.PitchFork;
import com.manticore.chart.annotation.Rectangle;
import com.manticore.chart.annotation.Ressistance;
import com.manticore.chart.annotation.Text;
import com.manticore.chart.annotation.Trend;
import com.manticore.database.Quotes;
import com.manticore.database.TickDataTimerTask;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.StockExchange;
import com.manticore.stream.WatchDog;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.font.*;
import java.io.IOException;

import javax.swing.*;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import javax.swing.event.ChangeEvent;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeListener;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import com.manticore.util.ThreadArrayList;

public class ChartCanvas extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, ChangeListener {

    private static final String LOGO_PNG = "/com/manticore/chart/logo.png";
    private ChartParameters chartParameters;
    private ArivaQuoteStream arivaQuoteStream;
    private TickDataTimerTask tickDataTimerTask;
    BufferedImage bufferedImage = null;
    BasicStroke glstroke, tlstroke;
    private int mouseX = 0;
    private int mouseY = 0;
    private Annotation selectedFigure;
    Point2D selectedPoint;
    private final int inset = 2;
    private final int indicatorChartHeight = 60;
    private final int indicatorChartCount = 3;
    private int chartWidth = 0;
    private int chartHeight = 0;
    private int h2 = 0;
    private double scaleWidth = 0f;
    private double scaleHeight = 0f;
    private long td = 0;
    private long mode = 0;
    public final static int ARROW_MODE = 0;
    public final static int SNAP_MODE = 1;
    public final static int DRAW_LINE_END_MODE = 2;
    public final static int DRAW_LINE_START_MODE = 4;
    public final static int INTRADAY_MODE = 8;
    public final static int DRAW_CANDLES_MODE = 16;
    public final static int DRAW_FIGURE = 32;
    private CandleArrayList candleVector;
    private DateTime lastDateTime;
    private long currentCandlePos;
    private Number currentPrice;

    /**
     *  Constructor for the ChartCanvas object
     */
    public ChartCanvas() {
        float dash[] = {1f, 2f};
        glstroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f);
        tlstroke = new BasicStroke(1f);

        addMouseListener(this);
        addMouseMotionListener(this);
        this.addComponentListener(this);

        setSize(800, 600);
        setPreferredSize(new Dimension(800, 600));
    }

    public Rectangle2D getChartingRect() {
        return new Rectangle2D.Double(inset, inset, chartWidth, chartHeight);
    }

    public Point2D getChartingMidPoint() {
        return new Point2D.Double(inset + chartWidth / 2, inset + chartHeight / 2);
    }

    public void paint(Graphics g) {
        //Logger.getAnonymousLogger().info("redraw Chart with paint()");

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        if (bufferedImage != null) {
            g2.drawImage(bufferedImage, 0, 0, null);

        }

        if (isModeSet(SNAP_MODE)) {
            paintCrossHair(g2);
        }

        for (int i = 0; chartParameters != null && i < chartParameters.getFigurelist().size(); i++) {
            Annotation f = chartParameters.getFigurelist().get(i);
            f.paint(g2);
        }

        paintCurrentPriceMarker(g);
    }

    private void paintCurrentPriceMarker(Graphics g) {
        if (currentCandlePos > 0) {
            Graphics2D g2 = (Graphics2D) g;

            double x1 = 0;
            double y1 = 0;

            //current price
            x1 = inset + scaleWidth * (float) currentCandlePos;
            y1 = inset + chartHeight - (currentPrice.floatValue() - chartParameters.getAdjustedMinPrice()) * scaleHeight;

            g2.setStroke(tlstroke);
            g2.setColor(Settings.MANTICORE_LIGHT_BLUE);
            g2.draw(new Line2D.Double(x1, y1, inset + chartWidth, y1));

            String s = NumberFormat.getInstance().format(currentPrice);
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT_BOLD, frc);

            Rectangle2D.Float rectangle = new Rectangle2D.Float((float) (chartWidth - layout.getBounds().getWidth()) - 10, (float) y1 - (float) layout.getBounds().getHeight() - inset, (float) layout.getBounds().getWidth() + 2 * inset, (float) layout.getBounds().getHeight() + 2 * inset);
            g2.setBackground(Color.WHITE);
            g2.setColor(Color.WHITE);
            g2.fill(rectangle);

            g2.setColor(Settings.MANTICORE_DARK_BLUE);
            g2.draw(rectangle);

            g2.setColor(Settings.MANTICORE_DARK_BLUE);
            layout.draw(g2, (float) (inset + chartWidth - layout.getBounds().getWidth()) - 10, (float) y1);
        }
    }

    private void paintLogo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

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

    public synchronized void drawChart() {
        //Logger.getLogger(this.getClass().getCanonicalName()).info("redraw chart");

        if (chartParameters == null || chartParameters.getAdjustedMaxPrice() == null || chartParameters.getAdjustedMinPrice() == null || candleVector.isEmpty()) {
            return;
        }

        BufferedImage bufferedImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        chartWidth = bufferedImage.getWidth() - 2 * inset;
        chartHeight = bufferedImage.getHeight() - (indicatorChartCount * indicatorChartHeight) - (indicatorChartCount + 2) * inset;


        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        try {
            BufferedImage bufferedLogoImage = ImageIO.read(ChartCanvas.class.getResourceAsStream(LOGO_PNG));

            double dx = (double) bufferedImage.getWidth() / (double) bufferedLogoImage.getWidth();
            double dy = (double) bufferedImage.getHeight() / (double) bufferedLogoImage.getHeight();
            dx = Math.min(dx, dy);

            g2.drawImage(bufferedLogoImage, 0, 0, (int) (bufferedLogoImage.getWidth() * dx), (int) (bufferedLogoImage.getHeight() * dx), null);
        } catch (IOException ex) {
            Logger.getLogger(ChartCanvas.class.getName()).log(Level.SEVERE, null, ex);
        }

        g2.setColor(Settings.MANTICORE_LIGHT_BLUE);
        String s = chartParameters.getInstrument().getName()
                + " (" + chartParameters.getStockExchange().getName() + " "
                + chartParameters.getPeriodSettings().getDescription() + ")";
        g2.setFont(Settings.SMALL_MANTICORE_FONT_BOLD);
        g2.drawString(s, 12, 20);

        g2.setColor(Settings.MANTICORE_LIGHT_GREY);
        g2.setStroke(tlstroke);
        g2.draw(new Rectangle2D.Float(inset, inset, chartWidth, chartHeight));
        scaleWidth = (double) chartWidth / (double) candleVector.size();
        scaleHeight = (double) chartHeight / (double) (chartParameters.getAdjustedMaxPrice() - chartParameters.getAdjustedMinPrice());

        ThreadArrayList drawingThreadVector = new ThreadArrayList(10);
        drawingThreadVector.addThread(new MarkersDrawingThread(this, candleVector));
        drawingThreadVector.addThread(new BollingerBandsDrawingThread(this, candleVector));
        drawingThreadVector.addThread(new MovingAverageDrawingThread(this, candleVector));
        //drawingThreadVector.addThread(new ParabolicSARDrawingThread(this, candleVector));
        drawingThreadVector.addThread(new CandleDrawingThread(this, candleVector));
        drawingThreadVector.addThread(new TransactionMarkerDrawingThread(this, candleVector));

        if (candleVector.isHasQuantity()) {
            drawingThreadVector.addThread(new ChaikinOscilatorDrawingThread(this, candleVector));
            drawingThreadVector.addThread(new MACDDrawingThread(this, candleVector));
            drawingThreadVector.addThread(new StochasticRSIDrawingThread(this, candleVector));
            //drawingThreadVector.addThread(new VolumeDrawingThread(this, candleVector));
            //drawingThreadVector.addThread(new ATRDrawingThread(this, candleVector));
        } else {
            drawingThreadVector.addThread(new ADXRDrawingThread(this, candleVector));
            //drawingThreadVector.addThread(new MACDDrawingThread(this, candleVector));
            drawingThreadVector.addThread(new StochasticRSIDrawingThread(this, candleVector));
            //drawingThreadVector.addThread(new AroonOscilatorDrawingThread(this, candleVector));
            drawingThreadVector.addThread(new ATRDrawingThread(this, candleVector));
        }
        drawingThreadVector.join();

        for (int i = 0; i < 8; i++) {
            DrawingThread drawingThread = (DrawingThread) drawingThreadVector.get(i);
            if (i < 5) {
                g2.drawImage(drawingThread.getBufferedImage(), 0, 0, null);
            } else {
                g2.drawImage(drawingThread.getBufferedImage(), inset, chartHeight + (i - 5) * indicatorChartHeight + (i - 3) * inset, null);
            }
        }
        this.bufferedImage = bufferedImage;

        repaint();
    }

    private Annotation getSelectedFigure() {
        Annotation selectedFigure = null;
        Iterator<Annotation> figureIterator = chartParameters.getFigurelist().iterator();
        while (figureIterator.hasNext() && selectedFigure == null) {
            Annotation figure = figureIterator.next();
//            if (selectedFigure == null) {
                selectedFigure = figure.isSelected() ? figure : null;
//            } else {
//                figure.setSelected(false);
//            }
        }
        return selectedFigure;
    }
//------- MouseMotionListener

    /**
     *  Description of the Method
     *
     *@param  e  Description of Parameter
     */
    public void mouseDragged(MouseEvent e) {
        if (selectedFigure != null) {
            if (isModeSet(DRAW_FIGURE)) {
                if (selectedPoint != null) {
                    selectedPoint.setLocation(e.getX(), e.getY());
                } else {
                    selectedFigure.move(e.getX() - mouseX, e.getY() - mouseY);
                }

            }
        }
        mouseX = e.getX();
        mouseY = e.getY();

        repaint();
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of Parameter
     */
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
    //----------------------------------------------------------------
    // MouseListener

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        //System.out.println("mouse exited");
    }

    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();


        selectedFigure = null;
        selectedPoint = null;

        for (int i = 0; i < chartParameters.getFigurelist().size() && selectedPoint == null; i++) {
            selectedPoint = chartParameters.getFigurelist().get(i).getSelectionPoint(getMousePosition());
            selectedFigure = selectedPoint != null ? chartParameters.getFigurelist().get(i) : null;
        }
        if (selectedPoint==null) {
            selectedFigure=getSelectedFigure();
        }

        for (int i = 0; i < chartParameters.getFigurelist().size(); i++) {
            if (!chartParameters.getFigurelist().get(i).equals(selectedFigure)) {
                chartParameters.getFigurelist().get(i).setSelected(false);
            }
        }

        if (selectedFigure!=null && e.isPopupTrigger()) {
            JPopupMenu popupMenu=selectedFigure.getPopupMenu();
            add(popupMenu);
            popupMenu.show(this, e.getX(), e.getY());
        }

        repaint();
    }

    public void mouseReleased(MouseEvent e) {        //if (mode==DRAW_LINE_END_MODE) mode=0;
        //if (isModeSet(DRAW_FIGURE)) toggleDrawingMode(DRAW_FIGURE);
    }
    //--------- Component Listner

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        drawChart();
    }

    public void componentShown(ComponentEvent e) {
    }

    private void paintCrossHair(Graphics2D g2) {
        Double price = getPriceFromY(mouseY);
        DateTime dateTime = getDateTimeFromX(mouseX);

        Line2D lineH = new Line2D.Double(0, mouseY, bufferedImage.getWidth(), mouseY);
        Line2D lineV = new Line2D.Double(mouseX, 0, mouseX, bufferedImage.getHeight());

        g2.draw(lineH);
        g2.draw(lineV);


        FontRenderContext frc = g2.getFontRenderContext();
        String s = NumberFormat.getInstance().format(price);
        TextLayout layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
        float x1 = (float) bufferedImage.getWidth() - (float) layout.getBounds().getWidth() - 2 * (float) inset;
        float y1 = (float) mouseY - (float) layout.getBounds().getHeight();
        layout.draw(g2, x1, y1);



        g2.rotate(-0.5f * Math.PI, 0, chartHeight);

        s = DateTimeFormat.mediumDateTime().print(dateTime);
        layout = new TextLayout(s, Settings.SMALL_MANTICORE_FONT, frc);
        layout.draw(g2, -inset, chartHeight + mouseX + layout.getAscent() + inset);

        g2.rotate(0.5f * Math.PI, 0, chartHeight);
    }

    public Double getPriceFromY(double y) {
        Double price = null;

        price = chartParameters.getAdjustedMaxPrice() - (y-inset) * (chartParameters.getAdjustedMaxPrice() - chartParameters.getAdjustedMinPrice()) / chartHeight;
        return price;
    }

    public DateTime getDateTimeFromX(double x) {
        DateTime dateTime = null;

        int i = (int) Math.round(x * candleVector.getTradingTimeUnits() / chartWidth);
        if (i >= 0 && i < candleVector.size()) {
            dateTime = candleVector.get(i).getStart();
        }
        return dateTime;
    }

    boolean isModeSet(long mode) {
        return ((this.mode & mode) == mode);
    }

    void toggleDrawingMode(long mode) {

        //toogle
        // myPermissions ^= permissionToToggle;

        //delete
        // myPermissions &= ~permissionsToDelete[i];

        // add
        // myPermissions |= permissionsToAdd[i];

        this.mode ^= mode;
    }

    public void setDrawingMode(long mode) {
        //this.mode |= mode;
        this.mode = mode;
    }

    public void removeDrawingMode(long mode) {
        this.mode &= ~mode;
    }

    public void clearDrawingMode() {
        this.mode = ARROW_MODE;
    }

    public void addFigure(Annotation fig) {
        Iterator<Annotation> figureIterator = chartParameters.getFigurelist().iterator();
        while (figureIterator.hasNext()) {
            figureIterator.next().setSelected(false);
        }
        chartParameters.getFigurelist().add(fig);
        fig.setSelected(true);

        repaint();
    }

    public void delFigure() {
        Annotation selectedFigure = getSelectedFigure();
        if (selectedFigure != null) {
            chartParameters.getFigurelist().remove(selectedFigure);
        }
        repaint();
    }

    public void setChartParameters(ChartParameters chartParameters) {
        this.chartParameters = chartParameters;

        if (chartParameters != null) {

            updateArivaStreamQuote();

            if (tickDataTimerTask != null) {
                tickDataTimerTask.cancel();
            }
            tickDataTimerTask = new TickDataTimerTask(chartParameters.getInstrument().getId(), chartParameters.getStockExchange().getId(), chartParameters.getStockExchange().getOpeningDate());
            tickDataTimerTask.addChangeListener(this);
        }
    }

    public void updateInstrument(Instrument instrument, StockExchange stockExchange, int treeItem) {
        if (chartParameters != null && chartParameters.updateInstrument(instrument, stockExchange, treeItem)) {

            updateArivaStreamQuote();

            if (tickDataTimerTask != null) {
                tickDataTimerTask.cancel();
            }

            tickDataTimerTask = new TickDataTimerTask(chartParameters.getInstrument().getId(), chartParameters.getStockExchange().getId(), chartParameters.getStockExchange().getOpeningDate());
            tickDataTimerTask.addChangeListener(this);
        }
    }

    public void updateChartSettings() {
        if (chartParameters != null) {
            candleVector = new CandleArrayList(chartParameters);
            chartParameters.setAdjustedMinMaxPrice(candleVector.getBottom(), candleVector.getTop());
        }
    }

    public ChartParameters getChartParameters() {
        return chartParameters;
    }

    public boolean dispatchActionCommand(String commandStr) {
        boolean dispatched = false;

        Method[] methodArray = getClass().getDeclaredMethods();

        for (int i = 0; i < methodArray.length; i++) {
            Method method = methodArray[i];
            if (method.getName().equalsIgnoreCase(commandStr)) {
                try {
                    method.invoke((Object) this);
                    dispatched = true;
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ChartPane.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(ChartPane.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(ChartPane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return dispatched;
    }

    public void mouse() {
        setDrawingMode(ARROW_MODE);
    }

    public void crossHair() {
        setDrawingMode(SNAP_MODE);
    }

    public void trend() {
        setDrawingMode(DRAW_FIGURE);

        addFigure(new Trend(this));
    }

    public void pitchFork() {
        setDrawingMode(DRAW_FIGURE);

        addFigure(new PitchFork(this));
    }

    public void support() {
    }

    public void resistance() {
        setDrawingMode(DRAW_FIGURE);

        addFigure(new Ressistance(this));
    }

    public void fibonacciRetracement() {
        setDrawingMode(DRAW_FIGURE);

        addFigure(new FibonacciRetracement(this));
    }

    public void fibonacciRectangle() {
        setDrawingMode(DRAW_FIGURE);

        addFigure(new FibonacciRectangle(this));
    }

    public void fibonacciPeriod() {
        setDrawingMode(DRAW_FIGURE);

        addFigure(new FibonacciPeriod(this));
    }

    public void fibonacciFanLine() {
        setDrawingMode(DRAW_FIGURE);

        //addFigure(new FibonacciFigure(this));
    }

    public void gap() {
        setDrawingMode(DRAW_FIGURE);
        addFigure(new Gap(this));
    }

    public void arc() {
        setDrawingMode(DRAW_FIGURE);
        addFigure(new Arc(this));
    }

    public void rectangle() {
        setDrawingMode(DRAW_FIGURE);
        addFigure(new Rectangle(this));
    }

    public void arrow() {
        setDrawingMode(DRAW_FIGURE);
        addFigure(new Arrow(this));
    }

    public void marker() {
        setDrawingMode(DRAW_FIGURE);
        addFigure(new Marker(this));
    }

    public void text() {
        setDrawingMode(DRAW_FIGURE);
        addFigure(new Text(this));
    }

    public void delete() {
        delFigure();
    }

    public void zoom() {
        //new RealTimeChartFrame(candleVector).setVisible(true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        if (e.getSource() instanceof ArivaQuoteStream) {
            ArivaQuoteStream ariveSreamQuote = (ArivaQuoteStream) e.getSource();
            DateTime currentDateTime = ariveSreamQuote.getCurrentDateTime();

            if (lastDateTime == null || lastDateTime.isBefore(currentDateTime)) {
                currentPrice = ariveSreamQuote.getCurrentPrice();
                lastDateTime = currentDateTime;

                String currentPriceStr = DecimalFormat.getInstance().format(currentPrice);

                String currentDateTimeStr = DateTimeFormat.mediumTime().print(currentDateTime);

                Logger.getLogger(this.getClass().getName()).finest("current underlying quote: " + currentPriceStr + ", " + currentDateTimeStr);

                if (candleVector != null && candleVector.size() > 0) {
                    currentCandlePos = candleVector.getCandleFromTick(currentDateTime, currentPrice.floatValue()).getPosition();
                    drawChart();
                }
            }
        } else if (e.getSource().equals(tickDataTimerTask)) {
            Logger.getAnonymousLogger().finest("canvas should repaint now");
            candleVector = new CandleArrayList(chartParameters);
            drawChart();
        }
    }

    public RenderedImage getRenderedImage() {
        BufferedImage bufferedImage = new BufferedImage(chartWidth + inset, chartHeight + inset, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(this.bufferedImage, inset, inset, this);
        for (int i = 0; i < chartParameters.getFigurelist().size(); i++) {
            Annotation f = chartParameters.getFigurelist().get(i);
            f.paint(g2d);
        }

        paintCurrentPriceMarker(g2d);

        g2d.dispose();
        return bufferedImage;

    }

    public void updateArivaStreamQuote() {
        WatchDog.getInstance().removeChangeListener(this);

        if (arivaQuoteStream != null) {
            arivaQuoteStream.removeChangeListener(this);
            arivaQuoteStream.stopThread();
        }

        int id_ext_key = 1;
        long id_instrument = chartParameters.getInstrument().getId();
        long id_stock_exchange = chartParameters.getStockExchange().getId();

        String key = Quotes.getInstance().getExtKeyInstrument(id_ext_key, id_instrument, id_stock_exchange);
        if (key.length() > 0) {
            arivaQuoteStream = new ArivaQuoteStream(key);
            arivaQuoteStream.addChangeListener(this);
        }
    }

    /**
     * @return the mouseX
     */
    public int getMouseX() {
        return mouseX;
    }

    /**
     * @return the mouseY
     */
    public int getMouseY() {
        return mouseY;
    }

    /**
     * @return the chartWidth
     */
    public int getChartWidth() {
        return chartWidth;
    }

    /**
     * @return the chartHeight
     */
    public int getChartHeight() {
        return chartHeight;
    }

    /**
     * @return the scaleWidth
     */
    public double getScaleWidth() {
        return scaleWidth;
    }

    /**
     * @return the scaleHeight
     */
    public double getScaleHeight() {
        return scaleHeight;
    }

    /**
     * @return the inset
     */
    public int getInset() {
        return inset;
    }

    /**
     * @return the indicatorChartHeight
     */
    public int getIndicatorChartHeight() {
        return indicatorChartHeight;
    }

    /**
     * @return the indicatorChartCount
     */
    public int getIndicatorChartCount() {
        return indicatorChartCount;
    }
//    public void preferences() {
//        try {
//            SimpleHighLowSimulator simulator = new SimpleHighLowSimulator(chartParameters);
//            simulator.run();
//            simulator.join();
//            candleVector.setTransactionMarkerVector(simulator.getTransactionVector());
//            drawChart();
//            //StopLossAnalyzer stopLossAnalyzer = new StopLossAnalyzer(simulator.getTransactionVector(), chartParameters.getInstrument().getId(), chartParameters.getStockExchange().getId(), candleVector.getDateTimeFrom().toDateTime());
//            //stopLossAnalyzer.printTickVector();
//            //SwingUtilities.invokeLater(stopLossAnalyzer);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ChartCanvas.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
