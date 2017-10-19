package cis501.submission;

import cis501.BranchType;
import cis501.ITraceAnalyzer;
import java.lang.Math;
import cis501.Insn;
import cis501.MemoryOp;


public class TraceAnalyzer implements ITraceAnalyzer {
    private double Avg;
    private double BwIn;
    private String mostCommonIC = "";
    private double[] sumforbit = new double[32];
    private int numDi=0;

    @Override
    public String author() {
        return "<Renzhi Huang>";
    }

    @Override
    public void run(Iterable<Insn> iiter) {
        for (Insn insn : iiter) {
            double sumInsnSize = 0;
            double sumInsnSize_Bw = 0;
            int Condi = 0;
            int Uncondi = 0;
            int Other = 0;
            int Lo= 0 ;
            int St= 0 ;
            int size=0;
            String[] mostCommon = new String[4];
            for(int ini=0;ini<32;ini++) {
                sumforbit[ini]=0;
            }
            double offsetSize;
            for (Insn insn : iiter) {
                //method1:get the sum of insns and the sum of insns size
                size++;
                sumInsnSize =  sumInsnSize + insn.insnSizeBytes;
                //method2:get the sum of insns size without thumb insns
                if (insn.insnSizeBytes == 2){
                    sumInsnSize_Bw = sumInsnSize_Bw + insn.insnSizeBytes * 2;
                }
                else{
                    sumInsnSize_Bw = sumInsnSize_Bw + insn.insnSizeBytes;
                }
                //method3:get the frequency of each category
                if (insn.branchType == BranchType.ConditionalDirect || insn.branchType == BranchType.ConditionalIndirect){
                    Condi++;
                }
                else if(insn.branchType == BranchType.UnconditionalDirect || insn.branchType == BranchType.UnconditionalIndirect){
                    Uncondi++;
                }
                else {
                    if (insn.mem != MemoryOp.Load && insn.mem != MemoryOp.Store){
                        Other++;
                    }
                }
                if(insn.mem == MemoryOp.Load){
                    Lo++;
                }
                else if(insn.mem == MemoryOp.Store){
                    St++;
                }
                //method4:get the sum of direct branches and input the offset value to an array;
                if(insn.branchType == BranchType.ConditionalDirect || insn.branchType == BranchType.UnconditionalDirect){
                    numDi++;
                    offsetSize = 2 + Math.floor((Math.log(Math.abs(insn.pc-insn.branchTarget)))/(Math.log(2)));
                    
                    for(int b=0;b<32;b++) {
                        if(offsetSize<=b+1) {
                            sumforbit[b]++;
                        }
                    }
                }
            }
            //method1:get the average size of instructions
            Avg = sumInsnSize / size;
            //method2:get the value to be returned
            BwIn = sumInsnSize_Bw / sumInsnSize;
            //method3:get the maximum value of these frequencies
            int m=Condi;
            if(Uncondi > m){
                m = Uncondi;
            }
            if(Other > m){
                m = Other;
            }
            if(Lo > m){
                m = Lo;
            }
            if(St > m){
                m = St;
            }
            //method3:input the categories of max value to the string array
            int len=0;
            if(m == Condi){
                mostCommon[len]="conditional";
                len++;
            }
            if(m == Uncondi){
                mostCommon[len]="unconditional";
                len++;
            }
            if(m == Other){
                mostCommon[len]="other";
                len++;
            }
            if(m == Lo){
                mostCommon[len]="load";
                len++;
            }
            if(m == St){
                mostCommon[len]="store";
                len++;
            }
            
            //method3:create the string to be returned
            for(int k=0;k<len;k++){
                mostCommonIC = mostCommonIC + " " + mostCommon[k];
            
        }
    }

    @Override
    public double avgInsnSize() {
        return Avg;
    }

    @Override
    public double insnBandwidthIncreaseWithoutThumb() {
        return BwIn;
    }

    @Override
    public String mostCommonInsnCategory() {
        return mostCommonIC;
    }

    @Override
    public double fractionOfDirectBranchOffsetsLteNBits(int bits) {
        double fraction=sumforbit[bits-1] / numDi;
        return fraction;
    }

}
