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


import com.manticore.system.PropertyManager;
import javax.swing.JFrame;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;

public class Main {
    public static void main(String[] args) {
				//        try {
				//            // create a script engine manager
				//            ScriptEngineManager factory = new ScriptEngineManager();
				//            // create a JavaScript engine
				//            ScriptEngine engine = factory.getEngineByName("JavaScript");
				//
				//				Iterator<ScriptEngineFactory> iterator= factory.getEngineFactories().iterator();
				//				while (iterator.hasNext()) {
				//					 System.out.println( iterator.next().getLanguageName());
				//				}
				//
				//            // create a Java object
				//            String name = "Tom";
				//            // create the binding
				//            engine.put("greetingname", name);
				//            // evaluate JavaScript code from String
				//            engine.eval("println('Hello, ' + greetingname)");
				//            engine.eval("println('The name length is ' +  greetingname.length)");
				//TradeSystemApplication tradeSystemApplication = new TradeSystemApplication();
				//tradeSystemApplication.setVisible(true);

				PropertyManager propertyManager=new PropertyManager();

				StandaloneTextArea textArea=new StandaloneTextArea(propertyManager);
				String text=new StringBuilder()
						  .append("String s1= new TestJavaClass(\"so\", \"läuft es nicht\").concat();")
						  .append("String s1= new TestJavaClass(\"so\", \"läuft es nicht\").concat();")
						  .toString();
				textArea.setText(text);

				Mode mode=new Mode("groovy");
				mode.setProperty("file","/home/are/data/src/TradingSystem/src/modes/groovy.xml");
				ModeProvider.instance.addMode(mode);

				textArea.getBuffer().setStringProperty("mode", "groovy");

				textArea.getBuffer().setMode(mode);
				textArea.getBuffer().setDirty(true);
				textArea.setQuickCopyEnabled(true);
				textArea.setVerifyInputWhenFocusTarget(true);

				textArea.formatParagraph();
				textArea.updateUI();

				JFrame frame=new JFrame("test");
				frame.add(textArea);
				frame.setSize(480, 240);
				frame.setVisible(true);

    }
}
