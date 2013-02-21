package inputOutput;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalenderStuff {

	
	public static final SimpleDateFormat DATE_Time = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss") ;
	public static final SimpleDateFormat TIME = new SimpleDateFormat( "HH-mm-ss") ;
	
	public static String now( SimpleDateFormat sdf ) 
	{
	    Calendar cal = Calendar.getInstance(); 
	    return sdf.format(cal.getTime());
	}


	
	public static void main(String[] args) 
	{
		System.out.println( "CurrenTime: " + now( DATE_Time ) ); 

	}

}
