package featureExtraction;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.AmbiguityDNACompoundSet;

import parser.BEDentry;
import parser.FastaEntry;

public class GeneticFeatureInfo {
	
	private BEDentry bedEntry; 
	private String intron; 
	private String terminalExon; 
	
	private double intronGCContent;
	private double polyPyGCContent;
	private double terminalExonGCContent; 
	
	private String fiveSS = "NA"; 
	private String threeSS = "NA"; 
	private String bps = "NA"; 
	
	private String fiveSSDist; 
	private String threeSSDist; 
	private String bpsDist;
	
	private int BPSThreeSSDistance; 

	
	private String fiveSSRank, threeSSRank, bpsRank; 
	
	public GeneticFeatureInfo( final BEDentry bedEntry ) {
		this.bedEntry = bedEntry;
		//System.out.println(this); 
	}
	
	/**
	 * Associate splice sequences. 
	 * @param chromosomeSeq
	 */
	public void associateSequence( final FastaEntry chromosomeSeq ) {
		this.intron = getIntron( chromosomeSeq );
		this.terminalExon = getTerminalExon( chromosomeSeq );
		
		this.fiveSS = this.intron.substring(0, 6);
		this.threeSS = this.intron.substring( intron.length() - 3 );
		
		this.intronGCContent = getGCFraction( this.intron ); 
		this.terminalExonGCContent = getGCFraction( this.terminalExon ); 
	}
	
	
	public void associateBPS( final String bps ) {
		this.bps =  bps.substring(1, 8); 
		
		//Check if BPS is contained in the intron sequence. If not, find BPS using sequence. 
		if( this.intron.lastIndexOf( getBPS() ) < 1 ) {
			findBPS();
		}
	}
	
	/**
	 * Find sequence best matching BPS consensus sequence between the 5SS and 3SS 
	 */
	public void findBPS() {
		final String consensus = "UACUAAC";
		String candidate = ""; 
		int currentDistance;
		
		int bestDistance = Integer.MAX_VALUE;
		String bestCandidate = ""; 
		for( int i = 6; i < this.intron.length() - consensus.length() - 2; i++ ) {
			candidate = this.intron.substring( i, (i+consensus.length()) );
			currentDistance = StringUtils.getLevenshteinDistance(consensus, candidate); 
			if( currentDistance <= bestDistance ) {
				bestCandidate = candidate; 
				bestDistance = currentDistance; 
			}
		}
		
		//System.out.println( bestCandidate + "\t" + bestDistance ); 
		this.bps = bestCandidate; 
	}
	
	/**
	 * Set PolyPy length (Distance between BPS and 3'SS) and GC content.
	 * 1. Get BPS positon.  
	 */
	public void extractPolyPyInfo() { 
		final int bpsEnd = this.intron.lastIndexOf( getBPS() ) + getBPS().length(); 
		this.BPSThreeSSDistance = (this.intron.length() - ( bpsEnd ));
		this.polyPyGCContent = getGCFraction( intron.substring( bpsEnd ) ); 
	}
	
	private double getGCFraction( final String seq ) { 
		int gcCount = 0;
		String nt; 
		for( int i = 0; i < seq.length(); i++ ) {
			nt = seq.substring(i, i+1); 
			if( nt.equals("G") || nt.equals("C")) {
				gcCount++;  
			}
		}
		
		return  ( (double) gcCount / seq.length()); 
	}
	
	public void rankSequences( final HashMap<String, String> fiveSSRankHash, 
			final HashMap<String, String> threeSSRankHash, 
			final HashMap<String, String> bpsRankHash, 
			final HashMap<String, String> fiveSSDistHash, 
			final HashMap<String, String> threeSSDistHash,
			final HashMap<String, String> bpsDistHash ) {
		
		this.fiveSSRank = fiveSSRankHash.get(getFiveSS()); 
		this.threeSSRank = threeSSRankHash.get(getThreeSS());
		this.bpsRank = bpsRankHash.get(getBPS());
		
		this.fiveSSDist = fiveSSDistHash.get(getFiveSS()); 
		this.threeSSDist = threeSSDistHash.get(getThreeSS()); 
		this.bpsDist = bpsDistHash.get(getBPS()); 
	}
	
	/**
	 * Extract intron sequence. 
	 * @param chromosomeSeq
	 * @return
	 */
	private String getIntron( final FastaEntry chromosomeSeq) { 
		
		String intron = ""; 
		if( getBEDentry().isPlusStrand() ) {
			intron = chromosomeSeq.getSubsequence((getLast5SSPos()+1), getLast3SSPos());  
		} else {
			intron = chromosomeSeq.getSubsequence(getLast3SSPos()+1, getLast5SSPos()); 
			intron = new DNASequence( intron, AmbiguityDNACompoundSet.getDNACompoundSet() ).getReverseComplement().getSequenceAsString();
		}
		
		
		return new DNASequence( intron, AmbiguityDNACompoundSet.getDNACompoundSet() ).getRNASequence().toString(); 
	}
	
	/**
	 * Extract terminal exon sequence. 
	 * @param chromosomeSeq
	 * @return
	 */
	private String getTerminalExon( final FastaEntry chromosomeSeq ) {
		String exon = ""; 
		if( getBEDentry().isPlusStrand() ) {
			exon = chromosomeSeq.getSubsequence((getLast3SSPos()+1), getPolyA());  
		} else {
			exon = chromosomeSeq.getSubsequence(getPolyA()+1, getLast3SSPos()); 
			exon = new DNASequence( exon, AmbiguityDNACompoundSet.getDNACompoundSet() ).getReverseComplement().getSequenceAsString();
		}
		
		
		return new DNASequence( exon, AmbiguityDNACompoundSet.getDNACompoundSet() ).getRNASequence().toString();
	}
	
	
	/**
	 * Returns transcriptional start site of gene. 
	 * @return
	 */
	public int getTSS() { 
		return( getStartOfBEDentry( getBEDentry().getBlockAtRelativePosition(1))); 
	}
	
	/**
	 * Returns the end of the gene.  
	 * @return
	 */
	public int getPolyA() { 
		return( getEndOfBEDentry( getBEDentry().getBlockAtRelativePosition(-1)));
	}
	
	/**
	 * Returns the start of a BEDentry in transcriptional terms 
	 * @param entry
	 * @return
	 */
	private int getStartOfBEDentry(final BEDentry entry ) {
		if( entry.getStrand().equals("+") ) { 
			return entry.getChromStart();   
		} else {
			return entry.getChromEnd(); 
		}
	}
	
	/**
	 * Returns the end of a BEDentry in transcriptional terms 
	 * @param entry
	 * @return
	 */
	private int getEndOfBEDentry(final BEDentry entry ) {
		if( entry.getStrand().equals("+") ) { 
			return entry.getChromEnd();   
		} else {
			return entry.getChromStart(); 
		}
	}
	
	/**
	 * Returns the position of the last 5SS of the gene
	 * @return
	 */
	public int getLast5SSPos() {
		final BEDentry secondLastExon = getBEDentry().getBlockAtRelativePosition(-2);
		return getEndOfBEDentry(secondLastExon); 
	}
	
	/**
	 * Returns the position of the last 3SS of the gene
	 * @return
	 */
	public int getLast3SSPos() {
		return getStartOfBEDentry(getBEDentry().getBlockAtRelativePosition(-1)); 
	}
	
	/**
	 * Returns the length of the intron 
	 * @return
	 */
	public int getIntronLength() { 
		final int start = getLast5SSPos(); 
		final int end = getLast3SSPos();
		return( Math.abs( end - start ) ); 
	}
	
	/**
	 * Returns the length of the terminal exon 
	 * @return
	 */
	public int getTerminalExonLength() { 
		final BEDentry te = getBEDentry().getBlockAtRelativePosition(-1);  
		return( te.getChromEnd() - te.getChromStart() ); 
	}
	
	/**
	 * Return field names of the toString method. 
	 * @return
	 */
	public String getHeader() {
		String header = "id" + "," + "chrId" + "," + "strand" + "," + "TSS" + "," + "PolyASite"
						+ "," + "5SSPos" + "," + "3SSPos" + "," + "intronLength" + "," + "terminalExonLength"
						+ "," + "BPS_3SS_distance" + "," + "PolyPyGCContent" + "," + "IntronGCContent" + "," + "terminalExonGCContent"
						+ "," + "5SS" + "," + "3SS" + "," + "BPS"
						+ "," + "5SSRank" + "," + "3SSRank" + "," + "BPSRank"
						+ "," + "5SSLevenshteinDistance" + "," + "3SSLevenshteinDistance" + "," + "BPSLevenshteinDistance";
		return header; 
	}
	
	public String toString() { 
		String s = getBEDentry().getName() + "," + getBEDentry().getChrom() + "," + getBEDentry().getStrand() 
				+ "," + getTSS() + "," + getPolyA() + "," + getLast5SSPos() + "," + getLast3SSPos() 
				+ "," + getIntronLength() + "," + getTerminalExonLength()
				+ "," + getBPSThreeSSDistance() + "," + getPolyPyGCContent() + "," + getIntronGCContent() + "," + getTerminalExonGCContent()
				+ "," + getFiveSS() + "," + getThreeSS() + "," + getBPS()
				+ "," + getFiveSSRank() + "," + getThreeSSRank()+ "," + getBPSRank()
				+ "," + getFiveSSDist() + "," + getThreeSSDist()+ "," + getBPSDist();
		
		return s; 
	}
	
	/**
	 * Returns list of positions of hallmark features in BED file format. 
	 * Hallmark features are 1) last 5SS, 2) last 3SS and 3) polyA site
	 * @return
	 */
	public ArrayList<BEDentry> getHallmarkBEDEntries() {
		
		final String chrom = getBEDentry().getChrom(); 
		final String parent_name = getBEDentry().getName(); 
		final String strand = getBEDentry().getStrand(); 
		 
		ArrayList<BEDentry> bedList = new ArrayList<BEDentry>(); 
		bedList.add( new BEDentry(chrom, getLast5SSPos(), getLast5SSPos()+1, (parent_name + "_5SS"), strand ) );  
		bedList.add( new BEDentry(chrom, getLast3SSPos(), getLast3SSPos()+1, (parent_name + "_3SS"), strand ) );
		bedList.add( new BEDentry(chrom, getPolyA(), getPolyA()+1, (parent_name + "_PolyASite"), strand ) ); 
		
	
		return( bedList );  
	}
	
	public BEDentry getBEDentry() { return this.bedEntry; }
	public String getFiveSS() { return this.fiveSS; } 
	public String getThreeSS() { return this.threeSS; }
	public String getBPS() { return this.bps; }
	public int getBPSThreeSSDistance() { return this.BPSThreeSSDistance; } 
	public double getPolyPyGCContent() { return this.polyPyGCContent; }
	
	public String getFiveSSRank() { return this.fiveSSRank; }
	public String getThreeSSRank() { return this.threeSSRank; }
	public String getBPSRank() { return this.bpsRank; }
	public String getFiveSSDist() { return this.fiveSSDist; }
	public String getThreeSSDist() { return this.threeSSDist; }
	public String getBPSDist() { return this.bpsDist; }
	
	
	public double getIntronGCContent() { return this.intronGCContent; }
	public double getTerminalExonGCContent() { return this.terminalExonGCContent; }
}
