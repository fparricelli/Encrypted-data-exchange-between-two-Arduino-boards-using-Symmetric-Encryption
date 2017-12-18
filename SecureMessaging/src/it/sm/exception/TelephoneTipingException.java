package it.sm.exception;

public class TelephoneTipingException extends Exception {
	
	public TelephoneTipingException() {
		printInfo();	
		};
		
		
		private void printInfo() {
			System.out.println("You've inserted not only digits.");
		}

}
