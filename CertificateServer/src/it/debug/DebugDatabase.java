package it.debug;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.utility.DatabaseTriple;
import it.utility.DatabaseUtility;

public class DebugDatabase {

	public static void main(String[] args) {
		debugResult();
	}

	public static void debugResult() {
		for (int i = 0; i < 100; i++) {

			DatabaseTriple triple = DatabaseUtility.getInstance().query("SELECT * FROM USERS");
			try {
				ResultSet result = triple.getResultSet();
				result.next();
				System.out.println(result.getString(1));
				triple.closeAll();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void debugConnection() {
		for (int i = 0; i < 100; i++) {
			Connection conn = DatabaseUtility.getInstance().connect();
			System.out.println(conn);
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
