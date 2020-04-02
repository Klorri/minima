package org.minima.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.minima.objects.base.MiniData;
import org.minima.utils.Streamable;
import org.minima.utils.digest.Digest;
import org.minima.utils.digest.KeccakDigest;
import org.minima.utils.digest.WinternitzOTSVerify;
import org.minima.utils.digest.WinternitzOTSignature;

public class PubPrivKey implements Streamable {
	
	private static final int WINTERNITZ_NUMBER = 12;
	
	/**
	 * Key details
	 */
	MiniData mPrivateSeed;
	MiniData mPublicKey;
	
	private static Digest getHashFunction() {
		return getHashFunction(256);
	}
	
	private static Digest getHashFunction(int zBitLength) {
		return new KeccakDigest(zBitLength);
	}
	
	public PubPrivKey() {
		this(MiniData.getRandomData(32));
	}
	
	public PubPrivKey(MiniData zPrivateSeed) {
		//Create a random seed
		mPrivateSeed = zPrivateSeed;

		//Create a WOTS
		WinternitzOTSignature wots = new WinternitzOTSignature(mPrivateSeed.getData(), getHashFunction(), WINTERNITZ_NUMBER);
		
		//Get the Public Key..
		mPublicKey  = new MiniData(wots.getPublicKey());
	}
	
	/**
	 * For reading from stream
	 * @param empty
	 */
	public PubPrivKey(boolean empty) {}
	
	public MiniData sign(MiniData zData) {
		//Create a WOTS
		WinternitzOTSignature wots = new WinternitzOTSignature(mPrivateSeed.getData(), getHashFunction(), WINTERNITZ_NUMBER);
		
		//Sign the data..
		byte[] signature = wots.getSignature(zData.getData());
		
		//Return 
		return new MiniData(signature);
	}
	
	public boolean verify(MiniData zData, MiniData zSignature) {
		return verify(mPublicKey, zData, zSignature);
	}
	
	public static boolean verify(MiniData zPubKey, MiniData zData, MiniData zSignature) {
		//WOTS Verify
		WinternitzOTSVerify wver = new WinternitzOTSVerify(getHashFunction(), WINTERNITZ_NUMBER);
		
		//Do it.. get the pubkey..
		byte[] pubkey = wver.Verify(zData.getData(), zSignature.getData());
		
		//Check it
		MiniData resp = new MiniData(pubkey);
		
		//Check..
		return resp.isEqual(zPubKey);
	}
	
	public MiniData getPublicKey() {
		return mPublicKey;
	}
	
	public MiniData getPrivateSeed() {
		return mPrivateSeed;
	}
	
	@Override
	public String toString() {
		return mPublicKey.to0xString();
	}

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mPublicKey.writeDataStream(zOut);
		mPrivateSeed.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mPublicKey   = MiniData.ReadFromStream(zIn);
		mPrivateSeed = MiniData.ReadFromStream(zIn);
	}
}
