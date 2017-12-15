package it.chat.gui.utility;

import javax.swing.UIManager;

public class LookAndFeelUtility {
	
	public static final String GRAPHITE = "com.jtattoo.plaf.graphite.GraphiteLookAndFeel";
	
	
	
	public static void setLookAndFeel(String laf) {
		
		try {
			
			UIManager.setLookAndFeel(laf);
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}

}
