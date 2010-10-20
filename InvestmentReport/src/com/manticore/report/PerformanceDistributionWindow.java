/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manticore.report;

import com.manticore.swingui.GridBagPane;
import com.manticore.swingui.MenuBar;
import com.manticore.swingui.SwingUI;
import com.manticore.swingui.ToolBar;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author are
 */
public class PerformanceDistributionWindow extends SwingUI implements ChangeListener {
    public final static String valueStr= "0.18, -1.11, 0.42, -0.83, 1.42, 0.42, -0.99, 0.87, 0.92, -0.4, -1.48, 1.87, 1.37, -1.48, -0.21, 1.82, 0.15, 0.32, -1.18, -0.43, 0.42, 0.57, 4.72, 12.42, 0.15, 0.15, -1.14, 1.12, -1.88, 0.17, 0.57, 0.47, -1.88, 0.17, -1.93, 0.92, 1.45, 0.17, 1.87, 0.52, 0.67, -1.58, -0.5, 0.17, 0.17, -0.65, 0.96, -0.88, 0.17, -1.53, 0.15, -0.93, 0.42, 2.77, 8.52, 2.47, -2.08, -1.88, -1.88, 1.67, -1.88, 3.72, 2.87, 2.17, 1.37, 1.62, 0.17, 0.62, 0.92, 0.17, 1.52, -1.78, 0.22, 0.92, 0.32, 0.17, 0.57, 0.17, 1.18, 0.17, 0.72, -3.33, -4.13, -1.63, -1.23, 1.62, 0.27, 1.97, -1.72, 1.47, -1.88, 1.72, 1.02, 0.67, 0.67, -1.18, 3.22, -4.83, 8.42, -1.58, -1.88, 1.23, 1.72, 1.12, -0.97, -1.88, -1.88, 1.27, 0.16, 1.22, -0.99, 1.37, 0.18, 0.18, 2.07, 1.47, 4.87, -1.08, 1.27, 0.62, -1.03, 1.82, 0.42, -2.63, -0.73, -1.83, 0.32, 1.62, 1.02, -0.81, -0.74, 1.09, -1.13, 0.52, 0.18, 0.18, 1.47, -1.07, -0.98, 1.07, -0.88, -0.51, 0.57, 2.07, 0.55, 0.42, 1.42, 0.97, 0.62, 0.32, 0.67, 0.77, 0.67, 0.37, 0.87, 1.32, 0.16, 0.18, 0.52, -2.33, 1.07, 1.32, 1.42, 2.72, 1.37, -1.93, 2.12, 0.62, 0.57, 0.42, 1.58, 0.17, 0.62, 0.77, 0.37, -1.33, -1.18, 0.97, 0.7, 1.64, 0.57, 0.24, 0.57, 0.35, 1.57, -1.73, -0.83, -1.18, -0.65, -0.78, -1.28, 0.32, 1.24, 2.05, 0.75, 0.17, 0.67, -0.56, -0.98, 0.17, -0.96, 0.35, 0.52, 0.77, 1.1, -1.88, 0.35, 0.92, 1.55, 1.17, 0.67, 0.82, -0.98, -0.85, 0.22, -1.08, 0.25, 0.14, 0.79, -0.55, 0.32, -1.3, 0.37, -0.51, 0.34, -1.28, 1.8, 2.12, 0.77, -1.33, 1.52, -4.2";
    public double[] values;
    public final static Logger logger = Logger.getLogger("Example");
    private JTextArea textArea;
    private SlotChartPanel chartPanel;
    JSlider binSlider;
    JButton clearButton;
    JButton calculateButton;

    private JSlider logSlider;
    private JSlider scaleSlider;
    private JSlider skrewSlider;
    private JSlider kurtSlider;



    public PerformanceDistributionWindow() {
        chartPanel = new SlotChartPanel();
        chartPanel.setSize(640,480);
                
        textArea=new JTextArea(valueStr, 20, 5);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);

        binSlider = new JSlider(JSlider.HORIZONTAL, 2, 50, 10);
        binSlider.addChangeListener(this);

        logSlider = new JSlider(JSlider.HORIZONTAL, -1000, 1000, 2);
        logSlider.addChangeListener(this);

        scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 276);
        scaleSlider.addChangeListener(this);

        skrewSlider = new JSlider(JSlider.HORIZONTAL, -10, 10, 0);
        skrewSlider.addChangeListener(this);

        kurtSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 178);
        kurtSlider.addChangeListener(this);

        clearButton=new JButton("calculate");
        clearButton.setActionCommand("CLEAR");
        clearButton.addActionListener(this);


        GridBagPane gridBagPanel = new GridBagPane();
        gridBagPanel.add(new JScrollPane(textArea), "gridx=0, gridy=0, gridwidth=2, weightx=1f, weighty=0.2f, fill=BOTH");
        gridBagPanel.add(chartPanel, "nl, weightx=1f, weighty=0.8f, fill=BOTH");
        gridBagPanel.add(binSlider, "nl, gridwidth=1, label=Slots:,  weightx=1f, weighty=0f, fill=BOTH");
        gridBagPanel.add(logSlider, "nl, label=LOG:,  weightx=1f, weighty=0f, fill=BOTH");
        gridBagPanel.add(scaleSlider, "nl, gridwidth=1, label=scale,  weightx=1f, weighty=0f, fill=BOTH");
        gridBagPanel.add(skrewSlider, "nl, gridwidth=1, label=skrew:,  weightx=1f, weighty=0f, fill=BOTH");
        gridBagPanel.add(kurtSlider, "nl, gridwidth=1, label=kurt:,  weightx=1f, weighty=0f, fill=BOTH");

        //doexec();

        setSize(800,600);
        initUI((JComponent) gridBagPanel);
    }

    private static void printOutArr(double[] arr) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(arr[0]);

        for (int i = 1; i < arr.length; i++) {
            stringBuilder.append(" ").append(arr[i]);
        }

        java.lang.System.out.println(stringBuilder.toString());
    }

    private void adjustDistribution() {
        chartPanel.dataPointVector.clear();
        chartPanel.dataPointVector2.clear();

        double avg=Statistics.average(values);
        double stdev = Statistics.stdev(values, avg);
        double normalizedValues[] = Statistics.getNormalizedValues(values, avg, stdev);

        //double[] bin_size = getOptBinSize_scott(normalizedValues, stdev, 3, 3);

        // bined data
         double[] bin_size = getBinSize(normalizedValues, stdev, 3, binSlider.getValue());
        
        double[] bin_x=bin_x=bin_x(stdev, 3, (int) 1000);
        double[] bin_y = bin_y(normalizedValues, stdev, 3, (int) bin_size[0]);

        for (int i = 0; i < bin_y.length; i++) {
            chartPanel.addData(i, (float) bin_y[i]);
        }

        // distribution
        double log = logSlider.getValue() / 100d;
        double scale = scaleSlider.getValue() / 100d;
        double skrew = skrewSlider.getValue() / 100d;
        double kurt = kurtSlider.getValue() / 100d;

        double[] dy = dy(bin_x, log, scale, skrew, kurt);
        double[] dy_adjustedToBin = dy_adjustedToBin(dy, bin_size[1]);

        for (int i = 0; i < dy.length; i++) {
            chartPanel.addData2(i, (float) dy_adjustedToBin[i]);
        }

        chartPanel.drawChart();
        chartPanel.repaint();

        //java.lang.System.out.println("log: " + log + " ,scale: " + scale + " ,skrew: " + skrew + " ,kurt: " + kurt);
        chartPanel.stringVector= findF(avg, stdev, 3, log, scale, skrew, kurt);
    }

    public static void main(String[] args) {
        PerformanceDistributionWindow example = new PerformanceDistributionWindow();
    }

    

    //Y = (1/(ABS((X - LOG) * SCALE) ^ KURT + 1)) ^ C
    public static double dy(double x, double log, double scale, double skew, double kurt) {
        double c = Math.sqrt(1 + Math.pow(Math.abs(skew), Math.abs(1 / x - log)) * Math.signum(x) * -Math.signum(skew));
        double y = Math.pow(1d / (Math.pow(Math.abs((x - log) * scale), kurt) + 1d), c);

        return y;


        

    }

    public static double[] dy(double[] x, double log, double scale, double skew, double kurt) {
        int maxLoops = x.length;
        double[] dy = new double[maxLoops];
        for (int i = 0; i < maxLoops; i++) {
            dy[i] = dy(x[i], log, scale, skew, kurt);
        }
        return dy;
    }

    public static double[] dy_adjustedToBin(double[] dy, double bin_width) {
        int maxLoops = dy.length;
        double[] dy_adjustedToBin = new double[maxLoops];
        for (int i = 0; i < maxLoops; i++) {
            dy_adjustedToBin[i] = dy[i] / bin_width;
        }
        return dy_adjustedToBin;
    }

    public static double y(double[] x, int n, double[] dy) {
        int maxLoops = dy.length;
        double a = 0;
        double b = 0;
        double c = 0;
        double t = 0;

        for (int i = 0; i < maxLoops; i++) {
            t += dy[i];

            if (i < n - 1) {
                b = t;
            } else if (i < n) {
                a = t;
            } else if (i == n) {
                c = t;
            }
        }
        return ((a + b) / 2) / c;
    }

    public static Vector<String> findF(double avg, double stddev, int n_stddev, double log, double scale, double skew, double kurt) {
        Vector<String> stringVector=new Vector<String>();

        int steps = 101;
        double w = 2 * n_stddev * stddev / (double) steps;

        java.lang.System.out.println(avg + ": " + stddev);

        avg=0.33013;
        stddev=1.74323;

        double x[] = new double[steps+1];
        double dy[] = new double[steps+1];
        double sum_dy = 0d;

        double y[] = new double[steps+1];
        double p[] = new double[steps+1];

        for (int i = 0; i <= steps; i++) {
            x[i] = -n_stddev * stddev + i * w;
            
            dy[i] = dy(x[i]/stddev, log, scale, skew, kurt);

            y[i] = sum_dy;

//            dy[i]= .398942 * Math.exp( - (Math.pow(x[i]/stddev,2)/2));
//
//            double Y = 1/(1 + .2316419 * Math.abs(x[i]/stddev));
//            dy[i]=  1 - dy[i] * ((1.330274429 * Math.pow(Y, 5)) - (1.821255978 * Math.pow(Y, 4) ) + (1.781477937 * Math.pow(Y, 3)) - (.356563782 * Math.pow(Y, 2)) + (.31938153 *Y));
//
//            if (x[i]<0) y[i]=1-dy[i]; else y[i]=1-dy[i];

            sum_dy += dy[i];

            y[i] += sum_dy;
            y[i] /= 2;
        }

        for (int i = 0; i <= steps; i++) {
            y[i] /= sum_dy;
        }

        for (int i = 1; i <= steps; i++) {
           p[i] = y[i]-y[i-1];
        }

        double f = 0.0d;

        double optimalF=-1d;
        double max_g=-1d;
        double avgProfit=-1d;

        for (int l = 1; l < 100; l++) {

            f += 0.01d;
            double twr = 1d;
            double sum_y = 0d;

            for (int i = 0; i <= steps; i++) {
                double maxLoss = getProfitFromNormValues(x[0], avg,stddev, 1d, 1d);
                double profit = getProfitFromNormValues(x[i], avg,stddev, 1d, 1d);
                double hpr = getHPR(f, profit, p[i], maxLoss);

                sum_y += p[i];
                twr *= hpr;

                //java.lang.System.out.println(x[i] + "   " + dy[i] + "   " + sum_dy + "   " + p[i]  + "   " +  hpr);
            }

            double g = Math.pow(twr, 1 / sum_y);
            double gat = (g - 1) * (getProfitFromNormValues(x[0], avg, stddev, 1d, 1d) / -f);

            if (g>max_g) {
                max_g=g;
                optimalF=f;
                avgProfit=gat;
            }

            //java.lang.System.out.println(l + ": " + f + "   " + twr + "  " + g + "   " + gat);
        }

        //java.lang.System.out.println("found best f: " + optimalF + "("+avgProfit+")");
        stringVector.add("found best f: " + optimalF + "("+avgProfit+")");

        return stringVector;
    }

    // D = (U * Shrink) + (S * E * Stretch)
    public static double getProfitFromNormValues(double x, double avg, double stddev, double shrink, double stretch) {
        return (avg * shrink) + (x * stretch);
    }

    public static double getHPR(double f, double profit, double y, double maxLoss) {
        return Math.pow(1 + (profit/ (maxLoss/-f)), y);
    }

    public static double[] bin_x(double stddev, int n_stddev, int bins) {
        double[] bin_x = new double[bins];
        double w = -stddev * n_stddev;
        double bin_width = stddev * 2 * n_stddev / bins;
        int k = 0;
        int i = 0;

        while (k < bins) {
            w += bin_width;

            bin_x[k] = w - bin_width / 2;
            k++;
        }

        return bin_x;
    }

    public static double[] bin_y(double[] normalizedValues, double stddev, int n_stddev, int bins) {
        double[] bin_y = new double[bins];
        double w = -stddev * n_stddev;
        double bin_width = stddev * 2 * n_stddev / bins;



        int k = 0;
        int i = 0;

        while (i < normalizedValues.length && k < bins) {
            w += bin_width;
            bin_y[k] = 0;
            while (i < normalizedValues.length && normalizedValues[i] <= w) {
                bin_y[k]++;
                i++;
            }
            bin_y[k] /= normalizedValues.length;
            //bin_y[k] /= bin_width;
            //bin_y[k] *= stddev*n_stddev*2;
            k++;
        }

        return bin_y;
    }

    public static double[] getOptBinSize_scott(double[] normalizedValues, double stddev, int n_stddev, int bins) {
        double[] result = new double[2];
        double bin_count = Math.round(2 * n_stddev / (3.49 * Math.pow(normalizedValues.length, (-0.3333333333333d))));
        double bin_width = stddev * 2 * n_stddev / bin_count;

        result[0] = bin_count;
        result[1] = bin_width;
        return result;
    }

    public static double[] getBinSize(double[] normalizedValues, double stddev, int n_stddev, int bins) {
        double[] result = new double[2];
        double bin_count = bins;
        double bin_width = stddev * 2 * n_stddev / bin_count;

        result[0] = bin_count;
        result[1] = bin_width;
        return result;
    }

    public void stateChanged(ChangeEvent e) {
        adjustDistribution();
    }

    @Override
    public void initMenuBar() {
        String MenuStr[] = {"Data", "Help"};
        String MenuItemStr[][] = {
            {"delete", "exec", "exit"}, {"index", "search", "--", "about"}
        };

        menuBar = new MenuBar(this, MenuStr, MenuItemStr);
    }

    @Override
    public void initToolBar() {
        String ButtonStr[] = {"exit", "delete", "exec"};
        toolBar = new ToolBar(ButtonStr);
    }

    public void doexit() {
        dispose();
    }

    public void dodelete() {
        textArea.setText("");
        values=new double[0];
    }

    public void doexec() {
        String[] arrStr=textArea.getText().split(",");
        values=new double[arrStr.length];
        for (int i=0; i<arrStr.length; i++) {
            values[i]=Double.valueOf(arrStr[i].trim());
        }

        binSlider.setValue(5);
        logSlider.setValue(2);
        scaleSlider.setValue(276);
        skrewSlider.setValue(0);
        kurtSlider.setValue(178);

        adjustDistribution();
    }

    public void setValues(String valueStr) {
        textArea.setText(valueStr);
        doexec();
    }
}
