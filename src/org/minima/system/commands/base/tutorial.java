package org.minima.system.commands.base;

import org.minima.system.commands.Command;
import org.minima.utils.json.JSONObject;

public class tutorial extends Command {

	public tutorial() {
		super("tutorial","Show the complete Grammar for Minima KISSVM scripting");
	}
	
	@Override
	public JSONObject runCommand() throws Exception{
		JSONObject ret = getJSONReply();

		String tutorial = "\n"+
				    "ADDRESS     ::= ADDRESS ( BLOCK )\n"
				  + "BLOCK       ::= STATEMENT_1 STATEMENT_2 ... STATEMENT_n\n"
				  + "STATEMENT   ::= LET VARIABLE = EXPRESSION |\n"
				  + "                LET ( EXPRESSION_1 EXPRESSION_2 ... EXPRESSION_n ) = EXPRESSION |\n"
				  + "                IF EXPRESSION THEN BLOCK [ELSEIF EXPRESSION THEN BLOCK]* [ELSE\n"
				  + "                BLOCK] ENDIF |\n"
				  + "                WHILE EXPRESSION DO BLOCK ENDWHILE |\n"
				  + "                EXEC EXPRESSION |\n"
				  + "                MAST EXPRESSION |\n"
				  + "                ASSERT EXPRESSION |\n"
				  + "                RETURN EXPRESSION\n"
				  + "EXPRESSION  ::= RELATION\n"
				  + "RELATION    ::= LOGIC AND LOGIC  | LOGIC OR LOGIC  |\n"
				  + "                LOGIC XOR LOGIC  | LOGIC NAND LOGIC |\n"
				  + "                LOGIC NOR LOGIC  | LOGIC NXOR LOGIC | LOGIC\n"
				  + "LOGIC       ::= OPERATION EQ OPERATION  | OPERATION NEQ OPERATION  |\n"
				  + "                OPERATION GT OPERATION  | OPERATION GTE OPERATION  |\n"
				  + "                OPERATION LT OPERATION  | OPERATION LTE OPERATION  | OPERATION\n"
				  + "OPERATION   ::= ADDSUB & ADDSUB | ADDSUB | ADDSUB | ADDSUB ^ ADDSUB | ADDSUB\n"
				  + "ADDSUB      ::= MULDIV + MULDIV | MULDIV - MULDIV | MULDIV % MULDIV |\n"
				  + "                MULDIV << MULDIV | MULDIV >> MULDIV | MULDIV\n"
				  + "MULDIV      ::= PRIME * PRIME | PRIME / PRIME | PRIME\n"
				  + "PRIME       ::= NOT PRIME |  NEG PRIME | NOT BASEUNIT | NEG BASEUNIT | BASEUNIT\n"
				  + "BASEUNIT    ::= VARIABLE | VALUE | -NUMBER | GLOBAL | FUNCTION | ( EXPRESSION )\n"
				  + "VARIABLE    ::= [a-z]+\n"
				  + "VALUE       ::= NUMBER | HEX | STRING | BOOLEAN\n"
				  + "NUMBER      ::= ^[0-9]+(\\\\\\\\.[0-9]+)?\n"
				  + "HEX         ::= 0x[0-9a-fA-F]+\n"
				  + "STRING      ::= [UTF8_String]\n"
				  + "BOOLEAN     ::= TRUE | FALSE\n"
				  + "FALSE       ::= 0\n"
				  + "TRUE        ::= NOT FALSE\n"
				  + "GLOBAL      ::= @BLOCK | @BLOCKTIME | @INBLOCK | @BLOCKDIFF | @INPUT |\n"
				  + "                @AMOUNT | @ADDRESS | @TOKENID | @COINID |\n"
				  + "                @SCRIPT | @TOTIN | @TOTOUT\n"
				  + "FUNCTION    ::= FUNC ( EXPRESSION_1 EXPRESSION_2 .. EXPRESSION_n )\n"
				  + "FUNC        ::= CONCAT | LEN | REV | SUBSET | GET | OVERWRITE |\n"
				  + "                CLEAN | UTF8 | REPLACE | SUBSTR |\n"
				  + "                BOOL | HEX | NUMBER | STRING | ADDRESS |\n"
				  + "                ABS | CEIL | FLOOR | MIN | MAX | INC | DEC | SIGDIG | POW |\n"
				  + "                BITSET | BITGET | BITCOUNT | PROOF | KECCAK | SHA2 | SHA3 |\n"
				  + "                SIGNEDBY | MULTISIG | CHECKSIG |\n"
				  + "                FUNCTION | SUMINPUT | SUMOUTPUT |\n"
				  + "                GETOUTADDR | GETOUTAMT | GETOUTTOK | VERIFYOUT |\n"
				  + "                GETINADDR | GETINAMT | GETINTOK | GETINID | VERIFYIN |\n"
				  + "                STATE | PREVSTATE | SAMESTATE\n"
				  + "\n"
				  + "Globals\n"
				  + "\n"
				  + "@BLOCK       : Block number this transaction is in\n"
				  + "@BLOCKTIME   : Block time in milliseconds since Jan 1 1970\n"
				  + "@INBLOCK     : Block number when this output was created\n"
				  + "@BLOCKDIFF   : Difference between @BLOCK and INBLOCK\n"
				  + "@INPUT       : Input number in the transaction\n"
				  + "@COINID      : CoinID of this input\n"
				  + "@AMOUNT      : Amount of this input\n"
				  + "@ADDRESS     : Address of this input\n"
				  + "@TOKENID     : TokenID of this input\n"
				  + "@SCRIPT      : Script for this input\n"
				  + "@TOTIN       : Total number of inputs for this transaction\n"
				  + "@TOTOUT      : Total number of outputs for this transaction\n"
				  + "\n"
				  + "Functions\n"
				  + "\n"
				  + "CONCAT ( HEX_1 HEX_2 ... HEX_n )\n"
				  + "Concatenate the HEX values.\n"
				  + "\n"
				  + "LEN ( HEX|SCRIPT )\n"
				  + "Length of the data\n"
				  + "\n"
				  + "REV ( HEX )\n"
				  + "Reverse the data\n"
				  + "\n"
				  + "SUBSET ( HEX NUMBER NUMBER )\n"
				  + "Return the HEX subset of the data - start - length\n"
				  + "\n"
				  + "OVERWRITE ( HEX NUMBER HEX NUMBER NUMBER)\n"
				  + "Copy bytes from the first HEX and pos to the second HEX and pos, length the last NUMBER\n"
				  + "\n"
				  + "GET ( VALUE1 VALUE2 .. VALUEn )\n"
				  + "Return the array value set with LET ( EXPRESSION EXPRESSION .. EXPRESSION )\n"
				  + "\n"
				  + "ADDRESS ( STRING )\n"
				  + "Return the address of the script\n"
				  + "\n"
				  + "REPLACE ( STRING STRING STRING )\n"
				  + "Replace in 1st string all occurrence of 2nd string with 3rd\n"
				  + "\n"
				  + "SUBSTR ( STRING NUMBER NUMBER )\n"
				  + "Get the substring                                                                                                                                                                             \n"
				  + "\n"
				  + "CLEAN ( STRING )\n"
				  + "Return a CLEAN version of the script\n"
				  + "\n"
				  + "UTF8 ( HEX )\n"
				  + "Convert the HEX value of a script value to a string\n"
				  + "\n"
				  + "BOOL ( VALUE )\n"
				  + "Convert to TRUE or FALSE value\n"
				  + "\n"
				  + "HEX ( SCRIPT )\n"
				  + "Convert SCRIPT to HEX\n"
				  + "\n"
				  + "NUMBER ( HEX )\n"
				  + "Convert HEX to NUMBER\n"
				  + "\n"
				  + "STRING ( HEX )\n"
				  + "Convert a HEX value to SCRIPT\n"
				  + "\n"
				  + "ABS ( NUMBER )\n"
				  + "Return the absolute value of a number\n"
				  + "\n"
				  + "CEIL ( NUMBER )\n"
				  + "Return the number rounded up\n"
				  + "\n"
				  + "FLOOR ( NUMBER )\n"
				  + "Return the number rounded down\n"
				  + "\n"
				  + "MIN ( NUMBER NUMBER )\n"
				  + "Return the minimum value of the 2 numbers\n"
				  + "\n"
				  + "MAX ( NUMBER NUMBER )\n"
				  + "Return the maximum value of the 2 numbers\n"
				  + "\n"
				  + "INC ( NUMBER )\n"
				  + "Increment a number\n"
				  + "\n"
				  + "DEC ( NUMBER )\n"
				  + "Decrement a number\n"
				  + "\n"
				  + "POW ( NUMBER NUMBER )\n"
				  + "Returns the power of N of a number. N must be a whole number\n"
				  + "\n"
				  + "SIGDIG ( NUMBER NUMBER )\n"
				  + "Set the significant digits of the number\n"
				  + "\n"
				  + "BITSET ( HEX NUMBER BOOLEAN )\n"
				  + "Set the value of the BIT at that Position to 0 or 1\n"
				  + "\n"
				  + "BITGET ( HEX NUMBER )\n"
				  + "Get the BOOLEAN value of the bit at the position\n"
				  + "\n"
				  + "BITCOUNT ( HEX )\n"
				  + "Count the number of bits set in a HEX value\n"
				  + "\n"
				  + "PROOF ( HEX HEX HEX )\n"
				  + "Check the data, mmr proof, and root match. Same as mmrproof on Minima\n"
				  + "\n"
				  + "KECCAK ( HEX|STRING )\n"
				  + "Returns the KECCAK value of the HEX value\n"
				  + "\n"
				  + "SHA2 ( HEX|STRING )\n"
				  + "Returns the SHA2 value of the HEX value\n"
				  + "\n"
				  + "SHA3 ( HEX|STRING )\n"
				  + "Returns the SHA3 value of the HEX value\n"
				  + "\n"
				  + "SIGNEDBY ( HEX )\n"
				  + "Returns true if the transaction is signed by this public key\n"
				  + "\n"
				  + "MULTISIG ( NUMBER HEX1 HEX2 .. HEXn )\n"
				  + "Returns true if the transaction is signed by N of the public keys\n"
				  + "\n"
				  + "CHECKSIG ( HEX HEX HEX)\n"
				  + "Check public key, data and signature\n"
				  + "\n"
				  + "GETOUTADDR ( NUMBER )\n"
				  + "Return the HEX address of the specified output\n"
				  + "\n"
				  + "GETOUTAMT ( NUMBER )\n"
				  + "Return the amount of the specified output\n"
				  + "\n"
				  + "GETOUTTOK ( NUMBER )Return the token id of the specified output\n"
				  + "\n"
				  + "VERIFYOUT ( NUMBER HEX NUMBER HEX )\n"
				  + "Verify the output has the specified  address, amount and tokenid\n"
				  + "\n"
				  + "GETINADDR ( NUMBER )\n"
				  + "Return the HEX address of the specified input\n"
				  + "\n"
				  + "GETINAMT ( NUMBER )\n"
				  + "Return the amount of the specified input\n"
				  + "\n"
				  + "GETINTOK ( NUMBER )\n"
				  + "Return the token id of the specified input\n"
				  + "\n"
				  + "VERIFYIN ( NUMBER HEX NUMBER HEX )\n"
				  + "Verify the input has the specified address, amount and tokenid\n"
				  + "\n"
				  + "SUMINPUTS ( HEX )\n"
				  + "Sum the input values of this token type\n"
				  + "\n"
				  + "SUMOUTPUTS ( HEX )\n"
				  + "Sum the output values of this token type\n"
				  + "\n"
				  + "STATE ( NUMBER )\n"
				  + "Return the state value for the given number\n"
				  + "\n"
				  + "PREVSTATE ( NUMBER )\n"
				  + "Return the state value stored in the coin MMR data - when the coin was created\n"
				  + "\n"
				  + "SAMESTATE ( NUMBER NUMBER )\n"
				  + "Return TRUE if the previous state and current state are the same for the start and end positions\n"
				  + "\n"
				  + "FUNCTION ( STRING VALUE1 VALUE2.. VALUEn )\n"
				  + "Run the script after replace the $1, $2.. $n variables with the provided parameters and returnvalue has the result\n"
				  + "";
		
		//Add balance..
		ret.put("response", tutorial);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new tutorial();
	}

}
