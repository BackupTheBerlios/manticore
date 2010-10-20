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
import com.manticore.position.PositionGrid;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

public class Demo2 {
    public static void main(String args[]) {
        PositionDataStorage positionDataStorage=new DefaultPositionDataStorage();
        TanReader tanReader=new TanDialog();
        Instrument instrument=Instrument.getDefaultInstrument();
        PositionGrid positionGrid=new PositionGrid(positionDataStorage, tanReader);
        positionGrid.setInstrument(instrument);
        
        JFrame frame=new JFrame("manticore trader - portfolio building demo");
        frame.setPreferredSize(new Dimension(120,80));
        frame.add(positionGrid);
        frame.pack();
        frame.addWindowListener(new WindowListener() {

            public void windowOpened(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            public void windowClosed(WindowEvent e) {
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });
        frame.setVisible(true);
    }
}
