/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manticore.parser;

import com.manticore.parser.WebsiteParser.Site;
import com.manticore.util.HttpClientFactory;
import java.util.Date;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

/**
 *
 * @author are
 */
public class WebsiteParserTest {
    public static DefaultHttpClient getPreparedHttpClient() {
        DefaultHttpClient client = HttpClientFactory.getClient();

        BasicClientCookie cookie = new BasicClientCookie("x%2Dmarkets%2Ede%2Ddisclaimer", "ok");
        cookie.setDomain("www.de.x-markets.db.com");
        cookie.setPath("/");


        cookie.setExpiryDate(new Date());

        client.getCookieStore().addCookie(cookie);
        return client;
    }
    public static void main(String[] args) {
        WebsiteParser.getInstance();
        DefaultHttpClient client = getPreparedHttpClient();
        WebsiteParser.getInstance().writeTempXMLFiles=true;
//        Site site = WebsiteParser.getInstance().getSite("NewsList");
//            while (site.hasNextNode()) {
//                System.out.println("time " + site.getString("time"));
//                System.out.println("date " + site.getString("date"));
//                System.out.println("href " + site.getString("href"));
//                System.out.println("caption" + site.getString("caption"));
//                System.out.println("agency" + site.getString("agency"));
//        }

        Site site = WebsiteParser.getInstance().getSite("WaveXXLFromIsin", "isin", "DB9BE3", client);
            if (site.hasNextNode()) {
            System.out.println("wkn " + site.getString("wkn"));
            System.out.println("bid " + site.getFloat("bid"));
            System.out.println("ask " + site.getFloat("ask"));
            System.out.println("strike " + site.getFloat("strike"));
            System.out.println("ko " + site.getFloat("ko"));
            System.out.println("leverage " + site.getFloat("leverage"));
            System.out.println("ratio " + site.getDouble("ratio"));
        }

        String[][] paramArr = {{"inwpnr", "26"}, {"inbwnr", "18"}};
        site = WebsiteParser.getInstance().getSite("WaveXXLByStrike", paramArr, client);
        while (site.hasNextNode()) {
            System.out.println("---------------------");
            System.out.println("wkn " + site.getString("wkn"));
            System.out.println("bid " + site.getFloat("bid"));
            System.out.println("ask " + site.getFloat("ask"));
            System.out.println("strike " + site.getFloat("strike"));
            System.out.println("ko " + site.getFloat("ko"));
            System.out.println("leverage " + site.getFloat("leverage"));
            System.out.println("ratio " + site.getDouble("ratio"));
        }
    }

//    public static void test_round_trip() throws IOException {
//        final File sourceFile = new File("/home/are/manticore-database-0.9.7.sql");
//        final File compressed = File.createTempFile("manticore-trader-0.9.7", ".dbu");
//        final File unCompressed = File.createTempFile("manticore-trader-0.9.7", ".uncompressed");
//
//        final LzmaOutputStream compressedOut = new LzmaOutputStream.Builder(
//                new BufferedOutputStream(new FileOutputStream(compressed))).useMaximalDictionarySize().useEndMarkerMode(true).useBT4MatchFinder().build();
//
//        final InputStream sourceIn = new BufferedInputStream(new FileInputStream(sourceFile));
//
//        copy(sourceIn, compressedOut);
//        sourceIn.close();
//        compressedOut.close();
//
//        final LzmaInputStream compressedIn = new LzmaInputStream(
//                new BufferedInputStream(new FileInputStream(compressed)),
//                new Decoder());
//
//        final OutputStream uncompressedOut = new BufferedOutputStream(
//                new FileOutputStream(unCompressed));
//
//        copy(compressedIn, uncompressedOut);
//        compressedIn.close();
//        uncompressedOut.close();
//    }
}
