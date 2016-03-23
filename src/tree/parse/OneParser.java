package tree.parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogUtils;

import tree.database.MySQLCor;
import tree.parse.entity.ApkEntity;
import tree.parse.entity.CallBase;
import tree.parse.entity.Callee;
import tree.parse.entity.Caller;

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

	public OneParser(File file2, MySQLCor mysql2, int j, String[] javapack,
			String[] androidpack) {
		this.file = file2;
		this.mysql = mysql2;
		this.count = j;
		this.javapack = javapack;
		this.androidpack = androidpack;
	}

	public int parseSimpleSeperate() {
		String ownpack[] = null;
		StringBuffer apk = null;
		Iterator<File> it = null;
		ApkEntity apkInfo =null;
		Logger log = Logger.getLogger("parseSimple");
		
		//将解析出来的全部信息，存入数据库中。（16个参数）
		String inWholeInfo = "insert into call_info_wang (callerApkVersion,callerApkName,callerPackageName,callerClassName,callerMethodName" +
				",callerMethodType,callerRreturnType,callerParameter," +
				"calleeApkVersion,calleeApkName,calleePackageName,calleeClassName,calleeMethodName,calleeMethodType," +
				"calleeRreturnType,calleeParameter) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; 
		
		int mark = listFiles();

		if (mark == 0) {
			return 0;
		}
		
		apkInfo = ApkEntity.getInstance();
		 //获得 apk 名字， 输入方法名， 输出方法名
		ownpack = listOwnPackages();
		apk = new StringBuffer(file.getName());
		String []apkNameAndVersion = stanAppVersionAndName(apk);
		
		if(apkNameAndVersion != null){
			apkInfo.setApkName(apkNameAndVersion[0]);
			apkInfo.setApkVersion(apkNameAndVersion[1]);
		}else{
			return 0;
		}
		
		apkInfo.setCallerList(new LinkedList<Caller>());
		
		// 先看看是否apk已经存在于 数据库中。
		String exist = "select callerApkName from call_info_wang where callerApkName = ? and callerApkVersion = ?";
		ResultSet ex = mysql.selectApkID(exist, apkNameAndVersion[0],apkNameAndVersion[1]);
		try {
			if (ex.next()) {
				System.out.println("apk已存在");
				return 0;
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		//TODO apk版本信息。
		
		log.log(Level.INFO, "the apk name is : " + apk.toString());
		
		it = filelist.iterator();
		
		// 对每个文件中的字符串进行处理，主要讲字符串中一个一个单位进行分离。
		while (it.hasNext()) {
			File f = it.next();

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)));
				StringBuffer line = new StringBuffer();
				StringBuffer classsig = new StringBuffer(), packagename = new StringBuffer();
				String linestr = "";
				log.log(Level.INFO, "the file name is : " + f.getName());
				
				while ((linestr = br.readLine()) != null) {
					line = new StringBuffer(linestr);
					line.trimToSize();

					if (line.toString().startsWith(".class")) {
						StringBuffer sclassname = parseClassName(line);
						classsig = stanClassSig(sclassname);
						packagename = stanPackageName(classsig);

						continue;
					}

					if (line.toString().startsWith(".method")) {
						//TODO 
						Caller callerTmp = new Caller();
						
						StringBuffer smethodname = parseMethodName(line);
						StringBuffer mname = new StringBuffer(smethodname
								.substring(0, smethodname.indexOf("(")));

						StringBuffer returntype = new StringBuffer(smethodname
								.substring((smethodname.lastIndexOf(")") + 1)));
						returntype.trimToSize();
						returntype = parseRType(returntype);

						StringBuffer args = new StringBuffer(smethodname
								.substring((smethodname.indexOf("(") + 1),
										smethodname.lastIndexOf(")")));
						args.trimToSize();

						if ((args.length() == 0)
								|| ((smethodname.indexOf("(") + 1) == smethodname
										.lastIndexOf(")"))) {
							args = new StringBuffer("()");

							// 无参数 处理 不插参数 不插method参数
						} else {
							args = (new StringBuffer("(")).append(
									parseArgs(args)).append(")");

							// 有参数处理 split 循环插入 直到插完为止
						}

						StringBuffer methodsig = new StringBuffer();
						methodsig = methodsig.append(apk.toString()).append(
								": ").append(classsig).append(": ").append(
								returntype).append(" ").append(mname).append(
								args);
						
						//向caller 中存入信息。
						setCallBase(callerTmp, packagename, classsig, mname, new StringBuffer("own"), returntype, args);

                        //按照属于自己的包，插入数据结构。
						callerTmp.setCalleeList(new LinkedList<Callee>());
						apkInfo.getCallerList().add(callerTmp);
						

						//TODO  插每一个 被调方法
						while ((!((linestr = ((br.readLine()).trim()))
								.equals(".end method")))
								&& (linestr != null)) {

							//创建被调用者信息。
							Callee  calleeTmp = new Callee();
							line = new StringBuffer(linestr);
							line.trimToSize();

							StringBuffer invokemethodsig = new StringBuffer();

							if (line.toString().startsWith("invoke")) {
								StringBuffer invokeclassname = parseInvokeClassName(line);
								StringBuffer invokemethodname = parseInvokeMethodName(line);

								line = null;

								StringBuffer inclasssig = stanClassSig(invokeclassname);
								StringBuffer inpackagename = stanPackageName(inclasssig); // 这里对
																							// inpackagename
																							// 进行判断
								StringBuffer inclassname = stanClassName(inclasssig);

								invokeclassname = null;

								StringBuffer inmname = new StringBuffer(
										invokemethodname.substring(0,
												invokemethodname.indexOf("(")));

								// 特殊情况clone
								if ("clone".equals(inmname)) {
									inpackagename = new StringBuffer(
											inpackagename.substring(1));
									inclasssig = new StringBuffer(inclasssig
											.substring(1));
								}

								StringBuffer inreturntype = new StringBuffer(
										invokemethodname
												.substring((invokemethodname
														.lastIndexOf(")") + 1)));
								inreturntype.trimToSize();
								inreturntype = parseRType(inreturntype);

								StringBuffer inargs = new StringBuffer(
										invokemethodname
												.substring(
														(invokemethodname
																.indexOf("(") + 1),
														invokemethodname
																.lastIndexOf(")")));
								inargs.trimToSize();

								if ((inargs.length() == 0)
										|| ((invokemethodname.indexOf("(") + 1) == invokemethodname
												.lastIndexOf(")"))) {
									inargs = new StringBuffer("()");
								} else {
									inargs = (new StringBuffer("(")).append(
											parseArgs(inargs)).append(")");
								}

								if (isAndroid(inpackagename)) {
									invokemethodsig = invokemethodsig.append(
											"android").append(": ").append(
											inclasssig).append(": ").append(
											inreturntype).append(" ").append(
											inmname).append(inargs);
								// TODO 
									setCallBase(calleeTmp, inpackagename, inclasssig, inmname, new StringBuffer("android"), inreturntype, inargs);
									callerTmp.getCalleeList().add(calleeTmp);
								}

								else if (isJava(inpackagename)) {
									invokemethodsig = invokemethodsig.append(
											"java").append(": ").append(
											inclasssig).append(": ").append(
											inreturntype).append(" ").append(
											inmname).append(inargs);
									//  存入数据结构
									setCallBase(calleeTmp, inpackagename, inclasssig, inmname, new StringBuffer("java"), inreturntype, inargs);
									callerTmp.getCalleeList().add(calleeTmp);

								}

								else if (isOwn(inpackagename, ownpack)) {
									invokemethodsig = invokemethodsig.append(
											apk.toString()).append(": ")
											.append(inclasssig).append(": ")
											.append(inreturntype).append(" ")
											.append(inmname).append(inargs);
									
									setCallBase(calleeTmp, inpackagename, inclasssig, inmname, new StringBuffer("own"), inreturntype, inargs);
									callerTmp.getCalleeList().add(calleeTmp);
								}

								else {
									invokemethodsig = invokemethodsig.append(
											"thirdpackage").append(": ")
											.append(inclasssig).append(": ")
											.append(inreturntype).append(" ")
											.append(inmname).append(inargs);
								
									setCallBase(calleeTmp, inpackagename, inclasssig, inmname, new StringBuffer("thirdpackage"), inreturntype, inargs);
									callerTmp.getCalleeList().add(calleeTmp);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				StringBuffer write = new StringBuffer(
						"F:\\app_detail\\exception.txt");
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							new File(write.toString()), true));
					writer.write("apkname: " + apk.toString());
					writer.newLine();
					writer.write("smalifilename: " + f.getName());
					writer.newLine();

					writer.write("e.getMessage(): " + e.getMessage());
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
		//TODO　进行数据库操作，数据结构存储，集体进行数据库操作，批次处理。
		mysql.insertWholeBasicInfo(apkInfo, inWholeInfo);
		
		
		//TODO 清空apk相关的信息。
		apkInfo.clear();
		return 1;
		
	}

	public int parse() {
		String inapk = "insert into app_info (apkid,apkname,beizhu,tag) values (?,?,?,?)",
		// 放在repeat里面

		inpara = "insert into parameter (apkid, mid, ptype, pname, type, beizhu, tag) values (?,?,?,?,?,?,?)",

		inme = "insert into method (mid,apkid,methodname,type,pid,beizhu,tag) values (?,?,?,?,?,?,?)",

		inan = "insert into invoke_android (apkid,method,method_invoke,method_type) "
				+ "SELECT ?,?,?,? FROM DUAL WHERE NOT EXISTS(SELECT method,method_invoke FROM invoke_android "
				+ "WHERE method = ? and method_invoke = ?)",

		in = "insert into invoke (apkid,mid,inapkid,inmid) values (?,?,?,?)",

		selectm = "select max(mid) from method", selectp = "select max(pid) from parameter", selectr = "select max(rid) from returntype",

		selectpid = "select pid from parameter where parameter = ?", selectrid = "select rid from returntype where returntype = ?", selectmid = "select mid from method where methodname = ?";

		/*
		 * String insert1 =
		 * "INSERT INTO method_input_output (method,input,output,beizhu) " +
		 * "SELECT ?,?,?,? FROM DUAL WHERE NOT EXISTS(SELECT method FROM method_input_output WHERE method = ?)"
		 * ;
		 */
		// insert6插入自动判别去重

		String ownpack[] = null;
		StringBuffer apk = null;
		int mark = listFiles();

		if (mark == 0) {
			return 0;
		}

		// Parent====D:\APK\smaliTest\a.b.namespace_8\smali\a\b\namespace
		// ========activity2.smali

		/*
		 * Parent====D:\APK\smaliTest\a.b.namespace_8\smali\a\b\namespace
		 * a\b\namespace replace后====a.b.namespace
		 */

		ownpack = listOwnPackages();

		apk = new StringBuffer(file.getName());

		// 判断 apkname 是否在数据库中已经存在
		String exist = "select apkid from app_info where apkname = ?";
		ResultSet ex = mysql.select(exist, apk.toString());
		try {
			if (ex.next()) {
				System.out.println("apk已存在");
				return 0;
			}
		} catch (SQLException e2) {
			e2.printStackTrace();
		}

		// 插主表
		int pre_count = count;
		mysql.insert(inapk, pre_count, apk.toString(), null, null);

		Iterator<File> it = filelist.iterator();
		while (it.hasNext()) {
			File f = it.next();

			try {
				System.out.println("1st while ,file is : " + f.getName());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)));
				StringBuffer line = new StringBuffer();
				StringBuffer classsig = new StringBuffer(), packagename = new StringBuffer();
				String linestr = "";
				while ((linestr = br.readLine()) != null) {
					System.out.println("2 while : " + linestr);
					line = new StringBuffer(linestr);
					line.trimToSize();

					if (line.toString().startsWith(".class")) {
						StringBuffer sclassname = parseClassName(line);
						classsig = stanClassSig(sclassname);
						packagename = stanPackageName(classsig);

						continue;
					}

					if (line.toString().startsWith(".method")) {
						StringBuffer smethodname = parseMethodName(line);
						StringBuffer mname = new StringBuffer(smethodname
								.substring(0, smethodname.indexOf("(")));

						StringBuffer returntype = new StringBuffer(smethodname
								.substring((smethodname.lastIndexOf(")") + 1)));
						returntype.trimToSize();
						returntype = parseRType(returntype);

						StringBuffer args = new StringBuffer(smethodname
								.substring((smethodname.indexOf("(") + 1),
										smethodname.lastIndexOf(")")));
						args.trimToSize();

						if ((args.length() == 0)
								|| ((smethodname.indexOf("(") + 1) == smethodname
										.lastIndexOf(")"))) {
							args = new StringBuffer("()");

							// 无参数 处理 不插参数 不插method参数
						} else {
							args = (new StringBuffer("(")).append(
									parseArgs(args)).append(")");

							// 有参数处理 split 循环插入 直到插完为止
						}

						StringBuffer methodsig = new StringBuffer();
						methodsig = methodsig.append(apk.toString()).append(
								": ").append(classsig).append(": ").append(
								returntype).append(" ").append(mname).append(
								args);

						// 确定 mid 的值 最后一个mid+1
						int mid = (selectMax(selectm)) + 1;

						// 插returntype，且记录pid, 插method
						mysql.insert(inpara, count, mid, returntype.toString(),
								null, "out", null, null);

						int pid = selectMax(selectp);

						mysql.insert(inme, mid, count, methodsig.toString(),
								"own", pid, null, null);

						if (!(args.toString().equals("()"))) {
							String argss = args.substring(1,
									(args.length()) - 1);
							String a[] = argss.split(",");

							for (int i = 0; i < a.length; i++) {
								mysql.insert(inpara, count, mid, a[i], null,
										"in", null, null);
								pid = selectMax(selectp);

								mysql.insert(inme, mid, count, methodsig
										.toString(), "own", pid, null, null);
							}
						}

						// 插每一个 被调方法------需要盼重
						while ((!((linestr = ((br.readLine()).trim()))
								.equals(".end method")))
								&& (linestr != null)) {

							line = new StringBuffer(linestr);
							System.out.println("2.2 while : " + line);
							line.trimToSize();

							StringBuffer invokemethodsig = new StringBuffer();

							if (line.toString().startsWith("invoke")) {
								StringBuffer invokeclassname = parseInvokeClassName(line);
								StringBuffer invokemethodname = parseInvokeMethodName(line);

								line = null;

								StringBuffer inclasssig = stanClassSig(invokeclassname);
								StringBuffer inpackagename = stanPackageName(inclasssig); // 这里对
																							// inpackagename
																							// 进行判断
								StringBuffer inclassname = stanClassName(inclasssig);

								invokeclassname = null;

								StringBuffer inmname = new StringBuffer(
										invokemethodname.substring(0,
												invokemethodname.indexOf("(")));

								// 特殊情况clone
								if ("clone".equals(inmname)) {
									inpackagename = new StringBuffer(
											inpackagename.substring(1));
									inclasssig = new StringBuffer(inclasssig
											.substring(1));
								}

								StringBuffer inreturntype = new StringBuffer(
										invokemethodname
												.substring((invokemethodname
														.lastIndexOf(")") + 1)));
								inreturntype.trimToSize();
								inreturntype = parseRType(inreturntype);

								// inargs:Landroid/content/Context;ILandroid/database/Cursor;[Ljava/lang/String;[I
								// 出错点
								StringBuffer inargs = new StringBuffer(
										invokemethodname
												.substring(
														(invokemethodname
																.indexOf("(") + 1),
														invokemethodname
																.lastIndexOf(")")));
								inargs.trimToSize();

								if ((inargs.length() == 0)
										|| ((invokemethodname.indexOf("(") + 1) == invokemethodname
												.lastIndexOf(")"))) {
									inargs = new StringBuffer("()");
								} else {
									inargs = (new StringBuffer("(")).append(
											parseArgs(inargs)).append(")");
								}

								if (isAndroid(inpackagename)) {
									invokemethodsig = invokemethodsig.append(
											"android").append(": ").append(
											inclasssig).append(": ").append(
											inreturntype).append(" ").append(
											inmname).append(inargs);
									// method 是否已经存在判断
									// 判断方法：先从 method 表中查看，若不存在，插；若存在，找到
									// mid，再从invoke表中查看，若存在，不插，若不存在，插
									// 插返回类型，记录pid, 插方法
									// 插参数，记录pid，顺便插方法
									// 插invoke表
									ResultSet rs = mysql.select(selectmid,
											invokemethodsig.toString());
									if (rs.next()) {
										int inmid = rs.getInt(1);
										String sn = "select apkid from invoke where mid = ? and inmid = ?";
										ResultSet rss = mysql.select(sn, mid,
												inmid);
										if (!(rss.next())) {
											// 确定 mid 的值 最后一个mid+1
											inmid = (selectMax(selectm)) + 1;

											// 插inreturntype，且记录pid, 插method
											mysql.insert(inpara, count, inmid,
													inreturntype.toString(),
													null, "out", null, null);

											pid = selectMax(selectp);

											mysql.insert(inme, inmid, count,
													invokemethodsig.toString(),
													"android", pid, null, null);

											if (!(inargs.toString()
													.equals("()"))) {
												String argss = inargs
														.substring(1, (inargs
																.length()) - 1);
												String a[] = argss.split(",");

												for (int i = 0; i < a.length; i++) {
													mysql.insert(inpara, count,
															inmid, a[i], null,
															"in", null, null);
													pid = selectMax(selectp);

													mysql
															.insert(
																	inme,
																	inmid,
																	count,
																	invokemethodsig
																			.toString(),
																	"android",
																	pid, null,
																	null);
												}
											}
											// 再插invoke表
											mysql.insert(in, count, mid, count,
													inmid);
										}
									} else {
										// 确定 mid 的值 最后一个mid+1
										int inmid = (selectMax(selectm)) + 1;

										// 插returntype，且记录pid, 插method
										mysql.insert(inpara, count, inmid,
												inreturntype.toString(), null,
												"out", null, null);

										pid = selectMax(selectp);

										mysql.insert(inme, inmid, count,
												invokemethodsig.toString(),
												"android", pid, null, null);

										if (!(inargs.toString().equals("()"))) {
											String argss = inargs.substring(1,
													(inargs.length()) - 1);
											String a[] = argss.split(",");

											for (int i = 0; i < a.length; i++) {
												mysql.insert(inpara, count,
														inmid, a[i], null,
														"in", null, null);
												pid = selectMax(selectp);

												mysql.insert(inme, inmid,
														count, invokemethodsig
																.toString(),
														"android", pid, null,
														null);
											}
										}
										// 再插invoke表
										mysql.insert(in, count, mid, count,
												inmid);
									}
								}

								else if (isJava(inpackagename)) {
									invokemethodsig = invokemethodsig.append(
											"java").append(": ").append(
											inclasssig).append(": ").append(
											inreturntype).append(" ").append(
											inmname).append(inargs);
									ResultSet rs = mysql.select(selectmid,
											invokemethodsig.toString());
									if (rs.next()) {
										int inmid = rs.getInt(1);
										String sn = "select apkid from invoke where mid = ? and inmid = ?";
										ResultSet rss = mysql.select(sn, mid,
												inmid);
										if (!(rss.next())) {
											// 确定 mid 的值 最后一个mid+1
											inmid = (selectMax(selectm)) + 1;

											// 插inreturntype，且记录pid, 插method
											mysql.insert(inpara, count, inmid,
													inreturntype.toString(),
													null, "out", null, null);

											pid = selectMax(selectp);

											mysql.insert(inme, inmid, count,
													invokemethodsig.toString(),
													"java", pid, null, null);

											if (!(inargs.toString()
													.equals("()"))) {
												String argss = inargs
														.substring(1, (inargs
																.length()) - 1);
												String a[] = argss.split(",");

												for (int i = 0; i < a.length; i++) {
													mysql.insert(inpara, count,
															inmid, a[i], null,
															"in", null, null);
													pid = selectMax(selectp);

													mysql
															.insert(
																	inme,
																	inmid,
																	count,
																	invokemethodsig
																			.toString(),
																	"java",
																	pid, null,
																	null);
												}
											}
											// 再插invoke表
											mysql.insert(in, count, mid, count,
													inmid);
										}
									} else {
										// 确定 mid 的值 最后一个mid+1
										int inmid = (selectMax(selectm)) + 1;

										// 插returntype，且记录pid, 插method
										mysql.insert(inpara, count, inmid,
												inreturntype.toString(), null,
												"out", null, null);

										pid = selectMax(selectp);

										mysql.insert(inme, inmid, count,
												invokemethodsig.toString(),
												"java", pid, null, null);

										if (!(inargs.toString().equals("()"))) {
											String argss = inargs.substring(1,
													(inargs.length()) - 1);
											String a[] = argss.split(",");

											for (int i = 0; i < a.length; i++) {
												mysql.insert(inpara, count,
														inmid, a[i], null,
														"in", null, null);
												pid = selectMax(selectp);

												mysql
														.insert(
																inme,
																inmid,
																count,
																invokemethodsig
																		.toString(),
																"java", pid,
																null, null);
											}
										}
										// 再插invoke表
										mysql.insert(in, count, mid, count,
												inmid);
									}

								}

								else if (isOwn(inpackagename, ownpack)) {
									invokemethodsig = invokemethodsig.append(
											apk.toString()).append(": ")
											.append(inclasssig).append(": ")
											.append(inreturntype).append(" ")
											.append(inmname).append(inargs);
									ResultSet rs = mysql.select(selectmid,
											invokemethodsig.toString());
									if (rs.next()) {
										int inmid = rs.getInt(1);
										String sn = "select apkid from invoke where mid = ? and inmid = ?";
										ResultSet rss = mysql.select(sn, mid,
												inmid);
										if (!(rss.next())) {
											// 确定 mid 的值 最后一个mid+1
											inmid = (selectMax(selectm)) + 1;

											// 插inreturntype，且记录pid, 插method
											mysql.insert(inpara, count, inmid,
													inreturntype.toString(),
													null, "out", null, null);

											pid = selectMax(selectp);

											mysql.insert(inme, inmid, count,
													invokemethodsig.toString(),
													"own", pid, null, null);

											if (!(inargs.toString()
													.equals("()"))) {
												String argss = inargs
														.substring(1, (inargs
																.length()) - 1);
												String a[] = argss.split(",");

												for (int i = 0; i < a.length; i++) {
													mysql.insert(inpara, count,
															inmid, a[i], null,
															"in", null, null);
													pid = selectMax(selectp);

													mysql
															.insert(
																	inme,
																	inmid,
																	count,
																	invokemethodsig
																			.toString(),
																	"own", pid,
																	null, null);
												}
											}
											// 再插invoke表
											mysql.insert(in, count, mid, count,
													inmid);
										}
									} else {
										// 确定 mid 的值 最后一个mid+1
										int inmid = (selectMax(selectm)) + 1;

										// 插returntype，且记录pid, 插method
										mysql.insert(inpara, count, inmid,
												inreturntype.toString(), null,
												"out", null, null);

										pid = selectMax(selectp);

										mysql.insert(inme, inmid, count,
												invokemethodsig.toString(),
												"own", pid, null, null);

										if (!(inargs.toString().equals("()"))) {
											String argss = inargs.substring(1,
													(inargs.length()) - 1);
											String a[] = argss.split(",");

											for (int i = 0; i < a.length; i++) {
												mysql.insert(inpara, count,
														inmid, a[i], null,
														"in", null, null);
												pid = selectMax(selectp);

												mysql.insert(inme, inmid,
														count, invokemethodsig
																.toString(),
														"own", pid, null, null);
											}
										}
										// 再插invoke表
										mysql.insert(in, count, mid, count,
												inmid);
									}
								}

								else {
									invokemethodsig = invokemethodsig.append(
											"thirdpackage").append(": ")
											.append(inclasssig).append(": ")
											.append(inreturntype).append(" ")
											.append(inmname).append(inargs);
									ResultSet rs = mysql.select(selectmid,
											invokemethodsig.toString());
									if (rs.next()) {
										int inmid = rs.getInt(1);
										String sn = "select apkid from invoke where mid = ? and inmid = ?";
										ResultSet rss = mysql.select(sn, mid,
												inmid);
										if (!(rss.next())) {
											// 确定 mid 的值 最后一个mid+1
											inmid = (selectMax(selectm)) + 1;

											// 插inreturntype，且记录pid, 插method
											mysql.insert(inpara, count, inmid,
													inreturntype.toString(),
													null, "out", null, null);

											pid = selectMax(selectp);

											mysql.insert(inme, inmid, count,
													invokemethodsig.toString(),
													"third", pid, null, null);

											if (!(inargs.toString()
													.equals("()"))) {
												String argss = inargs
														.substring(1, (inargs
																.length()) - 1);
												String a[] = argss.split(",");

												for (int i = 0; i < a.length; i++) {
													mysql.insert(inpara, count,
															inmid, a[i], null,
															"in", null, null);
													pid = selectMax(selectp);

													mysql
															.insert(
																	inme,
																	inmid,
																	count,
																	invokemethodsig
																			.toString(),
																	"third",
																	pid, null,
																	null);
												}
											}
											// 再插invoke表
											mysql.insert(in, count, mid, count,
													inmid);
										}
									} else {
										// 确定 mid 的值 最后一个mid+1
										int inmid = (selectMax(selectm)) + 1;

										// 插returntype，且记录pid, 插method
										mysql.insert(inpara, count, inmid,
												inreturntype.toString(), null,
												"out", null, null);

										pid = selectMax(selectp);

										mysql.insert(inme, inmid, count,
												invokemethodsig.toString(),
												"third", pid, null, null);

										if (!(inargs.toString().equals("()"))) {
											String argss = inargs.substring(1,
													(inargs.length()) - 1);
											String a[] = argss.split(",");

											for (int i = 0; i < a.length; i++) {
												mysql.insert(inpara, count,
														inmid, a[i], null,
														"in", null, null);
												pid = selectMax(selectp);

												mysql.insert(inme, inmid,
														count, invokemethodsig
																.toString(),
														"third", pid, null,
														null);
											}
										}
										// 再插invoke表
										mysql.insert(in, count, mid, count,
												inmid);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				StringBuffer write = new StringBuffer(
						"F:\\app_detail\\exception.txt");
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							new File(write.toString()), true));
					writer.write("apkname: " + apk.toString());
					writer.newLine();
					writer.write("smalifilename: " + f.getName());
					writer.newLine();

					writer.write("e.getMessage(): " + e.getMessage());
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

	private int selectId(String selectpid, StringBuffer args) {
		int pid = 0;
		ResultSet p = mysql.select(selectpid, args.toString());
		try {
			if (p.next()) {
				pid = p.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pid;
	}

	private int selectMax(String selectp) {
		int pid = 0;
		ResultSet p = mysql.select(selectp);
		try {
			if (p.next()) {
				pid = p.getInt(1);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return pid;
	}

	private boolean isJava(StringBuffer inpackagename) {
		/*
		 * for(int i = 0; i < javapack.length; i ++){
		 * if((inpackagename.toString()).equals(javapack[i])){ return true; } }
		 * return false;
		 */

		if ((inpackagename.toString().startsWith("java."))
				|| (inpackagename.toString().startsWith("javax."))) {
			return true;
		}
		return false;
	}

	private boolean isAndroid(StringBuffer inpackagename) {
		/*
		 * for(int i = 0; i < androidpack.length; i ++){
		 * if((inpackagename.toString()).equals(androidpack[i])){ return true; }
		 * }
		 */
		if (inpackagename.toString().startsWith("android.")) {
			return true;
		}

		return false;
	}

	private boolean isOwn(StringBuffer inpackagename, String[] ownpack) {
		for (int i = 0; i < ownpack.length; i++) {
			if ((inpackagename.toString()).equals(ownpack[i])) {
				return true;
			}
		}
		return false;
	}

	private String[] listOwnPackages() {
		StringBuffer packages[] = new StringBuffer[filelist.size()];

		Iterator<File> packs = filelist.iterator();
		int c = 0;
		while (packs.hasNext()) {
			File pack = packs.next();
			StringBuffer parent = new StringBuffer(pack.getParent());
			parent.trimToSize();
			parent = new StringBuffer(parent.substring(parent
					.indexOf("\\smali\\") + 7));
			parent = new StringBuffer((parent.toString()).replace("\\", "."));
			packages[c] = parent;
			c++;
		}

		List<String> list = Collections
				.synchronizedList(new LinkedList<String>());
		for (int i = 0; i < packages.length; i++) {
			if (!(list.contains(packages[i].toString()))) {
				list.add(packages[i].toString());
			}
		}

		String[] npackages = list.toArray(new String[list.size()]); // packages
																	// 中存储 apk
																	// 自定义的包名
																	// ========
																	// StringBuffer
																	// 格式
		list.clear();
		list = null;

		return npackages;
	}

	private StringBuffer stanClassName(StringBuffer inclasssig) {
		inclasssig = new StringBuffer(inclasssig.substring((inclasssig
				.lastIndexOf(".") + 1)));
		inclasssig.trimToSize();

		return inclasssig;
	}

	private StringBuffer parseInvokeMethodName(StringBuffer line) {
		try {
			int index = line.indexOf("->");

			line = new StringBuffer(line.substring(index + 2));
			line.trimToSize();

			if (line.toString().endsWith(";")) {
				line = new StringBuffer(line
						.substring(0, ((line.length()) - 1)));
			}

		} catch (Exception e) {
		}
		return line;
	}

	private StringBuffer parseInvokeClassName(StringBuffer line) {
		try {
			int index1 = line.indexOf("},");
			int index2 = line.indexOf("->");

			line = new StringBuffer(line.substring((index1 + 3), (index2 - 1)));
			line.trimToSize();

		} catch (Exception e) {
		}

		return line;
	}

	private Object parseArgs(StringBuffer args) {
		int index = 0;
		StringBuffer stanargs = new StringBuffer();

		for (int i = 0; i < args.length(); i++) {
			StringBuffer letter = new StringBuffer(String.valueOf(args
					.charAt(i)));
			String letstr = letter.toString();

			if (isBaseLetter(letstr)) {
				StringBuffer basestr = new StringBuffer(parseBaseType(letstr));

				if (i == (args.length() - 1))
					stanargs = stanargs.append(basestr);
				else {
					stanargs = stanargs.append(basestr).append(",");
				}
			}

			else if (isObjectLetter(letstr)) {
				index = args.indexOf(";", i);
				letter = new StringBuffer(args.substring(i, index));
				letter = stanClassSig(letter);

				i = index;
				if (i == (args.length() - 1)) {
					stanargs = stanargs.append(letter);
				} else {
					stanargs = stanargs.append(letter).append(",");
				}
			}

			else if (isArrayLetter(letstr)) {
				int count = 1;
				i++;
				letter = new StringBuffer(String.valueOf(args.charAt(i)));
				letstr = letter.toString();

				while (isArrayLetter(letstr)) {
					count++;
					i++;
					letter = new StringBuffer(String.valueOf(args.charAt(i)));
					letstr = letter.toString();

				}

				if (isBaseLetter(letstr)) {
					letter = new StringBuffer(parseBaseType(letstr));
					for (int j = 0; j < count; j++) {
						letter = letter.append("[]");
					}
					if (i == (args.length() - 1)) {
						stanargs = stanargs.append(letter);
					} else {
						letter = letter.append(",");
						stanargs = stanargs.append(letter);
					}
				}

				else if (isObjectLetter(letstr)) {
					index = args.indexOf(";", i);
					letter = new StringBuffer(args.substring(i, index));
					letter = stanClassSig(letter);
					for (int j = 0; j < count; j++) {
						letter = letter.append("[]");
					}
					i = index;
					if (i == (args.length() - 1)) {
						stanargs = stanargs.append(letter);
					} else {
						letter = letter.append(",");
						stanargs = stanargs.append(letter);
					}
				} else
					continue;
			} else
				continue;
		}
		return stanargs;
	}

	private boolean isArrayLetter(String letter) {
		if ("[".equals(letter))
			return true;
		return false;
	}

	private boolean isObjectLetter(String letter) {
		if ("L".equals(letter))
			return true;
		return false;
	}

	private boolean isBaseLetter(String letter) {
		if (("Z".equals(letter)) || ("B".equals(letter))
				|| ("S".equals(letter)) || ("C".equals(letter))
				|| ("I".equals(letter)) || ("J".equals(letter))
				|| ("F".equals(letter)) || ("D".equals(letter)))
			return true;
		return false;
	}

	private boolean isArray(String returntype) {
		if (returntype.startsWith("["))
			return true;
		return false;
	}

	private boolean isObject(String returntype) {
		if (returntype.startsWith("L"))
			return true;
		return false;
	}

	private boolean isBaseType(StringBuffer returntype) {
		if (returntype.length() == 1)
			return true;
		return false;
	}

	private String parseBaseType(String returntype) {
		if ("V".equals(returntype))
			return "void";
		if ("Z".equals(returntype))
			return "boolean";
		if ("B".equals(returntype))
			return "byte";
		if ("S".equals(returntype))
			return "short";
		if ("C".equals(returntype))
			return "char";
		if ("I".equals(returntype))
			return "int";
		if ("J".equals(returntype))
			return "long";
		if ("F".equals(returntype))
			return "float";
		if ("D".equals(returntype))
			return "double";

		return returntype;
	}

	private StringBuffer parseRType(StringBuffer returntype) {
		int count = 0;
		if (isBaseType(returntype)) {
			returntype = new StringBuffer(parseBaseType(returntype.toString()));
			return returntype;

		}

		else if (isObject(returntype.toString())) {
			returntype = stanClassSig(returntype);
			return returntype;

		}

		else if (isArray(returntype.toString())) {
			count = (returntype.lastIndexOf("[")) + 1;
			returntype = new StringBuffer(returntype.substring(count));
			returntype.trimToSize();

			if (isBaseType(returntype)) {
				returntype = new StringBuffer(parseBaseType(returntype
						.toString()));
				for (int i = 0; i < count; i++) {
					returntype = returntype.append("[]");
				}
				return returntype;
			}

			if (isObject(returntype.toString())) {
				returntype = stanClassSig(returntype);
				for (int i = 0; i < count; i++) {
					returntype = returntype.append("[]");
				}
				return returntype;
			}
		}

		else {
			System.out.println("error type！");
		}

		return returntype;
	}

	private StringBuffer parseMethodName(StringBuffer line) {
		StringBuffer methodname = new StringBuffer(line.substring(8));
		methodname.trimToSize();
		if (methodname.toString().contains(" ")) {
			int index = methodname.lastIndexOf(" ");
			methodname = new StringBuffer(methodname.substring(index + 1));

		}
		if (methodname.toString().endsWith(";")) {
			methodname = new StringBuffer(methodname.substring(0, ((methodname
					.length()) - 1)));

		}

		return methodname;
	}

	private StringBuffer stanPackageName(StringBuffer classsig) {
		if (classsig.lastIndexOf(".") < 0) {
			return classsig;
		}

		classsig = new StringBuffer(classsig.substring(0, classsig
				.lastIndexOf(".")));
		classsig.trimToSize();
		return classsig;
	}

	private StringBuffer stanClassSig(StringBuffer sclassname) {
		String str = sclassname.substring(1);
		sclassname = new StringBuffer(str.replace("/", "."));
		sclassname.trimToSize();

		return sclassname;
	}

    private String[] stanAppVersionAndName(StringBuffer appName){
    	//TODO 解析出version.
    	String str_version = appName.toString() ;
		String[] goal = str_version.split("-");
		if(goal != null && goal.length ==2){
			return goal;
		}else{
			return null;
		}
    }	
	private StringBuffer parseClassName(StringBuffer line) {
		StringBuffer classname = new StringBuffer((line.substring(7, (line
				.length()) - 1)));
		classname.trimToSize();
		if (classname.toString().contains(" ")) {
			int index = classname.lastIndexOf(" ");
			classname = new StringBuffer(classname.substring(index + 1));
		}

		return classname;
	}

	private int listFiles() {
		ExtractOuterClass outcla = new ExtractOuterClass(file);
		filelist = outcla.extractClassFile();

		if (filelist.isEmpty())
			return 0;
		return 1;
	}

	
/**
 * 用来向caller 或者 callee中存入数据。
 * @param callBase
 * @param packageName
 * @param className
 * @param methodName
 * @param methodType
 * @param returnType
 * @param parameter
 */
    private void  setCallBase(CallBase callBase,StringBuffer packageName,StringBuffer className,StringBuffer methodName,
    		StringBuffer methodType,StringBuffer returnType,StringBuffer parameter){
    	callBase.setPackageName(packageName.toString());
		callBase.setClassName(className.toString());
		callBase.setMethodName(methodName.toString());
		callBase.setMethodType(methodType.toString());
		callBase.setReturnType(returnType.toString()); 
		callBase.setParameter(parameter.toString());
    }
    
    /**
     * 将一个apk完全解析后的信息放进数据库中。
     * @param apkInfo
     * @param sql
     */
    private void sendDataToDB(ApkEntity apkInfo,MySQLCor sql){
    	
    	
    }
}
