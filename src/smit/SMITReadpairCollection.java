package smit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;


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
	
	/**
	 * This method set's all readpair with only a reverse read as not valid. 
	 */
	public void ignoreIncompleteReadpairs()
	{
		for( SMITReadpair readpair : getSMITReadpairCollection() )
		{
			if( !readpair.hasForwardAndReverseRead() )
				readpair.setValid( false ); 
		}
	}
	
	/**
	 * This method removes all readpairs with a valid = false tag
	 */
	public void removeInvalidReadpairs()
	{
		System.out.println( "Removing invalid readpairs." ); 
		//Store indices of readpairs to be removed.
		ArrayList<Integer> removeIndex = new ArrayList<Integer>();
		SMITReadpair readpair = null; 
		ArrayList<SMITReadpair> validSMITReadpairCollection = new ArrayList<SMITReadpair>(); 
		for( int i = 0; i < getSMITReadpairCollection().size(); i ++ )
		{
			readpair = getSMITReadpairCollection().get( i );
			if( !readpair.isValid() )
			{
				//Unregister readpair in parent gene
				if( readpair.getParentGene() != null )
				{
					//System.out.println( "Remove " + readpair.getReadName() + " from " + readpair.getParentGene().getName() ); 						
					readpair.getParentGene().unregister( readpair );					
				}  
				removeIndex.add( i ); 
			} else {
				validSMITReadpairCollection.add( getSMITReadpairCollection().get(i) ); 
			}
		}
		
		setSMITReadpairCollection(validSMITReadpairCollection);
		
		/*
		System.out.println("Removing reads from readpair collection"); 
		//Remove readpairs based on index. 
		for( int j = ( removeIndex.size() - 1 ); j >= 0; j-- )
		{
			getSMITReadpairCollection().remove( j ); 
		}
		*/
			
		//Update hashmap 
		generateSMITReadpairHashMap(); 
		
		
		System.out.println( "Done. Removed " + removeIndex.size() + " invalid entries." ); 
	}
	
	/**
	 * Overrides the toString method. It returns an info string. 
	 */
	public String toString()
	{
		String s = "Readpair collection contains: " + getSMITReadpairCollection().size() + " entries.";
		int count = 0; 
		HashSet<SMITGene> geneHash = new HashSet<SMITGene>(); 
		for( SMITReadpair rp : getSMITReadpairCollection() )
		{
			if( rp.isValid() )
			{
				if( !geneHash.contains( rp.getParentGene() ) )
					geneHash.add( rp.getParentGene() ); 
				
				count++;
			}
		}
		s += " Of these " + count + " are valid.\n";
		s += "Genes with at least 10 valid reads:\n"; 
		for( SMITGene sg : geneHash )
		{
			if( sg.getSMITReadpairList().size() > 9 )
				s += sg + "\n"; 
		}
		
		return s; 
	}

	//Getter and Setter
	public void setSMITReadpairCollection( final ArrayList<SMITReadpair> SMITReadpairCollection ) { this.SMITReadpairCollection = SMITReadpairCollection; }
	public ArrayList<SMITReadpair> getSMITReadpairCollection() { return this.SMITReadpairCollection; } 
	public void addReadPair( final SMITReadpair readpair ) { getSMITReadpairCollection().add( readpair ); }
	
	public void setSMITReadpairHashMap( final HashMap<String, SMITReadpair> SMITReadpairHashMap ) { this.SMITReadpairHashMap = SMITReadpairHashMap; }
	public HashMap<String, SMITReadpair> getSMITReadpairHashMap() { return this.SMITReadpairHashMap; } 

}
