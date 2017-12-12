package it.authorization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.FilePolicyModule;




public class ContactListFilter implements Filter {

   
    public ContactListFilter() {
        // TODO Auto-generated constructor stub
    }

	
	public void destroy() {
		// TODO Auto-generated method stub
	}

	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		//necessario controllo: se non hai inserito le credenziali (ovvero: non sei stato autenticato)
		//ti rimando da qualche altra parte
		
		String list = ((HttpServletRequest)request).getParameter("list");
		String ruolo = ((HttpServletRequest)request).getParameter("ruolo");

		System.out.println("[ContactListFilter] Ho trovato i seguenti parametri:");
		System.out.println("List:"+list);
		System.out.println("Ruolo:"+ruolo);
		
		File f;
        String policyfile;
        FilePolicyModule policyModule = new FilePolicyModule();
        PolicyFinder policyFinder = new PolicyFinder();
        Set policyModules = new HashSet();
        
        String PATH_POLICY = ((HttpServletRequest)request).getServletContext().getRealPath("/policy");
        File [] listaFile = (new File(PATH_POLICY)).listFiles();
        
        for(int i=0;i<listaFile.length;i++)
        {
             
             f=listaFile[i];
             policyfile = f.getAbsolutePath();
             policyModule.addPolicy(policyfile); 
             policyModules.add(policyModule);
             policyFinder.setModules(policyModules);
        }

        CurrentEnvModule envModule = new CurrentEnvModule();
        AttributeFinder attrFinder = new AttributeFinder();
        List attrModules = new ArrayList();
        attrModules.add(envModule);
        attrFinder.setModules(attrModules);
		
        try {
        	
        
        RequestCtx XACMLrequest = RequestBuilder.createXACMLRequest((HttpServletRequest)request);
  	  
    	
        PDP pdp = new PDP(new PDPConfig(attrFinder, policyFinder, null));

        ResponseCtx XACMLresponse = pdp.evaluate(XACMLrequest);
        
        Set ris_set = XACMLresponse.getResults();
        Result ris = null;
        Iterator it = ris_set.iterator();

        while (it.hasNext()) {
            ris = (Result) it.next();
        }
        int dec = ris.getDecision();

        if (dec == 0) {//permit
        	System.out.println("PERMIT");
        	sendRequestedList(request,response);
        	
        } else if (dec == 1) {//deny
        	System.out.println("DENY");
        	PrintWriter out = response.getWriter();
        	((HttpServletResponse)response).setContentType("text/plain");
        	out.println("Accesso negato!");
        	out.close();
        	
        } else if (dec == 2||dec==3) {//not applicable o indeterminate
        	
        	System.out.println("NOT APPLICABLE");
        	PrintWriter out = response.getWriter();
        	((HttpServletResponse)response).setContentType("text/plain");
        	out.println("Errore di policy!");
        	out.close();
        }
	
        }catch(Exception e) {
        	e.printStackTrace();
        }
        
		
	
	}

	
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
	
	
	private void sendRequestedList(ServletRequest request, ServletResponse response) {
		
	
		try {
		ServletContext context = ((HttpServletRequest)request).getServletContext();
		String list = ((HttpServletRequest)request).getParameter("list");
		System.out.println("SendRequestedList, parametro:"+list);
		
		File reqList = null;
		
		if(list.equals("admins")) {
			System.out.println("Prendo lista admin");
			String path = context.getRealPath("/contact-lists/admins/admin-list.xml");
			reqList = new File(path);
			
		}else if(list.equals("utenti")){
			System.out.println("Prendo lista users");
			String path = context.getRealPath("/contact-lists/utenti/utenti-list.xml");
			reqList = new File(path);
		
		}else {
			
			System.out.println("Prendo lista tecnici");
			String path = context.getRealPath("/contact-lists/tecnici/tecnici-list.xml");
			reqList = new File(path);
		}
		
		response.setContentType("application/octet-stream");
		response.setContentLength((int) reqList.length());
		((HttpServletResponse)response).setHeader( "Content-Disposition",
		         String.format("attachment; filename=\"%s\"", reqList.getName()));
		
		OutputStream outs = response.getOutputStream();
		
		try (FileInputStream in = new FileInputStream(reqList)) {
		    byte[] buffer = new byte[4096];
		    int length;
		    while ((length = in.read(buffer)) > 0) {
		        outs.write(buffer, 0, length);
		    }
		}
		outs.flush();
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
