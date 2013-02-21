package inputOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SplitDir 
{
	protected ArrayList<String> files; 
	
	public SplitDir( final File dir, final int subDirNr )
	{
		System.out.println( "Splitting " + dir + " into " + subDirNr + " subdirectories" );
		setFiles( dir ); 
		makeDirs( dir, subDirNr ); 
		distributeFiles( dir, subDirNr ); 
	}
	
	public void distributeFiles( final File dir, final int subDirNr )
	{
		int currentFolder = 0; 
		for( int i = 0; i < getFiles().size(); i++ )
		{
			final File file = new File ( dir + "/" + getFiles().get( i ) );
			file.renameTo( new File( dir + "/" + currentFolder + "/" + getFiles().get( i ) ) ); 
			
			if( currentFolder == subDirNr - 1 )
				currentFolder = 0; 
			else
				currentFolder++; 
		}
	}
	
	public void makeDirs( final File dir, final int subDirNr )
	{
		for( int i = 0; i < subDirNr; i++)
		{
			File subDir = new File( dir.getPath() + "/" + i ); 
			if( !subDir.exists() )
			{
				subDir.mkdir(); 
			}
			else
			{
				System.err.println( "Subdirectory already exists, Exiting!" ); 
				System.exit( 1 ); 
			}
		}
	}
	
	public ArrayList<String> getFiles() { return this.files; } 
	public void setFiles( final File dir )
	{
		String[] temp = dir.list( FileNameFilterTemplates.getVisibleFiles() );
		ArrayList<String> files = new ArrayList<String>(); 
		
		for( String file : temp )
			files.add( file );
		
		Collections.sort( files ); 
		
		this.files = files;  
	}
	
	 
	
	public static void main(String[] args) 
	{
		if( args.length != 2 )
		{
			final String info = "\n#######################\n" +
					"java -jar splitDir.jar -n=int dirToSplit\n" +
					"#######################\n" +
					"This script splits the files contained in the dirToSplit into n subdirectories\n" +
					"Please specify the number of subdirs (-n=int).\n" + 
					"#######################\n\n";  
			System.err.println( info ); 
		}
		else 
		{
			final int subDirNr = Integer.parseInt( args[ 0 ].substring( args[ 0 ].indexOf( "=" ) + 1 ) ) ; 
			final File dir = new File( args[ 1 ] );
			
			// Start main 
			new SplitDir( dir, subDirNr ); 
		}

	}
}
