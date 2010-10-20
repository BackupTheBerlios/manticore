package com.manticore.swingui;


import java.awt.*;
import javax.swing.*;

public class GridBagPane extends JPanel {

    GridBagLayout gbl;
    static int FLAG_SIZE = 1;
    private static final Insets DEFAULT_LABEL_INSETS = new Insets(2, 2, 2, 1);
    private static final GridBagConstraints DEFAULT_LABEL_CONSTRAINTS = new GridBagConstraints(1, 1, 1, 1, 0d, 0d, GridBagConstraints.BASELINE_TRAILING, GridBagConstraints.NONE, DEFAULT_LABEL_INSETS, 0, 0);
    private static final Insets DEFAULT_CONTROL_INSETS = new Insets(2, 1, 2, 2);
    private static final GridBagConstraints DEFAULT_CONTROL_CONSTRAINTS = new GridBagConstraints(0, 0, 1, 1, 0d, 0d, GridBagConstraints.BASELINE_LEADING, GridBagConstraints.NONE, DEFAULT_CONTROL_INSETS, 0, 0);
    private GridBagConstraints currentLabelConstraints = (GridBagConstraints) DEFAULT_LABEL_CONSTRAINTS.clone();
    private GridBagConstraints currentControlConstraints = (GridBagConstraints) DEFAULT_CONTROL_CONSTRAINTS.clone();

    public GridBagPane() {
        gbl = new GridBagLayout();

        setLayout(gbl);
    }

    public void setControlInsets(int top, int left, int bottom, int right) {
        currentControlConstraints.insets.set(top, left, bottom, right);
    }

    public void setLabelInsets(int top, int left, int bottom, int right) {
        currentLabelConstraints.insets.set(top, left, bottom, right);
    }

    public Component add(JComponent c, String l, int gridx, int gridy) {

        add(new JLabel(l, SwingConstants.RIGHT), new GridBagConstraints(gridx - 1, gridy, 1, 1, 1d, 1d, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 6, 6, 3), 0, 0));

        currentControlConstraints.gridx = gridx;
        currentControlConstraints.gridy = gridy;
        currentControlConstraints.gridwidth = 5;
        currentControlConstraints.gridheight = 1;
        //c.setPreferredSize(GlobalProperties.getInstance().getMinDim());
        add(c, currentControlConstraints);
        return c;
    }

    public Component add(JComponent c, String f) {
        String e[] = f.split("[,]+");
        int flags = 0;

        for (int i = 0; i < e.length; i++) {
            String s[] = e[i].split("[\\=]+");

            if (s.length == 1) {
                flags += parseFormatString(c, s[0].trim());
            } else if (s.length == 2) {
                flags += parseFormatString(c, s[0].trim(), s[1].trim());
            }
        }

        if ((flags & FLAG_SIZE) == 0) {
            //c.setPreferredSize(GlobalProperties.getInstance().getMinDim());
        }
        add(c, currentControlConstraints);

        currentControlConstraints.gridx += currentControlConstraints.gridwidth;
        return c;
    }

    public GridBagConstraints getConstraints() {
        return currentControlConstraints;
    }

    public int parseFormatString(JComponent c, String s, String a) {
        int flag = 0;

        if (s.equalsIgnoreCase("gridx")) {
            currentControlConstraints.gridx = Integer.valueOf(a).intValue();

        } else if (s.equalsIgnoreCase("gridy")) {
            currentControlConstraints.gridy = Integer.valueOf(a).intValue();

        } else if (s.equalsIgnoreCase("label")) {
            String N = GlobalProperties.getInstance().localizeStr(a);
            JLabel L = new JLabel(N, SwingConstants.RIGHT);

            currentLabelConstraints.gridx = currentControlConstraints.gridx;
            currentLabelConstraints.gridy = currentControlConstraints.gridy;
            add(L, currentLabelConstraints);
            L.setLabelFor(c);
            c.setName(N);
            currentControlConstraints.gridx++;

        } else if (s.equalsIgnoreCase("gridheight")) {
            currentControlConstraints.gridheight = Integer.valueOf(a).intValue();

        } else if (s.equalsIgnoreCase("gridwidth")) {
            currentControlConstraints.gridwidth = Integer.valueOf(a).intValue();

        } else if (s.equalsIgnoreCase("weightx")) {
            currentControlConstraints.weightx = Double.valueOf(a).floatValue();

        } else if (s.equalsIgnoreCase("weighty")) {
            currentControlConstraints.weighty = Double.valueOf(a).floatValue();

        } else if (s.equalsIgnoreCase("anchor")) {
            try {
                currentControlConstraints.anchor = currentControlConstraints.getClass().getField(a).getInt(currentControlConstraints);
            } catch (Exception ex) {
            }
        } else if (s.equalsIgnoreCase("fill")) {
            try {
                currentControlConstraints.fill = currentControlConstraints.getClass().getField(a).getInt(currentControlConstraints);
            } catch (Exception ex) {
            }
        } else if (s.equalsIgnoreCase("ipdax")) {
            currentControlConstraints.ipadx = Integer.valueOf(a).intValue();
        } else if (s.equalsIgnoreCase("ipady")) {
            currentControlConstraints.ipady = Integer.valueOf(a).intValue();
        } else if (s.equalsIgnoreCase("insets")) {
            String t[] = a.split("[\\s]+");
            if (t.length == 4) {
                currentControlConstraints.insets.left = Integer.valueOf(t[0]).intValue();
                currentControlConstraints.insets.top = Integer.valueOf(t[1]).intValue();
                currentControlConstraints.insets.right = Integer.valueOf(t[2]).intValue();
                currentControlConstraints.insets.bottom = Integer.valueOf(t[3]).intValue();
            }
        } else if (s.equalsIgnoreCase("labelinsets")) {
            String t[] = a.split("[\\s]+");
            if (t.length == 4) {
                currentLabelConstraints.insets.left = Integer.valueOf(t[0]).intValue();
                currentLabelConstraints.insets.top = Integer.valueOf(t[1]).intValue();
                currentLabelConstraints.insets.right = Integer.valueOf(t[2]).intValue();
                currentLabelConstraints.insets.bottom = Integer.valueOf(t[3]).intValue();
            }
        } else if (s.equalsIgnoreCase("Size")) {
            String t[] = a.split("[\\s]+");
            if (t.length == 2) {
                c.setPreferredSize(new Dimension(Integer.valueOf(t[0]).intValue(),
                  Integer.valueOf(t[1]).intValue()));
                flag = FLAG_SIZE;
            }
        } else if (s.equalsIgnoreCase("setToolTipText") || s.equalsIgnoreCase("tooltip")) {
            c.setToolTipText(GlobalProperties.getInstance().localizeStr(a));
        }

        return flag;
    }

    public int parseFormatString(JComponent c, String s) {
        int flag = 0;

        if (s.equalsIgnoreCase("nl")) {
            currentControlConstraints.gridx = 0;
            currentControlConstraints.gridy++;

        } else if (s.equalsIgnoreCase("dx")) {
            currentControlConstraints.gridx++;

        } else if (s.equalsIgnoreCase("dy")) {
            currentControlConstraints.gridy++;

        } else if (s.equalsIgnoreCase("reset")) {
            int gridx = currentControlConstraints.gridx;
            int gridy = currentControlConstraints.gridy;

            currentControlConstraints = (GridBagConstraints) DEFAULT_CONTROL_CONSTRAINTS.clone();
            currentControlConstraints.gridx = gridx;
            currentControlConstraints.gridy = gridy;
        }

        return flag;
    }
}
