package tree.database;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import tree.parse.entity.ApkEntity;
import tree.parse.entity.Callee;
import tree.parse.entity.Caller;

public class MySQLCor {
	
    private String dbUrl = "";
    
    private Connection con = null;   
	
    public MySQLCor() {	
    	getCon();
	}
	
	public MySQLCor(String url){
		this.dbUrl = url;
		getCon();
	}

	public Connection getCon(){
		String dbDriver = "com.mysql.jdbc.Driver";	
		String dbUserName = "root";
//		String dbPassword = "ly123456789";
		String dbPassword = "asd123";
		try {
			Class.forName(dbDriver);
			con = DriverManager.getConnection(dbUrl,dbUserName,dbPassword);
			System.out.println("connected....");
			System.out.println();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;		
	}
	
	public void ExceptionLog(Exception e){
	    StringBuffer write = new StringBuffer("E:\\apk\\apk_detail_string\\exception.txt");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(write.toString()),true));

			writer.write("e.getMessage(): "+e.getMessage());			
			writer.newLine();
			e.printStackTrace();

			writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}	
	}
	
	public ResultSet select(String sql){
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}
	
	public ResultSet select(String sql,String classname){
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(sql);
			prestmt.setString(1, classname);
			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}
	
	 
	public ResultSet selectApkID(String sql,String apkName,String apkVersion){
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(sql);
			prestmt.setString(1, apkName);
			prestmt.setString(2, apkVersion);
			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}
	public ResultSet select(String sql,String classname,String sig){
		ResultSet rs = null;
		try {
			PreparedStatement  prestmt = con.prepareStatement(sql);
			prestmt.setString(1, classname);
			prestmt.setString(2, sig);
			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}

	public void insert(String insertsql){
		try {
			PreparedStatement prestmt = con.prepareStatement(insertsql);
			prestmt.executeUpdate();
			System.out.println("insert successful");
			
			prestmt = null;
		} catch (SQLException e) {
			ExceptionLog(e);
		}
	}

	public int insert(String insertsql,String classname,String sig){
		int i = 0;
		try {
			PreparedStatement prestmt = con.prepareStatement(insertsql);
			prestmt.setString(1, classname);
			prestmt.setString(2, sig);
			i = prestmt.executeUpdate();
			System.out.println((i == 1)?("更改一条记录成功"):("更改多条记录成功"));
			
			prestmt = null;
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return i;
	}
	
	public int insert(String insertsql,String classname,String sig,String str){
		int i = 0;
		try {
			PreparedStatement prestmt = con.prepareStatement(insertsql);
			prestmt.setString(1, classname);
			prestmt.setString(2, sig);
			prestmt.setString(3, str);

			i = prestmt.executeUpdate();
			System.out.println((i == 1)?("插入一条记录成功"):("插入一条记录失败"));
			
			prestmt = null;
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return i;
	}

	public int insert(String sql,String apkname,InputStream input,int length){
		int i = 0;

		try {
			PreparedStatement prestmt = con.prepareStatement(sql);
			prestmt.setString(1, apkname);
			prestmt.setBinaryStream(2, input, length);
			
			i = prestmt.executeUpdate();
			System.out.println((i == 1)?("插入一条记录成功"):("插入一条记录失败"));
			
			prestmt = null;
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return i;
	}
	
	public void insert(String sql,String classname,String methodname,String invokeclassname,String invokemethodname){
		try {
			PreparedStatement prestmt = con.prepareStatement(sql);
			prestmt.setString(1, classname);
			prestmt.setString(2, methodname);
			prestmt.setString(3, invokeclassname);
			prestmt.setString(4, invokemethodname);

			prestmt.executeUpdate();
			System.out.println("insert successful");

		} catch (SQLException e) {
			ExceptionLog(e);
			
		}
	}

	public void insert(String insertsql, String tablename, String packagename,
			String classname, String classsig, String methodname,
			String methodsig, String invokemethodsig, String isandroidapi,
			String string) {
		int i = 0;

		try {
			PreparedStatement prestmt = con.prepareStatement(insertsql);
			prestmt.setString(1, tablename);
			prestmt.setString(2, packagename);
			prestmt.setString(3, classname);
			prestmt.setString(4, classsig);
			prestmt.setString(5, methodname);
			prestmt.setString(6, methodsig);
			prestmt.setString(7, invokemethodsig);
			prestmt.setString(8, isandroidapi);
			prestmt.setString(9, string);

			i = prestmt.executeUpdate();
			System.out.println((i == 1)?("插入一条记录成功" + tablename):("插入一条记录失败" + tablename));

			prestmt = null;
		} catch (SQLException e) {
			ExceptionLog(e);
		}		
	}

	public void create(String tablename){
		String sql1 = "CREATE TABLE " + tablename + " (id int not null AUTO_INCREMENT, apkname varchar(3000), "
				+ "packagename varchar(3000), classname varchar(3000), classsig varchar(3000), methodname text, "
				+ "methodsig text, invokemethodsig text, isandroidapi varchar(1000), beizhu varchar(3000), "
				+ "primary key (id));";
		
//		String sql2 = "CREATE TABLE " + tablename + "_g (id int not null AUTO_INCREMENT, methodname_g varchar(1000), invokemethodname_g varchar(1000), primary key (id));";//有待完善
		
		PreparedStatement pstmt;
		try {
			pstmt = con.prepareStatement(sql1);
			pstmt.executeUpdate();
			
			
/*			pstmt = con.prepareStatement(sql2);
			pstmt.executeUpdate();			
*/
			
		} catch (SQLException e) {
			ExceptionLog(e);
		}
	}
	
	//测试double
	public static void main(String[] args) {
		String testname = "name";
		String dburl = "jdbc:mysql://localhost:3306/permission_code_parser";
		MySQLCor mysql = new MySQLCor(dburl);		
		mysql.createdouble(testname);
		mysql.createdouble("testname");

	}
	
	private void createdouble(String testname) {
		String sql1 = "CREATE TABLE " + testname + " (id int not null AUTO_INCREMENT, name varchar(1000), na varchar(1000), "
				+ "primary key (id));";
		
		PreparedStatement pstmt;
		try {
			pstmt = con.prepareStatement(sql1);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			ExceptionLog(e);
		} catch(Exception e){
			ExceptionLog(e);
		}
	}


	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public void insert(String insert1, int count, String apk,
			String packagename, String classsig, String methodsig, String args,
			String string) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert1);
			prestmt.setInt(1, count);
			prestmt.setString(2, apk);
			prestmt.setString(3, packagename);
			prestmt.setString(4, classsig);
			prestmt.setString(5, methodsig);
			prestmt.setString(6, args);
			prestmt.setString(7, string);
			
/*			prestmt.setInt(8, count);
			prestmt.setString(9, apk);
			prestmt.setString(10, packagename);
			prestmt.setString(11, classsig);
			prestmt.setString(12, methodsig);
			prestmt.setString(13, args);
			prestmt.setString(14, string);
*/
			prestmt.executeUpdate();
			System.out.println("操作一条记录成功");
	
			prestmt = null;
		} catch (SQLException e) {
			ExceptionLog(e);
		}
	}

	public void insert(String insert2, int count, String apk,
			String packagename, String inpackagename, String string) {

		try {
			PreparedStatement prestmt = con.prepareStatement(insert2);
			prestmt.setInt(1, count);
			prestmt.setString(2, apk);
			prestmt.setString(3, packagename);
			prestmt.setString(4, inpackagename);
			prestmt.setString(5, string);

			prestmt.executeUpdate();
			System.out.println("insert successful");

			prestmt = null;
		} catch (SQLException e) {
			
			ExceptionLog(e);
		}
	}

	public ResultSet select(StringBuffer select, int count, String apk,
			String packagename, String classsig, String methodsig, String args,
			String string) {
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(select.toString());
			prestmt.setInt(1, count);
			prestmt.setString(2, apk);
			prestmt.setString(3, packagename);
			prestmt.setString(4, classsig);
			prestmt.setString(5, methodsig);
			prestmt.setString(6, args);
			prestmt.setString(7, string);

			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}

	public ResultSet select(StringBuffer selectpackage, int count, String apk,
			String packagename, String inpackagename, String string) {
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(selectpackage.toString());
			prestmt.setInt(1, count);
			prestmt.setString(2, apk);
			prestmt.setString(3, packagename);
			prestmt.setString(4, inpackagename);
			prestmt.setString(5, string);

			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	
	}

	public void insert(String insertsql, String name, int count, String string) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insertsql);
			prestmt.setString(1, name);
			prestmt.setInt(2, count);
			prestmt.setString(3, string);

			prestmt.executeUpdate();
			System.out.println("insert successful");

			prestmt = null;
		} catch (SQLException e) {			
			ExceptionLog(e);
		}
		
	}

	public void insert(String insert1, int count, String string, String string2) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert1);
			prestmt.setInt(1, count);
			prestmt.setString(2, string);
			prestmt.setString(3, string2);

			prestmt.executeUpdate();
			System.out.println("insert successful");

			prestmt = null;
		} catch (SQLException e) {			
			ExceptionLog(e);
		}
	}

	public void insert(String insert2, int count, String string,
			String string2, String string3, String string4, String string5) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert2);
			prestmt.setInt(1, count);
			prestmt.setString(2, string);
			prestmt.setString(3, string2);
			prestmt.setString(4, string3);
			prestmt.setString(5, string4);
			prestmt.setString(6, string5);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}		
	}

	public void insert(String insert1, String string, String apk,
			String packagename, String inpackagename, String string2) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert1);
			prestmt.setString(1, string);
			prestmt.setString(2, apk);
			prestmt.setString(3, packagename);
			prestmt.setString(4, inpackagename);
			prestmt.setString(5, string2);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}		
	}

	public ResultSet select(String selectmethod, int j) {
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(selectmethod);
			prestmt.setInt(1, j);
			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}

	public void insert(String insert, int apkid, int method_own_id,
			int method_invoke_id, int type_id) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert);
			prestmt.setInt(1, apkid);
			prestmt.setInt(2, method_own_id);
			prestmt.setInt(3, method_invoke_id);
			prestmt.setInt(4, type_id);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}				
	}

	public void insert(String insert1, int count, String string,
			String string2, String string3) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert1);
			prestmt.setInt(1, count);
			prestmt.setString(2, string);
			prestmt.setString(3, string2);
			prestmt.setString(4, string3);
			
			prestmt.executeUpdate();
			System.out.println(string + "insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}				
	}

	public void insert(String inme, int mid, String string, int pid, int rid) {
		try {
			PreparedStatement prestmt = con.prepareStatement(inme);
			prestmt.setInt(1, mid);
			prestmt.setString(2, string);
			prestmt.setInt(3, pid);
			prestmt.setInt(4, rid);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}						
	}

	public void insert(String inme, int mid, String string, int pid, int rid,
			String string2) {
		try {
			PreparedStatement prestmt = con.prepareStatement(inme);
			prestmt.setInt(1, mid);
			prestmt.setString(2, string);
			prestmt.setInt(3, pid);
			prestmt.setInt(4, rid);
			prestmt.setString(5, string2);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}								
	}

	public void insert(String in, int pre_count, int mid, int minid, int i,
			int mid2, int minid2) {
		try {
			PreparedStatement prestmt = con.prepareStatement(in);
			prestmt.setInt(1, pre_count);
			prestmt.setInt(2, mid);
			prestmt.setInt(3, minid);
			prestmt.setInt(4, i);
			prestmt.setInt(5, mid2);
			prestmt.setInt(6, minid2);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}										
	}

	public void insert(String in, int i, String permission) {
		try {
			PreparedStatement prestmt = con.prepareStatement(in);
			prestmt.setInt(1, i);
			prestmt.setString(2, permission);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}										
	}

	public void insert(String insert, int apkid, int mid, int per_id) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert);
			prestmt.setInt(1, apkid);
			prestmt.setInt(2, mid);
			prestmt.setInt(3, per_id);
			
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}										
	}

	public void insert(String inpara, int count, int mid,
			String returntype, String classsig, String methodsig,
			String args, String string) {
		try {
			PreparedStatement prestmt = con.prepareStatement(inpara);
			prestmt.setInt(1, count);
			prestmt.setInt(2, mid);
			prestmt.setString(3, returntype);
			prestmt.setString(4, classsig);
			prestmt.setString(5, methodsig);
			prestmt.setString(6, args);
			prestmt.setString(7, string);

			
			prestmt.executeUpdate();
			System.out.println(returntype+": " + "insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}		

	}

	public void insert(String inme, int mid, int count, String string,
			String classsig, int pid, String args, String string2) {
		try {
			PreparedStatement prestmt = con.prepareStatement(inme);
			prestmt.setInt(1, mid);
			prestmt.setInt(2, count);
			prestmt.setString(3, string);
			prestmt.setString(4, classsig);
			prestmt.setInt(5, pid);
			prestmt.setString(6, args);
			prestmt.setString(7, string2);
			
			prestmt.executeUpdate();
			System.out.println(string + ":" + "insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}		

	}
	/**
	 * 将一个apk的信息，从内存中转移到数据库中。
	 * @param apkInfo
	 * @param insertWhole
	 */
	public void insertWholeBasicInfo(ApkEntity apkInfo,String insertWhole) {
//下面数字对应的含义如下：
//   1: callerApkVersion, 2 ： callerApkName ， 3 ： calllerPackageName ，4：callerClassName , 5：callerMethodName 
//	 6： callerMethodType ,7： callerRreturnType , 8：callerParameter
//	 9：calleeApkVersion , 10 ：calleeApkName , 11 ：calleePackageName　,　12　：calleeClassName　,13　：calleeMethodName,
//	　14:calleeMethodType ,15 :calleeRreturnType , 16:calleeParameter
		try {
			PreparedStatement prestmt = con.prepareStatement(insertWhole);
			
			for(Caller caller :apkInfo.getCallerList()){
				prestmt.setString(1, apkInfo.getApkVersion());
				prestmt.setString(2, apkInfo.getApkName());
				prestmt.setString(3, caller.getPackageName());
				prestmt.setString(4, caller.getClassName());
				prestmt.setString(5, caller.getMethodName());
				prestmt.setString(6, caller.getMethodType());
				prestmt.setString(7, caller.getReturnType());
				prestmt.setString(8, caller.getParameter());
				
				for(Callee callee : caller.getCalleeList()){
					prestmt.setString(9, apkInfo.getApkVersion());
					prestmt.setString(10, apkInfo.getApkName());
					prestmt.setString(11, callee.getPackageName());
					prestmt.setString(12, callee.getClassName());
					prestmt.setString(13, callee.getMethodName());
					prestmt.setString(14, callee.getMethodType());
					prestmt.setString(15, callee.getReturnType());
					prestmt.setString(16, callee.getParameter());
					
				}
				prestmt.addBatch();
			}
			
			prestmt.executeBatch();
			prestmt.close();
			System.out.println( apkInfo.getApkName() +  "insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}		

	}

	public ResultSet select(String sn, int mid, int inmid) {
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(sn);
			prestmt.setInt(1, mid);
			prestmt.setInt(2, inmid);

			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;
	}

	public ResultSet select(String selectinvoke, int apkid, String sig) {
		ResultSet rs = null;
		try {
			PreparedStatement prestmt = con.prepareStatement(selectinvoke);
			prestmt.setInt(1, apkid);
			prestmt.setString(2, sig);

			rs = prestmt.executeQuery();
		} catch (SQLException e) {
			ExceptionLog(e);
		}
		return rs;

	}

	public void insert(String insert, int apkid, int pid, int per_id,
			String object, String string2) {
		try {
			PreparedStatement prestmt = con.prepareStatement(insert);
			prestmt.setInt(1, apkid);
			prestmt.setInt(2, pid);
			prestmt.setInt(3, per_id);
			prestmt.setString(4, object);
			prestmt.setString(5, string2);
		
			prestmt.executeUpdate();
			System.out.println("insert successful~");
	
		} catch (SQLException e) {
			ExceptionLog(e);
		}		

	}	
}
