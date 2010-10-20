/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author are
 */
public class RegexTest {
    public static void main(String[] args) {
        Pattern p=Pattern.compile("([\\d]+)",Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
        //String s="09.06.201016:30:5715304765";

        String s="/chart/push.m?secu=103096708";

        String result = "";
        if (s.length() > 0) {
            Matcher m = p.matcher(s);
            while (m.find()) {
                    result+=" " + m.group();
            }
        }

        System.out.println(result.trim());
        
    }
}
