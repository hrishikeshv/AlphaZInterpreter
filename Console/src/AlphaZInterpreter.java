import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AlphaZInterpreter {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		// TODO Auto-generated method stub
		Scanner reader = new Scanner(System.in);
		CommandProcessor processor = new CommandProcessor();
		while(true){
			System.out.print(">>>");
			String s=reader.nextLine();
			Pattern p= Pattern.compile("(\\w+)[(](\"[^\"]*\")[)];");
			//System.out.println(s);
			if(s.contains("readScript")){
				Matcher m = p.matcher(s);
				if(m.find()){
					if(m.group(1).equals("readScript")){
						String filename = m.group(2);
						if(filename.startsWith("\"") && filename.endsWith("\"")){
							try{
								filename = filename.replaceAll("\"", "");
								Scanner scan = new Scanner(new File(filename));
							    while(scan.hasNextLine()){
							        String line = scan.nextLine();
							        if(line.startsWith("#") || line.isEmpty())
							        	continue;
							        System.out.println(line);
							        processor.computeFunc(line);
							    }
							}
							catch(FileNotFoundException e){
								System.err.println("Filepath does not exist");
							}
								
						}
						else{
							System.err.println("Incorrect parameter types");
						}
						
					}
					else{
						System.err.println("Function does not exist");
					}
				}
			}
			else if(s.equals("exit")){
				System.out.println("Program terminated");
				break;
			}
			else
				processor.computeFunc(s);
		}
		reader.close();
		
		/*
		 *	TODO:
		 *	1. Check if program already in context
		 *	2. Generate switch case via parsing xml file
		 *	3. Check function validity by comparing number of parameters
		 *	
		 *	prog = ReadAlphabets("/home/hrishi/workspace/AlphabetsExamples/MatMult/MM.ab")		
		 */

	}

}
