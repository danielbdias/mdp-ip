package lrs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

import mdp.Config;

public class LRSCaller {
	final static String LINE_BREAK = System.getProperty("line.separator");
	
	public static List<HashMap<String, Double>> callLRSToGetVertex(LinearConstraintExpression[] expressions, String[] variables) {
		String lrsInputFilePath = createTempPath(Config.getConfig().getLrsInputFile());
		String lrsOutputFilePath = createTempPath(Config.getConfig().getLrsOutputFile());
		
		File lrsInputFile = null, lrsOutputFile = null;
		
		List<HashMap<String, Double>> result = null;
		
		try {
			lrsInputFile = new File(lrsInputFilePath);
			createLrsInputFile(lrsInputFile, expressions, variables);
			
			lrsOutputFile = new File(lrsOutputFilePath);
			
			callLrs(lrsInputFile, lrsOutputFile);
			
			result = readLrsOutputFile(lrsOutputFile, variables);	
		} finally {
			if (lrsInputFile != null) lrsInputFile.delete();
			if (lrsOutputFile != null) lrsOutputFile.delete();
		}
		
		return result;
	}
	
	private static List<HashMap<String, Double>> readLrsOutputFile(
			File lrsOutputFile, String[] variables) {
		
		final Exception INVALID_FORMAT_ERROR = new Exception("Output file in invalid format !");
		
		List<HashMap<String, Double>> result = new ArrayList<HashMap<String,Double>>();
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(lrsOutputFile));
			
			String line = null;
			
			while ((line = reader.readLine()) != null)
				if (line.equals("begin")) break;
			
			if (!line.equals("begin")) throw INVALID_FORMAT_ERROR; 
			line = reader.readLine();
			
			String[] header = splitWithoutEmptyEntries(line, "( )");
			
			if (header.length != 3) throw INVALID_FORMAT_ERROR;
			
			int variablesDetected = Integer.parseInt(header[1]) - 1;
			
			if (variablesDetected == 0) throw INVALID_FORMAT_ERROR;
			
			while ((line = reader.readLine()) != null) {
				if (line.equals("end")) break;
				
				String[] temp = splitWithoutEmptyEntries(line, "( )");
				
				if (temp.length != variablesDetected + 1) throw INVALID_FORMAT_ERROR;
				
				boolean vertexFlag = temp[0].equals("1");
				
				if (vertexFlag) {
					HashMap<String, Double> vertex = new HashMap<String, Double>();
					
					for (int j = 0; j < variables.length; j++) {
						String variableName = variables[j];
						String variableValueAsString = temp[j + 1];
						
						RationalNumber variableValue = RationalNumber.parseRationalNumber(variableValueAsString);
						vertex.put(variableName, variableValue.toDouble());
					}
					
					result.add(vertex);
				}
			}
			
			reader.close();
		}
		catch (Exception ex) {
			System.err.println("Error when reading LRS output file.");
	   		System.err.println("Error:" + ex);
	   		ex.printStackTrace(System.err);
	   		System.exit(-1);
		}
		

		return result;
	}

	private static void callLrs(File lrsInputFile, File lrsOutputFile) {
    	try {
    		String command = String.format("lrs %s %s", lrsInputFile.getAbsolutePath(), lrsOutputFile.getAbsolutePath()); 
    		
    		// Open files for reading and writing
    		Process pros = Runtime.getRuntime().exec(command);
    		 
    		BufferedReader process_out = new BufferedReader(new InputStreamReader(pros.getInputStream()));
    		
    		PrintWriter process_in  = new PrintWriter(pros.getOutputStream(), true);
    		process_in.close(); // Need to close input stream so process exits!!!
    		
    		ArrayList<String> lines = new ArrayList<String>();
    		
    		// Provide input to process (could come from any stream)
    		String temp = null;

    		while ((temp = process_out.readLine()) != null)
    			lines.add(temp);    		

			process_out.close();

			pros.waitFor();
    		
			if (pros.exitValue() != 0)
			{
				System.err.println("Error when executing LRS.");
				
				for (String line : lines)
					System.err.println(line);
				
				System.exit(-1);
			}			
    	 } catch (Exception ie) {
    		 System.err.println("Error when executing LRS.");
    		 System.err.println("Error:" + ie);
    		 ie.printStackTrace(System.err);
    		 System.exit(-1);
    	 }
	}

	private static void createLrsInputFile(File lrsInputFile,
			LinearConstraintExpression[] expressions, String[] variables) {
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(lrsInputFile));
			
			writer.write("temp" + LINE_BREAK);
			writer.write("H-representation" + LINE_BREAK);
			writer.write("begin" + LINE_BREAK);
			
			writer.write(String.format("%d %d rational", expressions.length, variables.length + 1) + LINE_BREAK);
			
			for (LinearConstraintExpression expression : expressions)
				writer.write(expression.toString() + LINE_BREAK);
			
			writer.write("end");
			
			writer.close();
		} catch (IOException e) {
			System.err.println("Error: " + e.toString());
			e.printStackTrace(System.err);
			System.exit(-1);
		} 
	}

	private static String createTempPath(String path) {
		String extension = path.substring(path.lastIndexOf('.'));	
 		String fileWithoutExtension = path.replace(extension, "");
 		
 		if (fileWithoutExtension.contains("_"))
 			fileWithoutExtension = fileWithoutExtension.replace(fileWithoutExtension.substring(fileWithoutExtension.lastIndexOf('_')), "");
 		
 		return fileWithoutExtension + "_" + System.currentTimeMillis() + extension;
	}
	
	private static String[] splitWithoutEmptyEntries(String value, String match) {
		String[] result = value.split(match);
		
		List<String> list = new ArrayList<String>();
		
		for (String string : result) {
			if (string != null && !string.isEmpty())
				list.add(string);
		}
		
		return list.toArray(new String[0]);
	}
}