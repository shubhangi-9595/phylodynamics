package phylodynamics.parameterization;

import bdmmprime.parameterization.EpiParameterization;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import phylodynamics.BDSIR;
import java.util.Arrays;

public class BDSIRParameterization extends EpiParameterization{
    public Input<Function> S0_input =
            new Input<Function>("S0", "The numbers of susceptible individuals");

    public Input<RealParameter> m_dS =
            new Input<RealParameter>("dS", "dS vector containing the changes in numbers of susceptibles per location", Input.Validate.REQUIRED);
    public Input<RealParameter> m_dE =
            new Input<RealParameter>("dE", "dE vector containing the changes in numbers of exposed per location");
    public Input<RealParameter> m_dR =
            new Input<RealParameter>("dR", "dR vector containing the changes in numbers of recovered per location", Input.Validate.REQUIRED);

    public Input<Boolean> checkTreeConsistent = new Input<Boolean>("checkTreeConsistent", "check if trajectory is consistent with number of lineages in tree? default true", true);

    public Input<Boolean> isSeasonal = new Input<Boolean>("isSeasonal", "Is this a SeasonalSIRSEpidemic? default false", false);

    private BDSIR bdsir;

    public void setBDSIR(BDSIR bdsir_) {
        this.bdsir = bdsir_;
    }

    @Override
    public void initAndValidate() {
        if (bdsir == null)
            return;
        intervalEndTimes = null;
        storedIntervalEndTimes = null;
        birthRates = null;
        super.initAndValidate();
    }


    @Override
    public double[] getBirthRateChangeTimes() {
        // same as bdsky‘s getChangeTimes() -> equidistant intervals over the processLength
        int numChanges = Math.max(bdsir.dim-1 , 0);
        double intervalWidth = getTotalProcessLength()/bdsir.dim;
        double[] changeTimes = new double[numChanges];
        for (int i =0; i < numChanges; i++)
            changeTimes[i] = intervalWidth*(i+1);

        bdsir.birthRateChangeTimes = changeTimes;
        Double result = bdsir.updateRatesAndTimes(bdsir.treeInput.get());
        bdsir.treeConsistent = (result != Double.NEGATIVE_INFINITY);
        return changeTimes;
    }

    @Override
    public double[] getBirthRateValues(double time) {
        if(time == intervalEndTimes[0]) {
            bdsir.times = intervalEndTimes;
            bdsir.totalIntervals = intervalEndTimes.length;
            if (bdsir.birth.length != bdsir.totalIntervals)
                bdsir.birth = new double[bdsir.totalIntervals];
            bdsir.adjustBirthRates(bdsir.birthSIR);
        }

        return new double[]{bdsir.birth[bdsir.index(time, intervalEndTimes)]};
    }

    @Override
    protected boolean requiresRecalculation(){
        dirty = true;
        return true;
    }

    /*
    public  void printRates() {
        System.out.println("TIMES: " + java.util.Arrays.toString(bdsir.times));
        System.out.println("BIRTH: " + java.util.Arrays.toString(bdsir.birth));
        System.out.println("birthRateChangeTimes: " + java.util.Arrays.toString(bdsir.birthRateChangeTimes));
        System.out.println("DEATH: " +   java.util.Arrays.deepToString(getDeathRates()));
        System.out.println("deathRateChangeTimes: " + java.util.Arrays.toString(getDeathRateChangeTimes()));
        System.out.println("psi: " +    java.util.Arrays.deepToString(getSamplingRates()));
        System.out.println("samplingRateChangeTimes: " + java.util.Arrays.toString(getSamplingRateChangeTimes()));
    }*/

    @Override
    public boolean valuesAreValid(){
        if(super.valuesAreValid() && bdsir.treeConsistent) {
            return true;
        }
        else{
           return false;
         }
    }
}