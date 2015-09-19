package featureExtraction;

import inputOutput.TextFileAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

import parser.BEDFilter;
import parser.BEDParser;
import parser.BEDentry;
import parser.FastaEntry;

public class ExtractGeneticFeatures {
	
	private File bedFile;
	private File genomeFile;
	private File bpsFile; 
	private ArrayList<GeneticFeatureInfo> genes; 
	
	public ExtractGeneticFeatures(final File bedFile, final File genomeFile, final File bpsFile ) {
		setBedFile( bedFile );
		setGenomeFile( genomeFile );
		setBPSFile( bpsFile ); 
	}
	
	/**
	 * Read genes of interest. Keep only genes with 2 exons. 
	 * @throws IOException
	 */
	public void readBedFile() throws IOException {
		System.out.println("Extracting 1 intron containing genes with intron length > 8nt."); 
		ArrayList<BEDentry> genes = BEDParser.parse(getBedFile());
		//System.out.println(genes.size()); 
		genes = BEDFilter.filterByBlockCount(genes, 2, 2);
		//System.out.println(genes.size()); 
		genes = BEDFilter.filterByLastIntronLength(genes, 16, Integer.MAX_VALUE);
		//System.out.println(genes.size()); 
	
		final ArrayList<GeneticFeatureInfo> gfi = new ArrayList<GeneticFeatureInfo>(); 
		for( BEDentry gene : genes ) {
			gfi.add( new GeneticFeatureInfo(gene) ); 
		}
		
		setGenes(gfi);
		System.out.println("Extracting 1 intron containing genes with intron length > 8nt. Done.");
	}
	
	public void associateSpliceSequence() throws FileNotFoundException {
		System.out.println("Associate splice sequences to splice junctions." );
		
		final Scanner read = new Scanner( getGenomeFile() );   
		read.useDelimiter( ">" );  
		
		FastaEntry currentChromosome; 
		while( read.hasNext() )
		{
			currentChromosome = new FastaEntry( ">" + read.next() );  
			for( GeneticFeatureInfo gfi : getGenes() ) {
				if( currentChromosome.getId().equals( gfi.getBEDentry().getChrom() ) )
				{
					gfi.associateSequence( currentChromosome );
					//System.out.println(gfi); 
				}
			}
			
			currentChromosome = null; 
			System.gc(); 
			 
		}
		System.out.println("Associate splice sequences to splice junctions. Done\n-----" );
	}
	
	/**
	 * Associate Branchpoint sequence file to genes. 
	 */
	public void associateBranchPoint() throws IOException {
		
		System.out.println("Associate branch point sequence." ); 
		
		final BufferedReader in = TextFileAccess.openFileRead(bpsFile);
		final HashMap<String, String> bpsHash = new HashMap<String, String>( getGenes().size() ); 
		String[] entries;
		int lineCount = 0; 
		while(in.ready()) {
			if( lineCount != 0 ) {
				entries = in.readLine().split("\t");
				bpsHash.put(entries[0], entries[1]); 
			}
			lineCount++; 
		}
		
		for( GeneticFeatureInfo gfi : getGenes() ) {
			if( bpsHash.containsKey( gfi.getBEDentry().getName() ) ) {
				gfi.associateBPS( bpsHash.get( gfi.getBEDentry().getName() ));
				//System.out.println("Gene present " + gfi.getBEDentry().getName() ); 
			} else {
				//System.out.println("Gene not present " + gfi.getBEDentry().getName() );
				gfi.findBPS();
			}
			gfi.extractPolyPyInfo();
		}
		
		System.out.println("Associate branch point sequence. Done." ); 
	}
	
	/**
	 * Rank 5'SS, 3'SS and BPS by frequency. 
	 */
	public void rankSequences() {
		
		//Count occurances. 
		final HashMap<String, Integer> fiveSSHash = new HashMap<String, Integer>(); 
		final HashMap<String, Integer> threeSSHash = new HashMap<String, Integer>();
		final HashMap<String, Integer> bpsHash = new HashMap<String, Integer>();
		
		for( GeneticFeatureInfo gfi : getGenes() ) {
			addValueToHash(fiveSSHash, gfi.getFiveSS());
			addValueToHash(threeSSHash, gfi.getThreeSS());
			addValueToHash(bpsHash, gfi.getBPS());
		}
		
		final HashMap<String, String> fiveSSRankHash = countToRankHash( fiveSSHash );
		final HashMap<String, String> threeSSRankHash = countToRankHash( threeSSHash );
		final HashMap<String, String> bpsRankHash = countToRankHash( bpsHash );
		
		final HashMap<String, String> fiveSSDistHash = rankHashToDistanceHash( fiveSSRankHash ); 
		final HashMap<String, String> threeSSDistHash = rankHashToDistanceHash( threeSSRankHash );
		final HashMap<String, String> bpsDistHash = rankHashToDistanceHash( bpsRankHash );
		
		for( GeneticFeatureInfo gfi : getGenes() ) {
			gfi.rankSequences( fiveSSRankHash, threeSSRankHash, bpsRankHash, 
					fiveSSDistHash, threeSSDistHash, bpsDistHash );
		}
	}

	private void addValueToHash( final HashMap<String, Integer> hash, final String value ) {
		if( hash.containsKey(value)) {
			hash.put(value, hash.get( value ) + 1 ); 
		} else {
			hash.put(value, 1 );
		}
	}
	
	/**
	 * Rank sequences based on their observation frequency. 
	 * @param countHash
	 * @return
	 */
	private HashMap<String, String> countToRankHash(final HashMap<String, Integer> countHash ) {
		
		final ArrayList<StringIntegerPair> pairs = new ArrayList<StringIntegerPair>(); 
		for( String key : countHash.keySet() ) {
			pairs.add( new StringIntegerPair(key, countHash.get(key))); 
		}
		
		Collections.sort(pairs, new StringIntegerPairComparator() );
		
		int rank = 0;
		HashMap<String, String> rankHash = new HashMap<String, String>(); 
		for( StringIntegerPair sip : pairs ) {
			if( sip.s.equals("NA") ) {
				rankHash.put(sip.s, "NA"); 
			} else {
				rank++; 
				rankHash.put(sip.s, String.valueOf(rank)); 
			}
			//System.out.println(sip.s + "\t" + sip.i);
		}
		
		return rankHash; 
	}
	
	/**
	 * Convert ranked sequence to score base on levenshtein distance.  
	 * @param countHash
	 * @return
	 */
	private HashMap<String, String> rankHashToDistanceHash(final HashMap<String, String> rankHash ) {
		
		//Find Rank 1 sequence.
		String rank1Seq = ""; 
		for( String key : rankHash.keySet() ) {
			 if( rankHash.get(key).equals("1") ) {
				 rank1Seq = key; 
				 break; 
			 }
		}
		 
		final HashMap<String, String> scoreHash = new HashMap<String, String>(); 
		for( String key : rankHash.keySet() ) {
			if( key.equals("NA") ) {
				scoreHash.put("NA", "NA"); 
			} else {
				scoreHash.put(key,  ("" + StringUtils.getLevenshteinDistance(rank1Seq, key))); 
			} 
		}
		
		
		return scoreHash; 
	}
	
	/**
	 * Write hallmark feature positions in BED format. 
	 * @param fileout
	 */
	public void writeHallmarkBEDFiles( final File fileout ) {
		PrintWriter out = TextFileAccess.openFileWrite( fileout.getAbsolutePath() ); 
		
		for( GeneticFeatureInfo gfi : getGenes() ) {
			//out.println(gfi); 
			for( BEDentry entry : gfi.getHallmarkBEDEntries() ) {
				out.println( entry ); 
			}  
		}
		
		out.flush(); 
		out.close();
	}
	
	public void writeTerminalExonBED(final File fileout ) {
		PrintWriter out = TextFileAccess.openFileWrite( fileout.getAbsolutePath() ); 
		
		BEDentry terminalExon; 
		for( GeneticFeatureInfo gfi : getGenes() ) {
			terminalExon = gfi.getBEDentry().getBlockAtRelativePosition(-1);
			terminalExon.setName( (gfi.getBEDentry().getName() + "_TerminalExon") );
			out.println(terminalExon); 
		}
		
		out.flush(); 
		out.close();
	}
	
	/**
	 * Write features to file 
	 */
	public void writeFeatureFile( final File fileout ) {
		
		PrintWriter out = TextFileAccess.openFileWrite( fileout.getAbsolutePath() ); 
		
		out.println(getGenes().get(0).getHeader()); 
		for( GeneticFeatureInfo gfi : getGenes() ) {
			out.println(gfi); 
			System.out.println(gfi); 
		}
		
		out.flush(); 
		out.close(); 
	}
	
	private void setBedFile( final File bedFile ) { this.bedFile = bedFile; }
	private void setGenomeFile( final File genomeFile ) { this.genomeFile = genomeFile; }
	private void setBPSFile( final File bpsFile ) { this.bpsFile = bpsFile; }  
	private void setGenes( final ArrayList<GeneticFeatureInfo> genes ) { this.genes = genes; }
	
	public File getBedFile() { return this.bedFile; } 
	public File getGenomeFile() { return this.genomeFile; } 
	public File getBPSFile() { return this.bpsFile; }
	public ArrayList<GeneticFeatureInfo> getGenes() { return this.genes; } 

	public static void main(String[] args) throws IOException
	{
		final long time = System.currentTimeMillis();
		
		File bedFile, genomeSequenceFile, bpsFile ; 
		if( args.length == 2 )
		{
			bedFile = new File( args[ 0 ] ); 
			genomeSequenceFile = new File( args[ 1 ] );
			bpsFile = new File( args[ 2 ] );
		}
		else 
		{
			System.err.println("Please provide bedFile, genome.fa and BPSSequenceFile \n");
			
			//bedFile = new File("/Users/carrillo/Dropbox/SMIT/annotations/Scer2/Scer2_snyder_allNoScore.bed");
			//genomeSequenceFile = new File( "/Users/carrillo/Dropbox/SMIT/annotations/Scer2/WholeGenomeFasta/genome.fa" );
			//bpsFile = new File( "/Users/carrillo/Dropbox/SMIT/annotations/aresBPS.txt" );
			
			bedFile = new File("/Users/carrillo/Dropbox/SMIT/annotations/Scer3/sacCer3_mod_5UTR.bed");
			genomeSequenceFile = new File( "/Users/carrillo/Dropbox/SMIT/annotations/Scer3/WholeGenomeFasta/genome.fa" );
			bpsFile = new File( "/Users/carrillo/Dropbox/SMIT/annotations/aresBPS.txt" );
		}
		
		final ExtractGeneticFeatures egf = new ExtractGeneticFeatures(bedFile, genomeSequenceFile, bpsFile);
		egf.readBedFile();
		egf.associateSpliceSequence();
		egf.associateBranchPoint();
		egf.rankSequences();
		egf.writeHallmarkBEDFiles( new File("/Users/carrillo/Dropbox/SMIT/annotations/Scer3/hallmarks.BED") );
		egf.writeTerminalExonBED( new File("/Users/carrillo/Dropbox/SMIT/annotations/Scer3/terminalExons.BED") );
		egf.writeFeatureFile( new File("/Users/carrillo/Dropbox/SMIT/annotations/Scer3/geneticFeatures.featureFile") );
			
		
		//gss.writeBED( new File( id +  "_spliceJunctionSpliceSitePositions.bed" ) );
		//gss.writeRelativeSpliceSitePosition( new File( id +  "_spliceJunctionSpliceSitePositionFeatures.txt" ) ); 
		
		
		System.out.println( "-----" );
		System.out.println( "Done. [" + (System.currentTimeMillis() - time)/1000 + " s]" );


	}

}
