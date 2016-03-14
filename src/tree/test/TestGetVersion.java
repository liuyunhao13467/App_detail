package tree.test;

public class TestGetVersion {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String testString = "air.animatusapps.helpmikey-1000003" ;
		String[] objects = testString.split("-");
		if(objects[1] != null && objects[1] != ""){
			System.out.println("the version is : " + objects[1]);
		}
	}

}
