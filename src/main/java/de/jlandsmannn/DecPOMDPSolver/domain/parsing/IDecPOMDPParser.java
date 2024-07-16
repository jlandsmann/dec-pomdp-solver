package de.jlandsmannn.DecPOMDPSolver.domain.parsing;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;

import java.util.Optional;

public interface IDecPOMDPParser<BUILDER extends DecPOMDPBuilder<?, ?, ?>> {

  Optional<BUILDER> parse(String fileName);

}
