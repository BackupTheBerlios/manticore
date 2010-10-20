package com.manticore.swingui;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import java.lang.reflect.Method;

/**
 *  Description of the Class
 *
 *@author     are
 *@created    12. Juni 2004
 */
public class SwingUI extends JFrame implements WindowListener, ActionListener {
    public MenuBar menuBar;
    public ToolBar toolBar;
    
    public void initUI(JComponent c) {
	GlobalProperties.getInstance().setActionListener(this);
	
	initMenuBar();
	initToolBar();
	
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(toolBar, BorderLayout.WEST);
	getContentPane().add(GlobalProperties.getInstance().getStatusBar(), BorderLayout.SOUTH);
	getContentPane().add(c, BorderLayout.CENTER);
	addWindowListener(this);
	setJMenuBar(menuBar);
	setVisible(true);
        validate();

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation( (tk.getScreenSize().width-getWidth()) / 2, (tk.getScreenSize().height-getHeight()) / 2);
    }

    public void initUI(JComponent c, JComponent southComponent) {
	GlobalProperties.getInstance().setActionListener(this);

	initMenuBar();
	initToolBar();

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(toolBar, BorderLayout.WEST);
	getContentPane().add(southComponent, BorderLayout.SOUTH);
	getContentPane().add(c, BorderLayout.CENTER);
	addWindowListener(this);
	setJMenuBar(menuBar);
	setVisible(true);
        validate();

        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation( (tk.getScreenSize().width-getWidth()) / 2, (tk.getScreenSize().height-getHeight()) / 2);
    }
    
    public void initMenuBar() {
	menuBar=new MenuBar(this);
    }
    
    public void initToolBar() {
	toolBar=new ToolBar();
    }
    
    public void exit() {
      setVisible(false);
    }
    
    // implements Windowlistener
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowActivated(WindowEvent e) {
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowClosed(WindowEvent e) {
      exit();
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowClosing(WindowEvent e) {
      exit();
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowDeactivated(WindowEvent e) {
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowDeiconified(WindowEvent e) {
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowIconified(WindowEvent e) {
    }
    
    
    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void windowOpened(WindowEvent e) {
    }
    
    
    // implements ActionListener
    public void actionPerformed(ActionEvent e) {
        try {
            String ActionCommand = e.getActionCommand();
            Method method=this.getClass().getMethod("do" + ActionCommand);
            method.invoke((Object) this);
            
            GlobalProperties.getInstance().showStatusMsg(ActionCommand);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(SwingUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(SwingUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(SwingUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(SwingUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SwingUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


