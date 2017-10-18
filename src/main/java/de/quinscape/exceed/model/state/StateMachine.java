package de.quinscape.exceed.model.state;

import com.google.common.collect.Maps;
import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.runtime.domain.InvalidMachineStateException;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple finite state machine implementation that is basically a structured enum type.
 *
 *<p>
 *     Each state machine has a number of states and each state a number of valid transitions to other state. 
 *</p>
 */
public class StateMachine
    extends AbstractTopLevelModel
{
    /**
     * Reserved symbolic name for the start state.
     */
    public static final String START = "START";

    /**
     * Name of the start state
     */
    private String startState;

    /**
     * Maps state names to a set of valid states names that can be transitioned from them.
     */
    private Map<String,Set<String>> stateSets;

    /**
     * Maps state names to a set of valid states names that can be transitioned from them.
     */
    private Map<String,List<String>> stateLists;


    private String description;


    @Override
    @JSONProperty(priority = 100, ignoreIfNull = true)
    public String getName()
    {
        return super.getName();
    }


    @Override
    public void setName(String name)
    {
        super.setName(name);
    }


    /**
     * Returns the map of all states to valid state names to transition to.
     *
     * @return map of all states
     */
    @JSONProperty(priority = 70)
    public Map<String, List<String>> getStates()
    {
        return stateLists;
    }


    /**
     * Sets the map of all states to valid state names to transition to.
     *
     * @param states map of all states
     */
    @JSONTypeHint(List.class)
    public void setStates(Map<String, List<String>> states)
    {
        this.stateLists = states;

        Map<String, Set<String>> map = Maps.newHashMapWithExpectedSize(states.size());

        for (Map.Entry<String, List<String>> e : states.entrySet())
        {
            map.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        this.stateSets = map;
    }


    /**
     * Returns <code>true</code> if there is a transition from the given source to the given target state.
     *
     * @param from      source state
     * @param to        target state
     *
     * @return <code>true</code> if valid transition
     *
     * @throws InvalidMachineStateException if <code>from</code> or <code>to</code> are not valid state names
     */
    public boolean isValidTransition(String from, String to)
    {
        final Set<String> state = getState(from);
        getState(to);

        return state.contains(to);
    }

    /**
     * Returns the set of valid transitions for the given state
     *
     * @param name  state
     *
     * @return set of valid transitions
     *
     * @throws InvalidMachineStateException if <code>name</code> is not a valid state name
     */

    private Set<String> getState(String name)
    {
        final Set<String> state = stateSets.get(name);
        if (state == null)
        {
            throw new InvalidMachineStateException("'" + name + "' is not a valid state in " + this);
        }
        return state;
    }


    /**
     * Returns the start state for this state machine.
     *
     * @return start state
     */
    @JSONProperty(priority = 80)
    public String getStartState()
    {
        return startState;
    }


    /**
     * Sets the start state for this state machine.
     *
     * @param startState    start state
     */
    public void setStartState(String startState)
    {
        this.startState = startState;
    }

    @PostConstruct
    public void validate()
    {
        getState(startState);

        final Set<String> visited = walkTransitionsRecursive(startState, new HashSet<>());

        final boolean startIsSTART = startState.equals(START);

        for (Map.Entry<String, Set<String>> e : stateSets.entrySet())
        {

            final String stateName = e.getKey();
            final Set<String> transitionsTo = e.getValue();

            if (!startIsSTART && stateName.equals(START))
            {
                throw new InvalidMachineStateException("Only the start state of a state machine can be called START. (START is a symbolic name for the actual start state in a state machine)");
            }

            transitionsTo.forEach(this::getState);

            if (!visited.contains(stateName))
            {
                throw new InvalidMachineStateException("State '" + stateName + "' is not reachable from start state '" + startState+ "'");
            }
        }
    }


    private Set<String> walkTransitionsRecursive(String state, Set<String> visited)
    {
        if (!visited.contains(state))
        {
            visited.add(state);

            for (String to : stateSets.get(state))
            {
                walkTransitionsRecursive(to, visited);
            }
        }
        return visited;
    }


    @Override
    public <I, O> O accept(TopLevelModelVisitor<I, O> visitor, I in)
    {
        return visitor.visit(this, in);
    }


    @JSONProperty(priority = 90, ignoreIfNull = true)
    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }
}
