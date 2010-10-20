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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author are
 */
public class AbstractChartExport extends Thread {
    protected  RenderedImage rendImage;

    public AbstractChartExport(RenderedImage rendImage) {
        this.rendImage = rendImage;
    }

    @Override
    public void run() {
        try {
            File file = writeTempImageFile();
            String urlStr = getUrlFromUpload(file);

            writeToClipboard("[img]".concat(urlStr).concat("[/img]\n[url]www.manticore-projects.com[/url] © 2010"));
        } catch (Exception ex) {
            Logger.getLogger(AbstractChartExport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String getUrlFromUpload(File file) {
        return "";
    };

    private File writeTempImageFile() throws IOException {
        File file = File.createTempFile("chart", ".png");
        file.deleteOnExit();
        ImageIO.write(rendImage, "png", file);
        return file;
    }
    
    public static void writeToClipboard(String writeMe) {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferableText =new StringSelection(writeMe);
        systemClipboard.setContents(transferableText, null);
    }
}
