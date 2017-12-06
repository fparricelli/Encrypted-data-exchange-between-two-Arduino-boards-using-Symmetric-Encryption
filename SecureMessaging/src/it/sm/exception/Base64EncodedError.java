package it.sm.exception;

public class Base64EncodedError extends Exception {

	private static final long serialVersionUID = -2754285498174868995L;

	public Base64EncodedError() {
		printMessage();
	}
	
	private void printMessage() {
		System.out.println("Wrong Encoded String!");
	}
}
