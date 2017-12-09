package it.dao;

import java.sql.SQLException;

import it.utility.DatabaseTriple;
import it.utility.DatabaseUtility;

public class DAOUsers {

	private static DatabaseUtility db = DatabaseUtility.getInstance();
	public static void store (String username, String hash)
	{
		String query1 = "INSERT INTO USERS ";
		String query2 = "(USERNAME,PASSWORD) VALUES ";
		String query3 = "(?,?);";
		String query = query1+query2+query3;
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		try {
			triple.setPreparedStatement(triple.getConn().prepareStatement(query));
			triple.getPreparedStatement().setString(1, username);
			triple.getPreparedStatement().setString(2, hash);
			triple.getPreparedStatement().executeUpdate();
			triple.closeAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String load_hash(String username)
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
		}
		
	
		
		catch (SQLException e)
		{
		e.printStackTrace();	
		}
	
				
		return hash;
	}
}
