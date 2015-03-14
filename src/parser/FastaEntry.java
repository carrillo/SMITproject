package parser;

import java.util.HashMap;
import java.util.HashSet;

public class FastaEntry 
{
	protected String id; 
	protected HashMap<Integer, Character> sequenceHash; 
	
	public FastaEntry( final String fastaLine ) 
	{ 
		setId( fastaLine );
		setSequenceHash( fastaLine );
		System.out.println( "Current fasta: " + getId() + "\t" + getSequenceHash().size() ); 
	}
	
	/*
	 * Returns subsequence from [start,end] (including both positions).
	 */
	public String getSubsequence( final int start, final int end )
	{
		final char[] a = new char[ ( end - start ) + 1 ];
		int index = 0;  
		for( int i = start; i <= end; i++ )
		{
			a[ index ] = getSequenceHash().get( i ); 
			index++; 
		}
		
		return new String( a ); 
	}
	
	
	//Setter
	private void setId( final String entry ) { this.id = entry.substring( 1, entry.indexOf("\n") ); }
	private void setSequenceHash( final String entry ) 
	{
		final int idOffset = id.length(); 
		final String sequence = entry.replace("\n", "").substring( idOffset + 1 ); 
		
		HashMap<Integer, Character> sequenceHash = new HashMap<Integer, Character>(); 
		for( int i = 0; i < sequence.length(); i++ )
		{
			sequenceHash.put( (i+1) , sequence.charAt( i ) ); 
		}
		
		this.sequenceHash = sequenceHash;  
	}
	
	//Getter
	public String getId() { return this.id; }
	public HashMap<Integer, Character> getSequenceHash() { return this.sequenceHash; } 
	
}
