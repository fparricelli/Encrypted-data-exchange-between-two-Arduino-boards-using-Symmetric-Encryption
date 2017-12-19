package it.sm.exception;

public class TelephoneTipingException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public TelephoneTipingException() {
		printInfo();	
		};
		
		
		private void printInfo() {
			System.out.println("You've inserted not only digits.");
		}

}
