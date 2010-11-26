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


package com.manticore.swingui;

import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;

/**
 *
 * @author are
 */
public class TextArea extends StandaloneTextArea {
	 private static final PropertyManager propertyManager=new PropertyManager();
	 public TextArea() {
				super(propertyManager);
				Mode mode=new Mode("groovy");
				mode.setProperty("file","/modes/groovy.xml");
				ModeProvider.instance.addMode(mode);

				getBuffer().setStringProperty("mode", "groovy");
				getBuffer().setMode(mode);
				getBuffer().setDirty(true);
				setQuickCopyEnabled(true);
				setVerifyInputWhenFocusTarget(true);
	 }

	 public TextArea(String modeName) {
				super(propertyManager);
				Mode mode=new Mode(modeName);
				mode.setProperty("file","/modes/"+modeName+".xml");
				ModeProvider.instance.addMode(mode);

				getBuffer().setStringProperty("mode", modeName);
				getBuffer().setMode(mode);
				getBuffer().setDirty(true);
				setQuickCopyEnabled(true);
				setVerifyInputWhenFocusTarget(true);
	 }
}
