package tree.test;

import java.io.File;

public class testFilelist {

	public static void main(String[] args) {
		StringBuffer path = new StringBuffer("D:\\APK\\smaliTest");
		File file = new File(path.toString());
		File files[] = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getName());
		}
	}

}
