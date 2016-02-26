package parse.mapping.permission;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;


public class parsePermission {
	private String tablename;
	private MySQLCor mysql, mysqlper;
	private int apkid;
	
	public parsePermission(){
		super();
	}
	
	public parsePermission(int apkid,String name,MySQLCor mysql,MySQLCor mysqlper){
		super();
		this.apkid = apkid; 
		this.tablename = name;
		this.mysql = mysql;
		this.mysqlper = mysqlper;
	}
	
	public void parse() {
		String selectmethod = "select distinct methodsig from published_api_mapping", 
				selectinvoke = "select distinct method_invoke from method_android where apkname = \"" + tablename + "\"";

//				selectinvoke = "select distinct method_invoke from " + tablename + " where isandroidapi = \"true\"";

		try {
			ResultSet rs = mysql.select(selectinvoke);
			while(rs.next()){
				String invoke = rs.getString(1).substring(9);
				
				ResultSet rsmethod = mysqlper.select(selectmethod);
				while(rsmethod.next()){
					String method = rsmethod.getString(1);
					if(invoke.equals(method)){
						String selectper = "select distinct permission from published_api_mapping where methodsig = ?";
						ResultSet rsper = mysqlper.select(selectper, method);
						while(rsper.next()){
							String per = rsper.getString(1);
							String insert = "insert into permission (apkid,apkname,method_invoke,permission,beizhu) values (?,?,?,?,?)";
							mysql.insert(insert, apkid,tablename,method, per,"");
						}
						break;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
