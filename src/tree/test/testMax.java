package tree.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;

public class testMax {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apk_info";
		MySQLCor mysql = new MySQLCor(dburl);

		String test = "select apkid from apk_info where apkname = \"air.androidapp_1000003\"";
		
		ResultSet rs = mysql.select(test);
		try {
			if(rs.next()){
				System.out.println("yicunzai");
			}
			else
				System.out.println("bucunzai");
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
}
