package it.certificates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CertificateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletConfig config;   
   
    public CertificateServlet() {
        super();
        
    }

    public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}
    

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String key = "";
		String value = "";
		
		
		Enumeration<String> e = request.getParameterNames();
		while(e.hasMoreElements()) {
			key = (String) e.nextElement();
			value = request.getParameter(key);
		}
		
		
		if(value.length() > 0 && key.length() > 0) {
			if(key.toLowerCase().equals("id") && (Integer.valueOf(value).equals(1) || Integer.valueOf(value).equals(2)) ) {
				
				int id = Integer.valueOf(value);
				File cert = null;
								
				if(id == 1) {
					String pathSend = config.getServletContext().getRealPath("/WEB-INF/certificates/ClientSendCertificate.cer");
					cert = new File(pathSend);
				}else {
					String pathRec = config.getServletContext().getRealPath("/WEB-INF/certificates/ClientReceiverCertificate.cer");
					cert = new File(pathRec);
				}
				
				response.setContentType("application/octet-stream");
				response.setContentLength((int) cert.length());
				response.setHeader( "Content-Disposition",
				         String.format("attachment; filename=\"%s\"", cert.getName()));
				
				OutputStream outs = response.getOutputStream();
				
				try (FileInputStream in = new FileInputStream(cert)) {
				    byte[] buffer = new byte[4096];
				    int length;
				    while ((length = in.read(buffer)) > 0) {
				        outs.write(buffer, 0, length);
				    }
				}
				outs.flush();
				
				
			}
		}
		
		
		
	
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
