package cis501.submission;

import cis501.*;

import java.util.Set;
import java.util.Iterator;

enum Stage {
	FETCH(0), DECODE(1), EXECUTE(2), MEMORY(3), WRITEBACK(4);

	private static Stage[] vals = values();
	private final int index;

	Stage(int idx) {
		this.index = idx;
	}

	/** Returns the index of this stage within the pipeline */
	public int i() {
		return index;
	}

	/** Returns the next stage in the pipeline, e.g., next after Fetch is Decode */
	public Stage next() {
		return vals[(this.ordinal() + 1) % vals.length];
	}
}

public class InorderPipeline implements IInorderPipeline {
	public long InsnCount = 0;
	public long CycleCount = 0;
	public int additionalMemLatency;
	public Set<Bypass> bypasses;
	public BranchPredictor bp;

	private static Insn makeInsn(int dst, int src1, int src2, MemoryOp mop) {
		return new Insn(dst, src1, src2, 1, 4, null, 0, null, mop, 1, 1, "synthetic");
	}

	/**
	 * Create a new pipeline with the given additional memory latency.
	 *
	 * @param additionalMemLatency
	 *            The number of extra cycles mem insns require in the M stage. If 0,
	 *            mem insns require just 1 cycle in the M stage, like all other
	 *            insns. If x, mem insns require 1+x cycles in the M stage.
	 * @param bypasses
	 *            Which bypasses should be modeled. For example, if this is an empty
	 *            set, then your pipeline should model no bypassing, using stalling
	 *            to resolve all data hazards.
	 */
	public InorderPipeline(int additionalMemLatency, Set<Bypass> bypasses) {
	}

	/**
	 * Create a new pipeline with the additional memory latency and branch
	 * predictor. The pipeline should model full bypassing (MX, Wx, WM).
	 *
	 * @param additionalMemLatency
	 *            see InorderPipeline(int, Set<Bypass>)
	 * @param bp
	 *            the branch predictor to use
	 */
	public InorderPipeline(int additionalMemLatency, BranchPredictor bp) {
		this.additionalMemLatency = additionalMemLatency;
		this.bp = bp;
	}

	@Override
	public String[] groupMembers() {
		return new String[] { "Renzhi Huang", "Yuanlong Xiao" };
	}

	@Override
	public void run(InsnIterator iter) {
		int FlushTrue = 0;
		int TrainTrue = 0;

		// initialize OnStageInsn
		Insn[] OnStageInsn = new Insn[5];
		for (int i = 0; i < 5; i++) {
			OnStageInsn[i] = makeInsn(-2, -2, -2, null);
		}

		Insn nop = makeInsn(-2, -2, -2, null);
		CycleCount = 0;
		int memtrue = 0;

		// declare an array to store the predicted target
		long[] PreTar = new long[] { 0, 0, 0 };

		while (true) {

			CycleCount++;
			// get the predicted target for the instruction on Fetch
			if (OnStageInsn[0].dstReg != -2) {
				PreTar[0] = bp.predict(OnStageInsn[0].pc, OnStageInsn[0].fallthroughPC());
			} else {
				PreTar[0] = 0;
			}

			// additional latency for mem instructions
			if (((OnStageInsn[3].mem == MemoryOp.Load) || (OnStageInsn[3].mem == MemoryOp.Store))
					&& memtrue != additionalMemLatency) {
				memtrue++;
				// if X is a nop
				if (OnStageInsn[2].dstReg == -2) {

					// if X&D are nops
					if (OnStageInsn[1].dstReg == -2) {
						OnStageInsn[1] = OnStageInsn[0];
						PreTar[1] = PreTar[0];
						if (iter.hasNext() == true) {
							OnStageInsn[0] = iter.next();
							InsnCount++;
						} else {
							OnStageInsn[0] = nop;
						}
						continue;
					}
					// if D isn't a nop
					if (OnStageInsn[1].dstReg != -2) {

						// M&D load to use/ load to store

						if (OnStageInsn[3].mem == MemoryOp.Load) {

							if (OnStageInsn[1].mem == MemoryOp.Store
									&& OnStageInsn[1].srcReg2 == OnStageInsn[3].dstReg) {
								continue;
							}

							else if ((OnStageInsn[1].mem != MemoryOp.Store)
									&& (OnStageInsn[1].srcReg2 == OnStageInsn[3].dstReg
											|| OnStageInsn[1].srcReg1 == OnStageInsn[3].dstReg)) {
								continue;
							}

						}
						// no dependence
						else {
							OnStageInsn[2] = OnStageInsn[1];
							PreTar[2] = PreTar[1];
							OnStageInsn[1] = OnStageInsn[0];
							PreTar[1] = PreTar[0];
							if (iter.hasNext() == true) {
								OnStageInsn[0] = iter.next();
								InsnCount++;
							} else {
								OnStageInsn[0] = nop;
							}
							continue;

						}

					}

				}
				// if prediction is correct
				if ((OnStageInsn[2].dstReg != -2) && (((OnStageInsn[2].branchDirection == Direction.Taken)
						&& (OnStageInsn[2].branchTarget == PreTar[2]))
						|| ((OnStageInsn[2].branchDirection != Direction.Taken)
								&& (PreTar[2] == OnStageInsn[2].fallthroughPC())))) {
					if (TrainTrue == 0) {
						bp.train(OnStageInsn[2].pc, PreTar[2], OnStageInsn[2].branchDirection);
						TrainTrue++;
						// System.out.println(Train++);

					}
					continue;
				}
				// if prediction is incorrect
				else if ((OnStageInsn[2].dstReg != -2) && (((OnStageInsn[2].branchDirection == Direction.Taken)
						&& (OnStageInsn[2].branchTarget != PreTar[2]))
						|| ((OnStageInsn[2].branchDirection != Direction.Taken)
								&& (PreTar[2] != OnStageInsn[2].fallthroughPC())))) {
					// 1st
					if (FlushTrue == 0) {

						if (OnStageInsn[2].branchDirection == Direction.Taken) {
							bp.train(OnStageInsn[2].pc, OnStageInsn[2].branchTarget, OnStageInsn[2].branchDirection);
							// System.out.println(Train++);

						} else {
							bp.train(OnStageInsn[2].pc, OnStageInsn[2].fallthroughPC(), OnStageInsn[2].branchDirection);

						}

						if (OnStageInsn[0].dstReg != -2) {
							iter.putBack(OnStageInsn[0]);
							OnStageInsn[0] = nop;
							PreTar[0] = 0;

							InsnCount--;

						}
						if (OnStageInsn[1].dstReg != -2) {
							iter.putBack(OnStageInsn[1]);
							OnStageInsn[1] = nop;
							PreTar[1] = 0;

							InsnCount--;

						}
						FlushTrue++;
						if (iter.hasNext() == true) {
							OnStageInsn[0] = iter.next();
						} else {
							OnStageInsn[0] = nop;
						}
						continue;
					}
					// 2st
					else if (FlushTrue == 1) {
						OnStageInsn[1] = OnStageInsn[0];
						PreTar[1] = PreTar[0];
						if (iter.hasNext() == true) {
							OnStageInsn[0] = iter.next();
						} else {
							OnStageInsn[0] = nop;
						}

						FlushTrue++;
						continue;
					}
					// 3+
					else if (FlushTrue == 2) {
						continue;
					}

				}
			}

			// end of mem latency
			else if ((OnStageInsn[3].mem == MemoryOp.Load || OnStageInsn[3].mem == MemoryOp.Store)
					&& memtrue == additionalMemLatency) {
				memtrue = 0;
			}

			// if the prediction is correct
			if ((FlushTrue == 0) && (OnStageInsn[2].dstReg != -2)
					&& (((OnStageInsn[2].branchDirection == Direction.Taken)
							&& (OnStageInsn[2].branchTarget == PreTar[2]))
							|| ((OnStageInsn[2].branchDirection != Direction.Taken)
									&& (PreTar[2] == OnStageInsn[2].fallthroughPC())))) {

				// train BTB and Predictor
				if (TrainTrue == 0) {
					bp.train(OnStageInsn[2].pc, PreTar[2], OnStageInsn[2].branchDirection);
					// System.out.println(Train++);
				}

				// the pipeline goes as normal
				if (OnStageInsn[2].mem == MemoryOp.Load) {

					if (OnStageInsn[1].mem == MemoryOp.Store && OnStageInsn[1].srcReg2 == OnStageInsn[2].dstReg) {
						OnStageInsn[4] = OnStageInsn[3];
						OnStageInsn[3] = OnStageInsn[2];
						OnStageInsn[2] = nop;
						PreTar[2] = 0;

						continue;

					}

					else if ((OnStageInsn[1].mem != MemoryOp.Store)
							&& ((OnStageInsn[1].srcReg2 == OnStageInsn[2].dstReg)
									|| (OnStageInsn[1].srcReg1 == OnStageInsn[2].dstReg))) {
						OnStageInsn[4] = OnStageInsn[3];
						OnStageInsn[3] = OnStageInsn[2];
						OnStageInsn[2] = nop;
						PreTar[2] = 0;

						continue;
					}
				}

			}

			// if the prediction is incorrect
			else if ((FlushTrue == 0) && (OnStageInsn[2].dstReg != -2)
					&& (((OnStageInsn[2].branchDirection == Direction.Taken)
							&& (OnStageInsn[2].branchTarget != PreTar[2]))
							|| ((OnStageInsn[2].branchDirection != Direction.Taken)
									&& (PreTar[2] != OnStageInsn[2].fallthroughPC())))) {

				// train the BTB and Predictor (train none-branch insn as well)
				if (OnStageInsn[2].branchDirection == Direction.Taken) {
					bp.train(OnStageInsn[2].pc, OnStageInsn[2].branchTarget, OnStageInsn[2].branchDirection);
					// System.out.println(Train++);

				} else {
					bp.train(OnStageInsn[2].pc, OnStageInsn[2].fallthroughPC(), OnStageInsn[2].branchDirection);

				}

				// flush insn on the first 2 stages

				if (OnStageInsn[0].dstReg != -2) {
					iter.putBack(OnStageInsn[0]);
					OnStageInsn[0] = nop;
					PreTar[0] = 0;

					InsnCount--;

				}
				if (OnStageInsn[1].dstReg != -2) {
					iter.putBack(OnStageInsn[1]);
					OnStageInsn[1] = nop;
					PreTar[1] = 0;

					InsnCount--;

				}

			}
			// if mem latency >=2
			if (FlushTrue == 2) {
				if (OnStageInsn[2].mem == MemoryOp.Load) {

					if (OnStageInsn[1].mem == MemoryOp.Store && OnStageInsn[1].srcReg2 == OnStageInsn[2].dstReg) {
						OnStageInsn[4] = OnStageInsn[3];
						OnStageInsn[3] = OnStageInsn[2];
						OnStageInsn[2] = nop;
						PreTar[2] = 0;

						continue;

					}

					else if ((OnStageInsn[1].mem != MemoryOp.Store) && (OnStageInsn[1].srcReg2 == OnStageInsn[2].dstReg
							|| OnStageInsn[1].srcReg1 == OnStageInsn[2].dstReg)) {
						OnStageInsn[4] = OnStageInsn[3];
						OnStageInsn[3] = OnStageInsn[2];
						OnStageInsn[2] = nop;
						PreTar[2] = 0;

						continue;
					}
				}
			}

			if (iter.hasNext() == true) {
				OnStageInsn[4] = OnStageInsn[3];
				OnStageInsn[3] = OnStageInsn[2];
				OnStageInsn[2] = OnStageInsn[1];
				PreTar[2] = PreTar[1];
				OnStageInsn[1] = OnStageInsn[0];
				PreTar[1] = PreTar[0];
				OnStageInsn[0] = iter.next();

				InsnCount++;

			}

			else if (iter.hasNext() != true) {
				OnStageInsn[4] = OnStageInsn[3];
				OnStageInsn[3] = OnStageInsn[2];
				OnStageInsn[2] = OnStageInsn[1];
				PreTar[2] = PreTar[1];
				OnStageInsn[1] = OnStageInsn[0];
				PreTar[1] = PreTar[0];
				OnStageInsn[0] = nop;

			}

			FlushTrue = 0;
			TrainTrue = 0;

			// condition to stop the loop
			if (OnStageInsn[0].dstReg == -2 && OnStageInsn[1].dstReg == -2 && OnStageInsn[2].dstReg == -2
					&& OnStageInsn[3].dstReg == -2 && OnStageInsn[4].dstReg == -2) {

				break;
			}

		}
	}

	@Override
	public long getInsns() {
		return InsnCount;
	}

	@Override
	public long getCycles() {
		return CycleCount;
	}
}