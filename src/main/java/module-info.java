open module phylodynamics {
    requires beast.pkgmgmt;
    requires beast.base;
    requires bdmmprime;
    requires commons.math3;
    requires java.desktop;

    exports phylodynamics;
    exports phylodynamics.epidemiology;
    exports phylodynamics.evolution.operators;
    exports phylodynamics.evolution.tree.coalescent;
    exports phylodynamics.marsden;
    exports phylodynamics.parameterization;
    exports phylodynamics.util;

    provides beast.base.core.BEASTInterface with
        phylodynamics.BDSIR,
        phylodynamics.epidemiology.CoalescentSimulator,
        phylodynamics.epidemiology.CoalescentSimulatorBasic,
        phylodynamics.epidemiology.CompoundSIROperator,
        phylodynamics.epidemiology.DeterministicCoalescentSIR,
        phylodynamics.epidemiology.HybridSEIREpidemic,
        phylodynamics.epidemiology.HybridTrajectorySingleLocationSimulator,
        phylodynamics.epidemiology.SIRPopulationFunction,
        phylodynamics.epidemiology.SIR_Initializer,
        phylodynamics.epidemiology.SeasonalSIRSEpidemic,
        phylodynamics.epidemiology.StochasticCoalescent,
        phylodynamics.epidemiology.StochasticCoalescentSIR,
        phylodynamics.epidemiology.Volz2009TreeDistribution,
        phylodynamics.evolution.operators.MultiScaleOperator,
        phylodynamics.evolution.operators.ScaleOffsetOperator,
        phylodynamics.evolution.tree.coalescent.StochasticCoalescentSkyline,
	phylodynamics.parameterization.BDSIRParameterization;
}
