package tree.duplicate.removal;

import tree.database.MySQLCor;

//整体去除重复的数据
public class DistinctiveSelected {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apktree";
		MySQLCor mysql = new MySQLCor(dburl);

		String selectapktree = "insert into apktree_norepeat (apkid,apk,packagename,classsig,methodsig,inoutput,beizhu) "
				+ "select distinct apkid,apk,packagename,classsig,methodsig,inoutput,beizhu from apktree";
		
		mysql.insert(selectapktree);

		
		String selectclass = "insert into class_norepeat (apkid,apk,classsig,inclasssig,beizhu) "
				+ "select distinct apkid,apk,classsig,inclasssig,beizhu from class";
		
		mysql.insert(selectclass);

		
		String selectmethod = "insert into method_norepeat (apkid,apk,methodsig,invokemethodsig,beizhu) "
				+ "select distinct apkid,apk,methodsig,invokemethodsig,beizhu from method";
		
		mysql.insert(selectmethod);

		
		String selectpackage = "insert into package_norepeat (apkid,apk,packagename,inpackagename,beizhu) "
				+ "select distinct apkid,apk,packagename,inpackagename,beizhu from package";
		
		mysql.insert(selectpackage);
		
	}
}
