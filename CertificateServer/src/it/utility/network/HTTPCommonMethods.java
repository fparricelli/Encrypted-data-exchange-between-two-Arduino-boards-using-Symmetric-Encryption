package it.utility.network;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

public class HTTPCommonMethods {
	public static void sendReplyHeaderOnly(HttpServletResponse resp, Integer http) throws IOException {
		OutputStream out = resp.getOutputStream();
		resp.setStatus(http);
		out.flush();
	}

}
