package tree.test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tree.database.MySQLCor;
import tree.parse.ExtractOuterClass;

public class testPath {

	/*		

	File getAbsoluteFile() 
	        返回此抽象路径名的绝对路径名形式。 
	String getAbsolutePath() 
	        返回此抽象路径名的绝对路径名字符串。 
	File getCanonicalFile() 
	        返回此抽象路径名的规范形式。 
	String getCanonicalPath() 
	        返回此抽象路径名的规范路径名字符串。 
	long getFreeSpace() 
	        返回此抽象路径名指定的分区中未分配的字节数。 
	String getName() 
	        返回由此抽象路径名表示的文件或目录的名称。 
	String getParent() 
	        返回此抽象路径名父目录的路径名字符串；如果此路径名没有指定父目录，则返回 null。 
	File getParentFile() 
	        返回此抽象路径名父目录的抽象路径名；如果此路径名没有指定父目录，则返回 null。 
	String getPath() 
	        将此抽象路径名转换为一个路径名字符串。         
	*/
	
	
	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apktree";
		MySQLCor mysql = new MySQLCor(dburl);

		String filestr = "D:\\APK\\smaliTest\\a.b.namespace_8";
		File file = new File(filestr);

		ExtractOuterClass outcla = new ExtractOuterClass(file);
		List<File> filelist = outcla.extractClassFile();	
	
		StringBuffer buff[] = returnBuff(filelist);
		for (int i = 0; i < buff.length; i++) {
			System.out.println(buff[i]);
		}
		
	}

	private static StringBuffer[] returnBuff(List<File> filelist) {
		StringBuffer packages[] =  new StringBuffer[filelist.size()];
		
		Iterator<File> packs = filelist.iterator();
		int c = 0;
		while(packs.hasNext()){
			File pack = packs.next();
			StringBuffer parent = new StringBuffer(pack.getParent());
			parent.trimToSize();
			parent = new StringBuffer(parent.substring(parent.indexOf("\\smali\\") + 7));
			parent = new StringBuffer((parent.toString()).replace("\\", "."));
			packages[c] = parent;
			c++;			
		}

		List<String> list = Collections.synchronizedList(new LinkedList<String>());
		for (int i = 0; i < packages.length; i++) {
			if (!(list.contains(packages[i].toString()))) {
				list.add(packages[i].toString());
			}
		}

		String []npackages = list.toArray(new String[list.size()]);                    // packages 中存储 apk 自定义的包名 ======== StringBuffer 格式
		list.clear();
		list = null;
		
		StringBuffer packbuffer[] = new StringBuffer[npackages.length];
	       for(int i=0; i<npackages.length; i++) {  
	    	   packbuffer[i] = new StringBuffer(npackages[i]);
	    	   npackages[i] = null;      //释放
	        }  		       
	       npackages = null;
		
		return packbuffer;

	}
	
}
