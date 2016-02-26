package tree.test;

import tree.database.MySQLCor;

public class deleteTest {

	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apktree";
		MySQLCor mysql = new MySQLCor(dburl);

		String delete1 = "delete from apktree_norepeat where apkid = 14";
		String delete2 = "delete from class_norepeat where apkid = 14";
		String delete3 = "delete from method_norepeat where apkid = 14";
		String delete4 = "delete from package_norepeat where apkid = 14";

		mysql.insert(delete1);
		mysql.insert(delete2);
		mysql.insert(delete3);
		mysql.insert(delete4);

	}

}
