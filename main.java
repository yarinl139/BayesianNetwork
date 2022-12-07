import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
			DecimalFormat df = new DecimalFormat("#.#####");
	        df.setRoundingMode(RoundingMode.CEILING);
	        
			while(queries_iterator!=null)
			{
				char desirable_func = queries_iterator.getValue().charAt(queries_iterator.getValue().length()-1);
				if(desirable_func == '1')
				{
					arr = SimpleDeduction.Deduction(variables,queries_iterator.getValue().substring(0 ,queries_iterator.getValue().length()-2) , bayesian_network);
					fw.write(df.format(arr[0]) + "," + (int)arr[1] + "," + (int)arr[2] + "\n");
				}
				if(desirable_func == '2')
				{
					arr = VE.VariableElimination(variables,queries_iterator.getValue().substring(0 ,queries_iterator.getValue().length()-2) , bayesian_network);
					fw.write(df.format(arr[0]) + "," + (int)arr[1] + "," + (int)arr[2] + "\n");
				}
				if(desirable_func == '3')
				{
					arr = MyHeuristic.Heuristic(variables,queries_iterator.getValue().substring(0 ,queries_iterator.getValue().length()-2) , bayesian_network);
					fw.write(df.format(arr[0]) + "," + (int)arr[1] + "," + (int)arr[2] + "\n");
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
