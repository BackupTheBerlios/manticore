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

package com.manticore.portfolio;

import com.manticore.util.XMLTools;
import com.manticore.foundation.Instrument;
import com.manticore.ui.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.font.*;
import java.awt.geom.Rectangle2D;
import java.awt.AlphaComposite;
import java.util.Vector;
import java.util.List;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;



import java.util.TreeMap;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.dom4j.Node;

public class ListPane extends Canvas implements KeyListener, ActivePane, MouseListener, MouseMotionListener, ComponentListener {

    private static final int LEFT_ALIGNMENT = 0;
    private static final int CENTER_ALIGNMENT = 1;
    private static final int RIGHT_ALIGNMENT = 2;
    private int alignment = LEFT_ALIGNMENT;
    private int selectedrow = 0;
    private int scrollrow = 0;
    private float zoomfactor = 1;
    private Color color = Color.BLACK;
    private Color bgcolor1 = Color.WHITE;
    private Color bgcolor2 = Color.lightGray;
    private Color selcolor = Color.PINK;
    private Color capcolor = Color.BLUE;
    private float captionheight = 18.0f;
    private BufferedImage bimg;
    private Vector columns;

    private TreeMap<Instrument,Vector<String>> instrumentTreeMap;
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    ListPane() {

        addKeyListener(this);
        addMouseListener(this);
        addComponentListener(this);
        setColumns();

    }

    ListPane(TreeMap<Instrument,Vector<String>> instrumentTreeMap) {
        this.instrumentTreeMap=instrumentTreeMap;

        addKeyListener(this);
        addMouseListener(this);
        addComponentListener(this);
        setColumns();

    }

    private void setColumns() {
        try {
            //@fixme: fix this path
            List<Node> nl=XMLTools.readXML("/home/are/src/WaveTrader/etc/triggers.xml").selectNodes("//trigger");

            columns = new Vector(nl.size());
            for (int i = 0; i < nl.size(); i++) {
                Node e = nl.get(i);

                float width = e.numberValueOf("./@width").floatValue();
                int alignment = e.numberValueOf("./@alignment").intValue();
                String caption = e.valueOf("./@name");
                columns.add(new Column(width, alignment, caption));
            }
        } catch (Exception x) {
            System.out.println(x.getMessage());
        }
    }

    public void zoomIn() {
        zoomfactor += 0.1f;
        paintStockList();
    }

    public void zoomOut() {
        zoomfactor -= 0.1f;
        adjustBufferedImage();

        Graphics2D g2 = (Graphics2D) bimg.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Float(0, 0, (float) this.getBounds().getWidth(), (float) this.getBounds().getWidth()));
        paintStockList();
    }

    public void adjustBufferedImage() {
        if (bimg == null || bimg.getWidth() != this.getWidth() || bimg.getHeight() != this.getHeight()) {
            if (this.getWidth() <= 0 || getHeight() <= 0) {
                bimg = new BufferedImage(320, 200, BufferedImage.TYPE_INT_ARGB);
            } else {
                bimg = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            }

            Graphics2D g2 = bimg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        }


    }

    private void setRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    private void paintCaption(Graphics2D g2) {
        float y = 0;
        float x = 0;

        g2.setColor(capcolor);
        g2.setClip(null);
        g2.fill(new Rectangle2D.Float(0, y, (float) this.getBounds().getWidth() / zoomfactor, captionheight));

        g2.setColor(Color.WHITE);
        for (int c = 0; c < columns.size(); c++) {
            Column col = (Column) columns.get(c);
            Rectangle2D.Float cellshape = getCaptionCellShape(x, y, c, captionheight);
            g2.setClip(cellshape);

            if (col.getCaption().length() > 0) {
                drawCellText(g2, col.getCaption(), cellshape);
            }
            x += getColumnWidth(c);
        }
    }

    public void paintStockList() {
        float w = (float) this.getBounds().getWidth();
        float h = (float) this.getBounds().getHeight();

        adjustBufferedImage();

        Graphics2D graphics2d = (Graphics2D) bimg.getGraphics();
        setRenderingHints(graphics2d);
        graphics2d.scale(zoomfactor, zoomfactor);

        paintCaption(graphics2d);

        boolean odd = true;

        Vector<String> [] triggerListArr=instrumentTreeMap.values().toArray(new Vector[0]);

        float y = captionheight;
        for (int r = 0; r < instrumentTreeMap.size() && y < (float) this.getBounds().getHeight() / zoomfactor; r++) {
            float x = 0;

            if (r >= scrollrow) {
                
                if (odd) {
                    graphics2d.setColor(bgcolor1);
                    odd = false;
                } else {
                    graphics2d.setColor(bgcolor2);
                    odd = true;
                }
                graphics2d.setClip(null);
                graphics2d.fill(new Rectangle2D.Float(0, y, (float) this.getBounds().getWidth() / zoomfactor, getRowHeight(r)));

                graphics2d.setColor(color);

                for (int c = 0; c < triggerListArr[r].size(); c++) {
                    String s = triggerListArr[r].get(c);
                    Rectangle2D.Float cellshape = getCellShape(x, y, c, r);
                    graphics2d.setClip(cellshape);

                    if (s.length() > 0) {
                        drawCellText(graphics2d, s, cellshape);
                    }
                    x += getColumnWidth(c);
                }
                y += getRowHeight(r);
            }
        }
        repaint();
    }

    public void paint(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;
        float y = captionheight;
        int r = 0;

        setRenderingHints(g2);

        if (bimg != null) {
            g2.drawImage(bimg, 0, 0, this);
        }
        g2.scale(zoomfactor, zoomfactor);
        for (r = scrollrow; r < selectedrow; r++) {
            y += getRowHeight(r);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
        g2.setColor(selcolor);
        g2.fill(new Rectangle2D.Float(0, y, (float) this.getBounds().getWidth() / zoomfactor, getRowHeight(r)));

    }

    public void update(Graphics graphics) {
        paint(graphics);
    }

    private Rectangle2D.Float getCellShape(float x, float y, int c, int r) {
        return new Rectangle2D.Float(x, y, getColumnWidth(c), getRowHeight(r));
    }

    private Rectangle2D.Float getCaptionCellShape(float x, float y, int c, float h) {
        return new Rectangle2D.Float(x, y, getColumnWidth(c), h);
    }

    private float getRowHeight(int r) {
        return 14;
    }

    private float getColumnWidth(int c) {

        return ((Column) columns.get(c)).getWidth();
    }

    private int getListWidth() {
        float listwidth = 0;

        for (int i = 0; i < columns.size(); i++) {
            listwidth += ((Column) columns.get(i)).getWidth();
        }
        return Math.round(listwidth);
    }

    private void drawCellText(Graphics2D g2, String s, Rectangle2D.Float cellshape) {
        double x1 = 0;
        double y1 = 0;


        TextLayout tl = new TextLayout(s, g2.getFont(), g2.getFontRenderContext());
        switch (alignment) {
            case LEFT_ALIGNMENT:
                x1 = cellshape.getMinX();
                y1 = cellshape.getMaxY() - tl.getDescent();
                break;
            case CENTER_ALIGNMENT:
                x1 = cellshape.getCenterX() - tl.getVisibleAdvance() / 2;
                y1 = cellshape.getMaxY() - tl.getDescent();
                break;
            case RIGHT_ALIGNMENT:
                x1 = cellshape.getMaxX() - tl.getVisibleAdvance();
                y1 = cellshape.getMaxY() - tl.getDescent();
                break;
        }

        tl.draw(g2, (float) x1, (float) y1);
    }

    

    private void showChartPane(String symbol) {
       
    }
    //----------------------------------------------------------------
    // KeyListener
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == e.VK_UP) {
            if (selectedrow > 0) {
                selectedrow--;

                ChangeEvent changeEvent = new ChangeEvent(this);
                fireMyEvent(changeEvent);
            }
        } else if (e.getKeyCode() == e.VK_DOWN) {
            if (selectedrow < instrumentTreeMap.size() - 2) {
                selectedrow++;

                ChangeEvent changeEvent = new ChangeEvent(this);
                fireMyEvent(changeEvent);
            }
        } else if (e.getKeyCode() == e.VK_DELETE) {
            instrumentTreeMap.remove(selectedrow);

            if (selectedrow > instrumentTreeMap.size()) {
                selectedrow = instrumentTreeMap.size();
            }
            paintStockList();
        } else if (e.getKeyCode() == e.VK_F) {
            //StockList.getInstance().applyFilter("//Stock[value[9] < 11] | //Stock[value[10] < 10] | //Stock[value[12] > 15] | //Stock[value[13] > 15]");

            //| //Stock/value[9]<13 | //Stock/value[10]>3.5 | //Stock/value[11]>10 | //Stock/value[12]>10" );

            paintStockList();
        } else if (e.getKeyCode() == e.VK_ENTER) {
            //Vector v = new Vector( StockList.getInstance());
            //showChartPane( getSymbolFromYahoo( ((Vector) v.get(selectedrow)).get(1).toString()) );
        }

        //test for hiddenarea
        if (selectedrow < scrollrow) {
            scrollrow--;
            paintStockList();


        }

        float y = 0;
        for (int r = scrollrow; r < selectedrow; r++) {
            y += getRowHeight(r);
        }
        if ((y + captionheight + getRowHeight(selectedrow)) > (float) this.getBounds().getHeight() / zoomfactor) {
            scrollrow++;
            paintStockList();
        }
        this.repaint();
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
    //----------------------------------------------------------------
    // MouseListener
    public void mouseClicked(MouseEvent e) {
        float y = e.getY() - captionheight;

        selectedrow = scrollrow + Math.round(y / (getRowHeight(1) * zoomfactor));
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
    //------- MouseMotionListener
    /**
     *  Description of the Method
     *
     *@param  e  Description of Parameter
     */
    public void mouseDragged(MouseEvent e) {
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of Parameter
     */
    public void mouseMoved(MouseEvent e) {
    }
    //--------- Component Listner
    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        paintStockList();
    }

    public void componentShown(ComponentEvent e) {
    }

    /**
     * @return the instrumentTreeMap
     */
    public TreeMap<Instrument, Vector<String>> getInstrumentTreeMap() {
        return instrumentTreeMap;
    }

    /**
     * @param instrumentTreeMap the instrumentTreeMap to set
     */
    public void setInstrumentTreeMap(TreeMap<Instrument, Vector<String>> instrumentTreeMap) {
        this.instrumentTreeMap = instrumentTreeMap;
        paintStockList();
    }

    public Instrument getSelectedInstrument() {
        Instrument instrument =null;

        Instrument[] instrumentArr =instrumentTreeMap.keySet().toArray(new Instrument[0]);
        if (selectedrow<instrumentArr.length) {
            instrument=instrumentArr[selectedrow];
        } else {
            Logger.getLogger(this.getClass().getName()).warning("Selected row not in array of securities!");
        }
        return instrument;
    }
    //----------------------------------------------------------------
    // Column
    private class Column {

        float width;
        int alignment;
        String caption;

        public Column(float width, int alignment, String caption) {
            this.width = width;
            this.alignment = alignment;
            this.caption = caption;
        }

        public float getWidth() {
            return width;
        }

        public int getAlignment() {
            return alignment;
        }

        public String getCaption() {
            return caption;
        }
    }
    //--- ActivePane
    public void setActive(boolean Active) {
    }

    public void setActive() {
        adjustBufferedImage();
    }

    public boolean getActive() {
        return true;
    }

    // This methods allows classes to register for MyEvents
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }
    // This methods allows classes to unregister for MyEvents
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }
    // This private class is used to fire MyEvents
    void fireMyEvent(ChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(evt);
            }
        }
    }
}


