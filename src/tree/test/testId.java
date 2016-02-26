package tree.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;

public class testId {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apk_info_id";
		MySQLCor mysql = new MySQLCor(dburl);

		String test = "select apkid from apk_info where apkname = \"air.com.absolutist.fortunefootball-1000001\"",
		
		selectperid = "select per_id from permission where permission = ?";

		int pid = 0;
		ResultSet p = mysql.select(selectperid, "android.permission.ACCESS_FINE_LOCATION");
		try {
			if(p.next()){
				pid = p.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
		System.out.println(pid);

/*		ResultSet rs = mysql.select(test);
		try {
			if(rs.next()){
				int apkid = rs.getInt(1);
				String apkids = "1" + apkid;
				apkid = Integer.parseInt(apkids);
				System.out.println(apkid);
				
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
*/			
	}
}
