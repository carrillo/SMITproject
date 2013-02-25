package smit;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class holds all instances of SMITReadpair
 * @author carrillo
 *
 */
public class SMITReadpairCollection 
{
	//This ArrayList links to all SMITReadpairs
	protected ArrayList<SMITReadpair> SMITReadpairCollection; 
	//This Hashmap makes it easy to search for read ids 
	protected HashMap<String, SMITReadpair> SMITReadpairHashMap; 
	
	public SMITReadpairCollection()
	{
		setSMITReadpairCollection( new ArrayList<SMITReadpair>() ); 
	}
	
	/**
	 * Generates the SMITReadpair Hashmap. Once the reverse reads were added.
	 */
	public void generateSMITReadpairHashMap()
	{
		HashMap<String, SMITReadpair> readpairHashMap = new HashMap<String, SMITReadpair>(); 
		
		for( SMITReadpair readpair : getSMITReadpairCollection() )
		{
			readpairHashMap.put( readpair.getReverseRead().getReadName(), readpair ); 
		}
		
		setSMITReadpairHashMap( readpairHashMap ); 
	}

	//Getter and Setter
	public void setSMITReadpairCollection( final ArrayList<SMITReadpair> SMITReadpairCollection ) { this.SMITReadpairCollection = SMITReadpairCollection; }
	public ArrayList<SMITReadpair> getSMITReadpairCollection() { return this.SMITReadpairCollection; } 
	public void addReadPair( final SMITReadpair readpair ) { getSMITReadpairCollection().add( readpair ); }
	
	public void setSMITReadpairHashMap( final HashMap<String, SMITReadpair> SMITReadpairHashMap ) { this.SMITReadpairHashMap = SMITReadpairHashMap; }
	public HashMap<String, SMITReadpair> getSMITReadpairHashMap() { return this.SMITReadpairHashMap; } 

}
