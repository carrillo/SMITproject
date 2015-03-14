package parser;

import java.util.ArrayList;

public class BEDFilter {

	/**
	 * Filter ArrayList of BEDentries by block-count == exon count 
	 * @param entries
	 * @param minBlockCount
	 * @param maxBlockCount
	 * @return
	 */
	public static ArrayList<BEDentry> filterByBlockCount( final ArrayList<BEDentry> entries, 
			final int minBlockCount, final int maxBlockCount ) {
		
		final ArrayList<BEDentry> filteredEntries = new ArrayList<BEDentry>(); 
		
		for( BEDentry entry : entries ) {
			if( entry.getBlockCount() >= minBlockCount && entry.getBlockCount() <= maxBlockCount ) {
				filteredEntries.add( entry );  
			}
		}
		return filteredEntries; 		
	}

	/**
	 * Filter ArrayList of BEDentries by last intron length. Do not include entry if it does not contain an intron . 
	 * @param entries
	 * @param minBlockCount
	 * @param maxBlockCount
	 * @return
	 */
	public static ArrayList<BEDentry> filterByLastIntronLength( final ArrayList<BEDentry> entries, 
			final int minIntronLength, final int maxIntronLength ) {
		
		final ArrayList<BEDentry> filteredEntries = new ArrayList<BEDentry>(); 
		
		BEDentry terminalExon = null; 
		BEDentry secondTerminalExon = null;
		int intronLength = -1; 
		for( BEDentry entry : entries ) {
			if( entry.getBlockCount() > 1 ) {
				terminalExon = entry.getBlockAtRelativePosition(-1); 
				secondTerminalExon = entry.getBlockAtRelativePosition(-2);
				
				if( entry.isPlusStrand() ) {
					intronLength = terminalExon.getChromStart() - secondTerminalExon.getChromEnd();
				} else {
					intronLength = secondTerminalExon.getChromStart() - terminalExon.getChromEnd();
				}
				
				if( intronLength >= minIntronLength && intronLength <= maxIntronLength ) {
					filteredEntries.add(entry); 
				}
			}
			
		}
		return filteredEntries; 		
	}
}
