import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class main {

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
	public static boolean isOutcome(Variable vb , String st)
	{
		if(vb==null)
			return false;
		for (int i = 0; i < vb.getOptions(); i++) {
			if(vb.outcomes[i].equals(st))
				return true;
		}
		return false;
	}
	public static boolean isName(ArrayList<Variable> variables, String str)
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

	public static double[] SimpleConclusion(ArrayList<Variable> variables,String str,ArrayList<CPT> bayesian_network) { // Using simple conclusion algorithm
		Variable query = null;;
		ArrayList<Variable> evidence = new ArrayList<>();
		ArrayList<Variable> hidden = new ArrayList<>();
		String save_query = "";
		int flag_divider = 0; //divides the string at char '|'
		String name = "";
		String outcome = "";
		for (int i = 1; i < str.length() && str.charAt(i) != '|'; ++i) { //for to get the query parameter
			if(str.charAt(i)!='(' && str.charAt(i)!=')' && str.charAt(i)!=',' && str.charAt(i)!= '=')
			{	
				name+=str.charAt(i);
				outcome += str.charAt(i);
			}
			if(isName(variables,name))
			{
				query = getVariable(variables,name);
				outcome = "";
				name = "";
			}
			if(isOutcome(query,outcome))
			{
				save_query = outcome;
				name = "";
				outcome = "";
			}

			flag_divider = i;

		}
		flag_divider++;
		name = "";
		outcome = "";
		Variable vb = null ;
		for (int i = flag_divider+1; i < str.length(); i++) { //for to get the evidence parameters
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
			if(isOutcome(query,outcome))
			{
				vb.setCurrentOutcome(outcome);
				name = "";
				outcome = "";
			}
		}
		ArrayList<Variable> query_evidence = new ArrayList<>(); //an ArrayList for the query and the evidence parameters
		query_evidence.add(query);                              //to check which of the parameters are missing
		for (int i = 0; i < evidence.size(); i++) {
			query_evidence.add(evidence.get(i));
		}
		for (int i = 0; i < variables.size(); i++) {
			if(!(query_evidence.contains(variables.get(i))))
			{
				hidden.add(variables.get(i));
			}
		}
		//from this line of code i have the hidden variables stored in ArrayList
		int options = 1;
		Linked_List<Variable> hidden_ln = new Linked_List<Variable>(query);
		Linked_List<Variable> p = hidden_ln;
		for (int i = 0; i < hidden.size(); i++) {
			options*=hidden.get(i).getOptions();
			p.setNext(new Linked_List<Variable>(hidden.get(i)));
			p=p.getNext();
		}
		hidden_ln = hidden_ln.getNext();
		String [][] every_option_hidden = new String [options][hidden.size()];
		CPT hidden_truth_table = new CPT(every_option_hidden,hidden_ln);
		
		double [] arr = new double [3];//arr[0] will hold the answer, arr[1] will hold the number of additions ,arr[2] will hold the number of multiplications
		arr[1]=0;
		arr[2]=0;
		double result;
		double sum = 0;
		double total =0;
		int count_additions = 0;
		int index = 0;

		for(int k=0;k<query.getOptions();k++)
		{
			query.setCurrentOutcome(query.outcomes[k]);
			for (int i = 0; i < every_option_hidden.length; i++) {
				index = 0 ;
				for (int j = 0; j < every_option_hidden[0].length; j++) {
					hidden.get(index).setCurrentOutcome(every_option_hidden[i][j]);
					index++;
				}
				result = Calculate(arr,query,evidence,hidden,variables,bayesian_network);
				if(query.current_outcome.equals(save_query))
				{
					sum+=result;
				}
				total+=result;
				count_additions++;
				arr[0]++;
			}
		}
		
		arr [0] = sum/total;
		arr[1] = count_additions-1;

		return arr;
	}
	public static double Calculate(double []arr, Variable query, ArrayList<Variable> evidence, ArrayList<Variable> hidden ,ArrayList<Variable> variables,ArrayList<CPT> bayesian_network)
	{
		ArrayList<String> outcomes;
		double result=1;
		for (int i = 0; i < variables.size(); i++) {
			Linked_List<Variable> parents = getCPT(bayesian_network, variables.get(i).getName()).given;
			Linked_List<Variable> p = parents;
			outcomes = new ArrayList<String>();
			if(parents!=null)
			{
				while(p!=null)
				{
					outcomes.add(p.getValue().current_outcome);
					p=p.getNext();
				}
			}
			else
				outcomes.add(variables.get(i).current_outcome);
			arr[2]++;
			result*=getCPT(bayesian_network,variables.get(i).getName()).getProbabilityByOutcomes(outcomes);
			
		}
		arr[2]--;
		return result;
	}


	public static void main(String[] args) {

		ArrayList<Variable> variables = null; //Declaring an array list for the variables
		ArrayList<CPT> bayesian_network = null; //Declaring an array list for type CPT which is actually our Bayesian Network 


		//Reading the queries from the input file
		File file = new File("input.txt");
		String st_for_xml="";
		Linked_List<String> queries = new Linked_List<String>("");
		Linked_List<String> p = queries;
		try {
			Scanner sc = new Scanner(file);
			st_for_xml = sc.nextLine(); 
			//inserting the queries into a linked list
			while(sc.hasNext())
			{
				p.setNext(new Linked_List<String>(sc.nextLine()));
				p=p.getNext();
			}
			sc.close();
			queries = queries.getNext();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		// reading the XML file and creating an ArrayList of Bayesian's network variables
		// ---------------------------------------------------
		try {
			//first things first we will organize the variable element and store it
			File xml_doc = new File(st_for_xml);
			DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuild = dbFact.newDocumentBuilder();
			Document doc = dBuild.parse(xml_doc);	
			NodeList xml_variables = doc.getElementsByTagName("VARIABLE");
			String name = "";            //note that each variable has name
			int number_of_outcomes = 0; //note that each variable has number of outcomes
			variables = new ArrayList<Variable>();
			for (int i = 0; i < xml_variables.getLength(); i++) { // iterating over the VARIABLE elements
				Node nNode = xml_variables.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element variable_element = (Element) nNode;
					String variable_name = variable_element.getElementsByTagName("NAME").item(0).getTextContent(); // saving the name of each variable in the VARIABLE element
					NodeList variable_outcomes = variable_element.getElementsByTagName("OUTCOME"); //getting a NodeList of OUTCOME ELEMENTS
					ArrayList<String> actual_outcomes = new ArrayList<String>();
					number_of_outcomes = 0;
					for (int j= 0; j < variable_outcomes.getLength(); j++) { //this for loop counts the number of outcomes for each variable
						actual_outcomes.add(variable_outcomes.item(j).getTextContent());
						number_of_outcomes++;
					}
					variables.add(new Variable(variable_name,number_of_outcomes,actual_outcomes)); // Creating a Variable object and storing it in an ArrayList data structure
				}
			}

			//then we will get the CPT element and store it as CPT object


			NodeList xml_cpt = doc.getElementsByTagName("DEFINITION"); // getting the DEFINITION element from the XML file
			Linked_List<Variable> parents_for_variable = new Linked_List<Variable>(new Variable("",0));
			Linked_List<Variable> pointer = parents_for_variable ;
			String cpt_variable_name ="";
			String cpt_probability = "";
			bayesian_network = new ArrayList<>();
			for (int i = 0; i < xml_cpt.getLength(); i++) {
				Node nNode = xml_cpt.item(i);
				if(nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element cpt_element = (Element) nNode;
					cpt_variable_name = cpt_element.getElementsByTagName("FOR").item(0).getTextContent();
					parents_for_variable = new Linked_List<Variable>(new Variable("",0)); // Creating a parents list for each variable, with dummy node in order to chain
					pointer = parents_for_variable;
					NodeList cpt_given = cpt_element.getElementsByTagName("GIVEN");
					ArrayList<String> given = new ArrayList<String>();
					Variable recieved = null;
					for (int j = 0; j < cpt_given.getLength(); j++) {
						given.add(cpt_given.item(j).getTextContent());
					}
					int iterate = 0;
					// iterate over the array list of strings
					while(iterate<given.size())
					{
						recieved = getVariable(variables , given.get(iterate));
						pointer.setNext(new Linked_List<Variable>(recieved));
						pointer = pointer.getNext();
						iterate++;
					}
					parents_for_variable = parents_for_variable.getNext();
					cpt_probability = cpt_element.getElementsByTagName("TABLE").item(0).getTextContent();					



				}
				if(parents_for_variable!=null) //if the variable in the CPT has parent
				{
					bayesian_network.add(new CPT(getVariable(variables,cpt_variable_name),parents_for_variable,cpt_probability));	
				}
				else
				{
					bayesian_network.add(new CPT(getVariable(variables,cpt_variable_name),cpt_probability));
				}

			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(bayesian_network);

		for (Iterator iterator = bayesian_network.iterator(); iterator.hasNext();) {
			CPT cpt = (CPT) iterator.next();
			cpt.printTruthTable();
			System.out.println("-------------");

		}
		System.out.println(queries.getValue());

		Linked_List<String> queries_iterator = queries;
		try {
		File new_file = new File("output.txt");
		new_file.createNewFile();
		FileWriter fw = new FileWriter("output.txt");
		double []arr;
		while(queries_iterator!=null)
		{
			char desirable_func = queries_iterator.getValue().charAt(queries_iterator.getValue().length()-1);
			if(desirable_func == '1')
			{
				arr = SimpleConclusion(variables,queries_iterator.getValue().substring(0 ,queries_iterator.getValue().length()-2) , bayesian_network);
				fw.write(arr[0] + "," + (int)arr[1] + "," + (int)arr[2] + "\n");
			}
			queries_iterator = queries_iterator.getNext();
			
		}
		fw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}




}
