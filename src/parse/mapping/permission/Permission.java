package parse.mapping.permission;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;

public class Permission {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apk_info_id",
		dburlper = "jdbc:mysql://localhost:3306/permission";
		MySQLCor mysql = new MySQLCor(dburl);	
		MySQLCor mysqlper = new MySQLCor(dburlper);	

		String insert = "select distinct per_name from perm",
				in = "insert into permission (per_id,permission) values (?,?)";
		ResultSet rs = mysqlper.select(insert);
		try {
			int i = 1;
			while(rs.next()){
				String permission = rs.getString(1);
				int j = Integer.parseInt(("5" + i));
				
				mysql.insert(in, j, permission);
						
				i ++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
