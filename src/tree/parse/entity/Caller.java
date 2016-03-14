package tree.parse.entity;

import java.util.LinkedList;

public class Caller extends CallBase{
	private LinkedList<Callee> calleeList;

	public LinkedList<Callee> getCalleeList() {
		return calleeList;
	}

	public void setCalleeList(LinkedList<Callee> calleeList) {
		this.calleeList = calleeList;
	}
	
	
	

}
