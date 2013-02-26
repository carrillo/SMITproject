package smit;

import java.util.ArrayList;
import java.util.Collections;

public class SMITAnalysis 
{
	protected SMITGene smitGene; 
	protected ArrayList<SMITReadpair> smitReadpairList; 
	protected ArrayList<double[]> posSplicingvalueList; 
	
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
		makePosSplicingvalueListRelativeToTSS(); 
	}
	
	public ArrayList<double[]> generatePosSplicingvalueList() 
	{
		ArrayList<double[]> posSplicingList = new ArrayList<double[]>(); 
		
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
		
		return posSplicingList; 
	}
	
	public double[] getPosSplicingvaluePair( final ArrayList<SMITReadpair> samePosReads )
	{
		final double[] posSplicingvaluePair = new double[ 2 ]; 
				
		int spliced = 0;
		for( SMITReadpair rp : samePosReads )
		{
			if( rp.isSpliced() )
			{
				spliced++; 				
			}
		}
		
		//System.out.println( samePosReads.get( 0 ).getPolymerasePosition().getChromStart() + " " + spliced + " " + samePosReads.size() ); 
		
		posSplicingvaluePair[ 0 ] = (double) samePosReads.get( 0 ).getPolymerasePosition().getChromStart();
		posSplicingvaluePair[ 1 ] = ( (double) spliced / (double) samePosReads.size() );  
		
		//System.out.println( posSplicingvaluePair[ 0 ] + "\t" + posSplicingvaluePair[ 1 ] ); 
		
		return posSplicingvaluePair; 
	}
	
	public void makePosSplicingvalueListRelativeToTSS()
	{
		ArrayList<double[]> newPosSplicingList = new ArrayList<double[]>(); 
		 
		if( getSmitGene().isPlusStrand() )
		{
			final int tssPos = getSmitGene().getChromStart();
			for( int i = 0; i < getPosSplicingvalueList().size(); i++ )
			{
				final double[] posSplicingPair = new double[ 2 ];
				posSplicingPair[ 0 ] = getPosSplicingvalueList().get( i )[ 0 ] - tssPos; 
				posSplicingPair[ 1 ] = getPosSplicingvalueList().get( i )[ 1 ];
				newPosSplicingList.add( posSplicingPair ); 
			}
		}
		else
		{
			final int tssPos = getSmitGene().getChromEnd(); 
			for( int i = getPosSplicingvalueList().size() - 1; i >= 0; i-- )
			{
				final double[] posSplicingPair = new double[ 2 ];
				posSplicingPair[ 0 ] = getPosSplicingvalueList().get( i )[ 0 ] - tssPos; 
				posSplicingPair[ 1 ] = getPosSplicingvalueList().get( i )[ 1 ];
				newPosSplicingList.add( posSplicingPair );
			}
		}
		
		setPosSplicingvalueList( newPosSplicingList ); 
	}
	
	public String toString()
	{
		String s = getSmitGene().getName() + "\n";
		for( double[] posSplicingPair : getPosSplicingvalueList() )
		{
			s += posSplicingPair[ 0 ] + "\t" + posSplicingPair[ 1 ] + "\n"; 
		}
			
		return s; 
	}
	
	private void setSmitReadpairlist( final ArrayList<SMITReadpair> smitReadpairList ) { this.smitReadpairList = smitReadpairList; } 
	private ArrayList<SMITReadpair> getSmitReadpairlist() { return this.smitReadpairList; }
	
	private void setSmitGene( final SMITGene smitGene ) { this.smitGene = smitGene; } 
	private SMITGene getSmitGene() { return this.smitGene; }
	
	private void setPosSplicingvalueList( final ArrayList<double[]> posSplicingvalueList ) { this.posSplicingvalueList = posSplicingvalueList; } 
	public ArrayList<double[]> getPosSplicingvalueList() { return this.posSplicingvalueList; } 
}
