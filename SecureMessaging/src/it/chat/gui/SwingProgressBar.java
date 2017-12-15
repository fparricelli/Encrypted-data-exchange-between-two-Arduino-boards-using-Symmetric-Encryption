package it.chat.gui;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class SwingProgressBar extends JPanel {

	  JProgressBar pbar;

	  static final int MY_MINIMUM = 0;

	  static final int MY_MAXIMUM = 100;

	  public SwingProgressBar() {
	    // initialize Progress Bar
	    pbar = new JProgressBar();
	    pbar.setMinimum(MY_MINIMUM);
	    pbar.setMaximum(MY_MAXIMUM);
	    // add to JPanel
	    add(pbar);
	  }

	  public void updateBar(int newValue) {
	    pbar.setValue(newValue);
	  }
}