package featureExtraction;

import java.util.Comparator;

public class StringIntegerPairComparator implements Comparator<StringIntegerPair> {

	@Override
	public int compare(StringIntegerPair o1, StringIntegerPair o2) {
		return -Integer.compare(o1.i, o2.i);
	}

}
