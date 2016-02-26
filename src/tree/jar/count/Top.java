package tree.jar.count;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import tree.database.MySQLCor;

public class Top {

	public static void main(String[] args) {
		try {
			String javapack[] = readJavaPack();                       //存储Java package的数组
			String androidpack[] = readAndroidPack();                     //存储android package的数组
						
			StringBuffer write = new StringBuffer("E:\\apk\\error.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(write.toString()),true));
			
			StringBuffer writeapk = new StringBuffer("E:\\apk\\apkblue.txt");
			BufferedWriter writerapk = new BufferedWriter(new FileWriter(new File(writeapk.toString()),true));

			StringBuffer path = new StringBuffer("D:\\apktool-批处理\\11");
			File file = new File(path.toString());
			File files[] = file.listFiles();
			
			String dburl = "jdbc:mysql://localhost:3306/jarcount";
			MySQLCor mysql = new MySQLCor(dburl);

			//中断处理======删除最后一个解析的apk, 从删除的apk继续解析
			String selectMax = "select max(apkid) as maxapkid from android1";//==========改，选最大的
			
			ResultSet rs = mysql.select(selectMax);
			int maxapkid = 0;
			try {
				if(rs.next()){
					maxapkid = rs.getInt(1); 					
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			
			String selectAPKname = "select distinct apk from android1 where apkid = " + maxapkid;
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
						
			String delete1 = "delete from android1 where apkid = " + maxapkid;
			String delete2 = "delete from java1 where apkid = " + maxapkid;
			String delete3 = "delete from third1 where apkid = " + maxapkid;

			mysql.insert(delete1);
			mysql.insert(delete2);
			mysql.insert(delete3);
					
			int j = maxapkid,k = 1;
			for(int i = arraycount; i < files.length; i++){
				OneParser parser = new OneParser(files[i], mysql,j,javapack,androidpack);
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
					
					//记录		
					writerapk.write(j + ": " + files[i].getName() + ".apk");
					writerapk.newLine();
					
					System.out.println("warning--------The " + j
							+ "th APK parsed successuful..........................");
					
					j++;				
					
				} catch (Exception e) {
					e.printStackTrace();
					
					System.out.println("exception--------The " + j
							+ "th APK parsed successuful..........................");
					
					j++;				

				    StringBuffer write1 = new StringBuffer("E:\\apk\\exception.txt");
					try {

						BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(write1.toString()),true));
						writer1.write("apkname: "+files[i].getName());
						writer.newLine();
						writer1.write("e.getMessage(): "+e.getMessage());
						writer.newLine();
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
		    StringBuffer write2 = new StringBuffer("E:\\apk\\exception.txt");
			try {
				BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(write2.toString()),true));
				writer2.write("e1.getMessage(): "+e1.getMessage());
				writer2.newLine();
				e1.printStackTrace();

				writer2.close();
				} catch (IOException e3) {
					e3.printStackTrace();
				}
		}
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
