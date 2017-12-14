package it.authentication.twofactors;

import java.sql.SQLException;

import it.dao.DAOTrustedIPs;
import it.dao.DAOUsers;
import it.utility.RandomStringGenerator;
import it.utility.mail.MailUtility;

public class TwoFactorsManager {
	public static final Integer codeLength = 10;
	public static final String TWO_FACTORS_SUBJECT = "Confirm your access";
	public static final String TWO_FACTORS_BODY = "Please enter code: ";

	public static void sendMail (String username, String ip) throws Exception
	{
		boolean  onceSent = false;
	String mail = 	DAOUsers.getUserMail(username);
	String randomCode;
	if(!DAOTrustedIPs.validCodeExists(username, ip, onceSent))
	{randomCode = RandomStringGenerator.randomString(codeLength);
		if(!onceSent)
		{
			DAOTrustedIPs.insertCode(username, ip, randomCode);
			MailUtility.sendMail(mail, TWO_FACTORS_SUBJECT, TWO_FACTORS_BODY + randomCode);
		}
		else
		{
			DAOTrustedIPs.updateCode(username, ip, randomCode);
			MailUtility.sendMail(mail, TWO_FACTORS_SUBJECT, TWO_FACTORS_BODY + randomCode);
		}
	}
	}

}
