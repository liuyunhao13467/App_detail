package tree.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import parse.mapping.permission.*;
import tree.database.MySQLCor;

public class Top {

	public static void main(String[] args) {
		BufferedWriter writer = null;
		
		try {						
			StringBuffer write = new StringBuffer("E:\\apk\\apk_detail_string\\luanma.txt");
			writer= new BufferedWriter(new FileWriter(new File(write.toString()),true));
			
			StringBuffer path = new StringBuffer("E:\\apk\\apk6000");                  //------------daiding   			
			File file = new File(path.toString());
			File files[] = file.listFiles();
			
			String dburl = "jdbc:mysql://localhost:3306/app_detail";
			MySQLCor mysql = new MySQLCor(dburl);

			
			//中断处理
			String selectMax = "select max(apkid) as maxapkid from app_info";
			
			ResultSet rs = mysql.select(selectMax);
			int maxapkid = 0;
			try {
				if(rs.next()){
					maxapkid = rs.getInt(1); 					
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}			
			
			String selectAPKname = "select distinct apkname from app_info where apkid = " + maxapkid;
			rs = mysql.select(selectAPKname);
			String filename = "";
			try {
				if(rs.next()){
					filename = rs.getString(1); 			//最后一个apkname		
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			
			int arraycount = 0;
			for (int i = 0; i < files.length; i++) {
				if(filename.equals(files[i].getName())){
					arraycount = i;
				}
			}
									
			//删除最后一个解析的apk,  删除主表，级联删除从表
			String delete1 = "delete from app_info where apkid = " + maxapkid;
			mysql.insert(delete1);
											
			
			int j = maxapkid,k = 1;
			for(int i = arraycount; i < files.length; i++){
				OneParser parser = new OneParser(files[i], mysql,j);
				try {
					
					int mark = parser.parse();
					if (mark == 0) {
						System.out.println("此处已删除" + k + "个乱码项目------------");
						k++;			
						
						// 把乱码项目记录到一个文件中
						writer.write(i + "----");
						writer.write(files[i].getName());
						writer.newLine();          //换行
						continue;
					}
					
					String dburlper = "jdbc:mysql://localhost:3306/permission-mapping";
					MySQLCor mysqlper = new MySQLCor(dburlper);	
					
					//解析 permission
					parsePermissionOne permission = new parsePermissionOne(j, mysql, mysqlper);
					permission.parse();
										
/*					deleteRepeat(j,mysql);         //去重							
					parseMethodInvoke(j,mysql);         //method_invoke插入完成
					parseMethodInvokeId(j,mysql);       //method_invoke_id插入完成
*/					
										
					System.out.println("warning--------The " + j
							+ "th APK parsed successuful..........................");					
					j++;				
					
				} catch (Exception e) {
					e.printStackTrace();
					
/*					deleteRepeat(j,mysql);         //去重
					parseMethodInvoke(j,mysql);         //method_invoke插入完成
					parseMethodInvokeId(j,mysql);       //method_invoke_id插入完成
*/
					System.out.println("warning--------The " + j
							+ "th APK parsed successuful..........................");					
					j++;				

				    StringBuffer write1 = new StringBuffer("E:\\apk\\apk_detail_string\\exception.txt");
					try {

						BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(write1.toString()),true));
						writer1.write("apkname: "+files[i].getName());
						writer1.newLine();
						writer1.write("e.getMessage(): "+e.getMessage());
						writer1.newLine();
						e.printStackTrace();

						writer1.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
				}			
				System.gc();
				Runtime.getRuntime().gc();			
			}			
		} catch (IOException e1) {
			e1.printStackTrace();
		    StringBuffer write2 = new StringBuffer("F:\\app_detail\\exception.txt");
			try {
				BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(write2.toString()),true));
				writer2.write("e1.getMessage(): "+e1.getMessage());
				writer2.newLine();
				writer2.write("top exception");
				writer2.newLine();
				e1.printStackTrace();

				writer2.close();
				
				} catch (IOException e3) {
					e3.printStackTrace();
				}
		}
		
		finally{       //关闭文件流，否则文件不能读取
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}		
 	}
	
	//去重
	private static void deleteRepeat(int j, MySQLCor mysql) {
		String own = "insert into method_own (apkid,apkname,method_own,method_invoke,methodtype,beizhu) "
				+ "select distinct apkid,apkname,method_own,method_invoke,methodtype,beizhu from method_own_re where apkid = "
				+ j;				
		String android = "insert into method_android (apkid,apkname,method_own,method_invoke,methodtype,beizhu) "
				+ "select distinct apkid,apkname,method_own,method_invoke,methodtype,beizhu from method_android_re where apkid = "
				+ j;					
		String third = "insert into method_third (apkid,apkname,method_own,method_invoke,methodtype,beizhu) " 
				+ "select distinct apkid,apkname,method_own,method_invoke,methodtype,beizhu from method_third_re where apkid = " 
				+ j;
		String java = "insert into method_java (apkid,apkname,method_own,method_invoke,methodtype,beizhu) " 
				+ "select distinct apkid,apkname,method_own,method_invoke,methodtype,beizhu from method_java_re where apkid = " 
				+ j;
		
		mysql.insert(own);
		mysql.insert(android);
		mysql.insert(third);
		mysql.insert(java);
	}

	//解析映射 method id 
	private static void parseMethodInvokeId(int j, MySQLCor mysql) {
		String selectmethod = "select apkid,method_own,method_invoke,methodtype from method_invoke where apkid = ?";
		ResultSet rs = mysql.select(selectmethod, j);
		try {
			while(rs.next()){
				int apkid = rs.getInt(1);
				String method_own = rs.getString(2);
				String method_invoke = rs.getString(3);
				String methodtype = rs.getString(4);
				
				String selectown = "select id from method_input_output where method = ?";
				ResultSet rs1 = mysql.select(selectown, method_own);
				int method_own_id = 0;
				if(rs1.next()){
					method_own_id = rs1.getInt(1);
				}
				
				String selectinvoke = "select id from method_input_output where method = ?";
				ResultSet rs2 = mysql.select(selectinvoke, method_invoke);
				int method_invoke_id = 0;
				if(rs2.next()){
					method_invoke_id = rs2.getInt(1);
				}

				int type_id = 0;
				if("third".equals(methodtype)){
					type_id = 4;
				}
				else if("android".equals(methodtype)){
					type_id = 3;
				}
				else if("java".equals(methodtype)){
					type_id = 2;
				}
				else{
					type_id = 1;
				}
				
				String insert = "insert into method_invoke_id (apkid,method_own_id,method_invoke_id,type_id) values (?,?,?,?)";
				mysql.insert(insert, apkid, method_own_id, method_invoke_id, type_id);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	// 汇总一个apk中所有的方法调用
	private static void parseMethodInvoke(int apkid,MySQLCor sql) {
		String insertOwn = "INSERT INTO method_invoke (apkid,method_own,method_invoke,methodtype) "
				+ "SELECT apkid,method_own,method_invoke,methodtype FROM method_own where apkid =  " + apkid,
				
				insertAndroid = "INSERT INTO method_invoke (apkid,method_own,method_invoke,methodtype) "
						+ "SELECT apkid,method_own,method_invoke,methodtype FROM method_android where apkid =  " + apkid,
						
				insertThird = "INSERT INTO method_invoke (apkid,method_own,method_invoke,methodtype) "
						+ "SELECT apkid,method_own,method_invoke,methodtype FROM method_third where apkid =  " + apkid,
						
				insertJava = "INSERT INTO method_invoke (apkid,method_own,method_invoke,methodtype) "
						+ "SELECT apkid,method_own,method_invoke,methodtype FROM method_java where apkid =  " + apkid;
		
		sql.insert(insertOwn);
		sql.insert(insertAndroid);
		sql.insert(insertThird);
		sql.insert(insertJava);			
	}

	private static String[] readAndroidPack() {
		String[] array = new String[156];
		try {
			String str;
			FileReader word = new FileReader(new File(
					"D:\\API匹配文件\\AndroidAPI.txt"));
			BufferedReader br = new BufferedReader(word);
			int i = 0;

			while ((str = br.readLine()) != null) {
				array[i] = str;
				i++;
				str = null;
			}
			br.close();
			return array;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;
	}

	private static String[] readJavaPack() {
		String[] array = new String[165];
		try {
			String str;
			FileReader word = new FileReader(new File(
					"D:\\API匹配文件\\JavaAPI.txt"));
			BufferedReader br = new BufferedReader(word);
			int i = 0;

			while ((str = br.readLine()) != null) {
				array[i] = str;
				i++;
				str = null;
			}
			br.close();
			return array;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;
	}	
}
