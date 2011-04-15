package com.abstractedsheep.ShuttleTrackerServer;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;

import com.abstractedsheep.extractor.Shuttle;

/**
 * Connects to the server {@link http://www.abstractedsheep.com/phpMyAdmin/} and writes data to the
 * DB table
 * TODO: This program writes specifically to one table in the database called shutle_eta, for the sake of
 * 		 making the code more robust, it might be better to include this table in the file sts.properties along with
 * 		 the other data to connect to the database.
 * @author saiumesh
 * 
 */
public class JSONSender {
	private static Connection conn;
	
	/**
	 * Method connections to the MySQL server using the arguments in the file sts.properties.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void connectToDatabase() throws InstantiationException, IllegalAccessException,
											ClassNotFoundException, IOException, SQLException {
		String driver = "com.mysql.jdbc.Driver";
		String[] args = null;
		
		Class.forName(driver).newInstance();
		args = getArgumentsFromPropertiesFile("sts.properties");
		conn = DriverManager.getConnection(args[0], args[1], args[2]);
		
		deleteTable("shuttle_eta");
	}
	
	/**
	 * Writes the shuttle ETA data to the database. This is done through the use of
	 * MySQL commands.
	 * @param shuttleList - shuttle data to be written to the desired table
	 */
	public static void saveToDatabase(HashSet<Shuttle> shuttleList, String tableName, boolean writeFullList) {
		try {
			connectToDatabase();
			System.out.println("Connected to database");
			Statement stmt = conn.createStatement();
			for (Shuttle shuttle : shuttleList) {
				for (String stop : shuttle.getStopETA().keySet()) {
					//update shuttle_eta table values in DB
					String sql = "UPDATE " + tableName + " SET eta = '"
							+ shuttle.getStopETA().get(stop)
							+ "' WHERE shuttle_id = " + shuttle.getShuttleId()
							+ " AND stop_id = '"
							+ shuttle.getStops().get(stop).getShortName() + "' AND route = '" +
							shuttle.getRouteId() + "'";
					int updateCount = stmt.executeUpdate(sql);
					
					//if updateCount = 0, then the shuttle does not exist in the database.
					//to resolve this, insert the values into the DB as opposed to updating them.
					if (updateCount == 0) {
						String insertHeader = "INSERT INTO " + tableName + " (shuttle_id, stop_id, eta, route)\n";
						String time = writeFullList ? parseTimes(shuttle.getStopETA().get(stop)) :
									  "" + shuttle.getStopETA().get(stop).get(0);
						String interValues = "VALUES ("
								+ shuttle.getShuttleId() + ",'"
								+ shuttle.getStops().get(stop).getShortName()
								+ "','"
								+ time
								+ "', '"
								+ shuttle.getRouteId() + "')";
						stmt.executeUpdate(insertHeader + interValues);
					}
				}
			}
		} catch (Exception e) {
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
	
	private static String parseTimes(ArrayList<Integer> arrayList) {
		String str = "";
		for(int i : arrayList) {
			str += i + ";";
		}
		return (str.substring(0, str.length() - 1));
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
	public static void printToConsole(HashSet<Shuttle> shuttleList) {
		for(Shuttle shuttle : shuttleList) {
			System.out.println(shuttle.getName() + " " + shuttle.getShuttleId() + " " + shuttle.getRouteName() + " " + shuttle.getRouteId());
			for(String name : shuttle.getStopETA().keySet()) {
				System.out.println("\t" + name + " " + getTimeStamp(shuttle.getStopETA().get(name).get(0)) + " " + (shuttle.getStopETA().get(name).get(0)) / (1000 * 60));
				String str = parseTimes(shuttle.getStopETA().get(name));
				System.out.println(str);
			}
		}
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
	
	//Used by printToConsole method in order to make the arrival times show up
	//in a more readable format.
	private static String getTimeStamp(Integer integer) {
		String str = new Timestamp(System.currentTimeMillis() + integer)
				.toString();
		return str.substring(0, str.indexOf('.'));
	}
}