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


import com.manticore.connection.TanDialog;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.PositionDataStorage;
import com.manticore.foundation.TanReader;
import com.manticore.position.DefaultPositionDataStorage;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.manticore.swingui.MenuBar;
import com.manticore.swingui.SwingUI;
import com.manticore.swingui.ToolBar;
import com.manticore.position.PositionControler;
import javax.swing.JComponent;

class Demo extends SwingUI {
    PositionControler positionControler;

    public Demo() {
        this.setTitle("manticore trader - position building demo");
        PositionDataStorage positionDataStorage=new DefaultPositionDataStorage();
        TanReader tanReader=new TanDialog();
        Instrument instrument=Instrument.getDefaultInstrument();

        positionControler=new PositionControler(positionDataStorage, tanReader, instrument);
//        initUI((JComponent) positionControler.getPositionView());
    }

    public void initMenuBar() {
        String MenuStr[] = {"File", "Help"};
        String MenuItemStr[][] = {
            {"exit"}, { "about"}
        };

        menuBar = new MenuBar(this, MenuStr, MenuItemStr);
    }

    @Override
    public void initToolBar() {
        String ButtonStr[] = {"exit", "refresh"};
        toolBar = new ToolBar(ButtonStr);
    }

    public static void main(String args[]) {
        new Demo();
    }

    public boolean dispatchActionCommand(String commandStr) {
        boolean dispatched = false;
        Method[] methodArray = getClass().getMethods();

        for (int i = 0; i < methodArray.length; i++) {
            Method method = methodArray[i];
            if (method.getName().equalsIgnoreCase(commandStr)) {
                try {
                    method.invoke((Object) this);
                    dispatched = true;
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (!dispatched) {
            //dispatched &= realTimeChartPane.dispatchActionCommand(commandStr);
        }

        return dispatched;
    }
    // implements ActionListener
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (dispatchActionCommand(actionCommand)) {
            //GlobalProperties.getInstance().showStatusMsg(actionCommand);
        }
    }

}


