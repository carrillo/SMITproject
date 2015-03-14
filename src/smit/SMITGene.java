package smit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import parser.BEDentry;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

/**
 * Hi Lydia, 
 * This class SMITGenes is a subclass of BEDentry. Therefore it inherits all methods and instance variables
 * defined in its superclass (BEDentry). It further extends the characteristic of its superclass (BEDentry)
 * by all methods, instance variables etc. I define here below.
 * @author carrillo
 *
 */
public class SMITGene extends BEDentry 
{
	protected ArrayList<SMITReadpair> SMITReadpairList; 
	private boolean sortedSMITReadpairList = false; 
	protected SMITAnalysis smitAnalysis;
	protected int primerIntronDistance; 
	protected String forPrimerSequence; 
	
	/**
	 * To be able to generate an instance of this subclass we have to call the constructer of the superclass
	 * first: "this.super()". This is important so that java knows that all subclasses will just extend the
	 * superclass and thus can substitute it everywhere.  
	 * @param line
	 */
	public SMITGene(String line) 
	{
		//Call the constructor of the superclass.
		super(line);
		//Generate an empty SMITReadpairList
		setSMITReadpairList( new ArrayList<SMITReadpair>() ); 
	}
	
	/*
	 * This method registers a readpair to this gene. 
	 */
	public void register( final SMITReadpair SMITreadpair )
	{ 
		addSMITReadpair( SMITreadpair ); 
	}
	
	public void unregister( final SMITReadpair SMITreadpair )
	{
		getSMITReadpairList().remove( SMITreadpair );
	}
	
	/*
	 * This method sorts the SMITReadpairList based on the polymerase position 
	 */
	public void sortSMITReadpairList() 
	{
		if( !getSortedSMITReadpairList() )
		{
			Collections.sort(getSMITReadpairList(), new SMITReadpairPolymerasePosComparator() ); 
			setSortedSMITReadpairList( true ); 
		}
	}
	
	/*
	 * Associate primer info with smit gene. 
	 * 1. Add primer intron distance value. 
	 */
	public void associatePrimerInfo( final String[] entries )
	{
		setForPrimerSequence( entries[ 1 ] );
		setPrimerIntronDistance( Integer.valueOf( entries[ 2  ] ) );  
	}
	
	public void smitAnalysis( final File outputDir )
	{
		if( getSMITReadpairList().size() != 0 )
		{
			setSMITAnalysis( new SMITAnalysis( getSMITReadpairList(),this ) );
			getSMITAnalysis().analyze();
			getSMITAnalysis().filterPosSplicingvalueListByAbsoluteReadcount( 10, Integer.MAX_VALUE );
			getSMITAnalysis().writePosSplicingvalueListToDir( outputDir );  
			System.out.println( getSMITAnalysis() ); 			
		}
	}
	
	public int getIntronLength()
	{
		ArrayList<BEDentry> exons = getBlocks(); 
		
		
		int start, end; 
		if( isPlusStrand() )
		{
			start = exons.get( 0 ).getChromEnd(); 
			end = exons.get( 1 ).getChromStart();  
		}
		else 
		{
			start = exons.get( exons.size() - 2 ).getChromEnd(); 
			end = exons.get( exons.size() - 1 ).getChromStart(); 
		}
		
		return ( end - start - 1 ); 
	}
	
	
	//Getter and setter methods. Control your instance variables. 
	public void setSMITReadpairList( final ArrayList<SMITReadpair> SMITReadpairList ) { this.SMITReadpairList = SMITReadpairList; }
	public ArrayList<SMITReadpair> getSMITReadpairList() { return SMITReadpairList; }
	public void addSMITReadpair( final SMITReadpair smitReadpair ) 
	{ 
		getSMITReadpairList().add( smitReadpair );
		setSortedSMITReadpairList( false );  
	}
	
	private void setPrimerIntronDistance( final int primerIntronDistance ) { this.primerIntronDistance = primerIntronDistance; }  
	public int getPrimerIntronDistance() { return this.primerIntronDistance; }
	
	private void setForPrimerSequence( final String forPrimerSequence ) { this.forPrimerSequence = forPrimerSequence; } 
	public String getForPrimerSequence() { return this.forPrimerSequence; }
	
	private void setSMITAnalysis( final SMITAnalysis smitAnalysis ) { this.smitAnalysis = smitAnalysis; } 
	public SMITAnalysis getSMITAnalysis() { return this.smitAnalysis; } 
	
	private void setSortedSMITReadpairList( final boolean sortedSMITReadpairList ) { this.sortedSMITReadpairList = sortedSMITReadpairList; }
	public boolean getSortedSMITReadpairList() { return this.sortedSMITReadpairList; }
	
}
