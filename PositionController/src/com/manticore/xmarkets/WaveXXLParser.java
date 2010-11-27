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
package com.manticore.xmarkets;

import com.manticore.foundation.WaveXXL;
import com.manticore.parser.WebsiteParser;
import com.manticore.parser.WebsiteParser.Site;
import com.manticore.http.HttpClientFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.joda.time.MutableDateTime;

public class WaveXXLParser {
    public static DefaultHttpClient getPreparedHttpClient() {
        DefaultHttpClient client = HttpClientFactory.getClient();

        BasicClientCookie cookie = new BasicClientCookie("x%2Dmarkets%2Ede%2Ddisclaimer", "ok");
        cookie.setDomain("www.de.x-markets.db.com");
        cookie.setPath("/");

        MutableDateTime expiryDateTime = new MutableDateTime();
        expiryDateTime.addDays(10);

        cookie.setExpiryDate(expiryDateTime.toDate());

        client.getCookieStore().addCookie(cookie);
        return client;
    }

    public static WaveXXL getWaveXXLByLeverage(String inbwnr, float mode, Number leverage) {
        WaveXXL waveXXL = null;
        DefaultHttpClient client = getPreparedHttpClient();

        String[][] paramArr = {{"inwpnr", mode > 0 ? "26" : "27"}, {"inbwnr", inbwnr}};
        Site site = WebsiteParser.getInstance().getSite("WaveXXLByStrike", paramArr, client);
        while (site.hasNextNode() && (waveXXL == null || waveXXL.getLeverage().floatValue()<leverage.floatValue())) {
            waveXXL=new WaveXXL(site.getString("wkn")
                    , site.getFloat("bid")
                    , site.getFloat("ask")
                    , site.getFloat("strike")
                    , site.getFloat("ko")
                    , site.getFloat("leverage")
                    , site.getFloat("ratio")
                    );
        }
        return waveXXL;
    }

    public static WaveXXL getWaveXXLByStrike(String inbwnr, float mode, Number strike) {
        WaveXXL waveXXL = null;
        DefaultHttpClient client = getPreparedHttpClient();

        String[][] paramArr = {{"inwpnr", mode > 0 ? "26" : "27"}, {"inbwnr", inbwnr}};
        Site site = WebsiteParser.getInstance().getSite("WaveXXLByStrike", paramArr, client);
        while (site.hasNextNode() && (waveXXL == null || mode * waveXXL.getStrike().floatValue() > mode * strike.floatValue())) {
            try {
            waveXXL=new WaveXXL(site.getString("wkn")
                    , site.getFloat("bid")
                    , site.getFloat("ask")
                    , site.getFloat("strike")
                    , site.getFloat("ko")
                    , site.getFloat("leverage")
                    , site.getFloat("ratio")
                    );
            } catch (Exception ex) {
                
            }
        }
        return waveXXL;
    }

    public static WaveXXL getWaveXXLByIsin(String isin) {
        WaveXXL waveXXL = null;
        DefaultHttpClient client=WaveXXLParser.getPreparedHttpClient();
        Site site=WebsiteParser.getInstance().getSite("WaveXXLFromIsin", "isin", isin, client);
        if (site.hasNextNode()) {
            waveXXL=new WaveXXL(site.getString("wkn")
                    , site.getFloat("bid")
                    , site.getFloat("ask")
                    , site.getFloat("strike")
                    , site.getFloat("ko")
                    , site.getFloat("leverage")
                    , site.getFloat("ratio")
                    );
        }
        return waveXXL;
    }
}
