package smit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import parser.BEDParser;
import sun.security.provider.SystemIdentity;

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
		System.out.println( "Generate SMIT gene collection from bed file: " + bedFile.getPath() ); 
		
		//Get a list of SMIT genes 
		ArrayList<SMITGene> geneList = BEDParser.parseToSMITGenes( bedFile ); 
		
		//Instanciate a SMITGeneCollection, link the list of SMIT genes and generate HashMap  
		SMITGeneCollection sgc = new SMITGeneCollection(); 
		sgc.setGeneList( geneList ); 
		sgc.setGeneHashMap( sgc.generateGeneHashMap() ); 
		
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
		System.out.println( "Generating SMIT readpair collection." );
		//Generate a SMITReadpairCollection which will be updated with reverse and forward reads
		SMITReadpairCollection readpairCollection = new SMITReadpairCollection();  
		
		//Add reverse reads first
		System.out.println( "Generating readpair collection with reverse reads (Pol II position) from sam file: " + reverseReads.getPath() );
		addReverseReads( reverseReads, readpairCollection ); 
		//Index the Hashmap in the ReadpairCollection to be able to search indices for forward read matching 
		readpairCollection.generateSMITReadpairHashMap(); 
		System.out.println( "Reverse reads added. Generated " + readpairCollection.getSMITReadpairCollection().size() + " SMIT readpairs." );
		
		//Add spliced forward reads
		System.out.println( "Adding spliced forward reads from sam file: " + splicedForwardReads.getPath() );
		boolean spliced = true; 
		addForwardReads(splicedForwardReads, readpairCollection, spliced ); 
		
		//Add unspliced forward reads 
		System.out.println( "Adding unspliced forward reads from sam file: " + unsplicedForwardReads.getPath() );
		spliced = false; 
		addForwardReads(unsplicedForwardReads, readpairCollection, spliced );
		
		int count = 0;
		ArrayList<SMITReadpair> list = readpairCollection.getSMITReadpairCollection(); 
		for( int i = 0; i < list.size(); i++ )
		{
			if( list.get( i ).hasForwardAndReverseRead() ) 
			{
				count++; 
			}
		}
		/*
		for( SMITReadpair rp : readpairCollection.getSMITReadpairCollection() )
		{
			System.out.println( rp );
		}
		*/
		
		System.out.println( "The readpair collection contains correct " + count + " entries.");
		
		
		
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
		
		sfr.close(); 
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
			SMITReadpair rp = readpairCollection.getSMITReadpairHashMap().get( sr.getReadName() );
			
			if( rp != null )
			{ 
				rp.addForwardRead(sr, spliced, getGeneCollection() );
			}
			
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
		final long time = System.currentTimeMillis();
		
		
		final File bedFile = new File( "/Volumes/SMITProject/annotation/sgdGenes.bed" );
		final File reverseReads = new File( "/Volumes/SMITProject/mapped/PE_R1_segL14_tophat.sam" );
		final File splicedForwardReads = new File( "/Volumes/SMITProject/mapped/SMIT_EEJ_R2.sam" );
		final File unsplicedForwardReads = new File( "/Volumes/SMITProject/mapped/SMIT_EIJ_R2.sam" );
		
		SMITData sd = new SMITData( bedFile, splicedForwardReads, unsplicedForwardReads, reverseReads );
		
		System.out.println( "done. [" + (System.currentTimeMillis() - time) + " ms]" );
	}

}
