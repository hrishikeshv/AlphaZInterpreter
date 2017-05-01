import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class AlphaZInterpreter {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		// TODO Auto-generated method stub
		Scanner reader = new Scanner(System.in);
		CommandProcessor processor = new CommandProcessor();
		while(true){
			System.out.print(">>>");
			String s=reader.nextLine();
			//System.out.println(s);
			if(s.equals("exit")){
				System.out.println("Program terminated");
				break;
			}
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
