/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manticore.swingui;

import com.manticore.swingui.FormatedTextField;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.InputVerifier;
import javax.swing.JComponent;

/**
 *
 * @author are
 */
public class TextFieldInputVerifier extends InputVerifier {
    private static InputVerifier instance = null;

    public static InputVerifier getInstance() {
        if (instance == null) {
            instance = new TextFieldInputVerifier();
        }
        return instance;
    }

    @Override
    public boolean verify(JComponent input) {
        FormatedTextField formatedTextField = (FormatedTextField) input;
        int format = formatedTextField.getFormat();

        boolean result = false;
        try {
            if (format == FormatedTextField.INTEGER_FORMAT) {
                DecimalFormat.getIntegerInstance().parse(formatedTextField.getText());
            } else if (format == FormatedTextField.PERCENT_FORMAT) {
                DecimalFormat.getPercentInstance().parse(formatedTextField.getText());
            } else if (format == FormatedTextField.DECIMAL_FORMAT) {
                DecimalFormat.getInstance().parse(formatedTextField.getText());
            }
            result = true;
        } catch (ParseException ex) {
            Logger.getLogger(formatedTextField.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
};