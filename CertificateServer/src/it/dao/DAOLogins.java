package it.dao;

import java.sql.SQLException;
import java.sql.Timestamp;

import it.utility.MutableBoolean;
import it.utility.MutableInteger;
import it.utility.database.DatabaseTriple;
import it.utility.database.DatabaseUtility;

public class DAOLogins {
	private static DatabaseUtility db = DatabaseUtility.getInstance();
	private static final Integer LOCKOUT_DURATION = 20;
	private static final Integer MILLISECONDS_TO_MINUTES = 60 * 1000;
	private static final Integer FAILED_ATTEMPTS_DURATION = 15;
	private static final Integer MAXIMUM_FAILED_LOGINS = 5;
	private static final Integer IP_LOCKOUT = 2;
	private static final Integer MILLISECONDS_TO_HOURS = 60 * MILLISECONDS_TO_MINUTES;
	private static final Integer MAXIMUM_ACCOUNTS_ATTEMPTS_FOR_IP = 5;

	public static boolean isLockedOut(String username, String ip) throws SQLException {
		boolean locked = false;
		Timestamp startinglockout;
		long timestamp;
		String query = "SELECT * FROM ACCOUNT_LOCKDOWN WHERE LOCKDOWN_USERNAME = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		if (triple.getResultSet().next()) {
			startinglockout = triple.getResultSet().getTimestamp(3);
			timestamp = startinglockout.getTime();
			if (System.currentTimeMillis() > timestamp + LOCKOUT_DURATION * MILLISECONDS_TO_MINUTES) {
				deleteLockout(username, ip);
			} else {
				locked = true;
			}

		}
		triple.closeAll();
		return locked;
	}

	public static void deleteLockout(String username, String ip) throws SQLException {
		String query = "DELETE FROM ACCOUNT_LOCKDOWN WHERE LOCKDOWN_USERNAME = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();

	}

	public static void insertLockout(String username, String ip) throws SQLException {
		String query = "INSERT INTO ACCOUNT_LOCKDOWN VALUES (?,?,?)";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().setTimestamp(3, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void handleFailedLogin(String username, String ip, MutableBoolean needsUpdate, MutableBoolean locktimeout, MutableInteger attempts) throws SQLException {
		String query = "SELECT * FROM FAILED_LOGINS WHERE USERNAME_FAILED = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		Integer failedAttempts = 0;
		Timestamp loginTimestamp;
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		
		if (triple.getResultSet().next()) {
			loginTimestamp = triple.getResultSet().getTimestamp(4);
			if (System.currentTimeMillis() > loginTimestamp.getTime()
					+ FAILED_ATTEMPTS_DURATION * MILLISECONDS_TO_MINUTES) {
				updateFailedLogin(username, ip);
			} else {
				failedAttempts = triple.getResultSet().getInt(3);
				if (failedAttempts < MAXIMUM_FAILED_LOGINS) {
					updateFailedLogin(username, ip, failedAttempts + 1);
				} else {
					updateFailedLogin(username, ip, MAXIMUM_FAILED_LOGINS);
					insertLockout(username, ip);
					handleLockedUserPerIp(ip, needsUpdate, locktimeout, attempts);
				}
			}

		}

		else {
			insertNewFailedLogin(username, ip);
		}

		triple.closeAll();

	}

	public static void deleteFailedLogin(String username, String ip) throws SQLException {
		String query = "DELETE FROM FAILED_LOGINS WHERE USERNAME_FAILED =? AND IP =?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void insertNewFailedLogin(String username, String ip) throws SQLException {
		String query = "INSERT INTO FAILED_LOGINS VALUES (?,?,?,?)";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, username);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().setInt(3, 1);
		triple.getPreparedStatement().setTimestamp(4, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void updateFailedLogin(String username, String ip, Integer attempts, Timestamp timestamp)
			throws SQLException {
		String query = "UPDATE FAILED_LOGINS SET ATTEMPTS = ?,  FIRST_ATTEMPT = ? WHERE USERNAME_FAILED = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setInt(1, attempts);
		triple.getPreparedStatement().setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().setString(3, username);
		triple.getPreparedStatement().setString(4, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void updateFailedLogin(String username, String ip, Integer attempts) throws SQLException {
		String query = "UPDATE FAILED_LOGINS SET ATTEMPTS = ? WHERE USERNAME_FAILED = ? AND IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setInt(1, attempts);
		triple.getPreparedStatement().setString(2, username);
		triple.getPreparedStatement().setString(3, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void updateFailedLogin(String username, String ip) throws SQLException {
		String query = "UPDATE FAILED_LOGINS SET ATTEMPTS = ?, FIRST_ATTEMPT = ? WHERE USERNAME_FAILED = ? AND IP = ?;";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setInt(1, 1);
		triple.getPreparedStatement().setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().setString(3, username);
		triple.getPreparedStatement().setString(4, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static boolean isIPLocked(String ip, MutableBoolean needsUpdate, MutableBoolean lockTimeout,
			MutableInteger failed_account_attempts) throws SQLException {
		boolean locked = false;
		needsUpdate.setFlag(false);
		Timestamp lockingTime;
		failed_account_attempts.setInteger(0);
		String query = "SELECT * FROM LOCKDOWN_IPS WHERE IP = ?";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, ip);
		triple.setResultSet(triple.getPreparedStatement().executeQuery());
		if (triple.getResultSet().next()) {
			needsUpdate.setFlag(true);
			lockingTime = triple.getResultSet().getTimestamp(3);

			if (lockingTime.getTime() + IP_LOCKOUT * MILLISECONDS_TO_HOURS > System.currentTimeMillis()) {
				failed_account_attempts.setInteger(triple.getResultSet().getInt(2));
		
				if (failed_account_attempts.getInteger() >= MAXIMUM_ACCOUNTS_ATTEMPTS_FOR_IP) {
					locked = true;
				}
			} else {
				lockTimeout.setFlag(true);
			}

		}
		triple.closeAll();
		return locked;
	}

	public static void handleLockedUserPerIp(String ip, MutableBoolean needsUpdate, MutableBoolean locktimeout, MutableInteger attempts) throws SQLException {
		if (needsUpdate.isFlag()) {
			if (locktimeout.isFlag()) {
				updateLockedUsersPerIP(ip);
			}

			else {
				if(attempts.getInteger() > MAXIMUM_ACCOUNTS_ATTEMPTS_FOR_IP)
				{
					attempts.setInteger(MAXIMUM_ACCOUNTS_ATTEMPTS_FOR_IP-1);
					
				}

				updateLockedUsersPerIP(ip, attempts.getInteger()+1);

			}
		} else {
			insertLockedUsersPerIP(ip);
		}

	}

	public static void insertLockedUsersPerIP(String ip) throws SQLException {
		String query = "INSERT INTO LOCKDOWN_IPS VALUES (?,?,?)";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setString(1, ip);
		triple.getPreparedStatement().setInt(2, 1);
		triple.getPreparedStatement().setTimestamp(3, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void updateLockedUsersPerIP(String ip) throws SQLException {
		String query = "UPDATE LOCKDOWN_IPS SET FAILED = ?, STARTING = ? WHERE IP = ?;";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setInt(1, 1);
		triple.getPreparedStatement().setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		triple.getPreparedStatement().setString(3, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

	public static void updateLockedUsersPerIP(String ip, Integer attempts) throws SQLException {
		String query = "UPDATE LOCKDOWN_IPS SET FAILED = ? WHERE IP = ?;";
		DatabaseTriple triple = new DatabaseTriple(db.connect());
		triple.setPreparedStatement(triple.getConn().prepareStatement(query));
		triple.getPreparedStatement().setInt(1, attempts);
		triple.getPreparedStatement().setString(2, ip);
		triple.getPreparedStatement().executeUpdate();
		triple.closeAll();
	}

}
