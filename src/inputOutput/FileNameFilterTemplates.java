package inputOutput;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameFilterTemplates {
	
	public static FilenameFilter getVisibleFiles() 
	{
		 FilenameFilter filter = new FilenameFilter() 
		 {
		        public boolean accept(File dir, String name) 
		        {
		        	if( name.startsWith(".") )
		        		return false; 
		        	else 
		        		return true; 
		            
		        }
		 };
		 
		 return filter; 
	}
	
	public static FilenameFilter getFilesEndingWith( final String fileEnding ) 
	{
		 FilenameFilter filter = new FilenameFilter() 
		 {
		        public boolean accept(File dir, String name) 
		        {
		        	if( name.endsWith( fileEnding ) && !name.startsWith(".") ) 
		        	{	
		        		return true; 
		        	}
		        	else 
		        	{
		        		return false; 
		        	}
		            
		        }
		 };
		 
		 return filter; 
	}
	
	public static FilenameFilter getFilesContaining( final String fileContaining ) 
	{
		 FilenameFilter filter = new FilenameFilter() 
		 {
		        public boolean accept(File dir, String name) 
		        {
		        	if( name.contains( fileContaining ) && !name.startsWith(".") ) 
		        	{	
		        		return true; 
		        	}
		        	else 
		        	{
		        		return false; 
		        	}
		            
		        }
		 };
		 
		 return filter; 
	}
}
