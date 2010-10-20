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
package com.manticore.report;

import com.manticore.swingui.FormatedTextField;
import com.manticore.swingui.GridBagPane;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerformanceRatioPanel extends GridBagPane {

    public PerformanceRatioPanel(ResultSet rs) {
        try {
            double[]drawdown_amount=new double[0];
            int[]drawdown_count=new int[0];

            double[] profit_arr=new double[0];
            double[] profit_rel=new double[0];
            double[] profit_rel_w=new double[0];
            double[] profit_rel_l=new double[0];

            double dd_amount = 0d;
            int dd_count = 0;

            double profit = 0f;
            double loss = 0f;

            while (rs.next()) {
                profit_arr=java.util.Arrays.copyOf(profit_arr, profit_arr.length+1);
                profit_arr[profit_arr.length-1]=rs.getDouble("profit");

                profit_rel=java.util.Arrays.copyOf(profit_rel, profit_rel.length+1);
                profit_rel[profit_rel.length-1]=rs.getDouble("profit_rel");

                if (rs.getDouble("profit") < 0) {
                    loss += rs.getDouble("profit");

                    dd_amount += rs.getDouble("profit");
                    dd_count++;

                    profit_rel_l=java.util.Arrays.copyOf(profit_rel_l, profit_rel_l.length+1);
                    profit_rel_l[profit_rel_l.length-1]=rs.getDouble("profit_rel");
                } else if (rs.getDouble("profit") > 0) {
                    profit += rs.getDouble("profit");

                    profit_rel_w=java.util.Arrays.copyOf(profit_rel_w, profit_rel_w.length+1);
                    profit_rel_w[profit_rel_w.length-1]=rs.getDouble("profit_rel");

                    drawdown_amount=java.util.Arrays.copyOf(drawdown_amount, drawdown_amount.length+1);
                    drawdown_amount[drawdown_amount.length-1]=dd_amount;

                    drawdown_count=java.util.Arrays.copyOf(drawdown_count, drawdown_count.length+1);
                    drawdown_count[drawdown_count.length-1]=dd_count;

                    dd_amount = 0d;
                    dd_count = 0;
                }
            }

            // in case the current drawdown is the worst one
            drawdown_amount=java.util.Arrays.copyOf(drawdown_amount, drawdown_amount.length+1);
            drawdown_amount[drawdown_amount.length-1]=dd_amount;
            drawdown_count=java.util.Arrays.copyOf(drawdown_count, drawdown_count.length+1);
            drawdown_count[drawdown_count.length-1]=dd_count;


            java.util.Arrays.sort(drawdown_amount);
            java.util.Arrays.sort(drawdown_count);
            java.util.Arrays.sort(profit_arr);
            java.util.Arrays.sort(profit_rel);
            java.util.Arrays.sort(profit_rel_l);
            java.util.Arrays.sort(profit_rel_w);

            double P=(double) profit_rel_w.length/ (double) (profit_rel_l.length + profit_rel_w.length);
            double B=profit / (-loss);
            
            double optimalF = 0d;
            double maxG=0d;
            double GAT=0d;

            double[] f1=Statistics.getIntervals(0.0f, 1f, 0.01f);
            double G=0d;
            for (int k=0; k<f1.length && G>=maxG; k++) {
                double g[]=new double[profit_arr.length];
                for (int i=0; i<profit_arr.length; i++) {
                    g[i]=(1- f1[k] *(profit_arr[i]/profit_arr[0]));
                }

                G=Statistics.geoMean(g);
                if (G>maxG) {
                    maxG=G;
                    optimalF=f1[k];
                }
            }

            GAT = (maxG-1) * (profit_arr[0]/-optimalF);

            FormatedTextField w_count = new FormatedTextField(profit_rel_w.length, FormatedTextField.INTEGER_FORMAT, false);
            add(w_count, "label=Profit trades: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField l_count = new FormatedTextField(profit_rel_l.length, FormatedTextField.INTEGER_FORMAT, false);
            add(l_count, "nl, label=Loss trades: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField p = new FormatedTextField(P, FormatedTextField.PERCENT_FORMAT, false);
            add(p, "nl, label=P, fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField w_max_profit_rel = new FormatedTextField(profit_rel_w[profit_rel_w.length-1], FormatedTextField.PERCENT_FORMAT, false);
            add(w_max_profit_rel, "nl, label=Max. profit: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField w_avg_profit_rel = new FormatedTextField(Statistics.average(profit_rel_w), FormatedTextField.PERCENT_FORMAT, false);
            add(w_avg_profit_rel, "nl, label=Avg. profit: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField w_stddev_profit_rel = new FormatedTextField(Statistics.stdev(profit_rel_w), FormatedTextField.PERCENT_FORMAT, false);
            add(w_stddev_profit_rel, "nl, label=Std. deviation: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField l_max_profit_rel = new FormatedTextField(profit_rel_w[0], FormatedTextField.PERCENT_FORMAT, false);
            add(l_max_profit_rel, "nl, label=Max. loss: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField l_avg_profit_rel = new FormatedTextField(Statistics.average(profit_rel_l), FormatedTextField.PERCENT_FORMAT, false);
            add(l_avg_profit_rel, "nl, label=Avg. loss: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField l_stddev_profit_rel = new FormatedTextField(Statistics.stdev(profit_rel_l), FormatedTextField.PERCENT_FORMAT, false);
            add(l_stddev_profit_rel, "nl, label=Std. deviation: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField b = new FormatedTextField(B, FormatedTextField.PERCENT_FORMAT, false);
            add(b, "nl, label=Profit factor: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField f = new FormatedTextField(optimalF, FormatedTextField.PERCENT_FORMAT, false);
            add(f, "nl, label=Optimal F: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField gat = new FormatedTextField(GAT, FormatedTextField.DECIMAL_FORMAT, false);
            add(gat, "nl, label=Geom. avg. profit: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField avg_drawdown = new FormatedTextField(Statistics.average(drawdown_amount), FormatedTextField.DECIMAL_FORMAT, false);
            add(avg_drawdown, "nl, label=Avg. loss in row: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField max_drawdown = new FormatedTextField(drawdown_amount[0], FormatedTextField.DECIMAL_FORMAT, false);
            add(max_drawdown, "nl, label=Max. loss in row: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField avg_losses_in_row = new FormatedTextField(Statistics.average(drawdown_count), FormatedTextField.DECIMAL_FORMAT, false);
            add(avg_losses_in_row, "nl, label=Avg. losses in row: , fill=BOTH, weightx=0.0f, weighty=0.2f ");

            FormatedTextField max_losses_in_row = new FormatedTextField(drawdown_count[drawdown_count.length-1], FormatedTextField.DECIMAL_FORMAT, false);
            add(max_losses_in_row, "nl, label=Max. losses in row: , fill=BOTH, weightx=0.0f, weighty=0.2f ");
        } catch (SQLException ex) {
            Logger.getLogger(PerformanceRatioPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
