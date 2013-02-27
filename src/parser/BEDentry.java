package parser;

import java.util.ArrayList;


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
	
	/**
	 * This method extracts all exons   
	 */
	public ArrayList<BEDentry> getBlocks()
	{ 
		//Extract String[] holding start values (blockStart) and length (blockSize) of all exons.  
		final String[] blockStart = getBlockStarts().split(",");
		final String[] blockSize = getBlockSizes().split(",");
		
		int start, end; 
		String strand = "-"; 
		if( isPlusStrand() )
			strand = "+"; 
		
		ArrayList<BEDentry> blocks = new ArrayList<BEDentry>(); 
		//Loop through all exons (Exon count is contained in BED field 10). 
		for( int i = 0; i < getBlockCount() ; i++ )
		{
			//Assign absolute start and end values for each exon. This value doesn't consider strandness
			start = getChromStart() + Integer.parseInt( blockStart[ i ] ); 
			end = start + Integer.parseInt( blockSize[ i ] ); 
			
			//Generate new BED entry for each exon. Each Exon ID is derived from the transcript ID followed by "_Exon" the exon count, followed by "_Up" the extension upstream, followed by "_Do" the extension Downstream. 
			String bedEntry = getChrom() + "\t" + start + "\t" + end + "\t" + "Exon_" + i + "\t" + getScore() + "\t";
			bedEntry +=  strand + "\t" + start + "\t" + end + "\t" + getItemRgb() + "\t" + 1 + "\t" + ( end - start ) + "\t" + 0; 
			
			blocks.add( new BEDentry( bedEntry ) ); 
		}
		
		return blocks; 
	}
	
	/**
	 * This method returns the block (exon) at a defined relative position. 
	 * Use positive values for blocks relative to gene start and negative relative to gene ends. 
	 * @param relativePosition
	 * @return
	 */
	public BEDentry getBlockAtRelativePosition( final int relativePosition )
	{
		ArrayList<BEDentry> blocks = getBlocks(); 
		
		int index = -1; 
		//Extract blocks relative to start 
		if( relativePosition > 0 )
		{
			if( isPlusStrand() )
			{
				index = relativePosition - 1; 
			}
			else
			{
				index = blocks.size() - relativePosition; 
			}
		}
		//Extract blocks relative to end
		else
		{	
			if( isPlusStrand() )
			{
				index = blocks.size() + relativePosition; 
			}
			else
			{
				index = Math.abs( relativePosition ) - 1;   
			}
		}
		return blocks.get( index ); 
	}
	
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
