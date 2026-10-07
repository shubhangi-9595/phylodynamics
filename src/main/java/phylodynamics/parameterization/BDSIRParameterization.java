package phylodynamics.parameterization;

import bdmmprime.parameterization.EpiParameterization;
import bdmmprime.parameterization.SkylineVectorParameter;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.spec.domain.Real;
import beast.base.spec.type.RealScalar;
import beast.base.spec.type.RealVector;
import phylodynamics.BDSIR;
import java.util.Arrays;

public class BDSIRParameterization extends EpiParameterization{
    public Input<RealScalar<? extends Real>> S0_input =
            new Input<>("S0", "The numbers of susceptible individuals");

    public Input<RealVector<? extends Real>> m_dS =
            new Input<>("dS", "dS vector containing the changes in numbers of susceptibles per location", Input.Validate.REQUIRED);
    public Input<RealVector<? extends Real>> m_dE =
            new Input<>("dE", "dE vector containing the changes in numbers of exposed per location");
    public Input<RealVector<? extends Real>> m_dR =
            new Input<>("dR", "dR vector containing the changes in numbers of recovered per location", Input.Validate.REQUIRED);

    public Input<Boolean> checkTreeConsistent = new Input<Boolean>("checkTreeConsistent", "check if trajectory is consistent with number of lineages in tree? default true", true);

    public Input<Boolean> isSeasonal = new Input<Boolean>("isSeasonal", "Is this a SeasonalSIRSEpidemic? default false", false);

    private BDSIR bdsir; // set using setBDSIR method. getBirthRateValues needs bdsir.birth

    public void setBDSIR(BDSIR bdsir_) {
        this.bdsir = bdsir_;
        requiresRecalculation();
    }

    @Override
    public void initAndValidate() {
        intervalEndTimes = null; // protected in bdmmprime.parameterization
        birthRates = null; // protected in bdmmprime.parameterization
        super.initAndValidate();
    }

    @Override
    public double[] getBirthRateChangeTimes () {
        double[] changeTimes = new double[Math.max(m_dS.get().size() - 1, 0)];
        for (int i = 0; i<changeTimes.length; i++)
            changeTimes[i] = (getTotalProcessLength() / m_dS.get().size()) * (i+1);
        return changeTimes;
        //return super.getBirthRateChangeTimes();
        //not using super.getBirthRateChangeTimes() because in bdsky, BirthRateChangeTimes is calculated using origin and dim.
        //super,getBirthRateChangeTimes() may generate a different different array based on the changeTimes for ReInput and becomeUninfectiousRateInput
    }


    @Override
    public double[] getBirthRateValues(double time) {
        if(bdsir == null)
            return ZERO_VALUE_ARRAY;

        if (time == intervalEndTimes[0]) { // to avoid updateRatesAndTimes running intervalEndTimes.len times
            Double result = bdsir.updateRatesAndTimes(bdsir.treeInput.get());
            bdsir.treeConsistent = (result != Double.NEGATIVE_INFINITY);
        }
        return new double[]{bdsir.birth[bdsir.index(time, bdsir.times)]};
    }

    @Override
    public double[] getIntervalEndTimes() {
        return intervalEndTimes;
    }

    @Override
    public int getTotalIntervalCount() {
        return intervalEndTimes.length;
    }


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