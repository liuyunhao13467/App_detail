package tree.jar.count;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tree.database.MySQLCor;
import tree.parse.ExtractOuterClass;

public class OneParser {
	private File file;
	private MySQLCor mysql;	
	int count;
	private List<File> filelist;
	
	String[] javapack;
	String[] androidpack;
	
	public OneParser() {
	}

	public OneParser(File file, MySQLCor mysql, int j) {
		this.file = file;
		this.mysql = mysql;
		this.count = j;
	}

	public OneParser(File file2, MySQLCor mysql2, int j,String[] javapack, String[] androidpack) {
		this.file = file2;
		this.mysql = mysql2;
		this.count = j;
		this.javapack = javapack;
		this.androidpack = androidpack;	
		
	}

	public int parse() {
		String insert1 = "insert into android1 (apkid,apk,inpackagename,method_invoke,beizhu) values (?,?,?,?,?)",
				insert2 = "insert into java1 (apkid,apk,inpackagename,method_invoke,beizhu) values (?,?,?,?,?)",
				insert3 = "insert into third1 (apkid,apk,inpackagename,method_invoke,beizhu) values (?,?,?,?,?)";

		int mark = listFiles();

		if(mark == 0){
			return 0;
		}		

		
//		Parent====D:\APK\smaliTest\a.b.namespace_8\smali\a\b\namespace          ========activity2.smali
		
/*		Parent====D:\APK\smaliTest\a.b.namespace_8\smali\a\b\namespace
				a\b\namespace
				replace后====a.b.namespace                                                                         */
		
		String ownpack[] = listOwnPackages();		
				
		StringBuffer apk = new StringBuffer(file.getName());
		Iterator<File> it = filelist.iterator();
		while(it.hasNext()){
			File f = it.next();	
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				StringBuffer line = new StringBuffer();
				String linestr = "";
				while((linestr = br.readLine()) != null){
					linestr = linestr.trim();
					line = new StringBuffer(linestr);
					
					StringBuffer invokemethodsig = new StringBuffer();
					if (linestr.startsWith("invoke")) {
						StringBuffer invokeclassname = parseInvokeClassName(line);           //Lcom/adobe/flashruntime/shared/VideoView
						StringBuffer invokemethodname = parseInvokeMethodName(line);

						StringBuffer inclasssig = stanClassSig(invokeclassname);           //com.adobe.flashruntime.shared.VideoView
						StringBuffer inpackagename = stanPackageName(inclasssig); // 这里对
																					// inpackagename
																					// 进行判断
						StringBuffer inclassname = stanClassName(inclasssig);

						StringBuffer inmname = new StringBuffer(
								invokemethodname.substring(0,
										invokemethodname.indexOf("(")).trim());

						StringBuffer inreturntype = new StringBuffer(invokemethodname.substring((invokemethodname.lastIndexOf(")") + 1)).trim());
						
						inreturntype = parseRType(inreturntype);

						// inargs:Landroid/content/Context;ILandroid/database/Cursor;[Ljava/lang/String;[I
						// 出错点
						StringBuffer inargs = new StringBuffer(invokemethodname.substring((invokemethodname.indexOf("(") + 1),invokemethodname.lastIndexOf(")")).trim());

						if ((inargs.length() == 0)
								|| ((invokemethodname.indexOf("(") + 1) == invokemethodname
										.lastIndexOf(")"))) {
							inargs = new StringBuffer("()");
						} else {
							inargs = (new StringBuffer("(")).append(
									parseArgs(inargs)).append(")");
						}

						// 判断 method 类型
						if (isAndroid(inpackagename)) {
							invokemethodsig = invokemethodsig.append("android")
									.append(": ").append(inclasssig)
									.append(": ").append(inreturntype)
									.append(" ").append(inmname).append(inargs);
							mysql.insert(insert1, count, apk.toString(),
									inpackagename.toString(),
									invokemethodsig.toString(), "android");

						} else if (isJava(inpackagename)) {
							invokemethodsig = invokemethodsig.append("java")
									.append(": ").append(inclasssig)
									.append(": ").append(inreturntype)
									.append(" ").append(inmname).append(inargs);
							mysql.insert(insert2, count, apk.toString(),
									inpackagename.toString(),
									invokemethodsig.toString(), "java");

						} else if (isOwn(inpackagename, ownpack)) {
						}

						else {
							invokemethodsig = invokemethodsig
									.append("thirdpackage").append(": ")
									.append(inclasssig).append(": ")
									.append(inreturntype).append(" ")
									.append(inmname).append(inargs);
							mysql.insert(insert3, count, apk.toString(),
									inpackagename.toString(),
									invokemethodsig.toString(), "thirdpackage");

						}
					}
					}						
			} catch (Exception e) {
			    StringBuffer write = new StringBuffer("E:\\apk\\exception.txt");
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(write.toString()),true));
					writer.write("apkname: "+apk.toString());
					writer.newLine();
					writer.write("smalifilename: "+f.getName());
					writer.newLine();

					writer.write("e.getMessage(): "+e.getMessage());
					writer.newLine();
					e.printStackTrace();

					writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}	
		}
		// 释放链表空间
		filelist.clear();
		filelist = null;
				
		return 1;
	}

	private boolean isJava(StringBuffer inpackagename) {		
		for(int i = 0; i < javapack.length; i ++){
			if((inpackagename.toString()).equals(javapack[i])){
				return true;
			}			
		}
		return false;
	}

	private boolean isAndroid(StringBuffer inpackagename) {		
		for(int i = 0; i < androidpack.length; i ++){
			if((inpackagename.toString()).equals(androidpack[i])){
				return true;
			}			
		}
		return false;
	}

	private boolean isOwn(StringBuffer inpackagename, String[] ownpack) {
		for(int i = 0; i < ownpack.length; i ++){
			if((inpackagename.toString()).equals(ownpack[i])){
				return true;
			}			
		}	
		return false;
	}

	private String[] listOwnPackages() {
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
				
		return npackages;
	}

	//com.adobe.flashruntime.shared.VideoView
	private StringBuffer stanClassName(StringBuffer inclasssig) {
		inclasssig = new StringBuffer(inclasssig.substring((inclasssig.lastIndexOf(".") + 1)));
		inclasssig.trimToSize();
				
		return inclasssig;
	}

	private StringBuffer parseInvokeMethodName(StringBuffer line) {
		try {
			int index = line.indexOf("->");

			line = new StringBuffer(line.substring(index+2).trim());
			
			if(line.toString().endsWith(";")){
				line = new StringBuffer(line.substring(0, ((line.length())-1)));
			}

		} catch (Exception e) {
		}		
		return line;
	}

	private StringBuffer parseInvokeClassName(StringBuffer line) {
		try {
			int index1 = line.indexOf("},");
			int index2 = line.indexOf("->");
			
			line = new StringBuffer(line.substring((index1 + 2), (index2 - 1)).trim());
						
		} catch (Exception e) {
		}
					
		return line;
	}

	private Object parseArgs(StringBuffer args) {
		int index = 0;
		StringBuffer stanargs = new StringBuffer();
		
		for(int i = 0; i < args.length(); i ++){
			StringBuffer letter = new StringBuffer(String.valueOf(args.charAt(i)));
			String letstr = letter.toString();
			
			if(isBaseLetter(letstr)){
				StringBuffer basestr = new StringBuffer(parseBaseType(letstr));
				
				if(i == (args.length() - 1))
					stanargs = stanargs.append(basestr);
				else {
					stanargs = stanargs.append(basestr).append(",");
				}
			}
			
			else if(isObjectLetter(letstr)){
				index = args.indexOf(";", i);
				letter = new StringBuffer(args.substring(i, index));
				letter = stanClassSig(letter);

				i = index;
				if(i == (args.length() - 1)){
					stanargs = stanargs.append(letter);
				}else{
					stanargs = stanargs.append(letter).append(",");
				}			
			}
			
			else if(isArrayLetter(letstr)){
				int count = 1;
				i ++ ;
				letter =  new StringBuffer(String.valueOf(args.charAt(i)));
				letstr = letter.toString();

				while(isArrayLetter(letstr)){
					count ++;
					i ++ ;
					letter = new StringBuffer(String.valueOf(args.charAt(i)));
					letstr = letter.toString();

				}
				
				if(isBaseLetter(letstr)){
					letter = new StringBuffer(parseBaseType(letstr));
					for(int j = 0; j < count; j ++){
						letter = letter.append("[]");					
					}
					if(i == (args.length() - 1)){
						stanargs = stanargs.append(letter);
					}
					else{
						letter = letter.append(",");
						stanargs = stanargs.append(letter);
					}
				}

				else if(isObjectLetter(letstr)){
					index = args.indexOf(";", i);
					letter = new StringBuffer(args.substring(i, index));
					letter = stanClassSig(letter);
					for(int j = 0; j < count; j ++){
						letter = letter.append("[]");					
					}					
					i = index;
					if(i == (args.length() - 1)){
						stanargs = stanargs.append(letter);
					}
					else{
						letter = letter.append(",");
						stanargs = stanargs.append(letter);
					}				
				}
				else
					continue;
			}
			else
				continue;			
		}
		return stanargs;
	}

	private boolean isArrayLetter(String letter) {
		if("[".equals(letter))
			return true;
		return false;
	}

	private boolean isObjectLetter(String letter) {
		if("L".equals(letter))
			return true;
		return false;
	}

	private boolean isBaseLetter(String letter) {
		if(("Z".equals(letter)) || ("B".equals(letter)) || ("S".equals(letter)) || ("C".equals(letter))
				 || ("I".equals(letter)) || ("J".equals(letter)) || ("F".equals(letter)) || ("D".equals(letter)))
			return true;
		return false;
	}

	private boolean isArray(String returntype) {
		if(returntype.startsWith("["))
			return true;
		return false;
	}

	private boolean isObject(String returntype) {
		if(returntype.startsWith("L"))
			return true;
		return false;
	}

	private boolean isBaseType(StringBuffer returntype) {
		if(returntype.length() == 1)
			return true;	
		return false;
	}

	private String parseBaseType(String returntype) {
		if("V".equals(returntype))
			return "void";
		if("Z".equals(returntype))
			return "boolean";
		if("B".equals(returntype))
			return "byte";
		if("S".equals(returntype))
			return "short";
		if("C".equals(returntype))
			return "char";
		if("I".equals(returntype))
			return "int";
		if("J".equals(returntype))
			return "long";
		if("F".equals(returntype))
			return "float";
		if("D".equals(returntype))
			return "double";

		return returntype;
	}

	private StringBuffer parseRType(StringBuffer returntype) {
		int count = 0;
		if(isBaseType(returntype)){
			returntype = new StringBuffer(parseBaseType(returntype.toString()));
			return returntype;
			
		}
		
		else if(isObject(returntype.toString())){
			returntype = stanClassSig(returntype);
			return returntype;

		}
		
		else if(isArray(returntype.toString())){
			count = (returntype.lastIndexOf("[")) + 1;
			returntype =  new StringBuffer(returntype.substring(count));
			returntype.trimToSize();
			
			if(isBaseType(returntype)){
				returntype = new StringBuffer(parseBaseType(returntype.toString()));
				for(int i = 0; i < count; i ++){
					returntype = returntype.append("[]");					
				}
				return returntype;			
			}
			
			if(isObject(returntype.toString())){
				returntype = stanClassSig(returntype);
				for(int i = 0; i < count; i ++){
					returntype = returntype.append("[]");					
				}
				return returntype;				
			}
		}
		
		else{
			System.out.println("error type！");
		}	
				
		return returntype;
	}

	private StringBuffer parseMethodName(StringBuffer line) {
		StringBuffer methodname = new StringBuffer(line.substring(8));
		methodname.trimToSize();
		if(methodname.toString().contains(" ")){
			int index = methodname.lastIndexOf(" ");
			methodname = new StringBuffer(methodname.substring(index+1));
			
		}
		if(methodname.toString().endsWith(";")){
			methodname = new StringBuffer(methodname.substring(0, ((methodname.length())-1)));				
			
		}
			
		return methodname;
	}

	//com.adobe.flashruntime.shared.VideoView
	private StringBuffer stanPackageName(StringBuffer classsig) {
		if(classsig.lastIndexOf(".") < 0){
			return classsig;
		}

		classsig = new StringBuffer(classsig.substring(0, classsig.lastIndexOf(".")));
		classsig.trimToSize();
		return classsig;
	}

	//Lcom/adobe/flashruntime/shared/VideoView
	private StringBuffer stanClassSig(StringBuffer sclassname) {
		String str = sclassname.substring(1);
		sclassname = new StringBuffer(str.replace("/", "."));	

		return sclassname;
	}

	private StringBuffer parseClassName(StringBuffer line) {
		StringBuffer classname = new StringBuffer((line.substring(7, (line.length()) - 1)));
		classname.trimToSize();
		if (classname.toString().contains(" ")){
			int index = classname.lastIndexOf(" ");
			classname = new StringBuffer(classname.substring(index + 1));
		}
		
		return classname;
	}

	private int listFiles(){
		ExtractOuterClass outcla = new ExtractOuterClass(file);
		filelist = outcla.extractClassFile();	
		
		if(filelist.isEmpty())
			return 0;
		return 1;
	}
}
