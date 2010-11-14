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


package com.manticore.ui;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gjt.sp.jedit.IPropertyManager;

public class PropertyManager implements IPropertyManager {
	 private final static Logger logger=Logger.getLogger(PropertyManager.class.getName());
	 private Properties properties;

	 public PropertyManager() {
		  properties = new Properties();
		  load("/org/gjt/sp/jedit/jedit.props");
		  load("/org/gjt/sp/jedit/jedit_keys.props");
		  properties.setProperty("view.gutter.lineNumbers", "true");
		  properties.setProperty("view.antiAlias", "false");
		  properties.setProperty("view.fracFontMetrics", "false");
		  properties.setProperty("buffer.folding", "indent");
		  properties.setProperty("buffer.wrap", "soft");
		  properties.setProperty("buffer.maxLineLen", "120");
		  properties.setProperty("buffer.tabSize", "4");
		  properties.setProperty("buffer.indentSize", "4");
		  properties.setProperty("view.middleMousePaste", "false");
	 }

	 public void load(String ressourceName) {
		  InputStream inputStream = this.getClass().getResourceAsStream(ressourceName);
		  Properties p1 = new Properties();
		  try {
				p1.load(inputStream);
		  } catch (IOException ex) {
				logger.log(Level.SEVERE, null, ex);
		  }
		  properties.putAll(p1);
	 }

	 public String getProperty(String string) {
		  return properties.getProperty(string);
	 }

	 public String a(String string) {
		  return properties.getProperty(string);
	 }
}
