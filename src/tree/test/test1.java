package tree.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import tree.database.MySQLCor;

public class test1 {

/*	
    StringBuffer write = new StringBuffer("D:\\APK\\exception.txt");
	try {
		writer.write("apkname: "++"\n");
		writer.write("apkname: "++"\n");
		writer.write("apkname: "++"\n");

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(write.toString())));
		e.printStackTrace();
		writer.write("e.getMessage(): "+e.getMessage()+"\n");
		writer.write("\n");

		writer.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
*/
	public static void main(String[] args) {
		String dburl = "jdbc:mysql://localhost:3306/apktreetest";
		MySQLCor mysql = new MySQLCor(dburl);
		
		File f = new File("D:\\APK\\smaliTest\\a.b.namespace_8\\smali\\a\\b\\namespace\\activity2.smali");	
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			StringBuffer line = new StringBuffer();
			StringBuffer classsig = new StringBuffer(),
					packagename = new StringBuffer();
			String linestr = "";
			while((linestr = br.readLine()) != null){
				line = new StringBuffer(linestr);
				line.trimToSize();
				
				if(line.toString().startsWith(".class")){
					StringBuffer sclassname = parseClassName(line);
					classsig = stanClassSig(sclassname);
					packagename = stanPackageName(classsig); 
				
					continue;
				}                       
				
				if(line.toString().startsWith(".method")){
					StringBuffer smethodname = parseMethodName(line);
					StringBuffer mname = new StringBuffer(smethodname.substring(0, smethodname.indexOf("(")));
					mname.trimToSize();
					
					StringBuffer returntype = new StringBuffer(smethodname.substring((smethodname.lastIndexOf(")") + 1)));
					returntype.trimToSize();
					returntype = parseRType(returntype);
					
					StringBuffer args1 = new StringBuffer(smethodname.substring((smethodname.indexOf("(") + 1), smethodname.lastIndexOf(")")));
					args1.trimToSize();
					
					//测试args1
					System.out.println("args1:"+args1);
					
					if((args1.length()==0) || ((smethodname.indexOf("(") + 1) == smethodname.lastIndexOf(")"))){
						args1 = new StringBuffer("()");
					}
					else{
						args1 = (new StringBuffer("(")).append(parseArgs(args1)).append(")");
					}
		
					StringBuffer methodsig = new StringBuffer();
					methodsig = methodsig.append(classsig).append(": ").append(returntype).append(" ").append(mname).append(args1);
					
					String insert1 = "insert into apktree (apkid,apk,packagename,classsig,methodsig,inoutput,beizhu) values (?,?,?,?,?,?,?)",
							insert2 = "insert into package (apkid,apk,packagename,inpackagename,beizhu) values (?,?,?,?,?)",
							insert3 = "insert into class (apkid,apk,classsig,inclasssig,beizhu) values (?,?,?,?,?)",
							insert4 = "insert into method (apkid,apk,methodsig,invokemethodsig,beizhu) values (?,?,?,?,?)";

					mysql.insert(insert1,1,"apk".toString(),packagename.toString(),classsig.toString(),methodsig.toString(),args1.toString(),"input");
					mysql.insert(insert1,1,"apk".toString(),packagename.toString(),classsig.toString(),methodsig.toString(),returntype.toString(),"output");

					while((!((linestr = ((br.readLine()).trim())).equals(".end method"))) && (linestr != null)){
/*							StringBuffer inclasssig = new StringBuffer(""), 
								inpackagename = new StringBuffer(""), 
								inclassname = new StringBuffer("");          //inclasssig、inpackagename要存的数据
						StringBuffer inmname = new StringBuffer(""), 
								inreturntype = new StringBuffer(""), 
								inargs = new StringBuffer(""), 
			
						StringBuffer invokeclassname = new StringBuffer(""), 
								invokemethodname = new StringBuffer("");
*/
						
						line = new StringBuffer(linestr);
						line.trimToSize();
						
						//测试line
						System.out.println("line:"+line);
						
						StringBuffer invokemethodsig = new StringBuffer();
						
						if(line.toString().startsWith("invoke")){
							StringBuffer invokeclassname = parseInvokeClassName(line);							
							//测试invokeclassname         invokeclassname: Landroid/content/Intent
//							System.out.println("invokeclassname:"+invokeclassname);
							
							StringBuffer invokemethodname = parseInvokeMethodName(line);
							//测试invokemethodname          <init>(Landroid/content/Context;Ljava/lang/Class;)V
//							System.out.println("invokemethodname:"+invokemethodname);

							
							StringBuffer inclasssig = stanClassSig(invokeclassname);
							//测试inclasssig
//							System.out.println("inclasssig:"+inclasssig);

							StringBuffer inpackagename = stanPackageName(inclasssig);
							//测试inpackagename
//							System.out.println("inpackagename:"+inpackagename);

							StringBuffer inclassname = stanClassName(inclasssig);
							//测试inclassname
//							System.out.println("inclassname:"+inclassname);

							
							StringBuffer inmname = new StringBuffer(invokemethodname.substring(0, invokemethodname.indexOf("(")));
							inmname.trimToSize();
							
							StringBuffer inreturntype = new StringBuffer(invokemethodname.substring((invokemethodname.lastIndexOf(")") + 1)));
							inreturntype.trimToSize();
							inreturntype = parseRType(inreturntype);
							
							StringBuffer inargs = new StringBuffer(invokemethodname.substring((invokemethodname.indexOf("(") + 1), invokemethodname.lastIndexOf(")")));
							inargs.trimToSize();
							
							//测试inargs
							System.out.println("inargs:"+inargs);

							if((inargs.length() == 0) || ((invokemethodname.indexOf("(") + 1) == invokemethodname.lastIndexOf(")"))){
								inargs = new StringBuffer("()");
							}
							else{
								inargs = (new StringBuffer("(")).append(parseArgs(inargs)).append(")");
							}
							
							invokemethodsig = invokemethodsig.append(inclasssig).append(": ").append(inreturntype).append(" ").append(inmname).append(inargs);
							
							mysql.insert(insert1,1,"apk".toString(),inpackagename.toString(),inclasssig.toString(),invokemethodsig.toString(),inargs.toString(),"input");								
							mysql.insert(insert1,1,"apk".toString(),inpackagename.toString(),inclasssig.toString(),invokemethodsig.toString(),inreturntype.toString(),"output");		

							mysql.insert(insert2, 1,"apk".toString(),packagename.toString(),inpackagename.toString(),"");

							mysql.insert(insert1,1,"apk".toString(),packagename.toString(),inclasssig.toString(),invokemethodsig.toString(),inargs.toString(),"input");		
							mysql.insert(insert1,1,"apk".toString(),packagename.toString(),inclasssig.toString(),invokemethodsig.toString(),inreturntype.toString(),"output");	

							mysql.insert(insert3,1,"apk".toString(),classsig.toString(),inclasssig.toString(),""); 

							mysql.insert(insert1,1,"apk".toString(),packagename.toString(),classsig.toString(),invokemethodsig.toString(),inargs.toString(),"input");		
							mysql.insert(insert1,1,"apk".toString(),packagename.toString(),classsig.toString(),invokemethodsig.toString(),inreturntype.toString(),"output");		

							mysql.insert(insert4,1,"apk".toString(),methodsig.toString(),invokemethodsig.toString(),"");

						}
						
					}
					
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}

		
	}
	private static StringBuffer stanClassName(StringBuffer inclasssig) {
		inclasssig = new StringBuffer(inclasssig.substring((inclasssig.lastIndexOf(".") + 1)));
		inclasssig.trimToSize();
				
		return inclasssig;
	}

	private static StringBuffer parseInvokeMethodName(StringBuffer line) {
		try {
			int index = line.indexOf("->");

			line = new StringBuffer(line.substring(index+2));
			line.trimToSize();
			
			if(line.toString().endsWith(";")){
				line = new StringBuffer(line.substring(0, ((line.length())-1)));
			}

		} catch (Exception e) {
		}		
		return line;
	}

	private static StringBuffer parseInvokeClassName(StringBuffer line) {
		try {
			int index1 = line.indexOf("},");
			int index2 = line.indexOf("->");
			
			//变动的地方
			line = new StringBuffer(line.substring((index1 + 3), (index2 - 1)));
			line.trimToSize();
						
		} catch (Exception e) {
		}
					
		return line;
	}

	private static Object parseArgs(StringBuffer args) {
//		inargs:    Landroid/content/Context;ILandroid/database/Cursor;[Ljava/lang/String;[I
		
		int index = 0;
		StringBuffer stanargs = new StringBuffer();
				
		System.out.println("args.length():" + args.length());
		for(int i = 0; i < args.length(); i ++){
			StringBuffer letter = new StringBuffer(String.valueOf(args.charAt(i)));
			String letstr = letter.toString();          //单个字母的字符串表示
			
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
				i = i + 1;
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

	private static boolean isArrayLetter(String letter) {
		if("[".equals(letter))
			return true;
		return false;
	}

	private static boolean isObjectLetter(String letter) {
		if("L".equals(letter))
			return true;
		return false;
	}

	private static boolean isBaseLetter(String letter) {
		if(("Z".equals(letter)) || ("B".equals(letter)) || ("S".equals(letter)) || ("C".equals(letter))
				 || ("I".equals(letter)) || ("J".equals(letter)) || ("F".equals(letter)) || ("D".equals(letter)))
			return true;
		return false;
	}

	private static boolean isArray(String returntype) {
		if(returntype.startsWith("["))
			return true;
		return false;
	}

	private static boolean isObject(String returntype) {
		if(returntype.startsWith("L"))
			return true;
		return false;
	}

	private static boolean isBaseType(StringBuffer returntype) {
		if(returntype.length() == 1)
			return true;	
		return false;
	}

	private static String parseBaseType(String returntype) {
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

	private static StringBuffer parseRType(StringBuffer returntype) {
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

	private static StringBuffer parseMethodName(StringBuffer line) {
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

	private static StringBuffer stanPackageName(StringBuffer classsig) {
		if(classsig.lastIndexOf(".") < 0){
			return classsig;
		}

		classsig = new StringBuffer(classsig.substring(0, classsig.lastIndexOf(".")));
		classsig.trimToSize();
		return classsig;
	}

	private static StringBuffer stanClassSig(StringBuffer sclassname) {
		String str = sclassname.substring(1);
		sclassname = new StringBuffer(str.replace("/", "."));	
		sclassname.trimToSize();

		return sclassname;
	}

	private static StringBuffer parseClassName(StringBuffer line) {
		StringBuffer classname = new StringBuffer((line.substring(7, (line.length()) - 1)));
		classname.trimToSize();
		if (classname.toString().contains(" ")){
			int index = classname.lastIndexOf(" ");
			classname = new StringBuffer(classname.substring(index + 1));
		}
		
		return classname;
	}

	
	
	
	
	
}
