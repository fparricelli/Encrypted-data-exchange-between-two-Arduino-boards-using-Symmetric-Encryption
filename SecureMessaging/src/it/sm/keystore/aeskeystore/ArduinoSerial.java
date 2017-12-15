package it.sm.keystore.aeskeystore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
	
	public class ArduinoSerial implements SerialPortEventListener {
		SerialPort serialPort;
		
		private int client_type;
		
	        /** The port we're normally going to use. */
		private static final String PORT_NAMES_2[] = { 
				"/dev/cu.usbmodem14321","/dev/cu.usbmodem14121",
		};
		
		private static final String PORT_NAMES_1[] = { 
				"/dev/cu.usbmodem14311","/dev/cu.usbmodem14111",
		};
		
		
		/**
		* A BufferedReader which will be fed by a InputStreamReader 
		* converting the bytes into characters 
		* making the displayed results codepage independent
		*/
		private BufferedInputStream input2;
		private BufferedReader input;
		/** The output stream to the port */
		private OutputStream output;
		/** Milliseconds to block while waiting for port open */
		private static final int TIME_OUT = 3000;
		/** Default bits per second for COM port. */
		private static final int DATA_RATE = 9600;
		
		public ArduinoSerial(int client_type) {
			this.client_type = client_type;
		}

		public void initialize() throws PortInUseException, UnsupportedCommOperationException, IOException {

			CommPortIdentifier portId = null;
			@SuppressWarnings("rawtypes")
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

			//First, Find an instance of serial port as set in PORT_NAMES_1 or 2 based on client_type.
			switch(client_type) {
			case 1: while (portEnum.hasMoreElements()) {
						CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
						for (String portName : PORT_NAMES_1) {
							if (currPortId.getName().equals(portName)) {
								portId = currPortId;
								break;
							}
						}
					}
					break;
			case 2: while (portEnum.hasMoreElements()) {
					CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
					for (String portName : PORT_NAMES_2) {
						if (currPortId.getName().equals(portName)) {
							portId = currPortId;
							break;
							}
						}
					}
					break;
			default: throw new PortInUseException();
					
			}
			
			if (portId == null) {
				System.out.println("[Arduino] Could not find COM port.");
				return;
			}
			
				// open serial port, and use class name for the appName.

				serialPort = (SerialPort) portId.open(this.getClass().getName(),
						TIME_OUT);

				// set port parameters
				serialPort.setSerialPortParams(DATA_RATE,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				// open the streams
				input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
				input2 = new BufferedInputStream(serialPort.getInputStream());
				output = serialPort.getOutputStream();
				serialPort.notifyOnDataAvailable(true);

		}
		
		public synchronized String readData() {
			try {
				return input.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public synchronized byte[] readBytes() {
			try {
			while(input2.available() == 0);
			int n = input2.read();
			byte[] bytes = new byte[n]; 
			int i = 0;
			
				while(input2.available()==0);
				while(input2.available()>0) 
					bytes[i++]=(byte) input2.read();
				return bytes;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public synchronized boolean available() {
				
				try {
					return input.ready();
				} catch (IOException e) {
					e.printStackTrace();
				}
			return false;
			}
		
		public synchronized void close() {
			if (serialPort != null) {
				serialPort.removeEventListener();
				serialPort.close();
			}
		}

		public synchronized void writeBytes(byte[] bytes) {
			System.out.println("Sent: " + bytes);
			try {
				
				output.write(bytes);
				
				} catch (Exception e) {
				e.printStackTrace();
				System.out.println("could not write to port");
				
				}
		}
	
		
		public synchronized void writeData(String data) {
			
			System.out.println("[To Arduino] Sent: " + data);
			
			try {
			
			output.write(data.getBytes());
			
			} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[To Arduino] Could not write to port");
			
			}
			
			}

		public synchronized void serialEvent(SerialPortEvent oEvent) {
			if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
			String inputLine=input.readLine();
			System.out.println(inputLine);
			} catch (Exception e) {
			System.err.println(e.toString());
			}
			}
			}
	}


