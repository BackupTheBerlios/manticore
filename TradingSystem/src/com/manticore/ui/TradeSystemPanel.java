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

import com.manticore.chart.ChartCanvas;
import com.manticore.database.Quotes;
import com.manticore.position.PositionGrid;
import com.manticore.swingui.GridBagPane;
import com.manticore.system.StochRSIBreakOut;
import com.manticore.system.TradingSimulation1;
import com.manticore.system.TradingSystem;
import com.zcage.log.TextAreaHandler;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.logging.impl.Log4JLogger;
import sun.rmi.log.LogHandler;

public class TradeSystemPanel extends JPanel implements ActionListener {
	 private final static Logger logger=Logger.getLogger(TradeSystemPanel.class.getName());
	 private JTextField title;
	 private JTextField description;
	 private JTextField version;
	 private JTextField author;
	 private JTabbedPane tabbedPane;
	 private JTextArea textArea;
	 private JTextArea logTextArea;
	 private ChartCanvas chartCanvas;
	 private JList list;
	 private JButton button;
	 private PositionGrid positionGrid;
	 private TradingSystem system;

	 public TradeSystemPanel() {
		  title = new JTextField("system title");
		  description = new JTextField("description");
		  version = new JTextField("version");
		  author = new JTextField("author");

		  GridBagPane headerPane = new GridBagPane();
		  headerPane.add(title, "label=title:, fill=BOTH, weightx=1f, weighty=0f");
		  headerPane.add(title, "nl, label=description, fill=BOTH, weightx=1f, weighty=0f");

		  textArea = new JTextArea();
		  positionGrid=new PositionGrid(Quotes.getInstance(), Quotes.getInstance());

		  chartCanvas = new ChartCanvas();
		  chartCanvas.setMinimumSize(new Dimension(640,480));
		  
		  tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		  tabbedPane.addTab("System", new JScrollPane(textArea));

		  GridBagPane gridBagPane2=new GridBagPane();
		  gridBagPane2.add(chartCanvas, "weightx=0.8f, weighty=1f, fill=BOTH");
		  gridBagPane2.add(positionGrid, "weightx=0.2f, weighty=1f, fill=BOTH");
		  gridBagPane2.validate();
		  
		  tabbedPane.addTab("Chart and Trades", gridBagPane2);

		  logTextArea=new JTextArea();
		  tabbedPane.addTab("Log", new JScrollPane(logTextArea));

		  String[] itemList = {"Simulation", "Trading"};
		  list = new JList(itemList);
		  list.setSelectedIndex(0);

		  button = new JButton("start");
		  button.addActionListener(this);

		  JPanel panel = new JPanel(new GridLayout(1, 2, 48, 48));
		  panel.add(list);
		  panel.add(button);

		  setLayout(new BorderLayout(12, 12));
		  add(headerPane, BorderLayout.NORTH);
		  add(tabbedPane, BorderLayout.CENTER);
		  add(panel, BorderLayout.SOUTH);
	 }

	 String getTitle() {
		  return title.getText();
	 }

	 public boolean isSimulation() {
		  return list!=null && list.getSelectedIndex()==0;
	 }

	 public boolean isTrading() {
		  return list!=null && list.getSelectedIndex()==1;
	 }

	 public void actionPerformed(ActionEvent e) {
		  if (e.getSource().equals(button)) {
				runScript();
		  }
	 }

	 public void openScript() {
		  JFileChooser fileChooser = new JFileChooser();
		  if (fileChooser.showOpenDialog(list) == JFileChooser.APPROVE_OPTION) {
				File f = fileChooser.getSelectedFile();
				try {
					 FileReader fileReader = new FileReader(f);
					 BufferedReader bufferedReader = new BufferedReader(fileReader);
					 StringBuilder stringBuffer = new StringBuilder();
					 while (bufferedReader.ready()) {
						  stringBuffer.append(bufferedReader.readLine());
						  stringBuffer.append("\n\r");
					 }
					 textArea.setText(stringBuffer.toString());
				} catch (IOException ex) {
					 logger.log(Level.SEVERE, null, ex);
				}
		  }

	 }

	 public void runScript() {
		  if (isSimulation()) {
				startSimulation();
		  } else if (isTrading()) {
				startTrading();
		  }
	 }

	 private void startSimulation() {
		  system = new TradingSimulation1(chartCanvas);

		  TextAreaHandler handler=new TextAreaHandler();
		  handler.setTextArea(logTextArea);
		  system.logger.addHandler(handler);
	 }

	 private void startTrading() {
		  system=new StochRSIBreakOut(positionGrid, chartCanvas);
		  
		  TextAreaHandler handler=new TextAreaHandler();
		  handler.setTextArea(logTextArea);
		  system.logger.addHandler(handler);
	 }
}
