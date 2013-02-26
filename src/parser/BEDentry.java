package parser;


public class BEDentry 
{
	protected String chrom = "", name = "", itemRgb = "0,0,0", blockSizes = "", blockStarts ="", strand = "";
	protected int chromStart, chromEnd, score = 0, blockCount, thickStart, thickEnd;
	protected boolean plusStrand; 
	
	/**
	 * This method constructs a BEDentry object by taking an input line and assigning values to the instance variables
	 * @param line
	 */
	public BEDentry (final String line )
	{
		//Split the line at the tab delimiter into a string array
		String[] entries = line.split("\t"); 
		
		setChrom( entries[ 0 ] );
		setChromStart( Integer.parseInt( entries[ 1 ] ) );
		setChromEnd( Integer.parseInt( entries[ 2 ] ) );
		setName( entries[ 3 ] );
		
		setScore( Integer.parseInt( entries[ 4 ] ) ); 
		setStrand( entries[ 5 ] ); 
		setThickStart( Integer.parseInt( entries[ 6 ] ) ); 
		setThickEnd( Integer.parseInt( entries[ 7 ] ) );
		
		setItemRgb( entries[ 8 ] );
		setBlockCount( Integer.parseInt( entries[ 9 ] ) ); 
		setBlockSizes( entries[ 10 ] ); 
		setBlockStarts( entries[ 11 ] ); 
		
	}
	
	public BEDentry(){}
	
	
	public String toString()
	{
		String s = chrom + "\t" + chromStart + "\t"  + chromEnd + "\t"  + name + "\t" + score + "\t";
		if( plusStrand ) { s += "+"; }
		else { s += "-"; }
		s += "\t"  + getChromStart() + "\t"  + getChromEnd() + "\t";
		s += itemRgb + "\t" + getBlockCount() + "\t"  + getBlockSizes() + "\t"  + getBlockStarts() ;
		return s; 
	}

	public String getChrom() { return chrom; }
	public void setChrom(String chrID) { this.chrom = chrID; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getItemRgb() { return itemRgb; }
	public void setItemRgb(String itemRgb) { this.itemRgb = itemRgb; }

	public String getBlockSizes() { return blockSizes; }
	public void setBlockSizes(String blockSize) { this.blockSizes = blockSize; }

	public String getBlockStarts() { return blockStarts; }
	public void setBlockStarts(String blockStart) { this.blockStarts = blockStart; }

	public int getChromStart() { return chromStart; }
	public void setChromStart(int start) { this.chromStart = start; }

	public int getChromEnd() { return chromEnd; }
	public void setChromEnd(int end) { this.chromEnd = end; }
	
	public int getThickStart() { return this.thickStart; }
	public void setThickStart( final int thickStart ) { this.thickStart = thickStart; }
	
	public int getThickEnd() { return this.thickEnd; }
	public void setThickEnd( final int thickEnd ) { this.thickEnd = thickEnd; }
	
	public int getScore() {	return score; }
	public void setScore(int score) { this.score = score; }

	public int getBlockCount() { return blockCount; }
	public void setBlockCount(int blockCount) { this.blockCount = blockCount; }

	public void setStrand( final String strand ) { this.strand = strand; if( strand.equals("+")) setPlusStrand( true ); }
	public String getStrand() { return this.strand; } 
 	public boolean isPlusStrand() { return plusStrand; }
	public void setPlusStrand(boolean plusStrand) { this.plusStrand = plusStrand; }
}
