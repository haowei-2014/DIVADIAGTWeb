package ch.unifr;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnection {

	String url = "jdbc:mysql://localhost:3306/";
	String dbName = "images";
	String driver = "com.mysql.jdbc.Driver";
	String userName = "root";
	String password = "whnewpc";

	public void insert(String imageName, String imageURL)
			throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		Connection connection = DriverManager.getConnection(url + dbName,
				userName, password);
		Statement statement = connection.createStatement();
		
		ResultSet res = statement.executeQuery("select * from images1 "
				+ "where name = '" + imageName + "'");
		int val;
		if (res.next()){
			val = statement.executeUpdate("delete from images1 where name = '" 
					+ imageName + "'");
			if (val == 1)
				System.out.println("Successfully deleted value");
		} 
		String query = "insert into images1 (name, imageURL) values ('"
				+ imageName + "', '" + imageURL + "')";
		val = statement.executeUpdate(query);
		if (val == 1)
			System.out.println("Successfully inserted value");
		else
			System.out.println("Insert operation has a problem.");
		connection.close();
	}

	public String select(String imageName) throws ClassNotFoundException,
			SQLException {
		String msg = "";
		Class.forName(driver);
		Connection connection = DriverManager.getConnection(url + dbName,
				userName, password);
		Statement statement = connection.createStatement();
		String query = "select * from  images1 where name='" + imageName + "'";
		ResultSet res = statement.executeQuery(query);
		while (res.next()) {
			msg = res.getString("imageURL");
//			System.out.println(msg);
		}
		connection.close();
		return msg;
	}

	public static void main(String[] args) {
		MySQLConnection mySQLConnection = new MySQLConnection();
		try {
//			mySQLConnection.insert("image4", "ddddddddddddddd");
			mySQLConnection.select("image1");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
