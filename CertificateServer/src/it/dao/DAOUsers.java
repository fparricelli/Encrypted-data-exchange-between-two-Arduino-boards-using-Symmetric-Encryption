package it.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import it.entity.User;
import it.exception.authentication.NoSuchUserException;
import it.utility.database.DatabaseTriple;
import it.utility.database.DatabaseUtility;

public class DAOUsers {

	private static DatabaseUtility db = DatabaseUtility.getInstance();
	public static Vector<User> utenti = getAll();

	public static Vector<User> getAll() {
		Vector<User> vettore = new Vector<User>();

		try {
			ResultSet res;
			String query = "SELECT * FROM USERS";
			DatabaseTriple triple = new DatabaseTriple(db.connect());
			triple.setPreparedStatement(triple.getConn().prepareStatement(query));
			triple.setResultSet(triple.getPreparedStatement().executeQuery());
			triple.setPreparedStatement(triple.getConn().prepareStatement(query));
			while (triple.getResultSet().next()) {
				res = triple.getResultSet();
				String username = res.getString(1);
				String name = res.getString(3);
				String surname = res.getString(4);
				String password = res.getString(2);
				String email = res.getString(5);
				Integer telephone = res.getInt(6);
				String role = res.getString(7);
				vettore.add(new User(username, name, surname, password, email, role, telephone));
			}
			triple.closeAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vettore;
	}

	public static boolean usernameAlreadyTaken(String u) throws SQLException {
	if(getUser(u)!=null)
	{
		return true;
	}
	
	return false;
	}

	public static void store(String username, String hash) throws SQLException {
		String query1 = "INSERT INTO USERS ";
		String query2 = "(USERNAME,PASSWORD) VALUES ";
		String query3 = "(?,?);";
		String query = query1 + query2 + query3;
		DatabaseTriple triple = new DatabaseTriple(db.connect());

		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, hash);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
		utenti.add(new User(username, null, null, hash, null, null, null));

	}

	public static String load_hash(String username) throws NoSuchUserException, SQLException {
		for (int i = 0; i < utenti.size(); i++) {
			if (utenti.get(i).getUsername().equals(username)) {
				return utenti.get(i).getPasword();
			}
		}
		throw new NoSuchUserException();

	}

	public static User getUser(String username) {
		for (int i = 0; i < utenti.size(); i++) {
			if (utenti.get(i).getUsername().equals(username)) {
				return utenti.get(i);
			}
		}

		return null;
	}

	public static String getUserMail(String username) throws SQLException {
		return getUser(username).getEmail();
	}

	public static HashMap<String, String> getUserDetails(String username) throws SQLException {
		HashMap<String, String> map = new HashMap<String, String>();
		User u = getUser(username);
		map.put("name", u.getName());
		map.put("surname", u.getSurname());
		map.put("telephone", u.getTelephone().toString());
		map.put("role", u.getRole());
		return map;
	}
}
