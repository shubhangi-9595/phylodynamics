package phylodynamics.epidemiology;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.core.Loggable;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.spec.domain.Real;
import beast.base.spec.type.Tensor;
import beast.base.inference.CalculationNode;
import beast.base.inference.Operator;
import beast.base.inference.StateNode;
import beast.base.inference.parameter.BooleanParameter;
import beast.base.evolution.tree.Tree;



import java.util.ArrayList;
import java.util.List;


/**
 * User: Denise
 * Date: Jan 4, 2013
 * Time: 12:17:34 PM
 */
@Description("Operator that simulates new S(E)IR trajectory")
public class CompoundSIROperator extends Operator {

    public Input<Operator> affectingOperator =
            new Input<Operator>("affectingOperator", "Operator that affects S(E)IR trajectory", Input.Validate.REQUIRED);

    public Input<HybridSEIREpidemic> SIR =
            new Input<HybridSEIREpidemic>("SIR", "SIR trajectory calculation node - simulates dS", Input.Validate.REQUIRED);

    public Input<RealScalarParam<? extends Real>> S0_input =
            new Input<>("S0", "The numbers of susceptible individuals", Input.Validate.REQUIRED);

    public Input<RealVectorParam<? extends Real>> dS_input =
            new Input<>("dS", "dS vector containing the changes in numbers of susceptibles per location", Input.Validate.REQUIRED);
    public Input<RealVectorParam<? extends Real>> dR_input =
            new Input<>("dR", "dR vector containing the changes in numbers of susceptibles per location", Input.Validate.REQUIRED);

    public Input<RealVectorParam<? extends Real>> susceptiblePopulationInput = new Input<>("susceptiblePopulation", "susceptible population size at times eventTimes");

    public Input<RealVectorParam<? extends Real>> infectedPopulationInput =
            new Input<>("infectedPopulation", "infectedPopulation over time (relative to eventTimes)", Input.Validate.XOR, dS_input);

   public Input<RealVectorParam<? extends Real>> eventTimeInput =
            new Input<>("eventTimes", "ordered times at which events in SIR happen", Input.Validate.XOR, dR_input);

    public Input<Tensor<? extends Real, Double>> birth =
            new Input<>("birth", "birth rate vector with rate per location",  Input.Validate.REQUIRED);
    public Input<Tensor<? extends Real, Double>> death =
            new Input<>("death", "death rate vector with rate per location",  Input.Validate.REQUIRED);
    public Input<Tensor<? extends Real, Double>> sampling =
            new Input<>("sampling", "sampling rate vector with rate per location",  Input.Validate.REQUIRED);

    public Input<RealScalarParam<? extends Real>> loseImmunityRate = new Input<>("loseImmunityRate", "The rate at which recovereds lose immunity");

    public Input<Tree> m_tree =
            new Input<Tree>("tree", "The phylogenetic tree being estimated",  Input.Validate.REQUIRED);
    public Input<RealScalarParam<? extends Real>> origin =
            new Input<>("origin", "The origin of infection x0", Input.Validate.REQUIRED);

    int m;
    Integer S0;
    int ntaxa;

    Tree tree;
    double T;
    HybridSEIREpidemic current;

    Double[] b;
    Double[] d;
    Double[] s;
    Boolean birthChanges;
    Boolean deathChanges;
    Boolean samplingChanges;


    @Override
    public void initAndValidate() {


        affectingOperator.get().initAndValidate();

        S0 = (int) (S0_input.get().get());

        if (birth.get() != null && death.get() != null && sampling.get() != null){

            b = new Double[birth.get().size()];

            for (int i=0; i<b.length; i++){
                b[i] = birth.get().get(i);
            }


            int dim = death.get().size();
            if (dim != sampling.get().size()) throw new RuntimeException("Error: Death and sampling must have equal dimensions!");
            d = new Double[dim];
            s = new Double[dim];

            for (int i = 0; i<dim; i++){
                d[i] = death.get().get(i);
                s[i] = sampling.get().get(i);
            }
        }

        else{
            throw new RuntimeException("Either specify birthRate, deathRate and samplingRate OR specify R0, becomeUninfectiousRate and samplingProportion!");
        }


        m = SIR.get().Nsamples.get();

        tree = m_tree.get();
        T = origin.get().get();
        if (T<=0) T=tree.getRoot().getHeight();

        ntaxa = tree.getLeafNodeCount();

        // initialize trajectory
        current =  SIR.get();
        if ( !current.initTraj(b, 0., d, s, (loseImmunityRate.get()==null)?0.:loseImmunityRate.get().get(), T, ntaxa, 100, m, current.times, dS_input.get()==null))
            throw new RuntimeException("Could not find suitable trajectory. Please try different epi parameters!");

        if (dS_input.get() !=null){

            //dS_input.get().assignFromWithoutID(new RealVectorParameter(current.dS));
            //dR_input.get().assignFromWithoutID(new RealVectorParameter(current.dR));
            RealVectorParam<? extends Real> dS = dS_input.get();
            for(int i = 0; i < current.dS.length; i++) dS.set(i, current.dS[i]);

            RealVectorParam<? extends Real> dR = dR_input.get();
            for(int i = 0; i < current.dR.length; i++) dR.set(i, current.dR[i]);        
        }
        else {
            //susceptiblePopulationInput.get().assignFromWithoutID(new RealVectorParameter(current.S));
            //infectedPopulationInput.get().assignFromWithoutID(new RealVectorParameter(current.I));
            //eventTimeInput.get().assignFromWithoutID(new RealVectorParameter(current.eventTimes));
            RealVectorParam<? extends Real> S = susceptiblePopulationInput.get();
            S.setDimension(current.S.length);
            for(int i = 0; i < current.S.length; i++) S.set(i, current.S[i]);

            RealVectorParam<? extends Real> I = infectedPopulationInput.get();
            I.setDimension(current.I.length);
            for(int i = 0; i < current.I.length; i++) I.set(i, current.I[i]);   
            
            RealVectorParam<? extends Real> eventTime = eventTimeInput.get();
            eventTime.setDimension(current.eventTimes.length);           
            for(int i = 0; i < current.eventTimes.length; i++) eventTime.set(i, current.eventTimes[i]);


        }
    }

    @Override
    public double proposal() {

        double hastingsRatio = affectingOperator.get().proposal();

        S0 = (int) (S0_input.get().get());

//        b = new Double[birth.get().getDimension()];

        for (int i=0; i<b.length; i++){
            b[i] = birth.get().get(i);
        }

        int dim = death.get().size();
        if (dim != sampling.get().size()) throw new RuntimeException("Error: Death and sampling must have equal dimensions!");
//        d = new Double[dim];
//        s = new Double[dim];

        for (int i = 0; i<dim; i++){
            d[i] = death.get().get(i);
            s[i] = sampling.get().get(i);
        }

        T = origin.get().get();
        if (T<=0) T=tree.getRoot().getHeight();

        // simulate trajectory
        current =  SIR.get();
        if ( !current.refresh(S0,0, b, 0., d, s,(loseImmunityRate.get()==null)?0.:loseImmunityRate.get().get(), T, ntaxa, 1, m, current.times, dS_input.get()==null) /* && Randomizer.nextDouble()>.75*/){
            return Double.NEGATIVE_INFINITY;
        }

        if (dS_input.get()!=null){
            for (int i =0; i < m-1; i++){
                dS_input.get().set(i, current.dS[i]);
                dR_input.get().set(i, current.dR[i]);
            }
        }
        else{
            for (int i =0; i < current.eventTimes.length; i++){
                susceptiblePopulationInput.get().set(i, current.S[i]);
                infectedPopulationInput.get().set(i, current.I[i]);
                eventTimeInput.get().set(i, current.eventTimes[i]);
            }

        }
        return hastingsRatio;
    }


    /**
     * return list of state nodes that this operator operates on.
     * state nodes that are input to the operator but are never changed
     * in a proposal should not be listed
     */
    @Override
    public List<StateNode> listStateNodes() {

        final List<StateNode> list = affectingOperator.get().listStateNodes();

        list.add(dS_input.get());
        list.add(dR_input.get());

        return list;
    }

    public static void main(String[] args){
    }
}
