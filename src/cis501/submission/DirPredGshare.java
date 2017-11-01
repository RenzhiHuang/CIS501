package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;
import java.lang.Math;

public class DirPredGshare extends DirPredBimodal {
	public int historyBits;
	public long HisReg = 0;

	public DirPredGshare(int indexBits, int historyBits) {
		super(indexBits);
		this.historyBits = historyBits;
	}

	@Override
	public Direction predict(long pc) {
		Direction GsharePre;
		GsharePre = super.predict(pc ^ HisReg);
		return GsharePre;
	}

	@Override
	public void train(long pc, Direction actual) {

		super.train(pc ^ HisReg, actual);
		if (actual != null) {

			// left shift HisReg
			HisReg = HisReg << 1;

			// setting the lowest bit
			if (actual == Direction.Taken) {
				HisReg |= 1;
			}
			// clear the 'historyBits'th bit
			HisReg &= ~(1 << historyBits);
		}
	}
}
