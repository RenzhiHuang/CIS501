package cis501.submission;

import cis501.Direction;
import cis501.IDirectionPredictor;

public class DirPredTournament extends DirPredBimodal {
    private int chooserIndexBits;
    private IDirectionPredictor predictorNT;
    private IDirectionPredictor predictorT;
    private int chooserIndex;

    public DirPredTournament(int chooserIndexBits, IDirectionPredictor predictorNT, IDirectionPredictor predictorT) {
        super(chooserIndexBits); // re-use DirPredBimodal as the chooser table
        this.chooserIndexBits = chooserIndexBits;
        this.predictorNT = predictorNT;
        this.predictorT = predictorT;
    }

    @Override
    public Direction predict(long pc) {
        Direction TourPre = null;        
        chooserIndex = (int)(pc & (dimension -1));
        if(counter[chooserIndex] == 0 || counter[chooserIndex] == 1){
            TourPre = predictorNT.predict(pc);
        }
        else if(counter[chooserIndex] == 2 || counter[chooserIndex] == 3){
            TourPre = predictorT.predict(pc);
        }
        return TourPre;
    }

    @Override
    public void train(long pc, Direction actual) {
        predictorNT.train(pc, actual);
        predictorT.train(pc, actual);
        chooserIndex = (int)(pc & (dimension -1));
        if(actual == predictorNT.predict(pc) && counter[chooserIndex] != 0){
            counter[chooserIndex] = counter[chooserIndex] - 1;
        }
        else if(actual == predictorT.predict(pc) && counter[chooserIndex] !=3){
            counter[chooserIndex] = counter[chooserIndex] + 1;
        }

    }

}
