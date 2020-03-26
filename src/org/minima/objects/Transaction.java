package org.minima.objects;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.minima.objects.base.MiniByte;
import org.minima.objects.base.MiniHash;
import org.minima.objects.base.MiniNumber;
import org.minima.objects.base.MiniString;
import org.minima.objects.proofs.ScriptProof;
import org.minima.objects.proofs.TokenProof;
import org.minima.utils.Streamable;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;

/**
 * A transaction is a very simple structure. A list of inputs and a list of outputs. 
 * All the Witness data is Segregated..  So that there is no TXN malleability possible.
 * The CoinID of an output is the HASH ( TXN hash + Output Num ), which is ALWAYS Globally Unique.
 * 
 * @author spartacus
 *
 */
public class Transaction implements Streamable {

	/**
	 * The Inputs that make up the Transaction
	 */
	ArrayList<Coin> mInputs  = new ArrayList<>();
	
	/**
	 * All the Outputs
	 */
	ArrayList<Coin> mOutputs = new ArrayList<>();
	
	/**
	 * The State values of the Transaction
	 */
	ArrayList<StateVariable> mState = new ArrayList<>();
	
	/**
	 * The Scripts used in the transactions 
	 * 
	 * Addresses
	 * Tokens
	 * MAST
	 */
	ArrayList<ScriptProof> mScripts = new ArrayList<>();
	
	/**
	 * If you are generating a TOKEN.. here are the details..
	 * Needs to be here instead of witness so noone can alter it - you sign this.
	 */
	TokenProof mTokenGenDetails = null;
	
	
	/**
	 * Constructor
	 */
	public Transaction() {}
	
	public void addInput(Coin zCoin) {
		mInputs.add(zCoin);
	}
	
	public boolean isEmpty() {
		return mInputs.size() == 0 && mOutputs.size() == 0;
	}
	
	public void addOutput(Coin zCoin) {
		mOutputs.add(zCoin);
	}
	
	public ArrayList<Coin> getAllInputs(){
		return mInputs;
	}
	
	public ArrayList<Coin> getAllOutputs(){
		return mOutputs;
	}
	
	public MiniNumber sumInputs() {
		MiniNumber tot = new MiniNumber();
		for(Coin cc : mInputs) {
			tot = tot.add(cc.mAmount);
		}
		return tot;
	}
	
	public MiniNumber sumOutputs() {
		MiniNumber tot = new MiniNumber();
		for(Coin cc : mOutputs) {
			tot = tot.add(cc.mAmount);
		}
		return tot;
	}

	/**
	 * Set a state value from 0-255 to a certain value
	 * @param zStateNum
	 * @param zValue
	 */
	public void addStateVariable(StateVariable zValue) {
		//If it exists overwrite it..
		StateVariable sv = getStateValue(zValue.getPort());
		if(sv != null) {
			sv.resetData(zValue.getData());
		}else {
			mState.add(zValue);
		}
	}
	
	/**
	 * @param zStateNum
	 * @return
	 */
	public StateVariable getStateValue(int zStateNum) {
		for(StateVariable sv : mState) {
			if(sv.getPort() == zStateNum){
				return sv;
			}
		}
		
		return null;
	}
	
	/**
	 * Check exists
	 * @param zStateNum
	 * @return
	 */
	public boolean stateExists(int zStateNum) {
		for(StateVariable sv : mState) {
			if(sv.getPort() == zStateNum){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Clear all the state values
	 */
	public void clearState() {
		mState.clear();
	}
	
	/**
	 * Required to cycle..
	 * @return
	 */
	public ArrayList<StateVariable> getCompleteState(){
		return mState;
	}
	
	/**
	 * All the scripts
	 */
	public boolean addScript(ScriptProof zScriptProof) {
		if(!scriptExists(zScriptProof.getFinalHash())) {
			mScripts.add(zScriptProof);		
			return true;
		}
		return false;
	}
	
	public boolean addScript(String zScript) {
		return addScript(new ScriptProof(zScript));
	}
	
	public ScriptProof getScript(MiniHash zHash) {
		for(ScriptProof proof : mScripts) {
			if(proof.getFinalHash().isExactlyEqual(zHash)) {
				return proof;
			}
		}
		return null;
	}
	
	public boolean scriptExists(MiniHash zHash) {
		return getScript(zHash)!=null;
	}
	
	public void setTokenGenerationDetails(TokenProof zTokenDetails) {
		mTokenGenDetails = zTokenDetails;
	}
	
	public TokenProof getTokenGenerationDetails() {
		return mTokenGenDetails;
	}
	
	@Override
	public String toString() {
		return toJSON().toString();
	}
	
	public JSONObject toJSON() {
		JSONObject ret = new JSONObject();
		
		//Inputs
		JSONArray ins = new JSONArray();
		for(Coin in : mInputs) {
			ins.add(in.toJSON());
		}
		ret.put("inputs", ins);
		
		//Outputs
		JSONArray outs = new JSONArray();
		for(Coin out : mOutputs) {
			outs.add(out.toJSON());
		}
		ret.put("outputs", outs);
		
		//State
		outs = new JSONArray();
		for(StateVariable sv : mState) {
			outs.add(sv.toJSON());
		}
		ret.put("state", outs);
		
		//Script Proofs..
		outs = new JSONArray();
		for(ScriptProof proof : mScripts) {
			outs.add(proof.toJSON());	
		}
		ret.put("scripts", outs);
		
		//Token Generation..
		if(mTokenGenDetails != null) {
			ret.put("tokengen", mTokenGenDetails.toJSON());
		}
				
		return ret;
	}

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		//Max 255 inputs or outputs
		MiniByte ins = new MiniByte(mInputs.size());
		ins.writeDataStream(zOut);
		for(Coin coin : mInputs) {
			coin.writeDataStream(zOut);
		}
		
		//Max 255 inputs or outputs
		MiniByte outs = new MiniByte(mOutputs.size());
		outs.writeDataStream(zOut);
		for(Coin coin : mOutputs) {
			coin.writeDataStream(zOut);
		}
		
		//How many state variables..
		int len = mState.size();
		zOut.writeInt(len);
		for(StateVariable sv : mState) {
			sv.writeDataStream(zOut);
		}
		
		//Now the Scripts
		len = mScripts.size();
		zOut.writeInt(len);
		for(ScriptProof script : mScripts) {
			script.writeDataStream(zOut);
		}
		
		//Token generation
		if(mTokenGenDetails == null) {
			MiniByte.FALSE.writeDataStream(zOut);
		}else {
			MiniByte.TRUE.writeDataStream(zOut);
			mTokenGenDetails.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mInputs  = new ArrayList<>();
		mOutputs = new ArrayList<>();
		mState 	 = new  ArrayList<>();
		mScripts = new ArrayList<>();
		
		//Inputs
		MiniByte ins = new MiniByte();
		ins.readDataStream(zIn);
		
		int len = ins.getValue();
		for(int i=0;i<len;i++) {
			Coin coin = Coin.ReadFromStream(zIn);
			mInputs.add(coin);
		}
		
		//Outputs
		MiniByte outs = new MiniByte();
		outs.readDataStream(zIn);
		
		len = outs.getValue();
		for(int i=0;i<len;i++) {
			Coin coin = Coin.ReadFromStream(zIn);
			mOutputs.add(coin);
		}
		
		//State Variables
		len = zIn.readInt();
		for(int i=0;i<len;i++){
			StateVariable sv = StateVariable.ReadFromStream(zIn);
			mState.add(sv);
		}
		
		//Scripts
		len = zIn.readInt();
		for(int i=0;i<len;i++){
			ScriptProof sp = ScriptProof.ReadFromStream(zIn);
			mScripts.add(sp);
		}
		
		//Token generation
		MiniByte tokgen = MiniByte.ReadFromStream(zIn);
		if(tokgen.isTrue()) {
			mTokenGenDetails = TokenProof.ReadFromStream(zIn);
		}else {
			mTokenGenDetails = null;
		}
	}
	
	/**
	 * Get a DEEP copy of this transaction
	 */
	public Transaction deepCopy() {
		try {
			//First write transaction out to a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			writeDataStream(dos);
			dos.flush();
			dos.close();
			
			//Now read it into a new transaction..
			byte[] transbytes = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(transbytes);
			DataInputStream dis = new DataInputStream(bais);
			
			Transaction deepcopy = new Transaction();
			deepcopy.readDataStream(dis);
			
			dis.close();
			
			return deepcopy;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
