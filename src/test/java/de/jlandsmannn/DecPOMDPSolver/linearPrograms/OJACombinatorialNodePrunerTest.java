package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OJACombinatorialNodePrunerTest {

  @Mock
  private DecPOMDPWithStateController decPOMDP;

  @Mock
  private AgentWithStateController agent;

  @Mock
  private Collection<Distribution<State>> beliefPoints;

  @Mock
  private OJALinearProgramSolver solver;

  @Mock
  private OJACombinatorialNodePruningTransformer transformer;

  private OJACombinatorialNodePruner pruner;

  @BeforeEach
  void setUp() {
    pruner = spy(new OJACombinatorialNodePruner(transformer, solver));
  }

  @Test
  void setDecPOMDP_ShouldCallTransformerSetDecPOMDP() {
    pruner.setDecPOMDP(decPOMDP);
    Mockito.verify(transformer).setDecPOMDP(decPOMDP);
  }

  @Test
  void setAgent_ShouldCallTransformerSetAgent() {
    pruner.setAgent(agent);
    Mockito.verify(transformer).setAgent(agent);
  }

  @Test
  void setBeliefPoints_ShouldCallTransformerSetBeliefPoints() {
    pruner.setBeliefPoints(beliefPoints);
    Mockito.verify(transformer).setBeliefPoints(beliefPoints);
  }

  @Test
  void pruneNodesIfCombinatorialDominated_ShouldNotProceedIfAgentHasNoNodes() {
    when(agent.getControllerNodes()).thenReturn(Node.listOf());
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodesIfCombinatorialDominated();

    verify(pruner, times(0)).pruneNodeIfCombinatorialDominated(any());
  }

  @Test
  void pruneNodesIfCombinatorialDominated_ShouldNotProceedIfAgentHasSingleNode() {
    when(agent.getControllerNodes()).thenReturn(Node.listOf("Q1"));
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodesIfCombinatorialDominated();

    verify(pruner, times(0)).pruneNodeIfCombinatorialDominated(any());
  }

  @Test
  void pruneNodesIfCombinatorialDominated_ShouldNotProceedIfAgentHasTwoNodes() {
    when(agent.getControllerNodes()).thenReturn(Node.listOf("Q1", "Q2"));
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodesIfCombinatorialDominated();

    verify(pruner, times(0)).pruneNodeIfCombinatorialDominated(any());
  }

  @Test
  void pruneNodesIfCombinatorialDominated_ShouldProceedForEveryNodeOfAgent() {
    when(agent.getControllerNodes()).thenReturn(Node.listOf("Q1", "Q2", "Q3", "Q4"));
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodesIfCombinatorialDominated();

    verify(pruner, times(4)).pruneNodeIfCombinatorialDominated(any());
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldCallTransformerToCreateLinearProgram() {
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(transformer).getLinearProgramForNode(node);
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldCallSolverSetLinearProgram() {
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(solver).setLinearProgram(any());
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldCallSolverMaximise() {
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(solver).maximise();
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldCallTransformToGetDominatingNodesIfLPHasResult() {
    when(solver.maximise()).thenReturn(Optional.of(Map.of()));
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(transformer).getDominatingNodeDistributionFromResult(any());
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldNotCallTransformToGetDominatingNodesIfLPHasNoResult() {
    when(solver.maximise()).thenReturn(Optional.empty());
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(transformer, never()).getDominatingNodeDistributionFromResult(any());
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldCallAgentToPruneNodeIfDominatingNodesExist() {
    when(solver.maximise()).thenReturn(Optional.of(Map.of()));
    when(transformer.getDominatingNodeDistributionFromResult(any())).thenReturn(Optional.of(Distribution.createSingleEntryDistribution(Node.from("Q2"))));
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(agent).pruneNode(any(), any(Distribution.class));
  }

  @Test
  void pruneNodeIfCombinatorialDominated_ShouldNotCallAgentToPruneNodeIfNoDominatingNodesExist() {
    when(solver.maximise()).thenReturn(Optional.of(Map.of()));
    when(transformer.getDominatingNodeDistributionFromResult(any())).thenReturn(Optional.empty());
    var node = Node.from("Q0");
    pruner
      .setDecPOMDP(decPOMDP)
      .setAgent(agent)
      .setBeliefPoints(beliefPoints)
      .pruneNodeIfCombinatorialDominated(node);

    verify(agent, never()).pruneNode(any(), any(Distribution.class));
  }
}