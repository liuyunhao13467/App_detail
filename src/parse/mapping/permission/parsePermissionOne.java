package parse.mapping.permission;

import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;


public class parsePermissionOne {
	private MySQLCor mysql, mysqlper;
	private int apkid;
	
	public parsePermissionOne(){
		super();
	}
	
	public parsePermissionOne(int apkid,MySQLCor mysql,MySQLCor mysqlper){
		super();
		this.apkid = apkid; 
		this.mysql = mysql;
		this.mysqlper = mysqlper;
	}
	
	public void parse() {
		String selectmethod = "select distinct methodsig from published_api_mapping", 
				selectinvoke = "select distinct methodname from method where apkid = ? and type = ?";

		String selectmid = "select distinct mid from method where apkid = ? and methodname = ?",
				selectperid = "select per_id from permission where permission = ?";
//				selectinvoke = "select distinct method_invoke from " + tablename + " where isandroidapi = \"true\"";
		String insert = "insert into permission_mapping (apkid, mid, per_id,beizhu,tag) values (?,?,?,?,?)";

		int pid = 0;
		try {
			ResultSet rs = mysql.select(selectinvoke,apkid,"android");
			while(rs.next()){
				String invoke = rs.getString(1).substring(9);           // 每一个 invoke Android method
				
				ResultSet rsmethod = mysqlper.select(selectmethod);
				while(rsmethod.next()){
					String method = rsmethod.getString(1);              // 每一个权限相关的method
					if(invoke.equals(method)){
						String selectper = "select distinct permission from published_api_mapping where methodsig = ?";
						ResultSet rsper = mysqlper.select(selectper, method);
						while(rsper.next()){          //method对应的每一个permission
							String per = rsper.getString(1);
							
//							int mid = selectId(selectmid, "android: "+method);
							ResultSet p = mysql.select(selectmid, apkid,  "android: "+method);
							try {                  //重复的mid（每一个mid）
								while(p.next()){
									pid = p.getInt(1);
									
									int per_id = selectId(selectperid, per);
									
									mysql.insert(insert, apkid, pid, per_id, null, null);
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}									
						}
						break;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private int selectId(String selectpid, String args) {	
		int pid = 0;
		ResultSet p = mysql.select(selectpid, args);                 
		try {
			if(p.next()){
				pid = p.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return pid;
	}
}
