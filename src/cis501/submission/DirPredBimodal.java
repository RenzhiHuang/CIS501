package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;
import java.lang.Math;

public class DirPredBimodal implements IDirectionPredictor {
    private int indexBits;
    private int index;
    public int dimension;
    public int[] counter;
    public DirPredBimodal(int indexBits) {
        this.indexBits = indexBits;
        int dimensionsize =(int)Math.pow(2,indexBits);
        this.dimension = dimensionsize;
        this.counter = new int[dimensionsize];
    }
    
    //declare counter array
   // public int dimension = (int)Math.pow(2,indexBits);
    //public int[] counter = new int[dimension];
    
        
    @Override
    public Direction predict(long pc) {
    	 index = (int)(pc & (dimension-1));
         Direction BimodalPre = null;
         if(counter[index] == 0 || counter[index] == 1){
             BimodalPre = Direction.NotTaken;
         }
         else if(counter[index] == 2 || counter[index] == 3){
             BimodalPre = Direction.Taken;
         }
         return BimodalPre;
    }

    @Override
    public void train(long pc, Direction actual) {
        index = (int)(pc & (dimension-1));
        if(actual == Direction.NotTaken && counter[index] != 0){
            counter[index] = counter[index]-1;
        }
        else if(actual == Direction.Taken && counter[index] !=3){
            counter[index] = counter[index]+1;
        }
    }

}
