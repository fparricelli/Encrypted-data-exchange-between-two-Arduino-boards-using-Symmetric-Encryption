package it.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import it.utility.database.DatabaseTriple;
import it.utility.database.DatabaseUtility;

public class DAOLogins {
	private static DatabaseUtility db = DatabaseUtility.getInstance();
	private static final Integer LOCKOUT_DURATION = 15;
	private static final Integer MILLISECONDS_TO_MINUTES = 60*1000;
	
	public static boolean isLockedout (String username, String ip) throws SQLException
	{
		boolean locked = false;
		Timestamp startinglockout;
		SimpleDateFormat format =  new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long timestamp;
		System.out.println(ip);
		String query = "SELECT * FROM ACCOUNT_LOCKDOWN WHERE LOCKDOWN_USERNAME = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		if(triple.getResultSet().next())
		{
			startinglockout = triple.getResultSet().getTimestamp(3);
			timestamp = startinglockout.getTime();
			if(System.currentTimeMillis() > timestamp + LOCKOUT_DURATION * MILLISECONDS_TO_MINUTES)
			{
				deleteLockout(username, ip);
			}
			else
			{
				locked = true;
			}
			
			
		}
		triple.closeAll();
		return locked;
	}
	
	
	public static void deleteLockout (String username, String ip) throws SQLException
	{
		String query = "DELETE FROM ACCOUNT_LOCKDOWN WHERE LOCKDOWN_USERNAME = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
		
	}

}
