package it.sm.exception;

public class MaxDelayException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MaxDelayException() {
		printInfo();
		
	}
	
	public void printInfo(){
		
		System.out.println("Potential Replay Attack Running...");
	}

}
