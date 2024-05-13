package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DPOMDPFileParserTest {

  @Mock
  private DecPOMDPBuilder builder;

  @InjectMocks
  private DPOMDPFileParser parser;

  @Test
  void parseDecPOMDP() {
  }

  @Test
  void doParseDecPOMDP() {
  }

  @Test
  void parseLine() {
  }

  @Test
  void parseCurrentSection() {
  }

  @Test
  void startNewSection() {
  }

  @Test
  void parseSection() {
  }

}