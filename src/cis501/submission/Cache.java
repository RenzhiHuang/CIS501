package cis501.submission;

import cis501.ICache;
import java.lang.Math;

public class Cache implements ICache {
	public int[][] cache;
	public int[][] LRU;
	public int[][] dirty;
	public int indexBits;
	public int blockOffsetBits;
	public final int ways;
	public int accessLatency;
	public int cleanMissLatency;
	public int dirtyMissLatency;

	public Cache(int indexBits, int ways, int blockOffsetBits, final int accessLatency, final int cleanMissLatency,
			final int dirtyMissLatency) {
		assert indexBits >= 0;
		assert ways > 0;
		assert blockOffsetBits >= 0;
		assert indexBits + blockOffsetBits < 32;
		assert accessLatency >= 0;
		assert cleanMissLatency >= 0;
		assert dirtyMissLatency >= 0;
		int dimensionsize = (int) Math.pow(2, indexBits);
		this.cache = new int[dimensionsize][ways];
		for (int i = 0; i < dimensionsize; i++) {
			for (int j = 0; j < ways; j++) {
				cache[i][j] = -2;
			}
		}
		this.LRU = new int[dimensionsize][ways];
		this.dirty = new int[dimensionsize][ways];
		this.indexBits = indexBits;
		this.blockOffsetBits = blockOffsetBits;
		this.ways = ways;
		this.accessLatency = accessLatency;
		this.cleanMissLatency = cleanMissLatency;
		this.dirtyMissLatency = dirtyMissLatency;
	}

	@Override
	public int access(boolean load, long address) {
		int latency = 0;
		int LRUblock = 0;

		// get the index and tag of the address
		int getIndex = (int) address >> blockOffsetBits;
		int index = (int) (Math.pow(2, indexBits) - 1) & getIndex;
		int tag = (int) address >> (indexBits + blockOffsetBits);
		// System.out.println(index);

		// hit or miss
		boolean hit = false;
		int blocknum = -1;
		for (int k = 0; k < ways; k++) {
			if (cache[index][k] == tag) {
				hit = true;
				blocknum = k;
			}
		}
		// if load
		if (load == true) {

			// if hit, return the latency, update the LRU
			if (hit == true) {
				latency = accessLatency;

				for (int l = 0; l < ways; l++) {
					LRU[index][l] = LRU[index][l] + 1;
				}
				LRU[index][blocknum] = 0;
			}
			// if not hit, return the latency, update the cache and the LRU
			else {
				// find the LRUblock
				int temp = LRU[index][0];
				for (int p = 1; p < ways; p++) {
					if (LRU[index][p] > temp) {
						temp = LRU[index][p];
						LRUblock = p;
					}
				}

				// if dirty miss,return the latency,update the cache and the LRU
				if (dirty[index][LRUblock] == 1) {
					latency = dirtyMissLatency;
					cache[index][LRUblock] = tag;
					for (int l = 0; l < ways; l++) {
						LRU[index][l] = LRU[index][l] + 1;
					}
					LRU[index][LRUblock] = 0;
					dirty[index][LRUblock] = 0;
				}
				// if clean miss,return the latency,update the cache and the LRU
				else {
					latency = cleanMissLatency;
					cache[index][LRUblock] = tag;
					for (int l = 0; l < ways; l++) {
						LRU[index][l] = LRU[index][l] + 1;
					}
					LRU[index][LRUblock] = 0;
				}
			}
		}
		// if store
		else {
			// if hit, return the latency,update the LRU and mark the block as dirty
			if (hit == true) {
				latency = accessLatency;
				for (int l = 0; l < ways; l++) {
					LRU[index][l] = LRU[index][l] + 1;
				}
				LRU[index][blocknum] = 0;
				dirty[index][blocknum] = 1;
			}
			// if miss
			else {
				// find the LRU block
				int temp = LRU[index][0];
				for (int p = 1; p < ways; p++) {
					if (LRU[index][p] > temp) {
						temp = LRU[index][p];
						LRUblock = p;
					}
				}
				// if dirty miss,return the latency,update the cache and LRU, this block remains
				// dirty
				if (dirty[index][LRUblock] == 1) {
					latency = dirtyMissLatency;
					cache[index][LRUblock] = tag;
					for (int l = 0; l < ways; l++) {
						LRU[index][l] = LRU[index][l] + 1;
					}
					LRU[index][LRUblock] = 0;
				}
				// if clean miss,return the latency,update the cache and LRU, set this block to
				// dirty
				else {
					latency = cleanMissLatency;
					cache[index][LRUblock] = tag;
					for (int l = 0; l < ways; l++) {
						LRU[index][l] = LRU[index][l] + 1;
					}
					LRU[index][LRUblock] = 0;
					dirty[index][LRUblock] = 1;
				}
			}
		}
		return latency;
	}

	//@Override
	/*public void delete(long address) {

		// get the index and tag of the address
		int getIndex = (int) address >> blockOffsetBits;
		int index = (int) (Math.pow(2, indexBits) - 1) & getIndex;
		int tag = (int) address >> (indexBits + blockOffsetBits);

		for (int l = 0; l < ways; l++) {
			if (cache[index][l] == tag) {
				cache[index][l] = -2;
				int temp = LRU[index][0];
				for (int p = 1; p < ways; p++) {
					if (LRU[index][p] > temp) {
						temp = LRU[index][p];
					}
				}
				LRU[index][l] = temp+1;
			}
		}
		

	}*/

}
