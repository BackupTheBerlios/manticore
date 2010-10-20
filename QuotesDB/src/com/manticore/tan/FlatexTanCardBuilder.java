/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.tan;

import au.com.bytecode.opencsv.CSVReader;
import com.manticore.database.Quotes;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author are
 */
public class FlatexTanCardBuilder {
    public final static String[] colNameArr={"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    public final static String[] rowNameArr={"A", "B", "C", "D", "E", "F", "G", "H", "K", "L", "M"};
    public final static long MAX_LEN=512000L;

//    public static void main(String[] args) {
//        String[][] tanArr=getTanArray();
//        buildTanTable(1, tanArr);
//    }

    private static String[][] getTanArray() {
        String[][] tanArr=null;
        try {
            FileReader fileReader = new FileReader(new File("/home/are/tan.csv"));
            CSVReader reader = new CSVReader(fileReader, ',', '\"', 0);

            tanArr=new String[rowNameArr.length][colNameArr.length];
            String[] nextLine;
            int r=0;
            while ((nextLine = reader.readNext()) != null) {
                    for (int c=0; c<colNameArr.length; c++){
                        tanArr[r][c]=nextLine[c];
                    }
                    r++;
            }
        } catch (IOException ex) {
            Logger.getLogger(FlatexTanCardBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tanArr;
    }

    private static void buildTanTable(long id_tan_card, String[][] tanArr) {
        StringBuffer sqlStr=new StringBuffer();

        for (int c1=0; c1<colNameArr.length; c1++) {
            for (int r1=0; r1<rowNameArr.length; r1++) {

                for (int c2=0; c2<colNameArr.length; c2++) {
                    for (int r2=0; r2<rowNameArr.length; r2++) {

                        for (int c3=0; c3<colNameArr.length; c3++) {
                            for (int r3=0; r3<rowNameArr.length;r3++) {

                                if ((r1!=r2 || c1!=c2) && (r1!=r3 || c1!=c3) && (r2!=r3 || c2!=c3)) {
                                String key=new StringBuffer()
                                    .append(rowNameArr[r1])
                                    .append(colNameArr[c1])
                                    .append(rowNameArr[r2])
                                    .append(colNameArr[c2])
                                    .append(rowNameArr[r3])
                                    .append(colNameArr[c3])
                                    .toString();

                                String value=new StringBuffer()
                                    .append(tanArr[r1][c1])
                                    .append(tanArr[r2][c2])
                                    .append(tanArr[r3][c3])
                                    .toString();

                                sqlStr
                                    .append("(")
                                    .append(String.valueOf(id_tan_card)).append(", '")
                                    .append(key).append("', '")
                                    .append(value).append("', ")
                                    .append("true").append("), ");

                                if (sqlStr.length()>MAX_LEN) {
                                    executeUpdate(sqlStr);
                                    sqlStr=new StringBuffer();
                                }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (sqlStr.length() > 0) {
            executeUpdate(sqlStr);
            sqlStr = new StringBuffer();
        }
    }

    private static void executeUpdate(StringBuffer stringBuffer) {
        StringBuffer sqlStr=new StringBuffer("INSERT INTO trader.tan (id_tan_card, key, value, valid) VALUES ");
        sqlStr.append(stringBuffer);

        sqlStr.setLength(sqlStr.length()-2);
        sqlStr.append(";");

        int r=Quotes.executeUpdate(sqlStr.toString());
        Logger.getAnonymousLogger().info("wrote " + r + " tan(s) to database.");
    }

}
