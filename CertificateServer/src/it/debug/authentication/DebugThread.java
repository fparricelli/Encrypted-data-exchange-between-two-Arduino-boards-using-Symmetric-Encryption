package it.debug.authentication;

public class DebugThread implements Runnable{

	@Override
	public void run() {
		try {
			DebugAuthenticationServlet.createIPLockdownFromFailedLogins();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
