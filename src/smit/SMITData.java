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
	public static boolean VERBOSE = false; 

	public SMITData( final File bedFile, final File primerDesignFile,
			final File splicedForwardReads, final File unsplicedForwardReads, final File reverseReads, 
			final File outputDir ) throws IOException
	{
		//Generate Gene collection
		boolean verbose = true; 
		setGeneCollection( generateGeneCollection( bedFile, primerDesignFile, verbose ) );
		
		//Generate SMIT readpairs and connect to genes
		setReadpairCollection( generateSMITReadpairs( splicedForwardReads, unsplicedForwardReads, reverseReads, verbose ) );
		
		//Smit analysis
		smitAnalysis( verbose, outputDir ); 
	}
	
	/**
	 * This method constructs the geneCollection from the bedFile
	 * @param bedFile
	 */
	public SMITGeneCollection generateGeneCollection( final File bedFile, final File primerDesignFile, final boolean verbose ) throws IOException
	{
		System.out.println( "Generate SMIT gene collection from bed file: " + bedFile.getPath() ); 
		
		//Get a list of SMIT genes 
		ArrayList<SMITGene> geneList = BEDParser.parseToSMITGenes( bedFile ); 
		if( verbose )
		{
			int geneCountPlus = 0; 
			int geneCountMinus = 0; 
			for( SMITGene sg : geneList )
			{
				if( sg.isPlusStrand() )
					geneCountPlus++; 
				else
					geneCountMinus++; 
			}
			System.out.println( "Read a total of " + ( geneCountMinus + geneCountPlus) + " SMIT genes. On plus strand: " + geneCountPlus + " . On minus strand: " + geneCountMinus ); 
		}
		
		//Instanciate a SMITGeneCollection, link the list of SMIT genes and generate HashMap  
		SMITGeneCollection sgc = new SMITGeneCollection(); 
		sgc.setGeneList( geneList );
		sgc.addPrimerPositionInfo( primerDesignFile );
		sgc.setGeneHashMap( sgc.generateGeneHashMap() ); 
		
		System.out.println( "Generate SMIT gene collection, done. \n----- \n" );
		
		return sgc; 
	}
	
	/**
	 * Add the NGS reads to the SMITGeneCollection 
	 * @param splicedForwardReads 
	 * @param unsplicedForwardReads
	 * @param reverseReads
	 * @throws IOException
	 */
	public SMITReadpairCollection generateSMITReadpairs( final File splicedForwardReads, final File unsplicedForwardReads, final File reverseReads, final boolean verbose ) throws IOException
	{
		System.out.println( "Generating SMIT readpair collection." );
		//Generate a SMITReadpairCollection which will be updated with reverse and forward reads
		SMITReadpairCollection readpairCollection = new SMITReadpairCollection();  
		
		//Add reverse reads first
		addReverseReads( reverseReads, readpairCollection, verbose ); 
		//Index the Hashmap in the ReadpairCollection to be able to search indices for forward read matching 
		readpairCollection.generateSMITReadpairHashMap();
			
		
		//Add spliced forward reads
		boolean spliced = true; 
		addForwardReads(splicedForwardReads, readpairCollection, spliced, verbose ); 
		
		//Add unspliced forward reads 
		spliced = false; 
		addForwardReads(unsplicedForwardReads, readpairCollection, spliced, verbose );
		
		//Ignore those reads with no complete forward and reverse set
		readpairCollection.ignoreIncompleteReadpairs(); 
		
		if( verbose )
			System.out.println( readpairCollection );
		
		System.out.println( "Generating SMIT readpair collection, done. \n----- \n" );
		
		return readpairCollection; 
	}
	
	public void smitAnalysis( final boolean verbose, final File outputDir  )
	{
		System.out.println("Perform SMIT analysis." ); 
		getReadpairCollection().removeInvalidReadpairs(); 
		
		for( SMITGene smitGene : getGeneCollection().getGeneList() )
		{				
				smitGene.smitAnalysis( outputDir ); 
		}
		System.out.println( "Perform SMIT analysis, done. \n----- \n" );
	}
	
	/**
	 * Add reverse reads to readpairCollection
	 * @param reverseReads
	 * @param readpairCollection
	 */
	public void addReverseReads( final File reverseReads, SMITReadpairCollection readpairCollection, final boolean verbose )
	{
		if( verbose )
			System.out.println( "Generating readpair collection with reverse reads (Pol II position) from sam file: " + reverseReads.getPath() );
		
		SAMFileReader sfr = new SAMFileReader( reverseReads ); 
		
		//Go through each SAM file record
		Iterator<SAMRecord> i = sfr.iterator(); 
		int plusCount = 0; 
		int minusCount = 0; 
		while( i.hasNext() )
		{
			//Read next sam record
			SAMRecord sr = i.next();
			if( sr.getReadNegativeStrandFlag() )
				minusCount++; 
			else 
				plusCount++; 
				
			//Instanciate readpair with reverse read and add to collection 
			readpairCollection.addReadPair( new SMITReadpair( sr ) ); 
		}
		
		if( verbose )
		{
			System.out.println( "Reverse reads added. Generated " + readpairCollection.getSMITReadpairCollection().size() + " SMIT readpairs." );
			System.out.println( plusCount + " plus-strand reads and " + minusCount + " minus-strand reads." );
			System.out.println( "\n-----" );
			
		}
		
		sfr.close(); 
	}
	
	/**
	 * Add forward to reverse reads. Match to readpair with same id and pass true or false for splicing 
	 * dependent which file the reads stem from. 
	 * @param forwardReads SAM file, with forward read 
	 * @param readpairCollection Collection of SMITReadpairs (the reverse read are contained here). 
	 * @param spliced boolean based on the identity of the SAM file. Defined by the mapping strategy. 
	 */
	public void addForwardReads( final File forwardReads, SMITReadpairCollection readpairCollection, final boolean spliced, final boolean verbose )
	{
		if( verbose )
			System.out.println( "Adding forward reads from sam file: " + forwardReads.getPath() + "Spliced?" + spliced );
		//Initiate the SAM file reader (picar). 
		SAMFileReader sfr = new SAMFileReader( forwardReads ); 
		
		//Go through each SAM file record
		Iterator<SAMRecord> i = sfr.iterator(); 
		int plusCount = 0; 
		int minusCount = 0; 
		while( i.hasNext() )
		{
			//Read next sam record
			SAMRecord sr = i.next();
			
			//System.out.println(sr);
			
			if( sr.getReadNegativeStrandFlag() )
				minusCount++; 
			else 
				plusCount++;
			
			//Check if a SMITpair with the same id is present 
			SMITReadpair rp = readpairCollection.getSMITReadpairHashMap().get( sr.getReadName() );
			//System.out.println(rp); 

			
			if( rp != null )
			{ 
				rp.addForwardRead(sr, spliced, getGeneCollection(), VERBOSE );
			}
			
		}
		
		if( verbose )
		{
			System.out.println( "Forward reads added. Processed " + (plusCount + minusCount) + " reads." );
			System.out.println( plusCount + " plus-strand reads and " + minusCount + " minus-strand reads." );
			System.out.println( "\n-----" );
			
		}
		
		sfr.close(); 
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
		
		/*
		final File bedFile = new File( "/Users/carrillo/Desktop/smitData/sgdGenes.bed" ); 
		final File primerDesignFile = new File( "/Users/carrillo/Desktop/smitData/primerListAllIntronGenes.txt" ); 
		
		
		final File reverseReads = new File( "/Users/carrillo/Desktop/smitData/SMIT_082014/SMIT_R1_woRG.bam" );
		final File splicedForwardReads = new File( "/Users/carrillo/Desktop/smitData/SMIT_082014/SMIT_R2_EEJ_woRG.bam" );
		final File unsplicedForwardReads = new File( "/Users/carrillo/Desktop/smitData/SMIT_082014/SMIT_R2_EIJ_woRG.bam" );
		final File outputDir = new File( "/Users/carrillo/Desktop/smitData/SMIT_082014/SMITFiles" );
		*/
		
		
		final File bedFile = new File( "resources/Scer3_all.bed" ); 
		final File primerDesignFile = new File( "resources/primerListAllIntronGenes.txt" );
		 
		
		/*
		final File bedFile = new File( "resources/MS2_12field.bed" ); 
		final File primerDesignFile = new File( "resources/primer_MS2_190.txt" );
		*/
		
		/*
		final File reverseReads = new File( "resources/SMIT_082014/SMIT_R1_woRG.bam" );
		final File splicedForwardReads = new File( "resources/SMIT_082014/SMIT_R2_EEJ_woRG.bam" );
		final File unsplicedForwardReads = new File( "resources/SMIT_082014/SMIT_R2_EIJ_woRG.bam" );
		final File outputDir = new File( "resources/SMIT_082014/SMITFiles" );
		*/
		
		/*
		final File reverseReads = new File( "resources/totalRNA/R1.bam" );
		final File splicedForwardReads = new File( "resources/totalRNA/R2_EEJ.bam" );
		final File unsplicedForwardReads = new File( "resources/totalRNA/R2_EIJ.bam" );
		final File outputDir = new File( "resources/totalRNA/SMITFiles" );
		*/
		
		/*
		final File reverseReads = new File( "resources/475/468_475_MS2_R1.bam" );
		final File splicedForwardReads = new File( "resources/475/475_all_MS2_R2_EEJ.bam" );
		final File unsplicedForwardReads = new File( "resources/475/475_all_MS2_R2_EIJ.bam" );
		final File outputDir = new File( "resources/475/SMITFiles" );
		*/
		
		final File reverseReads = new File( "resources/fast/fast_R1.bam" );
		final File splicedForwardReads = new File( "resources/fast/fast_EEJ.bam" );
		final File unsplicedForwardReads = new File( "resources/fast/fast_EIJ.bam" );
		final File outputDir = new File( "resources/fast/SMITFiles" );
		
		
		/*
		final File reverseReads = new File( "resources/SMIT_178/SMIT_178_R1.sorted.bam" );
		final File splicedForwardReads = new File( "resources/SMIT_178/SMIT_178_R2_EEJ.bam" );
		final File unsplicedForwardReads = new File( "resources/SMIT_178/SMIT_178_R2_EIJ.bam" );
		final File outputDir = new File( "resources/SMIT_178/SMITFiles" );
		*/

		/*
		final File reverseReads = new File( "resources/totalRNA/SMIT_totalRNA_4_tophat2_R1.bam" );
		final File splicedForwardReads = new File( "resources/totalRNA/SMIT_totalRNA_4_EEJ.bam" );
		final File unsplicedForwardReads = new File( "resources/totalRNA/SMIT_totalRNA_4_EIJ.bam" );
		final File outputDir = new File( "resources/totalRNA/SMITFiles4" );
		*/
		
		/*
		final File reverseReads = new File( "/Volumes/DiskA/SMIT/reads/totalRNA/pooled/SMIT_totalRNA_all_tophat2_R1.bam" );
		final File splicedForwardReads = new File( "/Volumes/DiskA/SMIT/reads/totalRNA/pooled/SMIT_totalRNA_all_EEJ.bam" );
		final File unsplicedForwardReads = new File( "/Volumes/DiskA/SMIT/reads/totalRNA/pooled/SMIT_totalRNA_all_EIJ.bam" );
		final File outputDir = new File( "/Volumes/DiskA/SMIT/reads/totalRNA/pooled/SMITFiles" );
		*/
		
		/*
		final File reverseReads = new File( "/Volumes/DiskA/SMIT/reads/phz18/475/468_475_MS2_tophat2_R1.bam" );
		final File splicedForwardReads = new File( "/Volumes/DiskA/SMIT/reads/phz18/475/475_MS2_R2_EEJ.bam" );
		final File unsplicedForwardReads = new File( "/Volumes/DiskA/SMIT/reads/phz18/475/475_MS2_R2_EIJ.bam" );
		final File outputDir = new File( "/Volumes/DiskA/SMIT/reads/phz18/475/SMITFiles" );
		*/
		
		SMITData sd = new SMITData( bedFile, primerDesignFile, splicedForwardReads, unsplicedForwardReads, reverseReads, outputDir );
		
		System.out.println( "done. [" + (System.currentTimeMillis() - time) + " ms]" );
	}

}
