import java.util.ArrayList;

public class Variable {
	private String name;
	private int options;
	public String[] outcomes;
	public String current_outcome;
	public int index = 0;
	
	public Variable(String name, int options)
	{
		this.name = name;
		this.options = options;
	}
	public Variable(String name, int options, ArrayList<String>outcomes)
	{
		this.name = name;
		this.options = options;
		this.outcomes = new String [outcomes.size()];
		int index = 0;
		while(index<outcomes.size())
		{
			this.outcomes[index] = outcomes.get(index);
			index++;
		}
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getOptions() {
		return options;
	}
	public void setOptions(int options) {
		this.options = options;
	}
	public void increaseIndex()
	{
		this.index++;
	}	
	public String toString()
	{
		String st = "";
		for (int i = 0; i < outcomes.length; i++) {
			st+=outcomes[i] + " ";
		}
		return "name: " + this.name + " options: " + this.options + " current outcome: "+ this.current_outcome;
	}
	public void setCurrentOutcome(String s)
	{
		this.current_outcome = s;
	}
}
