package tree.test;

import tree.database.MySQLCor;

public class testInsertRepeat {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/test";
		MySQLCor mysql = new MySQLCor(dburl);

		String in = "INSERT INTO method_invoke (apkid,method_own,method_invoke,methodtype) "
				+ "SELECT ?,?,?,? FROM DUAL WHERE NOT EXISTS(SELECT method_own,method_invoke FROM method_invoke "
				+ "WHERE method_own = ? and method_invoke = ?)";
		
		mysql.insert(in, 5, "5", "5", "5","5","5");
		
/*		String insert1 = "INSERT INTO method_input_output (method,input,output,beizhu) "
				+ "SELECT ?,?,?,? FROM DUAL WHERE NOT EXISTS(SELECT method FROM method_input_output WHERE method = ?)";
//				insert2 = "INSERT INTO method_input_output (method,input,output,beizhu) VALUES(?,?,?,?)";
		
		mysql.insert(insert1, "method1","","","","method1");
		mysql.insert(insert1, "method2","","","","method2");
		mysql.insert(insert1, "method3","","","","method3");

		mysql.insert(insert1, "method7","","","","method3");
		mysql.insert(insert1, "method8","","","","method8");
		mysql.insert(insert1, "method9","","","","method9");
*/

//		mysql.insert(insert1, "method2","","","");

		
	}

}
