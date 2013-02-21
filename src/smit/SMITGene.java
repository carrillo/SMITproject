package smit;

import java.util.ArrayList;

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
	
	//Getter and setter methods. Control your instance variables. 
	public void setSMITReadpairList( final ArrayList<SMITReadpair> SMITReadpairList ) { this.SMITReadpairList = SMITReadpairList; }
	public ArrayList<SMITReadpair> getSMITReadpairList() { return SMITReadpairList; }
	public void addSMITReadpair( final SMITReadpair smitReadpair ) { getSMITReadpairList().add( smitReadpair ); } 
}
