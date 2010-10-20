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

package com.manticore.chartexport;

import com.manticore.parser.WebsiteParser;
import com.manticore.parser.WebsiteParser.Site;
import com.manticore.util.HttpClientFactory;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author are
 */
public class ABLoadChartExport extends AbstractChartExport {
//    private final static String XPATH0 = "/*[name()='html']/*[name()='body']/*[name()='form']/*[name()='textarea']";
//    private final static String XPATH1 = "/html:html/html:body[1]/html:div/html:div[3]/html:div[2]/html:table/html:tr[2]/html:td[2]/html:input/@value";

    public ABLoadChartExport(RenderedImage rendImage) {
        super(rendImage);
    }

    String getUrlFromUpload(File file)  {
        String paramStr="";
        String urlStr ="";

        try {
            DefaultHttpClient client = HttpClientFactory.getClient();

            WebsiteParser.getInstance().getSite("abload", client);
            
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("img0", new FileBody(file));
            reqEntity.addPart("img1", new StringBody(""));
            reqEntity.addPart("resize", new StringBody("none"));
            Site site=WebsiteParser.getInstance().getSiteFromPost("abloadUpload", client, reqEntity);
            if (site.hasNextNode()) paramStr=site.getString("paramStr");

            Vector<NameValuePair> nameValuePairList=new Vector();
            nameValuePairList.add(new BasicNameValuePair("gallery", null));
            nameValuePairList.add(new BasicNameValuePair("images", paramStr));
            UrlEncodedFormEntity entity2=new UrlEncodedFormEntity(nameValuePairList);
            site=WebsiteParser.getInstance().getSiteFromPost("abloadUploadComplete", client, entity2);
            if (site.hasNextNode()) urlStr=site.getString("imageUrl");
        } catch (Exception ex) {
            Logger.getLogger(ABLoadChartExport.class.getName()).log(Level.SEVERE, null, ex);
        }
        return urlStr;
    }
}
