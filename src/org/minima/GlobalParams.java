package org.minima;

import org.minima.objects.base.MiniNumber;

public class GlobalParams {

	/**
	 * Number of seconds before sending a pulse message
	 */
	public static final int USER_PULSE_FREQ      = 10 * 60;
	
	/**
	 * Just create a block every transaction. Useful when not mining 
	 * and just want a block every single transaction to debug.
	 * Automatically disables the auto mining
	 */
	public static final boolean MINIMA_ZERO_DIFF_BLK  = false;
	
	/**
	 * Speed in blocks per second
	 */
	public static final MiniNumber MINIMA_BLOCK_SPEED  = new MiniNumber("1");
	
	/**
	 * How deep before we think confirmed..
	 */
	public static final MiniNumber MINIMA_CONFIRM_DEPTH  = new MiniNumber("1");
	
	/**
	 * Depth before we cascade..
	 */
	public static final int MINIMA_CASCADE_DEPTH   = 16;
	
	/**
	 * Minimum number of blocks at each cascade level 
	 */
	public static final int MINIMA_MINUMUM_CASCADE_LEVEL  = 2;
	
	/**
	 * How Many Cascade Levels are there
	 */
	public static final int MINIMA_CASCADE_LEVELS  = 21;
	
}
