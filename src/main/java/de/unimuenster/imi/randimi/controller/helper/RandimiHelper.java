package de.unimuenster.imi.randimi.controller.helper;

import java.util.Random;

/**
 * A class with several helper methods.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class RandimiHelper {
	
	/**
	 * Returns a random object with the given seed and a given number of calls
	 * already done.
	 * @param seed Seed of the random object
	 * @param calls Number of calls, that have been done
	 * 
	 * @return A random object with the given seed and a given number of calls
	 * already done.
	 */
	public static Random getRandom(long seed, int calls) {
		Random random = new Random(seed);
		for (int i = 0; i < calls; i++) {
			random.nextInt();
		}
		return random;
	}
}
