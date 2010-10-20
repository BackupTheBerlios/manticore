/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.ui;

import com.manticore.util.Settings;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author are
 */

//New York: 06:09 	London: 11:09 	Frankfurt: 12:09 	Dubai: 14:09  	Singapore: 18:09 	Tokyo: 19:09 	Sydney: 20:09

public class WorldTimePane extends JPanel {
    public final static String[] timezones={"America/New_York", "Europe/London", "Europe/Berlin", "Asia/Dubai", "Asia/Singapore", "Asia/Tokyo", "Australia/Sydney"};
    public final static String[] cities={"New York", "London", "Frankfurt", "Dubai", "Singapore", "Tokyo", "Sydney"};
    public final static long PERIOD=60000L;
    private Vector<CityLabel> labelVector;

    public final static DateTimeFormatter shortDateTimeFormatter=DateTimeFormat.shortTime();
    public final static DateTimeFormatter mediumDateTimeFormatter=DateTimeFormat.mediumDateTime();

    public WorldTimePane() {
        super(new FlowLayout(FlowLayout.CENTER,24,2), true);
        setFont(Settings.SMALL_MANTICORE_FONT);

        labelVector=new Vector<CityLabel>(timezones.length);
        for (int i=0; i<timezones.length; i++) {
            CityLabel label=new CityLabel(timezones[i], cities[i]);

            labelVector.add(label);
            add(label);
        }

        Timer timer=new Timer("WorldTimerPane-Timer", true);
        timer.scheduleAtFixedRate(new TimeUpdate(), 0L, PERIOD);
    }

    private class TimeUpdate extends TimerTask {

        @Override
        public void run() {
            DateTime dateTime=new DateTime();

            Iterator<CityLabel> iterator=labelVector.iterator();
            while (iterator.hasNext()) {
                CityLabel label=iterator.next();
                label.setText(label.getCityName() +": " + shortDateTimeFormatter.withZone(label.getDateTimeZone()).print(dateTime) );
                label.setToolTipText(label.getCityName() +": " + mediumDateTimeFormatter.withZone(label.getDateTimeZone()).print(dateTime) );
            }
        }

    }

    private class CityLabel extends JLabel {
        private DateTimeZone dateTimeZone;
        private String cityName;
        
        CityLabel(String timeZoneID, String cityName) {
            this.dateTimeZone=DateTimeZone.forID(timeZoneID);
            this.cityName=cityName;

            this.setFont(Settings.SMALL_MANTICORE_FONT);
            this.setForeground(Settings.MANTICORE_DARK_BLUE);
            this.setHorizontalAlignment(JLabel.CENTER);
        }

        /**
         * @return the dateTimeZone
         */
        public DateTimeZone getDateTimeZone() {
            return dateTimeZone;
        }

        /**
         * @return the cityName
         */
        public String getCityName() {
            return cityName;
        }
    }
}
