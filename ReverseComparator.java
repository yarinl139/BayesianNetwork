import java.util.Comparator;

public class ReverseComparator implements Comparator<Variable> {

	@Override
	public int compare(Variable o1, Variable o2) { //sorting the array in flipped order
		return o1.getName().compareTo(o2.getName())*(-1);
	}
}
