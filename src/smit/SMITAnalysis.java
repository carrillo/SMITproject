package smit;

import inputOutput.TextFileAccess;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import parser.BEDentry;

public class SMITAnalysis 
{
	protected SMITGene smitGene; 
	protected ArrayList<SMITReadpair> smitReadpairList; 
	protected ArrayList<int[]> posSplicingvalueList; 
	
	public SMITAnalysis( final ArrayList<SMITReadpair> smitReadpairlist, final SMITGene smitGene )
	{
		setSmitReadpairlist( smitReadpairlist ); 
		setSmitGene( smitGene ); 
	}
	
	public void analyze() 
	{
		//Sort the readpair by position.
		Collections.sort( getSmitReadpairlist(), new SMITReadpairPolymerasePosComparator() );
		setPosSplicingvalueList( generatePosSplicingvalueList() ); 
		makePosSplicingvalueListRelativeToFirst3SS();  
	}
	
	public ArrayList<int[]> generatePosSplicingvalueList() 
	{
		ArrayList<int[]> posSplicingList = new ArrayList<int[]>(); 
		
		int previousPos = -1; 
		ArrayList<SMITReadpair> samePosReads = new ArrayList<SMITReadpair>(); 
		for( SMITReadpair rp : getSmitReadpairlist() )
		{ 
			if( rp.getPolymerasePosition().getChromStart() != previousPos )
			{
				if( samePosReads.size() > 0 )
				{
					posSplicingList.add( getPosSplicingvaluePair( samePosReads ) ); 					
				}
				
				previousPos = rp.getPolymerasePosition().getChromStart();
				samePosReads = new ArrayList<SMITReadpair>(); 
			}
			
			samePosReads.add( rp );  
		}
		posSplicingList.add( getPosSplicingvaluePair( samePosReads ) );
		
		return posSplicingList; 
	}
	
	public int[] getPosSplicingvaluePair( final ArrayList<SMITReadpair> samePosReads )
	{
		final int[] posSplicingvaluePair = new int[ 3 ]; 
				
		int spliced = 0;
		int unspliced = 0; 
		for( SMITReadpair rp : samePosReads )
		{
			if( rp.isSpliced() )
			{
				spliced++; 				
			}
			else
			{
				unspliced++; 
			}
		}
		
		//System.out.println( samePosReads.get( 0 ).getPolymerasePosition().getChromStart() + " " + spliced + " " + samePosReads.size() ); 
		
		posSplicingvaluePair[ 0 ] = samePosReads.get( 0 ).getPolymerasePosition().getChromStart();
		posSplicingvaluePair[ 1 ] = spliced;
		posSplicingvaluePair[ 2 ] = unspliced; 
		
		//System.out.println( posSplicingvaluePair[ 0 ] + "\t" + posSplicingvaluePair[ 1 ] ); 
		
		return posSplicingvaluePair; 
	}
	
	/**
	 * This method makes the absolute position values relative to the postion of the 5'SS;
	 */
	public void makePosSplicingvalueListRelativeToFirst3SS()
	{
		ArrayList<int[]> newPosSplicingList = new ArrayList<int[]>(); 
		//System.out.println(getSmitGene().getName() + "\t" + getSmitGene().getBlockCount() ); 
		final BEDentry secondExon = getSmitGene().getBlockAtRelativePosition( 2 ); 
		int fivePrimeSSPos; 
		if( getSmitGene().isPlusStrand() )
		{
			fivePrimeSSPos = secondExon.getChromStart(); 
			for( int i = 0; i < getPosSplicingvalueList().size(); i++ )
			{
				final int[] posSplicingPair = new int[ 3 ];
				posSplicingPair[ 0 ] = getPosSplicingvalueList().get( i )[ 0 ] - fivePrimeSSPos; 
				posSplicingPair[ 1 ] = getPosSplicingvalueList().get( i )[ 1 ];
				posSplicingPair[ 2 ] = getPosSplicingvalueList().get( i )[ 2 ];
				newPosSplicingList.add( posSplicingPair ); 
			}
		}
		else
		{
			fivePrimeSSPos = secondExon.getChromEnd();  
			for( int i = getPosSplicingvalueList().size() - 1; i >= 0; i-- )
			{
				final int[] posSplicingPair = new int[ 3 ];
				posSplicingPair[ 0 ] = fivePrimeSSPos - getPosSplicingvalueList().get( i )[ 0 ]; 
				posSplicingPair[ 1 ] = getPosSplicingvalueList().get( i )[ 1 ];
				posSplicingPair[ 2 ] = getPosSplicingvalueList().get( i )[ 2 ]; 
				newPosSplicingList.add( posSplicingPair );
			}
		}
		
		setPosSplicingvalueList( newPosSplicingList ); 
	}
	
	public void writePosSplicingvalueListToDir( final File directory )
	{
		final String fileName = directory.getAbsolutePath() + "/" + getSmitGene().getName() + ".smit"; 
		
		PrintWriter out = TextFileAccess.openFileWrite( fileName );
		
		final String header = "relPosition" + "\t" + "splicedLength" + "\t" + "unsplicedLength" + "\t" + "splicedReads" + "\t" + "unsplicedReads";
		out.println( header );
		
		int relPosition, splicedLength, unsplicedLength; 
		for( int[] posSplicingPair : getPosSplicingvalueList() )
		{
			relPosition = posSplicingPair[ 0 ]; 
			splicedLength = getSmitGene().primerIntronDistance + posSplicingPair[ 0 ];
			unsplicedLength = getSmitGene().getIntronLength() + splicedLength; 
			out.println( relPosition + "\t" + splicedLength + "\t" + unsplicedLength + "\t" + posSplicingPair[ 1 ] + "\t" + posSplicingPair[ 2 ]); 
		}
		
		out.close(); 
	}
	
	public void filterPosSplicingvalueListByAbsoluteReadcount( final int minReadCount, final int maxReadCount )
	{
		ArrayList<int[]> newList = new ArrayList<int[]>();
		
		int readCount = 0; 
		for( int[] valuePair : getPosSplicingvalueList() )
		{
			readCount = valuePair[ 1 ] + valuePair[ 2 ]; 
			if( readCount >= minReadCount && readCount <= maxReadCount )
				newList.add( valuePair ); 
		}
		setPosSplicingvalueList( newList ); 
	}
	
	public String toString()
	{
		String s = getSmitGene().getName() + "\n";
		for( int[] posSplicingPair : getPosSplicingvalueList() )
		{
			s += posSplicingPair[ 0 ] + "\t" + posSplicingPair[ 1 ] + "\t" + posSplicingPair[ 2 ] + "\n"; 
		}
			
		return s; 
	}
	
	private void setSmitReadpairlist( final ArrayList<SMITReadpair> smitReadpairList ) { this.smitReadpairList = smitReadpairList; } 
	private ArrayList<SMITReadpair> getSmitReadpairlist() { return this.smitReadpairList; }
	
	private void setSmitGene( final SMITGene smitGene ) { this.smitGene = smitGene; } 
	private SMITGene getSmitGene() { return this.smitGene; }
	
	private void setPosSplicingvalueList( final ArrayList<int[]> posSplicingvalueList ) { this.posSplicingvalueList = posSplicingvalueList; } 
	public ArrayList<int[]> getPosSplicingvalueList() { return this.posSplicingvalueList; } 
}
