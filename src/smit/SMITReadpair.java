package smit;

import parser.BEDentry;
import parser.SAMTools;
import net.sf.samtools.SAMRecord;

/**
 * This class holds the information of any readpair (i.e. forward and reverse read from NGS). It further 
 * stores the state of the forward read (Spliced vs. not spliced) gained from mapping.  
 * @author carrillo
 *
 */
public class SMITReadpair 
{	
	//Associated gene
	protected SMITGene parentGene = null; 
	//Forward and reverse read
	protected SAMRecord forwardRead = null, reverseRead = null;
	//Read Id; 
	protected String readName; 
	//The splicing state 
	protected boolean spliced, forwardAndReverseRead = false, valid = true;
	//The position of the polymerase based on the reverse read
	protected BEDentry polymerasePosition;  
	
	//This constructor takes the reverse read of the SAM files 
	public SMITReadpair( final SAMRecord reverseRead )
	{
		setReverseRead( reverseRead ); 
		setReadName( getReverseRead().getReadName() );  
		setPolymerasePosition( determinePolymerasePosition() ); 
	}
	
	/**
	 * This method calculates the polymerase position based on the position of the reverse read
	 * 
	 * THE GETTHREEPRIMEEND METHOD IS NOT TESTED!
	 * @return
	 */
	public BEDentry determinePolymerasePosition()
	{
		//return SAMTools.getThreePrimeEnd( getReverseRead() );
		
		return SAMTools.getThreePrimeEnd( getReverseRead() );
	}
	
	
	/*
	 * This method gets a read (SAMRecord) and a boolean if this read stems from the spliced or unspliced
	 * mapping.
	 * @param forwardRead SAMRecord of the forward read
	 * @param spliced boolean if present; 
	 * @return
	 */
	public boolean addForwardRead( final SAMRecord forwardRead, final boolean spliced, final SMITGeneCollection SMITGeneCollection, final boolean verbose )
	{ 
		//Only process those reads which are mapped
		if( forwardRead.getReadUnmappedFlag() )
			return false; 
		else 
		{
			if( getForwardRead() == null )
			{
				//Set forward read and splice information 
				setForwardRead( forwardRead ); 
				setSpliced( spliced );
				setForwardAndReverseRead( true );
				
				
				//Read the name of the associated gene from the SAM file. Lydia added the splicing state to the id. remove it here
				final String[] geneId = forwardRead.getReferenceName().split("_");
				//Link to the right gene in the colloection.
				setParentGene( SMITGeneCollection.getGeneHashMap().get( geneId[ 0 ] ) ); 
				 
				//Register readpair at parentgene. 
				getParentGene().register( this ); 
				
				return true; 	
			}
			else
			{
				if( verbose )
					System.err.println( "Forward read already present:\nOld entry: " + getForwardRead().toString() + "\nNew entry: " + forwardRead.toString() );
				
				setValid( false ); 
				return false; 
			}
		}
		
	}
	
	/**
	 * This method generates a information string about this object
	 */
	public String toString()
	{
		String s = "Name: " + getReadName() + " Forward read: " + getForwardRead() + " Reverse read: " + getReverseRead();
		return s; 
	}
	
	
	//Setter and getter method for instance variables follow 
	//Setters and getters are present to be able to control everything which is happening to the instance variables to minimize side effects
	public void setParentGene( final SMITGene parentGene ) { this.parentGene = parentGene; } 
	public SMITGene getParentGene() { return this.parentGene; } 
	
	public void setSpliced( final boolean spliced ) { this.spliced = spliced; }
	public boolean isSpliced() { return this.spliced; }
	
	public void setForwardAndReverseRead( final boolean forwardAndReverseRead ) { this.forwardAndReverseRead = forwardAndReverseRead; } 
	public boolean hasForwardAndReverseRead() { return forwardAndReverseRead; }
	
	public void setValid( final boolean valid ) { this.valid = valid; } 
	public boolean isValid() { return this.valid; } 
	
	public void setForwardRead( final SAMRecord forwardRead ) { this.forwardRead = forwardRead; } 
	public SAMRecord getForwardRead() { return this.forwardRead; }
	
	public void setReverseRead( final SAMRecord reverseRead ) { this.reverseRead = reverseRead; } 
	public SAMRecord getReverseRead() { return this.reverseRead; }
	
	public void setReadName( final String readName ) { this.readName = readName; }
	public String getReadName() { return this.readName; } 
	
	public void setPolymerasePosition( final BEDentry polymerasePosition ) { this.polymerasePosition = polymerasePosition; } 
	public BEDentry getPolymerasePosition() { return this.polymerasePosition; } 
	
}
