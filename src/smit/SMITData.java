package smit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import parser.BEDParser;

/**
 * This is the class collecting all primary information: 
 * 1. Generate a list of all genes in the gffFile
 * 2. Read forward and reverse reads 
 * 3. Link reads to genes
 * @author carrillo
 *
 */
public class SMITData 
{
	//This is the class linking to the SMITGene enteties
	protected SMITGeneCollection geneCollection; 
	protected SMITReadpairCollection readpairCollection; 

	public SMITData( final File bedFile, final File splicedForwardReads, final File unsplicedForwardReads, final File reverseReads ) throws IOException
	{
		//Generate Gene collection
		setGeneCollection( generateGeneCollection( bedFile ) );
		
		//Generate SMIT readpairs and connect to genes
		setReadpairCollection( generateSMITReadpairs( splicedForwardReads, unsplicedForwardReads, reverseReads ) ); 
	}
	
	/**
	 * This method constructs the geneCollection from the bedFile
	 * @param bedFile
	 */
	public SMITGeneCollection generateGeneCollection( final File bedFile ) throws IOException
	{
		//Get a list of SMIT genes 
		ArrayList<SMITGene> geneList = BEDParser.parseToSMITGenes( bedFile ); 
		
		//Instanciate a SMITGeneCollection, link the list of SMIT genes and generate HashMap  
		SMITGeneCollection sgc = new SMITGeneCollection(); 
		sgc.setGeneList( geneList ); 
		sgc.generateGeneHashMap(); 
		
		return sgc; 
	}
	
	/**
	 * Add the NGS reads to the SMITGeneCollection 
	 * @param splicedForwardReads 
	 * @param unsplicedForwardReads
	 * @param reverseReads
	 * @throws IOException
	 */
	public SMITReadpairCollection generateSMITReadpairs( final File splicedForwardReads, final File unsplicedForwardReads, final File reverseReads ) throws IOException
	{
		//Generate a SMITReadpairCollection which will be updated with reverse and forward reads
		SMITReadpairCollection readpairCollection = new SMITReadpairCollection();  
		
		//Add reverse reads first
		addReverseReads( reverseReads, readpairCollection ); 
		//Index the Hashmap in the ReadpairCollection to be able to search indices for forward read matching 
		readpairCollection.generateSMITReadpairHashMap(); 
		
		//Add spliced forward reads
		boolean spliced = true; 
		addForwardReads(splicedForwardReads, readpairCollection, spliced ); 
		
		//Add unspliced forward reads 
		spliced = false; 
		addForwardReads(unsplicedForwardReads, readpairCollection, spliced );
		
		return readpairCollection; 
	}
	
	/**
	 * Add reverse reads to readpairCollection
	 * @param reverseReads
	 * @param readpairCollection
	 */
	public void addReverseReads( final File reverseReads, SMITReadpairCollection readpairCollection )
	{
		SAMFileReader sfr = new SAMFileReader( reverseReads ); 
		
		//Go through each SAM file record
		Iterator<SAMRecord> i = sfr.iterator(); 
		while( i.hasNext() )
		{
			//Read next sam record
			SAMRecord sr = i.next();
			//Instanciate readpair with reverse read and add to collection 
			readpairCollection.addReadPair( new SMITReadpair( sr ) ); 
		}
	}
	
	/**
	 * Add forward and reverse reads. Match to readpair with same id and pass true or false for splicing 
	 * dependent which file the reads stem from. 
	 * @param forwardReads SAM file, with forward read 
	 * @param readpairCollection Collection of SMITReadpairs (the reverse read are contained here). 
	 * @param spliced boolean based on the identity of the SAM file. Defined by the mapping strategy. 
	 */
	public void addForwardReads( final File forwardReads, SMITReadpairCollection readpairCollection, final boolean spliced )
	{
		//Initiate the SAM file reader (picar). 
		SAMFileReader sfr = new SAMFileReader( forwardReads ); 
		
		//Go through each SAM file record
		Iterator<SAMRecord> i = sfr.iterator(); 
		while( i.hasNext() )
		{
			//Read next sam record
			SAMRecord sr = i.next();
			
			//Check if a SMITpair with the same id is present 
			SMITReadpair rp = readpairCollection.getSMITReadpairHashMap().get( sr.getReferenceName() ); 
			rp.addForwardRead(sr, spliced, getGeneCollection() ); 
		}
	}
	
	
	//Getter and Setter
	public void setGeneCollection( final SMITGeneCollection geneCollection) { this.geneCollection = geneCollection; } 
	public SMITGeneCollection getGeneCollection() { return this.geneCollection; }
	
	public void setReadpairCollection( final SMITReadpairCollection readpairCollection ) { this.readpairCollection = readpairCollection; } 
	public SMITReadpairCollection getReadpairCollection() { return this.readpairCollection; } 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		final File bedFile = new File( "/Volumes/SMITProject/" );
		final File reverseReads = new File( "/Volumes/SMITProject/" );
		final File splicedForwardReads = new File( "/Volumes/SMITProject/" );
		final File unsplicedForwardReads = new File( "/Volumes/SMITProject/" );
		
		SMITData sd = new SMITData( bedFile, splicedForwardReads, unsplicedForwardReads, reverseReads ); 

	}

}
