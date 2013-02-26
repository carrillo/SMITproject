package smit;

import java.util.ArrayList;
import java.util.Collections;

import parser.BEDentry;

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
	
	public void smitAnalysis()
	{
		setSMITAnalysis( new SMITAnalysis(getSMITReadpairList(),this) );
		getSMITAnalysis().analyze();
		System.out.println( getSMITAnalysis() ); 
	}
	
	
	//Getter and setter methods. Control your instance variables. 
	public void setSMITReadpairList( final ArrayList<SMITReadpair> SMITReadpairList ) { this.SMITReadpairList = SMITReadpairList; }
	public ArrayList<SMITReadpair> getSMITReadpairList() { return SMITReadpairList; }
	public void addSMITReadpair( final SMITReadpair smitReadpair ) 
	{ 
		getSMITReadpairList().add( smitReadpair );
		setSortedSMITReadpairList( false );  
	} 
	
	private void setSMITAnalysis( final SMITAnalysis smitAnalysis ) { this.smitAnalysis = smitAnalysis; } 
	public SMITAnalysis getSMITAnalysis() { return this.smitAnalysis; } 
	
	private void setSortedSMITReadpairList( final boolean sortedSMITReadpairList ) { this.sortedSMITReadpairList = sortedSMITReadpairList; }
	public boolean getSortedSMITReadpairList() { return this.sortedSMITReadpairList; } 
}
