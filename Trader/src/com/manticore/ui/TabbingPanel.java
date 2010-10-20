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

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class TabbingPanel extends JPanel {
    ActionListener actionListener;
    MouseListener mouseListener;

    public TabbingPanel(ActionListener actionListener, MouseListener mouseListener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        this.actionListener=actionListener;
        this.mouseListener=mouseListener;
    }

    public JToggleButton add(String caption) {
        for (int i=0; i<getComponents().length;i++) {
            JToggleButton button=(JToggleButton) getComponent(i);
            button.setSelected(false);
        }

        JToggleButton jToggleButton=new JToggleButton(caption, true);
        jToggleButton.addActionListener(actionListener);
        jToggleButton.addMouseListener(mouseListener);
        add(jToggleButton);
        doLayout();

        return jToggleButton;
    }

    public int getSelectedIndex(JToggleButton jToggleButton) {
        int result=-1;
        for (int i=0; i<getComponents().length;i++) {
            JToggleButton button=(JToggleButton) getComponent(i);
            if (button.equals(jToggleButton)) {
                result=i;
            }else {
                button.setSelected(false);
            }
        }
        return result;
    }

    public int getSelectedIndex() {
        int result=-1;
        for (int i=0; i<getComponents().length;i++) {
            JToggleButton button=(JToggleButton) getComponent(i);
            if (button.isSelected()) {
                result=i;
            }
        }
        return result;
    }

    public void setSelectedIndex(int index) {
        for (int i=0; i<getComponents().length;i++) {
            JToggleButton button=(JToggleButton) getComponent(i);
            button.setSelected(i==index);
        }
    }
}
