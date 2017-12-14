package it.dao;

import java.sql.SQLException;
import java.sql.Timestamp;

import it.utility.database.DatabaseTriple;
import it.utility.database.DatabaseUtility;

public class DAOTrustedIPs {
	private static final Integer CODE_DURATION = 30;
	private static final Integer MINUTES_TO_MILLISECONDS = 60*1000;
	private static DatabaseUtility db = DatabaseUtility.getInstance();
	
	public static boolean isTrusted (String username, String ip) throws SQLException
	{
		boolean isTrusted = false;
		String query = "SELECT COUNT(*) FROM TRUSTED_DEVICES WHERE USERNAME=? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		if(triple.getResultSet().next())
		{
			if(triple.getResultSet().getInt(1)>0)
			{
			isTrusted = true;
			}
		}
		triple.closeAll();
		return isTrusted;
	}
	
	
	public static boolean validCodeExists (String username, String ip, Boolean onceSent) throws SQLException
	{
		Boolean validCode = false;
		Timestamp codeTimestamp;
		String query = "SELECT * FROM MAIL_CODES WHERE USERNAME=? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		if(triple.getResultSet().next())
		{
			onceSent = true;
			codeTimestamp = triple.getResultSet().getTimestamp(3);
			if(System.currentTimeMillis() < codeTimestamp.getTime() + CODE_DURATION * MINUTES_TO_MILLISECONDS)
			{
				validCode = true;
			}
		}
		triple.closeAll();
		return validCode;
	}
	
	public static void updateCode(String username, String ip, String code) throws SQLException
	{
		String query = "UPDATE  MAIL_CODES SET ISSUED=?, VALUE=sha2(?,256) WHERE USERNAME=? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setTimestamp(1, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().setString(2, code);
		triple.getPreparedStatement().setString(3, username);
		triple.getPreparedStatement().setString(4, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}
	
	public static void deleteCode (String username, String ip) throws SQLException
	{
		String query = "DELETE FROM MAIL_CODES WHERE USERNAME=? AND IP=?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}
	public static void insertCode(String username, String ip, String code) throws SQLException
	{
		String query = "INSERT INTO MAIL_CODES VALUES (?,?,?,sha2(?,256))";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().setTimestamp(3, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().setString(4, code);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}
	
}


