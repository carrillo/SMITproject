package smit;

import java.util.Comparator;

/**
 * This comparator compares two SMITReadpair instances by their polymerase position
 * This class is used to sort Collection of SMITReadair instances. 
 * @author carrillo
 *
 */
public class SMITReadpairPolymerasePosComparator implements Comparator<SMITReadpair> 
{

	@Override
	public int compare(SMITReadpair arg0, SMITReadpair arg1) 
	{
		if( arg0.getPolymerasePosition().getChromStart() > arg1.getPolymerasePosition().getChromStart() )
			return 1; 
		else if( arg0.getPolymerasePosition().getChromStart() < arg1.getPolymerasePosition().getChromStart() )
			return -1; 
		else 
			return 0;
	}

}
