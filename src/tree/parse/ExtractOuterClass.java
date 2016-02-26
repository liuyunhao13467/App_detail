package tree.parse;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ExtractOuterClass {
	private File file = null;
	
	private List<File> filelist;	
	String filestr[] = null;
	File filearr[] = null;

	public ExtractOuterClass() {
		this.filelist = Collections.synchronizedList(new LinkedList<File>());
	}
	
	public ExtractOuterClass(File file){
		this.filelist = Collections.synchronizedList(new LinkedList<File>());
        this.file = file;	
	}
	
	public List<File> extractClassFile(){
		file = listSmaliFile();
		filelist = listSmali();
		Iterator<File> it = filelist.iterator();
		
		int mark = 1;
		while(it.hasNext()){
			File f = it.next();
			String docname = f.getName();
			String doc = parse(docname);
			
			if((docname.contains("$"))||(docname.equals("R.smali"))||(docname.equals("BuildConfig.smali"))){             //去掉内部类$、去掉配置文件
				it.remove();
			}
			
			else if((doc.length() == 1)&&(Character.isLetter(doc.charAt(0)))){
				mark = 0;				
				break;
			}

			else if((doc.length() == 2)&&((isSameCharacter(doc))||(isContinuityCharacter(doc)))){
				mark = 0;
				break;
			}
			else if((doc.length() == 3)&&(isSameCharacter(doc))){
				mark = 0;
				break;
			}

			else if((doc.length() == 4)&&(isSameCharacter(doc))){
				mark = 0;
				break;
			}
			
			else if((doc.length() == 5)&&(isSameCharacter(doc))){
				mark = 0;
				break;
			}			
			else 
				continue;
		}
		
		if(mark == 0){
			if(filelist.isEmpty()){
				return filelist;
			}			
			filelist.clear();
			System.out.println("filelist是否为空：-------"+filelist.isEmpty());
		}		
		return filelist;
	}
	
	private File listSmaliFile() {
		filearr = file.listFiles();
		if(filearr == null)
			return null;
		for(File f:filearr){
			if((f.isDirectory())&&("smali".equals(f.getName()))){
				file = f;
				return file;
			}
		}		
		return null;
	}

	private String parse(String docname) {
		int i = docname.lastIndexOf(".smali");
		docname = docname.substring(0,i);	
		return docname;
	}
	
	// 学会用一定的编程技巧
	private boolean isSameCharacter(String s){
		String character = "", replace = "", test = "";
		
		//如果不区分大小写，可以添加下面这句
		//s = s.toUpperCase();或者 s = s.toLowerCase();
	     character = s.substring(0,1);
		 replace = "";
		 test = s.replace(character, replace);
		if("".equals(test))
		return true;
		return false;
	}
	
	private static boolean isContinuityCharacter(String s) {
		boolean continuity = true;
		char[] data = s.toCharArray();
		for (int i = 0; i < data.length - 1; i++) {
			int a = (int)data[i];
			int b = (int)data[i + 1];
			continuity = continuity && (a + 1 == b || a - 1 == b);
		}
		return continuity;
	}

	public List<File> listSmali(){
		filearr = file.listFiles();
		if(filearr == null)
			return null;
		for(File f:filearr){
			if((f.isFile())&&((f.getName()).endsWith(".smali"))){
				filelist.add(f);				
			}
			else if(f.isDirectory()){
				file = f;
				listSmali();				
			}
			else
				System.out.println("error occured...............");
		}
		return filelist;
	}

	//测试
	public static void main(String []args){
		String tfn ="E:\\Android\\反编译apk\\软件相关\\jar文件\\jp";
		//E:\Android\反编译apk\软件相关\jar文件\jp
		File tfile = new File(tfn);
		Object o[] = null;
		ExtractOuterClass outerclass = new ExtractOuterClass(tfile);
		List<File> tlf = null;
		tlf = outerclass.extractClassFile();
//		System.out.println("--------------"+tfile.getName());
		o = tlf.toArray();
		for(Object object:o){
			System.out.println(object.toString());
		}
	}
}
