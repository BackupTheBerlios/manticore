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

import java.util.List;
import java.util.Vector;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author are
 */
public class Page {
    String name;
    String urlStr;
    Vector<Trigger> triggerVector;

    public Page(Element element) {
        name=element.attributeValue("name");
        urlStr=element.attributeValue("url");

        List<Node> nodeList=element.selectNodes("trigger");
        triggerVector=new Vector(nodeList.size());

        for (int i=0; i<nodeList.size(); i++) {
            Trigger trigger=new Trigger( (Element) nodeList.get(i) );
            triggerVector.add(trigger);
        }
    }

    public int size() {
        return triggerVector.size();
    }
}
