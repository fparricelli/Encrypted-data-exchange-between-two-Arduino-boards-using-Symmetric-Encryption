package it.authorization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.owasp.esapi.errors.AccessControlException;
import org.owasp.esapi.reference.IntegerAccessReferenceMap;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;
import com.sun.xacml.finder.impl.FilePolicyModule;

import it.authentication.AuthenticationLogic;
import it.exception.authentication.InvalidHopException;
import it.utility.network.HTTPCodesClass;
import it.utility.network.HTTPCommonMethods;




public class ContactListFilter implements Filter {

	private IntegerAccessReferenceMap listMap;
	private IntegerAccessReferenceMap roleMap;
	private static Logger log;
   
    public ContactListFilter() {
    	
    }

	
	public void destroy() {
		// TODO Auto-generated method stub
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		//Sessioni vengono invalidate al chiudersi della risposta
		String token = ((HttpServletRequest)request).getParameter("token");
		System.out.println("[contactListServlet] Token:"+token);
		String list,ruolo = "";
		String newToken = "";
	
		try {
		
		newToken = AuthenticationLogic.regenToken(token,request.getRemoteAddr());
		
		
		if(newToken == null) {
			System.out.println("[ContactListFilter] Token scaduto!");
			HTTPCommonMethods.sendReplyHeaderOnly(((HttpServletResponse)response), HTTPCodesClass.TEMPORARY_REDIRECT);
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			
			log.warn("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" - expired token");
		
		}else{
			
			System.out.println("[ContactListFilter] Token valido!");
			System.out.println("[ContactListFilter] new Token:"+newToken);
			String listp = ((HttpServletRequest)request).getParameter("list");
			String ruolop = ((HttpServletRequest)request).getParameter("ruolo");
			
			
			
			
			String listIntm = listMap.getDirectReference(listp);
			String ruoloIntm = roleMap.getDirectReference(ruolop);
			
			
			
			list = convertList(listIntm);
			ruolo = convertRole(ruoloIntm);
			
			((HttpServletRequest)request).getSession().setAttribute("lista", list);
			((HttpServletRequest)request).getSession().setAttribute("ruolo", ruolo);
			
		
		
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
				sendRequestedList(request,response,newToken);
        	
			} else if (dec == 1) {//deny
				System.out.println("DENY");
				HTTPCommonMethods.sendReplyHeaderWithToken(((HttpServletResponse)response), HTTPCodesClass.UNAUTHORIZED,newToken);
				((HttpServletRequest)request).getSession().invalidate();
				
				String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
				log.warn("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" - access denied for requested list="+list+"");
				//waitLog();
				
			} else if (dec == 2||dec==3) {//not applicable o indeterminate
        	
				System.out.println("NOT APPLICABLE");
				HTTPCommonMethods.sendReplyHeaderWithToken(((HttpServletResponse)response), HTTPCodesClass.CONFLICT,newToken);
				((HttpServletRequest)request).getSession().invalidate();
				
				String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
				log.warn("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" - unapplicable policy for requested list="+list+" and role="+ruolo);
				//waitLog();
			}
		}
    
	
    }catch(InvalidHopException e) {
        	
        System.out.println(e.getMessage());
        
        HTTPCommonMethods.sendReplyHeaderOnly(((HttpServletResponse)response), HTTPCodesClass.TEMPORARY_REDIRECT);
        ((HttpServletRequest)request).getSession().invalidate();
        
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        
		log.error("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" token error (invalid hop) for supplied token");
		
        
    }catch(IOException ex) {
        ex.printStackTrace();
        //In caso di IOException, non posso mandare risposta su output stream
        
       String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
       //Se inviamo lo stack trace come log, è necessario fare le opportune considerazioni di sicurezza riguardo
       //..tale scelta.
       log.error("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" IOException occured");
        
        ((HttpServletRequest)request).getSession().invalidate();
        
    }catch(AccessControlException ac) {
    	ac.printStackTrace();
    	HTTPCommonMethods.sendReplyHeaderOnly(((HttpServletResponse)response), HTTPCodesClass.UNAUTHORIZED);
	    ((HttpServletRequest)request).getSession().invalidate();
    	
    	String listp = ((HttpServletRequest)request).getParameter("list");
		String ruolop = ((HttpServletRequest)request).getParameter("ruolo");
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        log.error("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" no parameter mapping for supplied role:"+listp+"and list:"+ruolop);
        
        
    }catch(Exception exx) {
	   exx.printStackTrace();
	   HTTPCommonMethods.sendReplyHeaderOnly(((HttpServletResponse)response), HTTPCodesClass.UNAUTHORIZED);
       ((HttpServletRequest)request).getSession().invalidate();
	   
	   String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	   //Anche qui, vanno fatte considerazioni sul fatto di allegare o meno lo stacktrace nel log.
       log.error("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" Exception:"+exx.getMessage()+" occured");
      
       
       
   }
        
		
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init(FilterConfig fConfig) throws ServletException {
		Set listSet = new HashSet();
		listSet.add("listTecnici");
		listSet.add("listAdmins");
		listSet.add("listUtenti");
		listMap = new IntegerAccessReferenceMap(listSet);
		
		
		
		Set roleSet = new HashSet();
		roleSet.add("roleTecnico");
		roleSet.add("roleAdmin");
		roleSet.add("roleUtente");
		roleMap = new IntegerAccessReferenceMap(roleSet);
		
		log = LogManager.getRootLogger();
			
	}
	
	
	private void sendRequestedList(ServletRequest request, ServletResponse response,String newToken) {
		
	
		try {
		ServletContext context = ((HttpServletRequest)request).getServletContext();
		String list = (String)((HttpServletRequest)request).getSession().getAttribute("lista");
		
		
		
		File reqList = null;
		
		if(list.equals("admins")) {
			
			String path = context.getRealPath("/contact-lists/admins/admin-list.xml");
			reqList = new File(path);
			
			
		}else if(list.equals("utenti")){
			
			String path = context.getRealPath("/contact-lists/utenti/utenti-list.xml");
			reqList = new File(path);
		
		}else{
			
			String path = context.getRealPath("/contact-lists/tecnici/tecnici-list.xml");
			reqList = new File(path);
		}
		
		
		response.setContentType("application/octet-stream");
		response.setContentLength((int) reqList.length());
		
		((HttpServletResponse)response).setHeader("Content-Disposition",String.format("attachment; filename=\"%s\"", reqList.getName()));
		
		((HttpServletResponse)response).addHeader("NewToken", newToken);
		
		OutputStream outs = response.getOutputStream();
		
		
		
		try (FileInputStream in = new FileInputStream(reqList)) {
		    byte[] buffer = new byte[4096];
		    int length;
		    while ((length = in.read(buffer)) > 0) {
		        outs.write(buffer, 0, length);
		    }
		}
		
		
		outs.flush();
		
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        log.info("["+timeStamp+"] - Request from "+request.getRemoteAddr()+" - sent requested list");
        //waitLog();
        
		((HttpServletRequest)request).getSession().invalidate();
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	private String convertList(String list) {
		if(list.equals("listAdmins")) {
			return "admins";
		}else if(list.equals("listUtenti")) {
			return "utenti";
		}else {
			return "tecnici";
		}
	}

	private String convertRole(String role) {
		if(role.equals("roleAdmin")) {
			return "admin";
		}else if(role.equals("roleUtente")) {
			return "utente";
		}else {
			return "tecnico";
		}
	}
	
	
	
	
	 

}
