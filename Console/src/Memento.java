import java.util.ArrayList;
import java.util.Stack;

import org.polymodel.polyhedralIR.*;

public final class Memento
{
	private Program currState;

	private Stack<Program> undoStack = new Stack<Program>();
	private Stack<Program> redoStack = new Stack<Program>();

	private ArrayList<String> undone = new ArrayList<String>();
	private ArrayList<String> transformations = new ArrayList<String>();

	public Memento(Program p)
	{
		currState = p.copy();
	}

	public void clear()
	{
		currState = null;
		undoStack.clear();
		redoStack.clear();
		undone.clear();
		transformations.clear();
	}
	
	public final void setState(Program p)
	{
		currState = p.copy();
	}

	public final void recordAction(String action, Program program)
	{
		undoStack.push(currState);
		redoStack.clear();
		undone.clear();
		transformations.add(action);
		currState = program.copy();
	}

	public final boolean canUndo()
	{
		return !undoStack.empty();
	}

	public final boolean canRedo()
	{
		return !redoStack.empty();
	}

	//Undo the last 'n' actions.
	public final Program undo(int n)
	{
		int n0 = n;
		while(!undoStack.empty() && n > 0)
		{
			String last = transformations.get(transformations.size() - 1);
			undone.add(last);
			System.out.println("Undid: " + last);
			transformations.remove(transformations.size() - 1);
			redoStack.push(currState.copy());
			currState = undoStack.pop();
			n--;
		}

		if(n > 0) 
			System.out.println("Could not undo all " + n0 + " actions");
	
		return currState;
	}

	//ReDo the last 'n' undone actions.
	public final Program redo(int n)
	{
		int n0 = n;
		while(!redoStack.empty() && n > 0)
		{
			String last = undone.get(undone.size() -1);
			transformations.add(last);
			System.out.println("Redid: " +  last);
			undone.remove(undone.size() - 1);
			undoStack.push(currState.copy());
			currState = redoStack.pop();
			n--;
		}
		
		if(n > 0) 
			System.out.println("Could not redo all " + n0 + " actions");

		return currState;
	}

	//Print the effective transformation carried out so far.
	public final void printTransform()
	{
		for(String cmd: transformations)
		{
			System.out.println(cmd);	
		}
	}
};