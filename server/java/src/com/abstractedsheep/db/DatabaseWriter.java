package com.abstractedsheep.db;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.logging.Logger;

import com.abstractedsheep.ShuttleTrackerService.ETACalculator;
import com.abstractedsheep.ShuttleTrackerService.ETACalculator.Eta;

/**
 * Connects to the server {@link http://www.abstractedsheep.com/phpMyAdmin/} and writes data to the
 * DB table
 * TODO: This program writes specifically to one table in the database called shutle_eta, for the sake of
 * 		 making the code more robust, it might be better to include this table in the file sts.properties along with
 * 		 the other data to connect to the database.
 * @author saiumesh
 * 
 */
public class DatabaseWriter extends AbstractQueryRunner{
	private static Connection conn;
	//private final Logger log = Logger.getLogger(null);
	
	/**
	 * Method connections to the MySQL server using the arguments in the file sts.properties.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void connectToDatabase(String tableName) throws InstantiationException, IllegalAccessException,
											ClassNotFoundException, IOException, SQLException {
		String driver = "com.mysql.jdbc.Driver";
		String[] args = null;
		
		Class.forName(driver).newInstance();
		args = getArgumentsFromPropertiesFile("sts.properties");
		conn = DriverManager.getConnection(args[0], args[1], args[2]);
		
		deleteTable(tableName);
	}
	
	public void writeToDatabase(Connection conn, ETACalculator etaList, String tableName) throws SQLException {
		String header = "INSERT INTO {?} (shuttle_id, stop_id, eta_id, eta, absolute_eta, route)\n";
		String values = "VALUES ( {?},'{?}','{?}', '{?}', '{?}', '{?}')";
		String insertQuery = header + values + " ON DUPLICATE KEY ";
		String updateQuery = "eta=VALUES(eta), absolute_eta=VALUES(absolute_eta), route=VALUES(route)";
		Statement stmt = conn.createStatement();
		final String query = insertQuery + updateQuery;
		String sql = "";
		for(Eta eta : etaList.getETAs()) {
			sql = String.format(query, new Object[]{tableName, eta.time, eta.shuttleId, eta.stopName,
									  eta.routeId, eta.Id, eta.arrivalTime});
			
			stmt.addBatch(sql);
		}
		
		stmt.executeBatch();
	}
	
	public static void saveToDatabase(ETACalculator etaList, String tableName) {
		try {
			connectToDatabase(tableName);
			Statement stmt = conn.createStatement();
			MessageFormat f = null;
			for(Eta eta : etaList.getETAs()) {
				String query = "UPDATE {0} SET eta = '{1}'" +
						"WHERE shuttle_id = {2} AND stop_id = '{3}'" +
						" AND route = '{4}' AND eta_id = '{5}'" +
						" AND absolute_eta = '{6}'";
				
				f = new MessageFormat(query);
				f.format(new Object[]{tableName, eta.time, eta.shuttleId, eta.stopName,
									  eta.routeId, eta.Id, eta.arrivalTime});
				query = f.toString();
				
				int updateCount = stmt.executeUpdate(query);
				
				if(updateCount == 0) {
					String header = "INSERT INTO {0} (shuttle_id, stop_id, eta_id, eta, absolute_eta, route)\n";
					String values = "VALUES ( {1},'{2}','{3}', '{4}', '{5}', '{6}')";
					query = header + values;
					f = new MessageFormat(query);
					f.format(new Object[] {tableName, eta.shuttleId, eta.stopName, eta.Id,
										   eta.time, eta.arrivalTime, eta.routeId});
					query = f.toString();
					
					stmt.executeUpdate(query);
				}
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//after writing the values to the DB, close the connection to the database.
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
			}
		}
	}
	
	/**
	 * Delete the values in the database table
	 * @param tableName this value is currently not used
	 */
	private static void deleteTable(String tableName) {
		try {
			Statement stm = conn.createStatement();
			String sql = "TRUNCATE TABLE " + tableName;
			
			stm.executeUpdate(sql);
		} catch(SQLException e) {}
	}
	
	/**
	 * Outputs the shuttle ETA information to the console for quick debugging.
	 * @param shuttleList - shuttle data
	 */
	public static void printToConsole(ETACalculator etaList) {
	}
	
	/**
	 * @param path path to sts.properties file
	 * @return returns the arguments from the sts.properties file
	 * @throws IOException
	 */
	private static String[] getArgumentsFromPropertiesFile(String path)
			throws IOException {
		String[] values = new String[3];
		File f = new File(path);
		System.out.println(f.getAbsolutePath());
		BufferedReader buf = new BufferedReader(new FileReader(f));
		String line = "";
		line = buf.readLine();
		for (int i = 0; (line != null); i++) {
			values[i] = line;
			line = buf.readLine();
		}
		buf.close();
		return values;
	}
}