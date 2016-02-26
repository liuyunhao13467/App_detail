package tree.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class testFileStream {

	public static void main(String[] args) {

		String write = "D:\\APK\\test.txt";
		try {
			@SuppressWarnings("resource")
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(write),true));
//			writer.write("----");
			writer.write("aaa");
			writer.newLine();
			writer.close();
			System.out.println("write1 successful~~");
					
			writer = new BufferedWriter(new FileWriter(new File(write),true));
			writer.write("bbbb");
			writer.newLine();
			writer.close();
			System.out.println("write2 successful~~");
						
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
