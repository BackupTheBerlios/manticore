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

package com.manticore.swingui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;

public class FormatedTextField extends JTextField implements FocusListener, TableCellRenderer {
    public final static Color MANTICORE_DARK_BLUE = new Color(3, 1, 70);
    public final static Color MANTICORE_LIGHT_BLUE = new Color(211, 210, 227);
    public final static Color MANTICORE_LIGHT_BLUE_TRANSPARENT = new Color(211, 210, 227, 200);
    public final static Color MANTICORE_ORANGE = new Color(255, 66, 14);
    public final static Color MANTICORE_LIGHT_GREY = new Color(230, 230, 230);
    public final static Color MANTICORE_DARK_GREY = new Color(179, 179, 179);

    public final static int INTEGER_FORMAT = 0;
    public final static int PERCENT_FORMAT = 1;
    public final static int DECIMAL_FORMAT = 2;
    public final static int DATE_FORMAT = 3;
    public final static int TIME_FORMAT = 4;
    public final static int DATETIME_FORMAT = 5;
    public final static int TEXT_FORMAT=6;

    private int format = INTEGER_FORMAT;
    private int digits = 2;
    private NumberFormat numberFormat;
    private DateFormat dateFormat;

    public FormatedTextField() {
        setHorizontalAlignment(JTextField.TRAILING);
        setEditable(false);
        setInputVerifier(TextFieldInputVerifier.getInstance());
        addFocusListener(this);
    }

    public FormatedTextField(Number newValue, int format, boolean editable) {
        this.format = format;

        adjustFormat();

        setValue(newValue);
        setHorizontalAlignment(JTextField.TRAILING);
        setEditable(editable);
        setInputVerifier(TextFieldInputVerifier.getInstance());
        addFocusListener(this);

    }

    public FormatedTextField(Number newValue, int format, boolean editable, KeyListener keyListener) {
        this.format = format;

        adjustFormat();

        setValue(newValue);
        setHorizontalAlignment(JTextField.TRAILING);
        setEditable(editable);
        setInputVerifier(TextFieldInputVerifier.getInstance());
        addFocusListener(this);
        addKeyListener(keyListener);


    }

    public FormatedTextField(Date newValue, int format, boolean editable) {
        this.format = format;

        adjustFormat();

        setValue(newValue);
        setHorizontalAlignment(JTextField.TRAILING);
        setEditable(editable);
        setInputVerifier(TextFieldInputVerifier.getInstance());
        addFocusListener(this);

    }

    public FormatedTextField(Date newValue, int format, boolean editable, KeyListener keyListener) {
        this.format = format;
        adjustFormat();

        setValue(newValue);
        setHorizontalAlignment(JTextField.TRAILING);
        setEditable(editable);
        setInputVerifier(TextFieldInputVerifier.getInstance());
        addFocusListener(this);
        addKeyListener(keyListener);
    }

    private void adjustFormat() {
        if (format == INTEGER_FORMAT) {
            numberFormat = DecimalFormat.getIntegerInstance();
        } else if (format == PERCENT_FORMAT) {
            numberFormat = DecimalFormat.getPercentInstance();
            numberFormat.setGroupingUsed(true);
            numberFormat.setMinimumFractionDigits(0);
            numberFormat.setMinimumFractionDigits(3);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        } else if (format == DECIMAL_FORMAT) {
            numberFormat = DecimalFormat.getInstance();
            numberFormat.setGroupingUsed(true);
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        } else if (format == DATE_FORMAT) {
            dateFormat = DateFormat.getDateInstance();
        } else if (format == TIME_FORMAT) {
            dateFormat = DateFormat.getTimeInstance();
        } else if (format == DATETIME_FORMAT) {
            dateFormat = DateFormat.getDateTimeInstance();
        }
    }

    public int getFormat() {
        return format;
    }

    public void setValue(Number newValue) {
        if (newValue != null) {
            if (format == INTEGER_FORMAT || format == PERCENT_FORMAT || format == DECIMAL_FORMAT) {
                setText(numberFormat.format(newValue.doubleValue()));
            } else if (format == DATE_FORMAT || format == TIME_FORMAT || format == DATETIME_FORMAT) {
                setText(dateFormat.format(newValue.longValue()));
            }
        } else {
            setText("");
        }
    }

    public void parseNumberValue(String valueStr, Locale locale) throws ParseException {
        if (format == INTEGER_FORMAT) {
            Number newValue = DecimalFormat.getIntegerInstance(locale).parse(valueStr);
            setText(DecimalFormat.getIntegerInstance().format(newValue.longValue()));
        } else if (format == PERCENT_FORMAT) {
            Number newValue = DecimalFormat.getPercentInstance(locale).parse(valueStr);
            setText(DecimalFormat.getPercentInstance().format(newValue.doubleValue()));
        } else if (format == DECIMAL_FORMAT) {
            Number newValue = DecimalFormat.getInstance(locale).parse(valueStr);
            setText(DecimalFormat.getInstance().format(newValue.doubleValue()));
        }
    }

    public void parseDateValue(String valueStr, int style, Locale locale) throws ParseException {
        if (format == DATE_FORMAT) {
            Date d = DateFormat.getDateInstance(style, locale).parse(valueStr);
            setText(DateFormat.getDateInstance().format(d));
        } else if (format == TIME_FORMAT) {
            Date d = DateFormat.getTimeInstance(style, locale).parse(valueStr);
            setText(DateFormat.getTimeInstance().format(d));
        } else if (format == DATETIME_FORMAT) {
            Date d = DateFormat.getDateTimeInstance(style, style, locale).parse(valueStr);
            setText(DateFormat.getDateTimeInstance().format(d));
        }
    }

    public void setValue(Date newValue) {
        setText(dateFormat.format(newValue));
    }

    public void setValue(DateTime newValue) {
        setText(dateFormat.format(newValue.toDate()));
    }

    public Number getNumberValue() throws ParseException {
        Number number = null;

        if (format == INTEGER_FORMAT || format == PERCENT_FORMAT || format == DECIMAL_FORMAT) {
            number = numberFormat.parse(getText());
        } else if (format == DATE_FORMAT || format == TIME_FORMAT || format == DATETIME_FORMAT) {
            Date d = dateFormat.parse(getText());
            number = Long.valueOf(d.getTime());
        }
        return number;
    }

    public void addDateTime(DurationFieldType durationFieldType, int value) throws ParseException {
        MutableDateTime mutableDateTime = new MutableDateTime(getDateTimeValue());
        mutableDateTime.add(durationFieldType, value);
        setValue(mutableDateTime.toDate());
    }

    public void addPeriod(Period period, int value) throws ParseException {
        MutableDateTime mutableDateTime = new MutableDateTime(getDateTimeValue());
        mutableDateTime.add(period, value);
        setValue(mutableDateTime.toDate());
    }

    public void addPeriod(MutablePeriod period, int value) throws ParseException {
        MutableDateTime mutableDateTime = new MutableDateTime(getDateTimeValue());
        mutableDateTime.add(period, value);
        setValue(mutableDateTime.toDate());
    }

    public int getIntegerValue() throws ParseException {
        return numberFormat.parse(getText()).intValue();
    }

    public long getLongValue() throws ParseException {
        return numberFormat.parse(getText()).longValue();
    }

    public double getPercentValue() throws ParseException {
        return numberFormat.parse(getText()).doubleValue();
    }

    public double getDoubleValue() throws ParseException {
        return numberFormat.parse(getText()).doubleValue();
    }

    public float getFloatValue() throws ParseException {
        return numberFormat.parse(getText()).floatValue();
    }

    public Date getDateValue() throws ParseException {
        return dateFormat.parse(getText());
    }

    public Date getTimeValue() throws ParseException {
        return dateFormat.parse(getText());
    }

    public DateTime getDateTimeValue() throws ParseException {
        return new DateTime(dateFormat.parse(getText()));
    }

    @Override
    public void focusGained(FocusEvent e) {
        setSelectionStart(0);
        setSelectionEnd(getText().length());
    }

    @Override
    public void focusLost(FocusEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
                setBackground(MANTICORE_LIGHT_BLUE);
        }
        else if (row % 2 ==1) {
            setBackground(MANTICORE_LIGHT_GREY);
        } else {
            setBackground(Color.WHITE);
        }
        setBorder(null);

        if (format == INTEGER_FORMAT || format == PERCENT_FORMAT || format == DECIMAL_FORMAT) {
            setValue((Number) table.getModel().getValueAt(row, column));
            if (((Number) table.getModel().getValueAt(row, column)).floatValue()<0) {
                setForeground(MANTICORE_ORANGE);
            } else {
                setForeground(MANTICORE_DARK_BLUE);
            }
        } else if (format == DATE_FORMAT || format == TIME_FORMAT || format == DATETIME_FORMAT) {
            setValue((Date) table.getModel().getValueAt(row, column));
        } else  {
            setText(table.getModel().getValueAt(row, column).toString());
        }
        return this;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(int format) {
        this.format = format;
        adjustFormat();
    }
}
