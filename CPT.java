import java.util.ArrayList;
import java.util.Iterator;

public class CPT implements Comparable<Object>{

	Variable x;
	Linked_List<Variable> given;
	String [][]table; //a general truth table including x
	double [] probabilities; // a probability array
	String str_prob;
	
	
	public CPT()
	{
		this.x = null;
		this.given = null;
		this.table = null;
		this.probabilities = null;
		this.str_prob = null;
	}
	public CPT(CPT other) //deep copy of a CPT
	{
		this.x = new Variable(other.x);
		this.given = other.given;
		this.table = new String [other.table.length][other.table[0].length];
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[0].length; j++) {
				this.table[i] = other.table[i];
			}
		}
		this.probabilities = new double [other.probabilities.length];
		for (int i = 0; i < probabilities.length; i++) {
			this.probabilities[i] = other.probabilities[i];
		}
		this.str_prob = other.str_prob;

	}
	public CPT(Variable x , String str_prob) //if x has no parents
	{
		this.x = x;
		this.given = null;
		this.table = new String [x.getOptions()][1];
		this.probabilities = new double [x.getOptions()];
		this.str_prob = str_prob;
		for (int i = 0; i < x.outcomes.length; i++) {
			this.table [i][0] =  x.outcomes[i];
		}
		this.generate_probabilites_array(probabilities, str_prob);
	}
	public CPT(String [][]truth, Linked_List<Variable> given)
	{
		this.table = truth;
		if(given.getNext()==null)
		{
			generate_truth_table_for_single(given.getValue(), truth);
		}
		else {
			this.table = truth;
			Linked_List<Variable> p = given;
			while(p.getNext().getNext()!=null)
				p=p.getNext();
			Linked_List<Variable> m = p.getNext();
			p.setNext(null);
			generate_truth_table(m.getValue(), given, this.table);
		}
	}
	public CPT(Variable x, Linked_List<Variable> given ,String str_prob)
	{
		if(given!=null)
		{
			this.x=x;
			this.given=given;
			Linked_List<Variable> p = this.given;
			int sum = this.x.getOptions();
			int length =1;
			while(p!=null)
			{
				sum=sum*p.getValue().getOptions();
				length++;
				p=p.getNext();
			}
			this.table = new String[sum][length];
			this.probabilities = new double [sum];
			this.str_prob = str_prob;
			this.generate_truth_table(x, given, table);
			this.generate_probabilites_array(probabilities, str_prob);
		}

	}
	public void generate_truth_table_for_single(Variable k , String[][]table)
	{
		for (int i = 0; i < k.outcomes.length; i++) {
			this.table [i][0] =  k.outcomes[i];
		}
	}
	public  void generate_truth_table(Variable x , Linked_List<Variable> given , String table[][])
	{
		Linked_List<Variable> q = given;
		while(q.getNext()!=null)
			q=q.getNext();
		q.setNext(new Linked_List<Variable>(x)); //meaning the query is going to get attached to the end of this list
		if(given.getValue() == x) //if x has no parents
		{
			for (int i = 0; i < x.outcomes.length; i++) {
				this.table[i][0] = x.outcomes[i];
			}
		}
		else
		{ 
			/*The following code is generating a general truth table 
			 without necessarily True or False values*/
			Linked_List<Variable> p = given; //pointer to the start of the list
			int jumper = table.length;
			int columns = 0; //indexing the columns
			int[] repeater = {0}; //inserting outcome repeatedly in the index range
			while(p!=null)
			{
				jumper /= p.getValue().getOptions();
				repeater[0]=0;
				int inserting_index = 0;
				if(columns<table[0].length)
				{
					for (int i = 0; i < table.length; i++) {

						while(inserting_index<table.length)	
						{

							for (int j=0; j<p.getValue().outcomes.length;j++)
							{

								inserting_index += jumper;
								while(repeater[0]<inserting_index) {
									{
										table[repeater[0]][columns]=p.getValue().outcomes[j];
										repeater[0]++;
									}
								}				

							}

						}

					}
				}
				columns++;
				p=p.getNext();
			}

		}
	}
	private void generate_probabilites_array(double []arr , String str)
	{
		String [] result = str.split(" ");
		int index =0;
		for (int i = 0; i <= result.length-1; i++) {
			if(index<arr.length)
			{
				arr[index] = Double.parseDouble(result[i]);
				index++;
			}
		}

	}

	public void printTruthTable()
	{
		System.out.println("Query: " + this.x.getName() + " Parents: " + this.given);
		for (int i = 0; i < this.table.length; i++) {
			for (int j = 0; j < this.table[0].length; j++) {
				System.out.print(this.table[i][j] + " ");
			}
			System.out.print(" " + this.probabilities[i]);
			System.out.print("\n");
		}
	}
	public String getCurrentQueryName()
	{
		return this.x.getName();
	}
	public double getProbabilityByOutcomes(ArrayList<String> outcomes)
	{
		int index;
		for (int i = 0; i < this.table.length; i++) {
			index = 0;
			for (int j = 0; j < this.table[0].length;j++) {

				if(this.table[i][j].equals(outcomes.get(index)))
				{
					index++;
				}

				if(index == outcomes.size())
					return this.probabilities[i];
			}
		}
		return -1;
	}
	public double getProbailityByCurrentOutcomes()
	{
		Linked_List<Variable> p;
		if(this.given!=null)
		{
			p= this.given;
		}
		else
		{
			p = new Linked_List<>(this.x);
		}
		ArrayList<String> arr = new ArrayList<>();
		while(p!=null)
		{
			arr.add(p.getValue().current_outcome);
			p=p.getNext();
		}
		return this.getProbabilityByOutcomes(arr);

	}
	public ArrayList<Double> Variable_To_Eliminate(Variable me)
	{
		String last_outcome = me.current_outcome;
		me.setCurrentOutcome("rip");
		ArrayList<Double> arr = new ArrayList<>();
		String outcomes ="";
		int index_of_me = 0 ,flag = 0;
		Linked_List<Variable> p = this.given;
		while(p!=null)
		{
			if(p.getValue()!=me)
			{
				outcomes+=p.getValue().current_outcome;
				flag++;
				p=p.getNext();
			}
			else
			{
				outcomes+=p.getValue().current_outcome;
				index_of_me = flag;
				p=p.getNext();
			}
		}
		for (int i = 0; i < table.length; i++) {
			this.table[i][index_of_me] = "rip";
		}

		String st = "";
		for (int i = 0; i < table.length; i++) {
			st = "";
			for (int j = 0; j < table[0].length; j++) {
				st+=table[i][j];
			}
			if(st.equals(outcomes))
			{
				arr.add(probabilities[i]);
			}
		}
		me.setCurrentOutcome(last_outcome);
		return arr;
	}





	public int getNumOfVariables()
	{
		int sum=0;
		if(this.given==null)
			return 1;
		else
		{
			Linked_List <Variable> p = given;
			while(p!=null)
			{
				sum++;
				p=p.getNext();
			}
		}
		return sum;
	}
	public double getThisVariableProb()
	{
		ArrayList<String> arr = new ArrayList<String>();
		Linked_List<Variable> p = this.given;
		if(p!=null)
		{
			while(p!=null)
			{
				arr.add(p.getValue().current_outcome);
				p=p.getNext();
			}
		}
		else
			arr.add(x.current_outcome);


		return getProbabilityByOutcomes(arr);
	}

	public int getCPTSize()
	{
		int sum = 1;
		if(given==null)
			return x.getOptions();
		else
		{
			Linked_List <Variable> p = given;
			while(p!=null)
			{
				sum*=p.getValue().getOptions();
				p=p.getNext();
			}
		}
		return sum;
	}
	public CPT Diminish(Variable evidence) //local CPTS instantiated by evidence
	{
		if(this.getCPTSize()<=2)
			return null;
		double [] updated_probs = new double[this.getCPTSize()/evidence.getOptions()];
		String [][] updated_table = new String [this.getCPTSize()/evidence.getOptions()][getNumOfVariables()-1];
		Linked_List<Variable> parents = new Linked_List<Variable> (new Variable());
		Linked_List<Variable> p = parents;
		Linked_List<Variable> m = given;
		int place = 0;
		int index = 0;
		while(m != null)
		{
			if(m.getValue()!=evidence)
			{
				p.setNext(new Linked_List<Variable>(m.getValue()));
				p=p.getNext();
				index++;
			}
			else
				place = index;
			m=m.getNext();
		}
		
		int flag = 0;
		parents = parents.getNext();
		for (int i = 0; i < this.table.length; i++) {
			if(table[i][place].equals(evidence.current_outcome))
			{
				if(flag<updated_probs.length)
				{
					updated_probs[flag] = this.probabilities[i];
					flag++;
				}
			}
		}
		if(parents == null) // if the factor became one valued, discard the factor
			return null;

		CPT diminished = new CPT(updated_table,parents);
		diminished.probabilities = updated_probs;
		diminished.given = parents;
		return diminished;



	}

	public boolean inCPT(String name)
	{
		if(this.given == null)
		{
			if(this.x!=null)
			{
				if(this.x.getName().equals(name))
					return true;
			}

		}
		else
		{
			Linked_List<Variable> p = this.given;
			while(p!=null)
			{
				if(p.getValue().getName().equals(name))
					return true;
				p=p.getNext();
			}
		}
		return false;
	}	
	public boolean hasParents()
	{
		return given==null;
	}
	@Override
	public int compareTo(Object o) {
		CPT c1 = (CPT)o;
		if(this.getCPTSize()<c1.getCPTSize())
			return -1;
		else if (this.getCPTSize()>c1.getCPTSize())
			return 1;
		else
		{
			int ascii1 = 0;
			int ascii2 =0;
			
			String s1 = "";
			String s2 = "";
			
			
			Linked_List<Variable> p = this.given;
			Linked_List<Variable> m = c1.given;
			if(p==null)
				s1+=this.x.getName();
			while(p!=null)
			{
				s1 += p.getValue().getName();
				p=p.getNext();
			}
			if(m==null)
				s2+=c1.x.getName();
			while(m!=null)
			{
				s2+=m.getValue().getName();
				m=m.getNext();
			}
			
			for (int i = 0; i < s1.length(); i++) {
				ascii1+=s1.charAt(i);
			}
			for (int i = 0; i < s2.length(); i++) {
				ascii2+=s2.charAt(i);
			}
			
			if(ascii1<ascii2)
				return -1;
			else if (ascii1>ascii2)
				return 1;
			return 0;
		}
	}	
	
}
