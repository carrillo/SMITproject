package smit;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import parser.BEDParser;

/**
 * This class holds all instances of SMITGenes
 * @author carrillo
 *
 */
public class SMITGeneCollection 
{
	//This ArrayList links to all SMITGenes
	protected ArrayList<SMITGene> geneList;
	
	//This hashmap stores each SMITGene object under its id value. It allows very fast and efficient searches
	protected HashMap<String, SMITGene> geneHashMap;
	
	public HashMap<String, SMITGene> generateGeneHashMap() 
	{  
		//Instantiate geneList and geneHashMap. Fill them with content in the loop (s.below).
		HashMap<String, SMITGene> geneHashMap = new HashMap<String, SMITGene>(); 
		
		//Add all SMITGeneEntries to the hashMap 
		for( SMITGene gene : getGeneList() )
		{
			geneHashMap.put( gene.getName(), gene );  
		}
		
		return geneHashMap; 
	}
	
	/**
	 * Add the forward primer position to the smit genes. 
	 * @param primerDesignFile
	 */
	public void addPrimerPositionInfo( final File primerDesignFile ) throws IOException 
	{
		HashMap<String, SMITGene> geneHash = generateGeneHashMap(); 
		
		BufferedReader in = TextFileAccess.openFileRead( primerDesignFile );
		String[] entries;
		while( in.ready() ) {
			entries = in.readLine().split("\t");
			
			geneHash.get( entries[ 0 ] ).associatePrimerInfo( entries );
		}
	}
	
	//Getter and setter. 
	public void setGeneList( final ArrayList<SMITGene> geneList ) { this.geneList = geneList; }
	public ArrayList<SMITGene> getGeneList() { return this.geneList; } 
	
	public void setGeneHashMap( final HashMap<String, SMITGene> geneHashMap ) { this.geneHashMap = geneHashMap; } 
	public HashMap<String, SMITGene> getGeneHashMap() { return this.geneHashMap; } 
	
	/**
	 * This class is the 'entry point' if you start the program
	 * @param args Currently this main class doesn't take any arguments
	 */
	public static void main(String[] args) throws IOException
	{ 
		File bedFile = new File( "/Users/carrillo/Programs/myScripts/batchStuff/autAnn/sacCer2/refSeqMRNA.BED" ); 
		SMITGeneCollection sgc = new SMITGeneCollection();
		sgc.setGeneList( BEDParser.parseToSMITGenes( bedFile ) ); 
		sgc.generateGeneHashMap(); 
		 
	}
}
