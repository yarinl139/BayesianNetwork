import java.util.ArrayList;
import java.util.Iterator;

public class CPT {

	Variable x;
	Linked_List<Variable> given;
	String [][]table; //a general truth table including x
	double [] probabilities; // a probability array
	String str_prob;
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
		Linked_List<Variable> p = given;
		while(p.getNext().getNext()!=null)
			p=p.getNext();
		Linked_List<Variable> m = p.getNext();
		p.setNext(null);
	     generate_truth_table(m.getValue(), given, this.table);
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
			for (int j = 0; j < this.table[0].length; j++) {
				while(index<outcomes.size())
				{
					if(this.table[i][j].equals(outcomes.get(index)))
					{
						index++;
					}
				}
				if(index == outcomes.size())
					return this.probabilities[i];
			}
		}
		return -1;
	}
}
