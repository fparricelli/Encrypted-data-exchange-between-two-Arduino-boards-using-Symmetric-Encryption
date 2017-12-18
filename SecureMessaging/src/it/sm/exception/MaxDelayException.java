package it.sm.exception;

public class MaxDelayException extends Exception {
	
	public MaxDelayException() {
		printInfo();
		
	}
	
	public void printInfo(){
		
		System.out.println("Potential Replay Attack Running...");
	}

}
