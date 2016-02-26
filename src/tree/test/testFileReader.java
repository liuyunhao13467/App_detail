package tree.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class testFileReader {

	public static void main(String[] args) {
		StringBuffer[] javapack = readJavaPack();
		for (int i = 0; i < javapack.length; i++) {
			System.out.println(javapack[i]);
		}
		
	}
	private static StringBuffer[] readJavaPack() {
		StringBuffer[] array = new StringBuffer[165];
		try {
			String str;
			FileReader word = new FileReader(new File(
					"D:\\APIÆ¥ÅäÎÄ¼þ\\JavaAPI.txt"));
			BufferedReader br = new BufferedReader(word);
			int i = 0;

			while ((str = br.readLine()) != null) {
				array[i] = new StringBuffer(str);
				i++;
				str = null;
			}
			return array;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return array;
	}


}
