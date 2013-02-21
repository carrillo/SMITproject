package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import smit.SMITGene;

/**
 * Hi Lydia, 
 * This is a simple parser to generate an ArrayList of BED-file entries from a BED file. 
 * @author carrillo
 *
 */

public class BEDParser 
{
	/**
	 * This method opens the file (bedFile), generates an input stream and reads line per line into BEDentries. 
	 * @param bedFile
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<BEDentry> parse( final File bedFile ) throws IOException
	{
		//Initiate the output list 
		ArrayList<BEDentry> bedList = new ArrayList<BEDentry>(); 
		
		//Open the file and pass it as a stream
		BufferedReader in = new BufferedReader( new FileReader ( bedFile ) );
		
		//Read the stream line per line
		String line = ""; 
		while( in.ready() )
		{
			//Assign line to a string 
			line = in.readLine(); 
			
			//Generate a BEDentry and add it to the list
			bedList.add( new BEDentry( line ) ); 
		}
		
		//Return the output list to the 'caller' 
		return bedList; 
	}
	
	/**
	 * This method opens the file (bedFile), generates an input stream and reads line per line into BEDentries. 
	 * @param bedFile
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<SMITGene> parseToSMITGenes( final File bedFile ) throws IOException
	{
		//Initiate the output list 
		ArrayList<SMITGene> smitGeneList = new ArrayList<SMITGene>(); 
		
		//Open the file and pass it as a stream
		BufferedReader in = new BufferedReader( new FileReader ( bedFile ) );
		
		//Read the stream line per line
		String line = ""; 
		while( in.ready() )
		{
			//Assign line to a string 
			line = in.readLine(); 
			
			//Generate a SMITGene and add it to the list
			smitGeneList.add( new SMITGene( line ) ); 
		}
		
		//Return the output list to the 'caller' 
		return smitGeneList; 
	}
	
		
	/**
	 * This class is the 'entry point' if you start the program
	 * @param args Currently this main class doesn't take any arguments
	 */
	public static void main(String[] args) throws IOException
	{ 
		File bedFile = new File( "/Users/carrillo/Programs/myScripts/batchStuff/autAnn/sacCer2/refSeqMRNA.BED" ); 
		ArrayList<BEDentry> list = BEDParser.parse( bedFile ); 
		
		for( BEDentry e : list )
			System.out.println( e ); 
	}

}
