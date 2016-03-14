package tree.parse.entity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.log.LogUtils;

public class ApkEntity {
	
	private String apkName;
	private String apkVersion;
	

	private LinkedList<Caller> callerList;
	private static ApkEntity instance; 
	private Logger log = Logger.getLogger("apkInfo");
	
	private ApkEntity(){}  
	
	public static ApkEntity getInstance(){
		 if (instance == null) {  
	         instance = new ApkEntity(); 
	      }  
	      return instance;  
	}
	
	public String getApkVersion() {
		return apkVersion;
	}

	public void setApkVersion(String apkVersion) {
		this.apkVersion = apkVersion;
	}
	public String getApkName() {
		return apkName;
	}
	public void setApkName(String apkName) {
		this.apkName = apkName;
	}
	public LinkedList<Caller> getCallerList() {
		return callerList;
	}
	public void setCallerList(LinkedList<Caller> callerList) {
		this.callerList = callerList;
	}

	public void  clear(){
		log.log(Level.WARNING, "the apk info is cleared !!!");
		
		if(callerList != null){
			Iterator<Caller> callerIt = callerList.iterator();
			while(callerIt.hasNext()){
				Caller caller  = callerIt.next();
				if(caller.getCalleeList() != null){
					 caller.getCalleeList().clear();
				}
				
			}
		
			callerList.clear();
			callerList = null;
		}
		
	}

}
