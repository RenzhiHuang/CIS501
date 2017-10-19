package cis501.submission;

import cis501.IBranchTargetBuffer;
import java.lang.Math;


public class BranchTargetBuffer implements IBranchTargetBuffer {
    //private int indexBits;
    private int index;
    public int dimension;
    public long[] tag;
    public long[] target;
   

    public BranchTargetBuffer(int indexBits) {
        //this.indexBits = indexBits;   
        int dimensionsize = (int)Math.pow(2,indexBits);
        this.dimension = dimensionsize;
        this.tag = new long[dimensionsize];
        this.target = new long[dimensionsize];
        
    }
       
    @Override
    public long predict(long pc) {
        index = (int)(pc & (dimension-1));
        int match = -1;
        for(int i=0;i<dimension;i++){
            if(tag[i] == pc){
                match = i;
                break;
           }
        }
        
        long matchtarget;
        
        if(match == -1){
            matchtarget = 0;
        }
        else{
            matchtarget = target[match];
        }
        return matchtarget;
    }

    @Override
    public void train(long pc, long actual) {
            index = (int)(pc & (dimension-1));
            tag[index] = pc;
            target[index] = actual;
        
    }
}
