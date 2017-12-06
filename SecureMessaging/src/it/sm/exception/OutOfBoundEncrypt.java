package it.sm.exception;

public class OutOfBoundEncrypt extends Exception {

	private static final long serialVersionUID = 4138458485763401076L;

	public OutOfBoundEncrypt() {
		this.printMessage();
	}
	
	public void printMessage() {
		System.out.println("Error: Message too Long!");
	}
}
