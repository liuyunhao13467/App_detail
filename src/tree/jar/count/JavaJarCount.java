package tree.jar.count;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;

public class JavaJarCount {
	ResultSet rs = null;
	String dburl = "jdbc:mysql://localhost:3306/jarcount";
	MySQLCor mysql = new MySQLCor(dburl);	

	String sql_c = "";
	ResultSet rs_c = null;
	String name = "";
	int count = 0;
	String insertsql = "";

	public static void main(String[] args) {
	    String 	sql1 = "select distinct inpackagename from java",
	    		sql2 = "select distinct method_invoke from java";
		JavaJarCount jarcount = new JavaJarCount();

		jarcount.insert(sql1, "inpackagename", "java","javajarcount");
		jarcount.insert(sql2, "method_invoke", "java","javamethodcount");	
		
		System.out.println("Java统计完毕！");

	}
	
	private void insert(String sql,String attr,String tablename,String newtablename){
		rs = mysql.select(sql);
		try {
			while(rs.next()){
				name = rs.getString(1);
				
				sql_c = "select count(id) from " + tablename + " where " + attr + "=?";
				rs_c = mysql.select(sql_c,name);
				
				if(rs_c.next()){
					count = rs_c.getInt(1);
			    }
				
				insertsql = "insert into "+newtablename+" ("+attr+",count,beizhu) values (?,?,?)";
				mysql.insert(insertsql, name,count, "");			//待定	
			}
		} catch (SQLException e) {
	 		e.printStackTrace();
		}
	}
}
