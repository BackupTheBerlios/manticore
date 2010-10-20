/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.trader;

import com.manticore.database.Quotes;
import com.manticore.util.XMLTools;
import com.manticore.xmarkets.WaveXXLParser;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author are
 */
public class importExtKey {
    public static Pattern pattern=Pattern.compile(".*inbwnr\\=(\\d*)");
    public static void main(String[] args) {
        //System.out.println(XMLTools.extractString("showpage.asp?pageid=936&amp;inbwnr=18", pattern));
        
        importExtKeyDB();
    }
    
    public static void importExtKeyDB() {
        try {
            String sqlStr = "select id_instrument, description, value from trader.instrument left join trader.ext_key_instrument using (id_instrument) where id_ext_key=5 and not value is null;";
            ResultSet rs = Quotes.getInstance().getResultSet(sqlStr);
            while (rs.next()) {
                String underlyingID=getUnderlyingID(rs.getString(3));
                if (underlyingID.length()>0) System.out.println(", (" + rs.getString(1) + ", " + underlyingID +")");
            }
        } catch (SQLException ex) {
            Logger.getLogger(importExtKey.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getUnderlyingID(String isin) {
        String underlyingID = "";
        try {
            HttpResponse response;
            String urlStr = "http://www.de.x-markets.db.com/DE/showpage.asp?pageid=582&lang=DE&ajaxpid=1052&stinput=" + isin;
            //String urlStr="http://www.de.x-markets.db.com/DE/showpage.asp?pageid=582&lang=DE&stinput=AT0000676903";
            DefaultHttpClient client = WaveXXLParser.getPreparedHttpClient();
            client.getParams().setBooleanParameter("http.protocol.handle-redirects", false);
            //client.getParams().setIntParameter("http.protocol.max-redirects", 1);
            HttpGet get = new HttpGet(urlStr);
            response = client.execute(get);
            Header[] headers=response.getAllHeaders();
            response.getEntity().consumeContent();
            for (int i=0; i<headers.length; i++) {
                if (headers[i].getName().equals("Location")) {
                    get = new HttpGet(headers[i].getValue());
                    response = client.execute(get);

                    Document document=XMLTools.parseHtml(response.getEntity().getContent());
                    Node node=document.selectSingleNode("/html:html/html:body/html:a/@href");
                    if (node!=null) {
                        underlyingID=XMLTools.extractString(node.getText(), pattern);
                    }
                }
            }
            
        } catch (SAXException ex) {
            Logger.getLogger(importExtKey.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DocumentException ex) {
            Logger.getLogger(importExtKey.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(importExtKey.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(importExtKey.class.getName()).log(Level.SEVERE, null, ex);
        }

        return underlyingID;
    }

}
