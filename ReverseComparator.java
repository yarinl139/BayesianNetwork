import java.util.Comparator;

public class ReverseComparator implements Comparator<Variable> {

	@Override
	public int compare(Variable o1, Variable o2) {
		if(o1.getName().compareTo(o2.getName())<0)
			return -1;
		else if(o1.getName().compareTo(o2.getName())>0)
			return 1;
		return 0;
	}

}
