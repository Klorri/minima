package org.minima.miniscript;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.minima.miniscript.exceptions.ExecutionException;
import org.minima.miniscript.exceptions.MinimaParseException;
import org.minima.miniscript.expressions.GlobalExpression;
import org.minima.miniscript.functions.MinimaFunction;
import org.minima.miniscript.statements.StatementBlock;
import org.minima.miniscript.statements.StatementParser;
import org.minima.miniscript.tokens.Token;
import org.minima.miniscript.values.BooleanValue;
import org.minima.miniscript.values.HEXValue;
import org.minima.miniscript.values.NumberValue;
import org.minima.miniscript.values.ScriptValue;
import org.minima.miniscript.values.Value;
import org.minima.objects.StateVariable;
import org.minima.objects.Transaction;
import org.minima.objects.base.MiniByte;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.utils.MinimaLogger;

/**
 * A RamScript Contract. Executes a given script, with the 
 * given Witness, and returns either TRUE or FALSE
 * 
 * The language is a BASIC variant and very simple with only 3 commands.
 * 
 * LET | IF | RETURN
 * 
 * coupled with a comprehensive EXPRESSION parser
 * 
 * Exception handling IS CONSENSUS CRITICAL. When an exception is thrown 
 * the script immediately exits with a FAIL. Clearly everyone needs to agree on what does
 * and what does not count as an exception. RamScriptExceptions has juicy details.
 * 
 * @author Spartacus Rex
 *
 */

public class Contract {
	
	//The Transaction this Contract is being run on
	Transaction mTransaction;
		
	//The final version of the script
	String mRamScript; 
		
	//The Complete contract code, from root level
	StatementBlock mBlock;
	
	//A list of valid signatures
	ArrayList<Value> mSignatures;
	
	//A list of all the user-defined variables
	Hashtable<String, Value> mVariables;
	
	//A list of all the global variables available to scripts, like Blocknumber etc..
	Hashtable<String, Value> mGlobals;
	
	//The ARRAYS are all kept in the String hashtable
	Hashtable<String, Value> mArrayVariables;
		
	//The previous state variables - accessed from the MMR data
	ArrayList<StateVariable> mPrevState = new ArrayList<StateVariable>();
		
	//Has the Script returned TRUE or FALSE
	private boolean mSuccess;
	private boolean mSuccessSet;
	
	/**
	 * Trace shows debug info as the program is parsed and executed
	 */
	boolean mTraceON = false;
	
	/**
	 * Did the contract script parse ok
	 */
	boolean mParseOK;
	
	/**
	 * Was there an Exception error
	 */
	boolean mException;
	String  mExceptionString;
	
	/**
	 * The Number Of Instructions!
	 */
	int mNumInstructions;
	public static final int MAX_INSTRUCTIONS = 256;
	
	/**
	 * A complete log of the contract execution
	 */
	String mCompleteLog="";
	
	/**
	 * Main Constructor
	 * @param zRamScript - the RamScript in ASCII
	 */
	public Contract(String zRamScript, String zSigs, Transaction zTransaction, ArrayList<StateVariable> zPrevState) {	
		this(zRamScript, zSigs, zTransaction, zPrevState, false);
	}
	
	public Contract(String zRamScript, String zSigs, Transaction zTransaction, ArrayList<StateVariable> zPrevState, boolean zTrace) {
		//Trace?
		mCompleteLog ="";
		mTraceON     = zTrace;
		
		//Clean the RamScript
		mRamScript = cleanScript(zRamScript);
	
		mTransaction = zTransaction;
		
		mSignatures = new ArrayList<>();
		mVariables  = new Hashtable<>();
		mGlobals    = new Hashtable<>();
		mArrayVariables      = new Hashtable<>();
		
		mBlock      = null;
		mSuccess    = false;
		mSuccessSet = false;
		mParseOK    = false;
		mException  = false;
		mExceptionString = "";
		
		mNumInstructions = 0;
				
		//Load the Signatures
		StringTokenizer strtok = new StringTokenizer(zSigs, "#");
		while(strtok.hasMoreTokens()) {
			String sig = strtok.nextToken().trim();
			traceLog("Signature : "+sig);
			mSignatures.add( Value.getValue(sig) );
		}
		
		//Begin..
		traceLog("Contract   : "+mRamScript);
		traceLog("Size       : "+mRamScript.length());
		
		//Transaction..
		traceLog("Transaction   : "+mTransaction.toString());
		
		//State Variables
		ArrayList<StateVariable> svs = mTransaction.getCompleteState();
		for(StateVariable sv : svs) {
			traceLog("State["+sv.getPort()+"] : "+sv.getData().toString());
		}

		//PREVSTATE
		if(zPrevState == null) {
			mPrevState = new ArrayList<StateVariable>();	
		}else {
			mPrevState = zPrevState;
			for(StateVariable sv : mPrevState) {
				traceLog("PrevState["+sv.getPort()+"] : "+sv.getData().toString());
			}	
		}
		
		//Parse the tokens
		try {
			//Tokenize the script
			List<Token> tokens = Token.tokenize(mRamScript);
			
			int count=0;
			for(Token tok : tokens) {
				traceLog((count++)+") Token : ["+tok.getTokenTypeString()+"] "+tok.getToken());
			}
		
			//Convert this list of Tokens into a list of Statements
			mBlock = StatementParser.parseTokens(tokens);
			
			traceLog("Script token parse OK.");
			mParseOK = true;
			
		} catch (MinimaParseException e) {
//			e.printStackTrace();
			traceLog("PARSE ERROR : "+e.getMessage());
		}
	}
	
	public void setGlobalVariable(String zGlobal, Value zValue) {
		mGlobals.put(zGlobal, zValue);
		traceLog("Global ["+zGlobal+"] : "+zValue);
	}
	
	/**
	 * Return the array value if found or Number ZERO if not found
	 * @param zPosition
	 * @return
	 */
	public Value getArrayValue(String zPosition) {
		//Get the value
		Value val = mArrayVariables.get(zPosition);
		if(val == null) {
			return new NumberValue(0);
		}
		
		return val;
	}
	
	public void setArrayVariable(String zPosition, Value zValue) {
		mArrayVariables.put(zPosition, zValue);
		traceVariables();
	}
	
	public Value getPrevState(MiniNumber zPrev) throws ExecutionException {
		//Get the state variable..
		for(StateVariable sv : mPrevState) {
			if(sv.getPort().isEqual(zPrev)) {
				//Clean it..
				String stateval = Contract.cleanScript(sv.getData().toString());
				
				//Work it out
				return Value.getValue(stateval);
			}
		}
		
		throw new ExecutionException("PREVSTATE Missing : "+zPrev);
	}
	
	public boolean isParseOK() {
		return mParseOK;
	}
	
	public boolean isException() {
		return mException;
	}
	
	public String getException() {
		return mExceptionString;
	}
	
	public boolean isTrace() {
		return mTraceON;
	}
	
	public void traceLog(String zLog) {
		if(isTrace()) {
			MinimaLogger.log("INST["+mNumInstructions+"] - "+zLog);
		}
		
		//Store the complete Log
		mCompleteLog += "INST["+mNumInstructions+"] - "+zLog+"\n";
	}
	
	public String getCompleteTraceLog() {
		return mCompleteLog;
	}
	
	public void incrementInstructions() throws ExecutionException {
		mNumInstructions++;
		if(mNumInstructions > MAX_INSTRUCTIONS) {
			throw new ExecutionException("MAX instruction number reached! "+mNumInstructions);
		}
	}
	
	public int getNumberOfInstructions() {
		return mNumInstructions;
	}
	
	public void run() {
		if(!mParseOK) {
			traceLog("Script parse FAILED. Please fix and retry.");
			return;
		}
		
		//Run the code block
		try {
			traceLog("Start executing the contract");
			
			mBlock.run(this);
			
		} catch (Exception e) {
			if(mTraceON) {
				e.printStackTrace();
			}
			
			mException = true;
			mExceptionString = e.toString();
			
			//AUTOMATIC FAIL
			traceLog("Execution Error - "+e);
			
			mSuccess 	= false;
			mSuccessSet = true;
		}
		
		//How'd it go..
		traceLog("Contract instructions : "+mNumInstructions);
		traceLog("Contract finished     : "+mSuccess);
	}
	
	public void setRETURNValue(boolean zSUCCESS) {
		if(!mSuccessSet) {
			mSuccess 	= zSUCCESS;
			mSuccessSet = true;
		}
	}
	
	public boolean isSuccess() {
		return mSuccess;
	}
	
	public boolean isSuccessSet() {
		return mSuccessSet;
	}
	
	public String getMiniScript() {
		return mRamScript;
	}
	
	public Transaction getTransaction() {
		return mTransaction;
	}
	
	public Value getVariable(String zName) throws ExecutionException {
		Value ret = mVariables.get(zName);
		if(ret==null) {
			throw new ExecutionException("VARIABLE Missing : "+zName);
		}
		return ret;
	}
	
	public void setVariable(String zName, Value zValue) {
		mVariables.put(zName, zValue);
		
		traceVariables();
		
//		//Output..
//		String varlist = "{ ";
//		Enumeration<String> keys = mVariables.keys();
//		while(keys.hasMoreElements()) {
//			//Get the Key
//			String key = keys.nextElement();
//			
//			//Get the Value
//			Value val = mVariables.get(key);
//			
//			//Log it.. 
//			int type = val.getValueType();
//			switch (type)  {
//				case BooleanValue.VALUE_BOOLEAN :
//					varlist += key+" = "+Boolean.toString(val.isTrue()).toUpperCase()+", ";
//				break;
//				case HEXValue.VALUE_HEX :
//					varlist += key+" = "+val+", ";
//				break;
//				case NumberValue.VALUE_NUMBER :
//					varlist += key+" = "+val+", ";
//				break;
//				case ScriptValue.VALUE_SCRIPT :
//					varlist += key+" = [ "+val+" ], ";
//				break;
//			}		
//		}
//
//		traceLog(varlist+"}");
	}
	
	public void traceVariables() {
		//Output..
		String varlist = "{ ";
		
		//First the normal variables
		Enumeration<String> keys = mVariables.keys();
		while(keys.hasMoreElements()) {
			//Get the Key
			String key = keys.nextElement();
			
			//Get the Value
			Value val = mVariables.get(key);
			
			//Log it.. 
			int type = val.getValueType();
			switch (type)  {
				case BooleanValue.VALUE_BOOLEAN :
					varlist += key+" = "+Boolean.toString(val.isTrue()).toUpperCase()+", ";
				break;
				case ScriptValue.VALUE_SCRIPT :
					varlist += key+" = [ "+val+" ], ";
				break;
				default:
					varlist += key+" = "+val+", ";
				break;
			}		
		}

		//Now the array variables
		keys = mArrayVariables.keys();
		while(keys.hasMoreElements()) {
			//Get the Key
			String key = keys.nextElement();
			
			//Get the Value
			Value val = mArrayVariables.get(key);
			
			//Log it.. 
			String kk = "("+key+")"; 
			int type = val.getValueType();
			switch (type)  {
				case BooleanValue.VALUE_BOOLEAN :
					varlist += kk+" = "+Boolean.toString(val.isTrue()).toUpperCase()+", ";
				break;
				case ScriptValue.VALUE_SCRIPT :
					varlist += kk+" = [ "+val+" ], ";
				break;
				default:
					varlist += kk+" = "+val+", ";
				break;
			}		
		}
		
		traceLog(varlist+"}");
	}
	
	/**
	 * Get a Global value
	 * 
	 * @param zGlobal
	 * @return the value
	 * @throws ExecutionException
	 */
	public Value getGlobal(String zGlobal) throws ExecutionException {
		Value ret = mGlobals.get(zGlobal);
		if(ret==null) {
			throw new ExecutionException("Global not found - "+zGlobal);
		}
		return ret;
	}
	
	/**
	 * Check if this transaction has been signed by this public key
	 * @param zSignature
	 * @return
	 */
	public boolean checkSignature(Value zSignature) {
		MiniData checksig = zSignature.getMiniData();
		
		for(Value sig : mSignatures) {
			if(sig.getMiniData().isNumericallyEqual(checksig)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Convert a SCRIPT into the Required Format for MiniScript.
	 * @param zScript
	 * @return The Converted Script
	 */
	public static String cleanScript(String zScript) {
		//Quick check for empty..
		if(zScript.equals("")) {
			return "";
		}
		
		//Start cleaning..
		String script = new String(" "+zScript.toLowerCase()+" ");
		
		//Replace whitespace with a single space
		script = script.replaceAll("\\s+"," ");
		
		//Remove comments /* .. */
		int comment = script.indexOf("/*");
		while(comment != -1) {
			int endcomment = script.indexOf("*/",comment);
			int len = script.length();
			script = " "+script.substring(0,comment)+" "+script.substring(endcomment+2, len)+" ";
			comment = script.indexOf("/*");
		}
		
		//Incase this is a 'param' string
		script = script.replaceAll(",", " , ");
		script = script.replaceAll(";", " ; ");
		script = script.replaceAll(":", " : ");
		script = script.replaceAll("#", " # ");
		
		//Double up the spaces.. in case of double NOT 
		script = script.replaceAll(" ", "  ");
		
		//STILL NEED TO DO - .. minus.. ignoring numbers..
//		script = script.replaceAll("\\-[a-z]", " - ");
		
		//Operators
		script = script.replaceAll("\\(", " ( ");
		script = script.replaceAll("\\)", " ) ");
		script = script.replaceAll("\\[", " [ ");
		script = script.replaceAll("\\]", " ] ");
		script = script.replaceAll("<<", " << ");
		script = script.replaceAll(">>", " >> ");
		script = script.replaceAll("\\&" , " & ");
		script = script.replaceAll("\\|" , " | ");
		script = script.replaceAll("\\^" , " ^ ");
		script = script.replaceAll("\\*", " * ");
		script = script.replaceAll("\\+", " + ");
		script = script.replaceAll("\\=", " = ");
		script = script.replaceAll("\\%", " % ");
			
		//Boolean
		script = script.replaceAll(" nand ", " NAND ");
		script = script.replaceAll(" nxor ", " NXOR ");
		script = script.replaceAll(" nor ", " NOR ");
		script = script.replaceAll(" and ", " AND ");
		script = script.replaceAll(" xor ", " XOR ");
		script = script.replaceAll(" or ", " OR ");
		script = script.replaceAll(" not ", " NOT ");
		script = script.replaceAll(" neg ", " NEG ");
		script = script.replaceAll(" neq ", " NEQ ");
		script = script.replaceAll(" gte ", " GTE ");
		script = script.replaceAll(" lte ", " LTE ");
		script = script.replaceAll(" gt ", " GT ");
		script = script.replaceAll(" eq ", " EQ ");
		script = script.replaceAll(" lt ", " LT ");
		
		//Commands
		String[] allcommands = Token.TOKENS_COMMAND;
		for(int i=0;i<allcommands.length;i++) {
			String find = " "+allcommands[i].toLowerCase()+" ";
			String repl = " "+allcommands[i]+" ";
			script = script.replaceAll(find,repl);
		}
		
		script = script.replaceAll(" true ", " TRUE ");
		script = script.replaceAll(" false ", " FALSE ");
		
		//@Globals
		script = script.replaceAll(" @blknum "	, " @BLKNUM ");
		script = script.replaceAll(" @input "	, " @INPUT ");
		script = script.replaceAll(" @address "	, " @ADDRESS ");
		script = script.replaceAll(" @amount "	, " @AMOUNT "); 
		script = script.replaceAll(" @tokenid "	, " @TOKENID "); 
		script = script.replaceAll(" @coinid "	, " @COINID "); 
		script = script.replaceAll(" @script "	, " @SCRIPT "); 
		script = script.replaceAll(" @totin "	, " @TOTIN "); 
		script = script.replaceAll(" @totout "	, " @TOTOUT ");
		script = script.replaceAll(" @inblknum ", " @INBLKNUM ");
		script = script.replaceAll(" @blkdiff ", " @BLKDIFF ");
		
		//And now do all the functions
		for(MinimaFunction func : MinimaFunction.ALL_FUNCTIONS) {
			//Name
			String name = func.getName();
			
			//replace
			script = script.replaceAll(" "+name.toLowerCase()+" ", " "+name+" ");
		}
			
		//Convert the HEX to upper case..
		String finalstring = "";
		StringTokenizer strtok = new StringTokenizer(script," ");
		while(strtok.hasMoreTokens()) {
			String tok = strtok.nextToken();
			if(tok.startsWith("0x")) {
				finalstring = finalstring.concat(" 0x"+tok.substring(2).toUpperCase());
			}else {
				finalstring = finalstring.concat(" "+tok);
			}
		}
		
		//Remove all the excess white space
		script = script.replaceAll("\\s+"," ").trim();
				
		//Boom..
		return finalstring.trim();
	}
	
	public static void main(String[] zArgs) {
		
//		String RamScript = 
//				  "let y=4 "
//				+ "IF y EQ 0 THEN "
//				+ "  let x =1 "
//				+ "ELSEif y EQ 3 THEN "
//				+ "  let x=2 "
//				+ "ELSE"				
//				+ "  let x =3 "
//				+ "ENDIF let p=0";
		
//		String RamScript = 
//				"let he = 0xeeff let sc = [hello you] let bool = true let x=0 let y=0 while x LT 2 do let y=y+1 let x = x+1 endwhile ";
		
//		String RamScript = "return VERifyoutput( 0 0xffeeff00ff11 10 0x00)";
//		String RamScript = "return VERifyoutput( 0 0xffeeff00ff11 10 0x00)";
//		String RamScript = "return true or 1 lt 2 and true";
		
//		String RamScript = "LET x=1 while x Lt 10 THEN LET x = x + 1 ENDWHILE LET y =x";
//		String RamScript = "if 1 EQ 1 THEN let y = 3 endif";
//		String RamScript = "let x = true or false let y = [return x] Exec y";
		
//		String RamScript = "ASSERT VERIFYOUT ( ( @INPUT + 1 ) @ADDRESS ( @AMOUNT - amt ) @TOKENID )";
		String RamScript = "let t = 1 LET ( t (3 - t*2) t ) = 123 let gg = get ( 1 1 t )";

		//String RamScript = "let t = @SCRIPT let f = @AMOUNT +1 let g = State(1001) + [ sha3(123)]";

//		String RamScript = "let gg = [hello] let ff = 0x45678 let t = CONCAT ( gg [if signedby] SCRIPT(ff) [and @blknum gt 12345])";
		
		Transaction tt = new Transaction();
//		tt.setStateValue(1001, new StateVariable("[ let y = 0xFF ]"));
//		tt.setStateValue(2, new StateVariable("1.2345"));
		
		Contract ctr = new Contract(RamScript,"0x74A2222436C592046A6F576F67200C75DB3D9051BE31262BD0A0BF0DB30137C4",tt,null,true);
		
		ctr.setGlobalVariable("@SCRIPT", new ScriptValue(RamScript));
		ctr.setGlobalVariable("@BLKNUM", new NumberValue(new MiniNumber("31")));
		ctr.setGlobalVariable("@INBLKNUM", new NumberValue(new MiniNumber("10")));
		ctr.setGlobalVariable("@INPUT", new NumberValue(new MiniNumber("1")));
		ctr.setGlobalVariable("@ADDRESS", new HEXValue("0x67876AB"));
		ctr.setGlobalVariable("@TOKENID", new HEXValue("0x00"));
		ctr.setGlobalVariable("@AMOUNT", new NumberValue(new MiniNumber("1")));
		
		ctr.run();
	}
	
}
