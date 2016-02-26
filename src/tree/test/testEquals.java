package tree.test;

public class testEquals {

	public static void main(String[] args) {
		String a = "aaa";
		StringBuffer buffera = new StringBuffer(a);
		
		String b ="aaa"; 
		StringBuffer bufferb = new StringBuffer(b);

		System.out.println(a.equals(b));              //true
		
		
	}

}
