/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.report;

/**
 *
 * @author are
 */
public class Statistics {
    public static double average(double[] values) {
        double average = 0d;
        int maxLoops = values.length;
        for (int i = 0; i < maxLoops; i++) {
            average += values[i];
        }
        average /= maxLoops;

        return average;
    }

    public static double average(int[] values) {
        double average = 0d;
        int maxLoops = values.length;
        for (int i = 0; i < maxLoops; i++) {
            average += (double) values[i];
        }
        average /= (double) maxLoops;

        return average;
    }

    public static double variance(double[] values, double average) {
        double variance = 0d;
        int maxLoops = values.length;
        for (int i = 0; i < maxLoops; i++) {
            variance += Math.pow(values[i] - average, 2);
        }
        variance = variance / maxLoops;
        return variance;
    }

    public static double stdev(double[] values, double average) {
        double stdev = 0d;
        int maxLoops = values.length;
        for (int i = 0; i < maxLoops; i++) {
            stdev += Math.pow(values[i] - average, 2);
        }
        stdev = Math.sqrt(stdev / maxLoops);
        return stdev;
    }

    public static double stdev(double[] values) {
        double stdev = 0d;
        double average=average(values);
        int maxLoops = values.length;
        for (int i = 0; i < maxLoops; i++) {
            stdev += Math.pow(values[i] - average, 2);
        }
        stdev = Math.sqrt(stdev / maxLoops);
        return stdev;
    }

    public static double[] getNormalizedValues(double[] values) {
        int maxLoops = values.length;
        double[] normalizedValues = new double[maxLoops];
        double average = average(values);
        double stdev = stdev(values, average);

        for (int i = 0; i < maxLoops; i++) {
            normalizedValues[i] = (values[i] - average) / stdev;
        }
        java.util.Arrays.sort(normalizedValues);
        return normalizedValues;
    }

    public static double[] getNormalizedValues(double[] values, double average, double stdev) {
        int maxLoops = values.length;
        double[] normalizedValues = new double[maxLoops];

        for (int i = 0; i < maxLoops; i++) {
            normalizedValues[i] = (values[i] - average) / stdev;
        }
        java.util.Arrays.sort(normalizedValues);
        return normalizedValues;
    }

    public static double[] getIntervals(float min, float max, float w) {
        int maxLoops = Math.round((max-min)/w);
        double[] intervals=new double[maxLoops];

        for (int i = 0; i < maxLoops; i++) {
            intervals[i]=min+w*i;
        }
        return intervals;
    }

    public static double product(double[] values) {
        int maxLoops = values.length;
        double result=1d;

        for (int i = 0; i < maxLoops; i++) {
            result *= values[i];
        }
        return result;
    }

    public static double geoMean(double[] values) {
        int maxLoops = values.length;
        double result=1d;

        for (int i = 0; i < maxLoops; i++) {
            result *= values[i];
        }
        result=Math.pow(result, (double) 1 / (double) maxLoops);

        return result;
    }
}
