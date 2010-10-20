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

import java.util.regex.Pattern;
import org.dom4j.Element;

public class Trigger {
    String name;
    String xpathStr;
    String regexStr;
    Pattern pattern;
    int width;
    int allignment;

    public Trigger(Element element) {
        name=element.attributeValue("name");
        xpathStr=element.attributeValue("xpath");
        regexStr=element.attributeValue("regex");

        if (regexStr.length()>0) {
            pattern= Pattern.compile(regexStr, Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
        }

        width=Integer.decode( element.attributeValue("width") );
        allignment=Integer.decode( element.attributeValue("alignment") );
    }
}
