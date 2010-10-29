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
package com.manticore.util;

import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import org.dom4j.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

public class Settings {

	 private static Settings instance;
	 public final static String FILENAME = "settings.xml";
	 private boolean shallSave = false;
	 private Document doc;
	 public final static Color MANTICORE_DARK_BLUE = new Color(3, 1, 70);
	 public final static Color MANTICORE_LIGHT_BLUE = new Color(211, 210, 227);
	 public final static Color MANTICORE_LIGHT_BLUE_TRANSPARENT = new Color(211, 210, 227, 200);
	 public final static Color MANTICORE_ORANGE = new Color(255, 66, 14);
	 public final static Color MANTICORE_LIGHT_GREY = new Color(230, 230, 230);
	 public final static Color MANTICORE_DARK_GREY = new Color(179, 179, 179);
	 public final static Font SMALL_MANTICORE_FONT = new Font("VL PGothic", Font.PLAIN, 12);
	 public final static Font SMALL_MANTICORE_FONT_BOLD = new Font("VL PGothic", Font.BOLD, 12);
	 public final static Font MEDIUM_MANTICORE_FONT = new Font("VL PGothic", Font.PLAIN, 16);
	 public final static Font BIG_MANTICORE_FONT = new Font("VL PGothic", Font.PLAIN, 22);

	 public Settings() {
		  try {
				doc = XMLTools.readXML(getSettingsInputStream());
		  } catch (Exception x) {
				doc = DocumentFactory.getInstance().createDocument();
				Element e = doc.addElement("programs");
				e = e.addElement("program");
				e.addAttribute("name", System.getProperty("ant.project.name", ""));
				shallSave = true;
		  }
	 }

	 public static Settings getInstance() {
		  if (instance == null) {
				instance = new Settings();
		  }
		  return instance;
	 }

	 public void writeToFile() {
		  if (shallSave) {
				XMLTools.writeToXML(doc, getSettingsFilename());
		  }
	 }

	 public String get(String Program, String Module, String Option) {
		  String resultStr = "";
		  String XPath = "//program[@name='" + Program + "']/module[@name='" + Module + "']/option[@name='" + Option + "']";

		  Node n = doc.selectSingleNode(XPath);
		  if (n != null) {
				resultStr = n.getText();
		  }
		  return resultStr;
	 }

	 public Float getFloat(String Program, String Module, String Option) {
		  return Float.parseFloat(get(Program, Module, Option));
	 }

	 public Integer getInt(String Program, String Module, String Option) {
		  return Integer.parseInt(get(Program, Module, Option));
	 }

	 public boolean getBoolean(String Program, String Module, String Option) {
		  String s = get(Program, Module, Option);
		  return (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("1") || s.equalsIgnoreCase("y") || s.equalsIgnoreCase("ok"));
	 }

	 public void set(String Program, String Module, String Setting, String Value) {
		  Node n = getSetting(Program, Module, Setting);
		  if (!Value.equals(n.getText())) {
				shallSave = true;
				n.setText(Value);
		  }
	 }

	 //@todo: is there a smarter solution for the string-bool-transfomer?
	 public void set(String Program, String Module, String Setting, boolean Value) {
		  Node n = getSetting(Program, Module, Setting);
		  boolean previousValue = (n.getText().equals("true"));

		  if (Value != previousValue) {
				shallSave = true;

				if (Value) {
					 n.setText("true");
				} else {
					 n.setText("false");
				}
		  }
	 }

	 private Node getRoot() {
		  String XPath = "/programs";

		  Node n = doc.selectSingleNode(XPath);
		  if (n == null) {
				Element e = doc.addElement("programs");

				n = (Node) e;
				shallSave = true;
		  }
		  return n;
	 }

	 private Node getProgram(String Program) {
		  String XPath = "//program[@name='" + Program + "']";

		  Node n = doc.selectSingleNode(XPath);
		  if (n == null) {
				Element e = ((Element) getRoot()).addElement("program").addAttribute("name", Program);

				n = (Node) e;
				shallSave = true;
		  }
		  return n;
	 }

	 private Node getModule(String Program, String Module) {
		  String XPath = "//program[@name='" + Program + "']/module[@name='" + Module + "']";

		  Node n = doc.selectSingleNode(XPath);
		  if (n == null) {
				Element e = ((Element) getProgram(Program)).addElement("module").addAttribute("name", Module);

				n = (Node) e;
				shallSave = true;
		  }
		  return n;
	 }

	 private Node getSetting(String Program, String Module, String Option) {
		  String XPath = "//program[@name='" + Program + "']/module[@name='" + Module + "']/option[@name='" + Option + "']";

		  Node n = doc.selectSingleNode(XPath);
		  if (n == null) {
				Element e = ((Element) getModule(Program, Module)).addElement("option").addAttribute("name", Option);

				n = (Node) e;
				shallSave = true;
		  }
		  return n;
	 }

	 public String getSettingsFilename() {
		  String HomeDir = System.getProperty("user.home");

		  if (!HomeDir.endsWith(File.separator)) {
				HomeDir += File.separator;
		  }
		  return HomeDir.concat(FILENAME);
	 }

	 public InputStream getSettingsInputStream() {
		  InputStream inputStream = null;
		  File f = new File(getSettingsFilename());
		  if (f.canRead()) {
				try {
					 inputStream = new FileInputStream(f);
				} catch (FileNotFoundException ex) {
					 Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
				}
		  } else {
				inputStream = getClass().getResourceAsStream("/com/manticore/util/" + FILENAME);
				Logger.getLogger(Settings.class.getName()).info("Settings not found, start with predefined parameters");
		  }
		  return inputStream;
	 }

	 public String getProjectName() {
		  String projectName = "";
		  projectName = System.getProperty("ant.project.name", "none");
		  return projectName;
	 }

	 public Document getDatasourceDocument() throws Exception {
		  Document doc = null;
		  String filename = "";

		  filename = get("jPortfolioView", "file", "datasource");
		  if (filename.length() > 0 && (new File(filename).exists())) {
				doc = XMLTools.readXML(filename);

		  } else {
				doc = XMLTools.readXMLResource("/datasources.xml");
		  }
		  return doc;
	 }

	 public static void setProxy() {
		  //set proxy if available
		  String proxyIP = getInstance().get("PortfolioScreener", "network", "proxyIP");
		  String proxyPort = getInstance().get("PortfolioScreener", "network", "proxyPort");

		  if (proxyIP.length() > 0) {
				// Modify system properties
				Properties sysProperties = System.getProperties();

				// Specify proxy settings
				sysProperties.put("proxyHost", proxyIP);
				sysProperties.put("proxyPort", proxyPort);
				sysProperties.put("proxySet", "true");
		  } else {
				System.getProperties().put("proxySet", "false");
		  }
	 }

	 public HashMap<String, HashMap<String, String>> getSettingsHashMap(String programName) {
		  HashMap<String, HashMap<String, String>> settingsHashMap = new HashMap<String, HashMap<String, String>>();

		  Iterator<Element> programIterator = doc.getRootElement().elements("program").iterator();
		  while (programIterator.hasNext()) {
				Element programElement = programIterator.next();

				if (programElement.attributeValue("name").equalsIgnoreCase(programName)) {
					 Iterator<Element> moduleIterator = programElement.elements("module").iterator();
					 while (moduleIterator.hasNext()) {
						  Element moduleElement = moduleIterator.next();

						  HashMap<String, String> moduleHashMap = new HashMap<String, String>();
						  Iterator<Element> optionIterator = moduleElement.elements("option").iterator();
						  while (optionIterator.hasNext()) {
								Element optionElement = optionIterator.next();
								moduleHashMap.put(optionElement.attributeValue("name"), optionElement.getTextTrim());
						  }
						  settingsHashMap.put(moduleElement.attributeValue("name"), moduleHashMap);
					 }
				}
		  }
		  return settingsHashMap;
	 }

	 public void updateDocumentFromHashMap(String programName, HashMap<String, HashMap<String, String>> settingsHashMap) {
		  Iterator<Element> programIterator = doc.getRootElement().elements("program").iterator();
		  while (programIterator.hasNext()) {
				Element programElement = programIterator.next();

				if (programElement.attributeValue("name").equalsIgnoreCase(programName)) {
					 Iterator<String> moduleNameIterator = settingsHashMap.keySet().iterator();
					 while (moduleNameIterator.hasNext()) {
						  String moduleName = moduleNameIterator.next();

						  Iterator<Element> moduleElementIterator = programElement.elements("module").iterator();
						  while (moduleElementIterator.hasNext()) {
								Element moduleElement = moduleElementIterator.next();
								if (moduleElement.attributeValue("name").equalsIgnoreCase(moduleName)) {

									 Iterator<Entry<String, String>> optionIterator = settingsHashMap.get(moduleName).entrySet().iterator();
									 while (optionIterator.hasNext()) {
										  Entry<String, String> optionEntry = optionIterator.next();

										  Iterator<Element> optionElementIterator = moduleElement.elements("option").iterator();
										  while (optionElementIterator.hasNext()) {
												Element optionElement = optionElementIterator.next();

												if (optionElement.attributeValue("name").equalsIgnoreCase(optionEntry.getKey())) {
													 if (!optionElement.getTextTrim().equalsIgnoreCase(optionEntry.getValue())) {
														  Logger.getLogger(this.getClass().getName()).info("Option " + optionEntry.getKey() + " set to " + optionEntry.getValue());
														  optionElement.setText(optionEntry.getValue());
														  shallSave = true;
													 }
												}
										  }
									 }
								}
						  }
					 }
				}
		  }
		  if (shallSave) {
				writeToFile();
		  }
	 }
}
