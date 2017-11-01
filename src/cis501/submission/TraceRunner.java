package cis501.submission;

import cis501.ITraceAnalyzer;
import cis501.InsnIterator;

import java.io.IOException;

public class TraceRunner {

    public static void main(String[] args) throws IOException {
        final int insnLimit;

        switch (args.length) {
            case 1:
                insnLimit = -1; // by default, run on entire trace
                break;
            case 2: // use user-provided limit
                insnLimit = Integer.parseInt(args[1]);
                break;
            default:
                System.err.println("Usage: path/to/trace-file [insn-limit]");
                return;
        }

        ITraceAnalyzer ta = new TraceAnalyzer();
        InsnIterator uiter = new InsnIterator("/Users/koutsutomushiba/Desktop/cis501-master/path/streamcluster-10M-v2.trace.gz", -1);
        ta.run(uiter);
        System.out.println("Avg insn size is: " + ta.avgInsnSize());
        System.out.println("Insn bw increase sans thumb: " + ta.insnBandwidthIncreaseWithoutThumb());
        System.out.println("Most common insn category: " + ta.mostCommonInsnCategory());
        for (int b = 1; b <= 32; b++) {
            System.out.format("Direct branch offsets encodable in %d bits: %.2f%n",
                    b, ta.fractionOfDirectBranchOffsetsLteNBits(b));
        }
    }

}
