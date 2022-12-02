import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class VE {

	public static Variable getVariable(ArrayList<Variable> variables , String variable_name) //returns a variable object in the ArrayList of the variables
	{
		int index = 0 ;
		while(index<variables.size()) //iterating over the ArrayList
		{
			if(variables.get(index).getName().equals(variable_name))
				return variables.get(index);
			index++;
		}
		return null;
	}
	public static boolean isOutcome(Variable vb , String st) //return if a string is an outcome of a variable
	{
		if(vb==null)
			return false;
		for (int i = 0; i < vb.getOptions(); i++) {
			if(vb.outcomes[i].equals(st))
				return true;
		}
		return false;
	}

	public static boolean isName(ArrayList<Variable> variables, String str) //returns if a string is a name of a variable
	{
		if(getVariable(variables,str)!=null)
			return true;
		return false;
	}
	public static CPT getCPT(ArrayList<CPT> bayesian_network , String variable_name) //returns a CPT object in the ArrayList of the CPT's
	{
		int index = 0 ;
		while(index<bayesian_network.size()) //iterating over the ArrayList
		{
			if(bayesian_network.get(index).getCurrentQueryName().equals(variable_name))
				return bayesian_network.get(index);
			index++;
		}
		return null;
	}
	public static void PreProcess(String str ,ArrayList<Variable> query, ArrayList<Variable>evidence,ArrayList<Variable>hidden,ArrayList<Variable> variables,ArrayList<CPT> bayesian_network)
	{
		Variable x = null;
		String save_query = ""; //this is used for saving the query outcome for normalization later
		int flag_divider = 0; //divides the string at char '|'
		String name = "";
		String outcome = "";

		//This piece of code is used to get the query parameter and set its outcome
		for (int i = 1; i < str.length() && str.charAt(i) != '|'; ++i) {
			if(str.charAt(i)!='(' && str.charAt(i)!=')' && str.charAt(i)!=',' && str.charAt(i)!= '=')
			{	
				name+=str.charAt(i);
				outcome += str.charAt(i);
			}
			if(isName(variables,name))
			{
				x = getVariable(variables,name);
				query.add(x);
				outcome = "";
				name = "";
			}
			if(isOutcome(x,outcome))
			{
				x.setCurrentOutcome(outcome);
				save_query = outcome;
				name = "";
				outcome = "";
			}

			flag_divider = i;

		}
		flag_divider++;
		//Re_Initializing the variables
		name = "";
		outcome = "";
		Variable vb = null ;
		//This loop's purpose is to save the evidence variables and their outcomes from the '|' character until the end of the string
		for (int i = flag_divider+1; i < str.length(); i++) {
			if(str.charAt(i)!='(' && str.charAt(i)!=')' && str.charAt(i)!=',' && str.charAt(i)!= '=')
			{	
				name+=str.charAt(i);
				outcome += str.charAt(i);
			}
			if(isName(variables,name))
			{
				vb = getVariable(variables,name);
				evidence.add(vb);
				outcome = "";
				name = "";
			}
			if(isOutcome(vb,outcome))
			{
				vb.setCurrentOutcome(outcome);
				name = "";
				outcome = "";
			}
		}
		//saving the query's parent to check if the evidence is equal to the parents, so we can get the probability from the CPT with no calculation
		Linked_List<Variable> query_parents = getCPT(bayesian_network,x.getName()).given;
		Linked_List<Variable> p = query_parents;
		ArrayList<Variable> query_parents_al = new ArrayList<Variable>();
		if(p!=null)
		{
			while(p!=null)
			{
				query_parents_al.add(p.getValue());
				p=p.getNext();
			}
		}

		//Declaring an ArrayList for the query and the evidence parameters
		//to check which of the parameters are missing and add them to the hidden ArrayList.
		ArrayList<Variable> query_evidence = new ArrayList<>();                             
		for (int i = 0; i < evidence.size(); i++) {
			query_evidence.add(evidence.get(i));
		}
		query_evidence.add(x); 
		for (int i = 0; i < variables.size(); i++) {
			if(!(query_evidence.contains(variables.get(i))))
			{
				hidden.add(variables.get(i));
			}
		}
	}
	public static ArrayList<CPT> getAllCPT(ArrayList<CPT> bayesian_network,ArrayList<Variable> variables)
	{
		ArrayList<CPT> cpts = new ArrayList<CPT>();
		for (int i = 0; i < variables.size(); i++) {
			cpts.add(getCPT(bayesian_network,variables.get(i).getName()));
		}
		return cpts;
	}
	public static boolean AlreadyExists(Linked_List<Variable> list,Variable other)
	{
		Linked_List<Variable> p = list;
		while(p!=null)
		{
			if(p.getValue() == other)
				return true;
			p=p.getNext();
		}
		return false;
	}
	public static CPT join(double[]arr,ArrayList<CPT> factors)
	{
		CPT res = factors.get(0);
		for (int i = 1; i < factors.size(); i++) {

			res = joinTwoFactors(arr,res,factors.get(i));
		}

		return res;
	}
	private static CPT joinTwoFactors(double[] arr,CPT cpt1, CPT cpt2) {
		ArrayList<Variable> union = new ArrayList<Variable>();
		Linked_List<Variable> union_ln = new Linked_List<>(new Variable());
		Linked_List<Variable> q = union_ln;
		Linked_List<Variable> m = cpt1.given;
		Linked_List<Variable> p = cpt2.given;
		int sum = 1;
		while(m!=null)
		{
			if(!union.contains(m.getValue()))
			{
				union.add(m.getValue());
				q.setNext(new Linked_List<Variable>(m.getValue()));
				q=q.getNext();
				sum*=m.getValue().getOptions();	
			}
			m=m.getNext();
		}
		while(p!=null)
		{
			if(!union.contains(p.getValue()))
			{
				union.add(p.getValue());
				q.setNext(new Linked_List<Variable>(p.getValue()));
				q=q.getNext();
				sum*=p.getValue().getOptions();	
			}
			p=p.getNext();
		}
		union_ln = union_ln.getNext();
		String [][]truth = new String [sum][union.size()];
		double [] result_probs = new double[sum];
		CPT result = new CPT(truth,union_ln);
		result.given = union_ln;
		result.probabilities = result_probs;
		double prob1 = 0,prob2 =0;
		for (int i = 0; i < truth.length; i++) {
			p=result.given;
			for (int j = 0; j < truth[0].length; j++) {
				p.getValue().setCurrentOutcome(truth[i][j]);
				p=p.getNext();
			}
			prob1 = cpt1.getProbailityByCurrentOutcomes();
			prob2 = cpt2.getProbailityByCurrentOutcomes();	
			result_probs[i] = prob1*prob2;
			arr[2]++;
		}


		return result;
	}
	public static void Eliminate(CPT cpt)
	{
		
	}
	public static int[] VariableElimination(ArrayList<Variable> variables,String str,ArrayList<CPT> bayesian_network)
	{
		double []arr = new double [3];
		ArrayList<Variable> query = new ArrayList<>();//saving the query variable
		ArrayList<Variable> evidence = new ArrayList<>(); //saving the evidence variables
		ArrayList<Variable> hidden = new ArrayList<>(); //saving the hidden parameters
		PreProcess(str, query, evidence, hidden, variables, bayesian_network);
		ArrayList<CPT> diminished_evidence = new ArrayList<>();
		// i need to iterate over the evidence variables and save the diminished CPTs.
		for (int i = 0; i < evidence.size(); i++) {
			diminished_evidence.add(getCPT(bayesian_network,evidence.get(i).getName()).Diminish());
		}
		hidden.sort(null); //sorts the ArrayList by the ABC order
		ArrayList<CPT> factors = new ArrayList<>();
		factors.addAll(getAllCPT(bayesian_network,query));
		factors.addAll(diminished_evidence);
		factors.addAll(getAllCPT(bayesian_network,hidden));

		while(!hidden.isEmpty())
		{
			ArrayList<CPT> contains = new ArrayList<>(); //take all factors including the current variable
			for (int j = 0; j < factors.size(); j++) {
				if(factors.get(j)!= null && factors.get(j).inCPT(hidden.get(0).getName()))
				{
					contains.add(factors.get(j));
				}
			}
			contains.sort(null);
			CPT hidden_result = join(arr,contains);

			hidden.remove(0);
		}

		return null;
	}


}
