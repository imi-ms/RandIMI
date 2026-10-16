package de.unimuenster.imi.randimi.controller.helper;

import java.security.SecureRandom;
import java.util.Random;

/**
 * A class with several helper methods.
 * 
 * @author Tobias Hardt <tobiashardt@uni-muenster.de>
 */
public class RandimiHelper {
	
	private static final SecureRandom SEED_GENERATOR = new SecureRandom();

	/**
	 * Generates a new seed for the randomization of a site.
	 * <p>
	 * The seed is drawn from a secure random generator, so seeds cannot be predicted and sites created at the same
	 * time get different seeds.
	 *
	 * @return A new seed.
	 */
	public static long generateSeed() {
		return SEED_GENERATOR.nextLong();
	}

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
