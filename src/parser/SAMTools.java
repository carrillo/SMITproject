package parser;

import net.sf.samtools.SAMRecord;

public class SAMTools 
{	
	public static BEDentry getThreePrimeEnd( final SAMRecord samRecord )
	{
		//Initiate bed entry
		BEDentry bed = new BEDentry();
		
		bed.setChrom( samRecord.getReferenceName() ); 
		bed.setName( samRecord.getReadName() ); 
		
		bed.setScore( 0 ); 
		bed.setItemRgb(  "0,0,0" );
		 
		
		bed.setBlockCount( 1 ); 
		bed.setBlockSizes( "1," ); 
		bed.setBlockStarts( "0," ); 
		
		
		//Assign strand specific values. 
		String strand;
		int start; 
		int end; 
		if( !samRecord.getReadNegativeStrandFlag() )
		{
			strand = "+"; 
			start = samRecord.getAlignmentEnd(); 
			end = start + 1; 
		}
		else 
		{
			strand = "-"; 
			start = samRecord.getAlignmentStart(); 
			end = start + 1; 
		}
		
		bed.setStrand( strand ); 
		bed.setChromStart( start ); 
		bed.setChromEnd( end );
		bed.setThickStart( start ); 
		bed.setThickStart( end ); 
		
		return bed; 
	}
	
	public static BEDentry getFivePrimeEnd( final SAMRecord samRecord )
	{
		//Initiate bed entry
		BEDentry bed = new BEDentry();
		
		bed.setChrom( samRecord.getReferenceName() ); 
		bed.setName( samRecord.getReadName() ); 
		
		bed.setScore( 0 ); 
		bed.setItemRgb(  "0,0,0" );
		 
		
		bed.setBlockCount( 1 ); 
		bed.setBlockSizes( "1," ); 
		bed.setBlockStarts( "0," ); 
		
		
		//Assign strand specific values. 
		String strand;
		int start; 
		int end; 
		if( !samRecord.getReadNegativeStrandFlag() )
		{
			strand = "+"; 
			start = samRecord.getAlignmentStart(); 
			end = start + 1; 
		}
		else 
		{
			strand = "-"; 
			start = samRecord.getAlignmentEnd(); 
			end = start + 1; 
		}
		
		bed.setStrand( strand ); 
		bed.setChromStart( start ); 
		bed.setChromEnd( end );
		bed.setThickStart( start ); 
		bed.setThickStart( end ); 
		
		return bed; 
	}
}
