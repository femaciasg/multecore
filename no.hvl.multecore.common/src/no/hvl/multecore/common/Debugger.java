package no.hvl.multecore.common;

public class Debugger {

	public static void log (Object o){
		if(Constants.DEBUGGING) {
			System.out.println(o.toString());
		}           
	}
	
	public static void logError (Object o){
		if(Constants.DEBUGGING) {
			System.err.println(o.toString());
		}           
	}

}
