import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.polymodel.polyhedralIR.AffineFunction;
import org.polymodel.polyhedralIR.Domain;
import org.polymodel.polyhedralIR.Program;
import org.polymodel.polyhedralIR.impl.ProgramImpl;
import org.polymodel.polyhedralIR.polyIRCG.generator.CodeGenOptions;
import org.polymodel.polyhedralIR.polyIRCG.generator.TiledCodeGenOptions;
import org.polymodel.prdg.PRDG;
import org.polymodel.prdg.scheduling.ScheduledStatement;

import alphaz.mde.Analysis;
import alphaz.mde.Basic;
import alphaz.mde.Calculator;
import alphaz.mde.CodeGen;
import alphaz.mde.TargetMapping;
import alphaz.mde.Transformation;
import alphaz.mde.transformation.MonoparametricTiling;
import alphaz.mde.transformation.Reduction;


public class CommandProcessor {
	String progname; 
	Pattern p= Pattern.compile("((\\w*)[=])?(\\w*)[(](\\S*(,\\S*)*)[)];");
	HashMap<String,String> methodmap;
	SymbolTable st;
	HelpPrinter hp = new HelpPrinter();
	CommandProcessor() throws SecurityException, ClassNotFoundException{
		methodmap = genReturnTypeMap();
		st = new SymbolTable();
	}
	public HashMap<String,String> genReturnTypeMap() throws SecurityException, ClassNotFoundException{
		List<Class> subcatgs = Arrays.asList(Basic.class,Reduction.class,MonoparametricTiling.class,Transformation.class,CodeGen.class,Analysis.class,TargetMapping.class);
		HashMap<String,String> methodreturn = new HashMap();
		for(Class subcat: subcatgs){
			Method[] methods = subcat.getDeclaredMethods();
			for(Method m: methods){
				methodreturn.put(m.getName(), m.getReturnType().toString());
			}
		}
		return methodreturn;
	}
	
	public Object[] processParams(String[] params) throws IOException{
		List<Object> procparams = new ArrayList<Object>();
		for(String p: params){
			//System.out.println(p);
			if(p.startsWith("\"") && p.endsWith("\"")){
				procparams.add(p.replaceAll("\"",""));
			}
			else if(p.matches("\\d+")){
				procparams.add(Integer.valueOf(p));
			}
			else{
				if(!st.contains(p)){
					throw new IOException("\'" + p + "\' not defined");
				}
				Object obj=st.get(p);
				procparams.add(obj);
			}
		}
		return procparams.toArray(new Object[procparams.size()]);
		
	}
	public void computeFunc(String input) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
		input = input.replaceAll("\\s+", "");
		Matcher m=p.matcher(input);
		String func = null;
		String args[] = null;
		String assignvar = null;
		String paramstr = null;
		
		if(m.find()){
			input = input.substring(0, input.length()-1);
			assignvar = m.group(2);
			func = m.group(3);
			paramstr = m.group(4);
			args = paramstr.replaceAll("\\)", "").split(",");
		}
		else{
			System.out.println("Syntax Error");
		}
		
		Object[] params = processParams(args);
		if(methodmap.get(func).equals("void") && assignvar != null){
			System.out.println("Method returns void. Cannot assign to variable");
			return;
		}
		if(st.contains(assignvar)){
			System.out.println("Redeclaration of variable " + assignvar);
			return;
		}
		Object result = null;
		switch(func){
		case "ASave":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Basic.ASave((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Basic.ASave((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ASaveSystem":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Basic.ASaveSystem((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Basic.ASaveSystem((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "AShow":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				result = Basic.AShow((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				result = Basic.AShow((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Save":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Basic.Save((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Basic.Save((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "SaveSystem":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Basic.SaveSystem((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Basic.SaveSystem((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Show":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				result = Basic.Show((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				result = Basic.Show((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Normalize":
			if((params.length == 1) && (params[0] instanceof Program)){
				Basic.Normalize((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "PrintAST":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				result = Basic.PrintAST((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				result = Basic.PrintAST((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ReadAlphabets":
			if((params.length == 1) && (params[0] instanceof String)){
				result = Basic.ReadAlphabets((String) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "RenameSystem":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Basic.RenameSystem((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "RenameVariable":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Basic.RenameVariable((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "RemoveUnusedVariables":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Basic.RemoveUnusedVariables((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Basic.RemoveUnusedVariables((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "PermutationCaseReduce":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Reduction.PermutationCaseReduce((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Reduction.PermutationCaseReduce((Program) params[0]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Reduction.PermutationCaseReduce((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ReductionDecomposition":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Reduction.ReductionDecomposition((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "SimplifyingReduction":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Reduction.SimplifyingReduction((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
//			else if((params.length == 1) && (params[0] instanceof Program)){
//				Reduction.SimplifyingReduction((Program) params[0]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "NormalizeReduction":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Reduction.NormalizeReduction((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Reduction.NormalizeReduction((Program) params[0]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Reduction.NormalizeReduction((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "FactorOutFromReduction":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Reduction.FactorOutFromReduction((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "SplitReductionBody":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Reduction.SplitReductionBody((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "TransformReductionBody":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Reduction.TransformReductionBody((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "SerializeReduction":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Reduction.SerializeReduction((Program) params[0], (String) params[1], (String) params[2]);
			}
//			else if((params.length == 1) && (params[0] instanceof Program)){
//				Reduction.SerializeReduction((Program) params[0]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "MergeReductions":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Reduction.MergeReductions((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ReductionComposition":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Reduction.ReductionComposition((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "monoparametricTiling_noOutlining":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Boolean) && (params[4] instanceof Integer)){
				MonoparametricTiling.monoparametricTiling_noOutlining((Program) params[0], (String) params[1], (String) params[2], (Boolean) params[3], (Integer) params[4]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "monoparametricTiling_Outlining_noSubsystem":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Boolean) && (params[4] instanceof Integer)){
				MonoparametricTiling.monoparametricTiling_Outlining_noSubsystem((Program) params[0], (String) params[1], (String) params[2], (Boolean) params[3], (Integer) params[4]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "monoparametricTiling_Outlining":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Boolean) && (params[4] instanceof Integer)){
				MonoparametricTiling.monoparametricTiling_Outlining((Program) params[0], (String) params[1], (String) params[2], (Boolean) params[3], (Integer) params[4]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setRatio":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				MonoparametricTiling.setRatio((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setMinParamValues":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer)){
				MonoparametricTiling.setMinParamValues((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setTileGroup":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				MonoparametricTiling.setTileGroup((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setCoBPreprocess":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				MonoparametricTiling.setCoBPreprocess((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "CoB":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof AffineFunction)){
				Transformation.CoB((Program) params[0], (String) params[1], (String) params[2], (AffineFunction) params[3]);
			}
//			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
//				Transformation.CoB((Program) params[0], (String) params[1], (String) params[2]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ForceCoB":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof AffineFunction)){
				Transformation.ForceCoB((Program) params[0], (String) params[1], (String) params[2], (AffineFunction) params[3]);
			}
//			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
//				Transformation.ForceCoB((Program) params[0], (String) params[1], (String) params[2]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Split":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String) && (params[4] instanceof String)){
				Transformation.Split((Program) params[0], (String) params[1], (String) params[2], (String) params[3], (String) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Transformation.Split((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Merge":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String) && (params[4] instanceof String)){
				Transformation.Merge((Program) params[0], (String) params[1], (String) params[2], (String) params[3], (String) params[4]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Inline":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String) && (params[4] instanceof Integer)){
				Transformation.Inline((Program) params[0], (String) params[1], (String) params[2], (String) params[3], (Integer) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Transformation.Inline((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Simplify":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Transformation.Simplify((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Transformation.Simplify((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createFreeScheduler":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				result = Transformation.createFreeScheduler((Program) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				result = Transformation.createFreeScheduler((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "SplitUnion":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Transformation.SplitUnion((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ApplySTMap":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Transformation.ApplySTMap((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "UniformizeInContext":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer)){
				Transformation.UniformizeInContext((Program) params[0], (String) params[1], (Integer) params[2]);
			}
			else if((params.length == 1) && (params[0] instanceof Program)){
				Transformation.UniformizeInContext((Program) params[0]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Transformation.UniformizeInContext((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "InlineForce":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String) && (params[4] instanceof Integer)){
				Transformation.InlineForce((Program) params[0], (String) params[1], (String) params[2], (String) params[3], (Integer) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Transformation.InlineForce((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "InlineAll":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Transformation.InlineAll((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "InlineAllForce":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Transformation.InlineAllForce((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "InlineSubSystem":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Transformation.InlineSubSystem((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "OutlineSubSystem":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Transformation.OutlineSubSystem((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "AddLocal":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Transformation.AddLocal((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
//			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
//				Transformation.AddLocal((Program) params[0], (String) params[1], (String) params[2]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "AddLocalUnique":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				Transformation.AddLocalUnique((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "DetectReductions":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Transformation.DetectReductions((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "reduceDimVariable":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				result = Transformation.reduceDimVariable((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "alignDimVariable":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				result = Transformation.alignDimVariable((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateScheduledCode":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String) && (params[4] instanceof Boolean)){
				CodeGen.generateScheduledCode((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3], (Boolean) params[4]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateScheduledCode((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Boolean)){
				CodeGen.generateScheduledCode((Program) params[0], (String) params[1], (String) params[2], (Boolean) params[3]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generateScheduledCode((Program) params[0], (String) params[1]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Boolean)){
				CodeGen.generateScheduledCode((Program) params[0], (String) params[1], (Boolean) params[2]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generateScheduledCode((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateWriteC":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generateWriteC((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateWriteC((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generateWriteC((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateWrapper":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generateWrapper((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateWrapper((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generateWrapper((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateVerificationCode":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateVerificationCode((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generateVerificationCode((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateMakefile":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generateMakefile((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generateMakefile((Program) params[0], (String) params[1]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateMakefile((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions)){
				CodeGen.generateMakefile((Program) params[0], (String) params[1], (CodeGenOptions) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateMakefileInternal":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generateMakefileInternal((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateMakefileInternal((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createCGOptionForWriteC":
			if((params.length == 0)){
				result = CodeGen.createCGOptionForWriteC();
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createCGOptionForScheduledC":
			if((params.length == 0)){
				result = CodeGen.createCGOptionForScheduledC();
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createCGOptionForHybridScheduledCGPU":
			if((params.length == 1) && (params[0] instanceof Integer)){
				result = CodeGen.createCGOptionForHybridScheduledCGPU((Integer) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createCGOptionForHybridScheduledC":
			if((params.length == 1) && (params[0] instanceof Integer)){
				result = CodeGen.createCGOptionForHybridScheduledC((Integer) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setCGOptionFlattenArrays":
			if((params.length == 2) && (params[0] instanceof CodeGenOptions) && (params[1] instanceof Integer)){
				CodeGen.setCGOptionFlattenArrays((CodeGenOptions) params[0], (Integer) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setCGOptionDisableNormalize_depreciated":
			if((params.length == 1) && (params[0] instanceof CodeGenOptions)){
				CodeGen.setCGOptionDisableNormalize_depreciated((CodeGenOptions) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createTiledCGOptionForScheduledC":
			if((params.length == 0)){
				result = CodeGen.createTiledCGOptionForScheduledC();
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "createCGOptionsForPCOT":
			if((params.length == 0)){
				result = CodeGen.createCGOptionsForPCOT();
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setTiledCGOptionOptimize":
			if((params.length == 2) && (params[0] instanceof TiledCodeGenOptions) && (params[1] instanceof Integer)){
				CodeGen.setTiledCGOptionOptimize((TiledCodeGenOptions) params[0], (Integer) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "getDefaultCodeGenOptions":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				result = CodeGen.getDefaultCodeGenOptions((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateScanC":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof CodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generateScanC((Program) params[0], (String) params[1], (CodeGenOptions) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generatePCOTCode":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof TiledCodeGenOptions) && (params[3] instanceof String) && (params[4] instanceof Boolean)){
				CodeGen.generatePCOTCode((Program) params[0], (String) params[1], (TiledCodeGenOptions) params[2], (String) params[3], (Boolean) params[4]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generatePCOTCode((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Boolean)){
				CodeGen.generatePCOTCode((Program) params[0], (String) params[1], (String) params[2], (Boolean) params[3]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generatePCOTCode((Program) params[0], (String) params[1]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Boolean)){
				CodeGen.generatePCOTCode((Program) params[0], (String) params[1], (Boolean) params[2]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof TiledCodeGenOptions) && (params[3] instanceof String)){
				CodeGen.generatePCOTCode((Program) params[0], (String) params[1], (TiledCodeGenOptions) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "generateFMPPCode":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof TiledCodeGenOptions) && (params[4] instanceof String)){
				CodeGen.generateFMPPCode((Program) params[0], (String) params[1], (String) params[2], (TiledCodeGenOptions) params[3], (String) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				CodeGen.generateFMPPCode((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.generateFMPPCode((Program) params[0], (String) params[1], (String) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				CodeGen.generateFMPPCode((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setVecOptionForTiledC":
			if((params.length == 4) && (params[0] instanceof TiledCodeGenOptions) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				CodeGen.setVecOptionForTiledC((TiledCodeGenOptions) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else if((params.length == 1) && (params[0] instanceof TiledCodeGenOptions)){
				CodeGen.setVecOptionForTiledC((TiledCodeGenOptions) params[0]);
			}
			else if((params.length == 2) && (params[0] instanceof TiledCodeGenOptions) && (params[1] instanceof String)){
				CodeGen.setVecOptionForTiledC((TiledCodeGenOptions) params[0], (String) params[1]);
			}
			else if((params.length == 3) && (params[0] instanceof TiledCodeGenOptions) && (params[1] instanceof String) && (params[2] instanceof String)){
				CodeGen.setVecOptionForTiledC((TiledCodeGenOptions) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setVecVarsForTiledC":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof TiledCodeGenOptions) && (params[3] instanceof String)){
				CodeGen.setVecVarsForTiledC((Program) params[0], (String) params[1], (TiledCodeGenOptions) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setVecVarForTiledC":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof TiledCodeGenOptions) && (params[3] instanceof String) && (params[4] instanceof String)){
				CodeGen.setVecVarForTiledC((Program) params[0], (String) params[1], (TiledCodeGenOptions) params[2], (String) params[3], (String) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof TiledCodeGenOptions) && (params[3] instanceof String)){
				CodeGen.setVecVarForTiledC((Program) params[0], (String) params[1], (TiledCodeGenOptions) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "addRecursionDepthForPCOT":
			if((params.length == 1) && (params[0] instanceof TiledCodeGenOptions)){
				result = CodeGen.addRecursionDepthForPCOT((TiledCodeGenOptions) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "readDomain":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof String)){
				result = Calculator.readDomain((Domain) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof String)){
				result = Calculator.readDomain((String) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "readFunction":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof String)){
				result = Calculator.readFunction((Domain) params[0], (String) params[1]);
			}
			else if((params.length == 1) && (params[0] instanceof String)){
				result = Calculator.readFunction((String) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "inverse":
			if((params.length == 1) && (params[0] instanceof AffineFunction)){
				result = Calculator.inverse((AffineFunction) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "inverseInContext":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof AffineFunction)){
				result = Calculator.inverseInContext((Domain) params[0], (AffineFunction) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "compose":
			if((params.length == 2) && (params[0] instanceof AffineFunction) && (params[1] instanceof AffineFunction)){
				result = Calculator.compose((AffineFunction) params[0], (AffineFunction) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "intersection":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof Domain)){
				result = Calculator.intersection((Domain) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "union":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof Domain)){
				result = Calculator.union((Domain) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "join":
			if((params.length == 2) && (params[0] instanceof AffineFunction) && (params[1] instanceof AffineFunction)){
				result = Calculator.join((AffineFunction) params[0], (AffineFunction) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "isEmpty":
			if((params.length == 1) && (params[0] instanceof Domain)){
				result = Calculator.isEmpty((Domain) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "isEquivalent":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof Domain)){
				result = Calculator.isEquivalent((Domain) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "image":
			if((params.length == 2) && (params[0] instanceof AffineFunction) && (params[1] instanceof Domain)){
				result = Calculator.image((AffineFunction) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "preImage":
			if((params.length == 2) && (params[0] instanceof AffineFunction) && (params[1] instanceof Domain)){
				result = Calculator.preImage((AffineFunction) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "difference":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof Domain)){
				result = Calculator.difference((Domain) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "simplifyInContext":
			if((params.length == 2) && (params[0] instanceof Domain) && (params[1] instanceof Domain)){
				result = Calculator.simplifyInContext((Domain) params[0], (Domain) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "BuildPRDG":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer)){
				result = Analysis.BuildPRDG((Program) params[0], (String) params[1], (Integer) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				result = Analysis.BuildPRDG((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "ExportPRDG":
			if((params.length == 2) && (params[0] instanceof PRDG) && (params[1] instanceof String)){
				Analysis.ExportPRDG((PRDG) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "CheckProgram":
			if((params.length == 1) && (params[0] instanceof Program)){
				Analysis.CheckProgram((Program) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "VerifyTargetMapping":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				Analysis.VerifyTargetMapping((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "CheckSystem":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				Analysis.CheckSystem((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "Farkas1DScheduler":
			if((params.length == 1) && (params[0] instanceof PRDG)){
				result = Analysis.Farkas1DScheduler((PRDG) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "FarkasMDScheduler":
			if((params.length == 1) && (params[0] instanceof PRDG)){
				result = Analysis.FarkasMDScheduler((PRDG) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "PlutoScheduler":
			if((params.length == 1) && (params[0] instanceof PRDG)){
				result = Analysis.PlutoScheduler((PRDG) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "revertPRDGEdges":
			if((params.length == 1) && (params[0] instanceof PRDG)){
				result = Analysis.revertPRDGEdges((PRDG) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "printScheduledStatements":
			if((params.length == 1) && (params[0] instanceof List<?>)){
				Analysis.printScheduledStatements((List<ScheduledStatement>) params[0]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSpaceTimeMap":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer) && (params[3] instanceof String) && (params[4] instanceof AffineFunction)){
				TargetMapping.setSpaceTimeMap((Program) params[0], (String) params[1], (Integer) params[2], (String) params[3], (AffineFunction) params[4]);
			}
//			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer) && (params[3] instanceof String)){
//				TargetMapping.setSpaceTimeMap((Program) params[0], (String) params[1], (Integer) params[2], (String) params[3]);
//			}
//			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
//				TargetMapping.setSpaceTimeMap((Program) params[0], (String) params[1], (String) params[2]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setMemoryMap":
			if((params.length == 6) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String) && (params[4] instanceof AffineFunction) && (params[5] instanceof String)){
				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2], (String) params[3], (AffineFunction) params[4], (String) params[5]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof AffineFunction) && (params[4] instanceof String)){
				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2], (AffineFunction) params[3], (String) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof AffineFunction)){
				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2], (AffineFunction) params[3]);
			}
			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String) && (params[4] instanceof AffineFunction)){
				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2], (String) params[3], (AffineFunction) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
//			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
//				TargetMapping.setMemoryMap((Program) params[0], (String) params[1], (String) params[2]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setMemorySpace":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				TargetMapping.setMemorySpace((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setStatementOrdering":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				TargetMapping.setStatementOrdering((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "listSpaceTimeMaps":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer)){
				TargetMapping.listSpaceTimeMaps((Program) params[0], (String) params[1], (Integer) params[2]);
			}
			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				TargetMapping.listSpaceTimeMaps((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "listMemoryMaps":
			if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
				TargetMapping.listMemoryMaps((Program) params[0], (String) params[1]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSchedule":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof List<?>)){
				TargetMapping.setSchedule((Program) params[0], (String) params[1], (List<ScheduledStatement>) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setParallel":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer) && (params[3] instanceof String) && (params[4] instanceof String)){
				TargetMapping.setParallel((Program) params[0], (String) params[1], (Integer) params[2], (String) params[3], (String) params[4]);
			}
			else if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof String)){
				TargetMapping.setParallel((Program) params[0], (String) params[1], (String) params[2], (String) params[3]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "CreateSpaceTimeLevel":
			if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer)){
				TargetMapping.CreateSpaceTimeLevel((Program) params[0], (String) params[1], (Integer) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setOrderingDimensions":
			if((params.length == 4) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer) && (params[3] instanceof String)){
				TargetMapping.setOrderingDimensions((Program) params[0], (String) params[1], (Integer) params[2], (String) params[3]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer)){
				TargetMapping.setOrderingDimensions((Program) params[0], (String) params[1], (Integer) params[2]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				TargetMapping.setOrderingDimensions((Program) params[0], (String) params[1], (String) params[2]);
			}
//			else if((params.length == 2) && (params[0] instanceof Program) && (params[1] instanceof String)){
//				TargetMapping.setOrderingDimensions((Program) params[0], (String) params[1]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSpaceTimeMapForMemoryAllocation":
			if((params.length == 6) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof AffineFunction)){
				TargetMapping.setSpaceTimeMapForMemoryAllocation((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (AffineFunction) params[5]);
			}
//			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer)){
//				TargetMapping.setSpaceTimeMapForMemoryAllocation((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSpaceTimeMapForValueCopy":
			if((params.length == 6) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof AffineFunction)){
				TargetMapping.setSpaceTimeMapForValueCopy((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (AffineFunction) params[5]);
			}
//			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer)){
//				TargetMapping.setSpaceTimeMapForValueCopy((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSpaceTimeMapForMemoryFree":
			if((params.length == 6) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof AffineFunction)){
				TargetMapping.setSpaceTimeMapForMemoryFree((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (AffineFunction) params[5]);
			}
//			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer)){
//				TargetMapping.setSpaceTimeMapForMemoryFree((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSpaceTimeMapForUseEquationOptimization":
			if((params.length == 8) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof AffineFunction) && (params[6] instanceof AffineFunction) && (params[7] instanceof AffineFunction)){
				TargetMapping.setSpaceTimeMapForUseEquationOptimization((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (AffineFunction) params[5], (AffineFunction) params[6], (AffineFunction) params[7]);
			}
//			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer)){
//				TargetMapping.setSpaceTimeMapForUseEquationOptimization((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4]);
//			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setMemorySpaceForUseEquationOptimization":
			if((params.length == 6) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof String)){
				TargetMapping.setMemorySpaceForUseEquationOptimization((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (String) params[5]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setBandForTiling":
			if((params.length == 7) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof String) && (params[5] instanceof Integer) && (params[6] instanceof Integer)){
				TargetMapping.setBandForTiling((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (String) params[4], (Integer) params[5], (Integer) params[6]);
			}
			else if((params.length == 6) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof Integer)){
				TargetMapping.setBandForTiling((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (Integer) params[5]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setSubTilingWithinBand":
			if((params.length == 7) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof Integer) && (params[5] instanceof Integer) && (params[6] instanceof String)){
				TargetMapping.setSubTilingWithinBand((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4], (Integer) params[5], (String) params[6]);
			}
			else if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String) && (params[3] instanceof Integer) && (params[4] instanceof String)){
				TargetMapping.setSubTilingWithinBand((Program) params[0], (String) params[1], (String) params[2], (Integer) params[3], (String) params[4]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		case "setDefaultDTilerConfiguration":
			if((params.length == 5) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof Integer) && (params[3] instanceof Integer) && (params[4] instanceof String)){
				TargetMapping.setDefaultDTilerConfiguration((Program) params[0], (String) params[1], (Integer) params[2], (Integer) params[3], (String) params[4]);
			}
			else if((params.length == 3) && (params[0] instanceof Program) && (params[1] instanceof String) && (params[2] instanceof String)){
				TargetMapping.setDefaultDTilerConfiguration((Program) params[0], (String) params[1], (String) params[2]);
			}
			else{
				hp.printHelp(func);
			}
			break;
		}
		
		if(assignvar != null){
			st.put(assignvar, result);
		}
	}
}
