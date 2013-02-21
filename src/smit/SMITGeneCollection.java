package smit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import parser.BEDParser;
import parser.BEDentry;

public class SMITGeneCollection 
{
	//This ArrayList links to all SMITGenes
	protected ArrayList<SMITGene> geneList;
	
	//This hashmap stores each SMITGene object under its id value. It allows very fast and efficient searches
	protected HashMap<String, SMITGene> geneHashMap;
	
	public void generateGeneHashMap() 
	{  
		//Instantiate geneList and geneHashMap. Fill them with content in the loop (s.below).
		HashMap<String, SMITGene> geneHashMap = new HashMap<String, SMITGene>(); 
		
		//Add all SMITGeneEntries to the hashMap 
		for( SMITGene gene : getGeneList() )
		{
			geneHashMap.put( gene.getName(), gene );  
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
