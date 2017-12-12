package it.dao;

import java.sql.SQLException;

import it.exception.authentication.NoSuchUserException;
import it.utility.database.DatabaseTriple;
import it.utility.database.DatabaseUtility;

public class DAOUsers {
	
	
	

	private static DatabaseUtility db = DatabaseUtility.getInstance();
	public static boolean usernameAlreadyTaken (String u) throws SQLException
	{
	return db.query("SELECT USERNAME FROM USERS WHERE USERNAME =\"" + u + "\";").getResultSet().next();
	}
	
	
	
	
	public static void store (String username, String hash) throws SQLException
	{
		String query1 = "INSERT INTO USERS ";
		String query2 = "(USERNAME,PASSWORD) VALUES ";
		String query3 = "(?,?);";
		String query = query1+query2+query3;
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		
			triple.setPreparedStatement(triple.getConn().prepareStatement(query));
			triple.getPreparedStatement().setString(1, username);
			triple.getPreparedStatement().setString(2, hash);
			triple.getPreparedStatement().executeUpdate();
			triple.closeAll();
		
		
	}
	
	public static String load_hash(String username) throws NoSuchUserException
	{
		String hash = null;
		String query1= "SELECT PASSWORD FROM USERS ";
		String query2= "WHERE USERNAME=?";
		String query = query1+query2;
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		try {
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));	
		triple.getPreparedStatement().setString(1, username);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		if(triple.getResultSet().next())
		{
			hash =triple.getResultSet().getString(1);
			
		}
		else
		{
			throw new NoSuchUserException();
		}
		}
		
	
		
		catch (SQLException e)
		{
		e.printStackTrace();	
		}
	
				
		return hash;
	}
}
