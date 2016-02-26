package tree.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;

public class test2 {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apktree";
		MySQLCor mysql = new MySQLCor(dburl);

		String selectapktree = "select distinct apkid,apk,packagename,classsig,methodsig,inoutput,beizhu from apktree where apkid = 1";
		
		ResultSet rs = mysql.select(selectapktree);
		try {
			while(rs.next()){
				System.out.println(rs.getString(3));
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
