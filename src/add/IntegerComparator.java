package add;

import java.util.Comparator;

public class IntegerComparator implements Comparator {

public int compare(Object o1,Object o2) {
		Integer i1 =new Integer((String)o1);
		Integer i2 =new Integer((String)o2);
		return i1.compareTo(i2);
}

public boolean   equals(Object obj){
	return this == obj;
}

}