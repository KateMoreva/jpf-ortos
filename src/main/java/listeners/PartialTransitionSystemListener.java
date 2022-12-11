package listeners;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.annotation.JPFOptions;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.VM;

/**
 * Add a partial state space observer to JPF and build a graph of the state
 * space that is explored by JPF, as well as any unexplored states. The graph
 * can be generated in different formats. The current formats that are supported
 * are DOT and TRA. The graph is stored in a file called
 * "jpf-state-space.extension" where extension is ".dot" or ".tra". By default
 * it generates a DOT graph.
 *
 * <p>
 * </p>
 *
 * <b>Options</b> (all keys should be prefixed with
 * {@code partialtransitionsystemlistener.}):
 * <table summary="options">
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Type</b></td>
 * <td><b>Default</b></td>
 * <td><b>Description</b></td>
 * </tr>
 * <tr>
 * <td>{@code max_new_states}</td>
 * <td>{@code Integer}</td>
 * <td>{@code 0}</td>
 * <td>The maximum amount of allowed states for this listener</td>
 * </tr>
 * </table>
 *
 * @see gov.nasa.jpf.JPFListener
 *
 * @author Richard Robinson [Implementation, Testing, Documentation]
 * @author Matt Walker [Implementation, Testing, Documentation]
 */
@JPFOptions({
		@JPFOption(type = "Int", key = "partialtransitionsystemlistener.max_new_states", defaultValue = "0", comment = "maximum states for listener") })
public class PartialTransitionSystemListener extends SearchListenerAdapter {
	private final static String CONFIG_PREFIX = "partialtransitionsystemlistener";

	private final Map<Integer, Set<Integer>> transitions;
	private final Set<Integer> unexploredStates;
	private final VM vm;
	private final int maxNewStates;

	private PrintWriter writer;

	private int source;
	private int target;
	private int newStates;
	
	private static final int SINK_STATE = -2;

	/**
	 * Creates a new PartialTransitionSystemListener instance
	 * 
	 * @param config the config properties
	 * @param jpf    the JPF instance
	 */
	public PartialTransitionSystemListener(Config config, JPF jpf) {
		this.transitions = new LinkedHashMap<>();
		this.unexploredStates = new TreeSet<>();

		this.newStates = 0;

		this.source = -1;
		this.target = -1;

		this.maxNewStates = config.getInt(CONFIG_PREFIX + ".max_new_states", 0);

		this.vm = jpf.getVM();
	}

	/**
	 * Invoked when the search is started.
	 *
	 * @implNote Creates and instantiates a {@code PrintWriter} to be used for
	 *           output. The path of the outputted file is the SUT name of the VM
	 *           concatenated with {@code .tra}.
	 *
	 * @param search the Search instance
	 */
	public void searchStarted(Search search) {
		String name = search.getVM().getSUTName() + ".tra"; //TODO revert this
		try {
			this.writer = new PrintWriter(name);
		} catch (FileNotFoundException e) {
			System.out.println("Listener could not write to file " + name);
			search.terminate();
		}
	}

	/**
	 * Invoked when a state has advanced.
	 *
	 * @implNote The output is not processed in this method. Instead, the method
	 *           adds the source and the target to a private MultiMap field (a map
	 *           whose keys are the source states and whose values are a list of all
	 *           the targets reached from the source).
	 *
	 *           <p>
	 *           </p>
	 *           In addition, this method does additional logic depending on if the
	 *           state is an end state or a new state, or if the number of new
	 *           states has been exceeded. In the latter case, the search
	 *           terminates.
	 *
	 * @param search the Search instance
	 */
	@Override
	public void stateAdvanced(Search search) {

		this.source = this.target;
		this.target = search.getStateId();

		this.transitions.computeIfAbsent(this.source, k -> new LinkedHashSet<>()).add(this.target);

		if (search.isNewState()) {
			unexploredStates.add(this.target);
		}

		if (search.isEndState()) {
			unexploredStates.remove(this.target);
		}

		if (!search.isNewState()) {
			return;
		}

		if (!this.vm.isTraceReplay()) {
			this.newStates++;
		}

		if (this.newStatesExceeded()) {
			search.notifySearchConstraintHit("New States Exceeded at: " + this.maxNewStates);
			search.terminate();
		}
	}

	/**
	 * Invoked when the search has finished.
	 *
	 * @implNote This method prints a formatted version of the transitions recorded
	 *           in the stateAdvanced method as well as the set of unexplored
	 *           states.
	 *
	 * @param search - the Search instance
	 */
	public void searchFinished(Search search) {
		for (Map.Entry<Integer, Set<Integer>> entry : transitions.entrySet()) {
			int source = entry.getKey();
			Set<Integer> targets = entry.getValue();

			for (int target : targets) {
				writer.printf("%d -> %d%n", source, target);
			}
		}

		StringJoiner sj = new StringJoiner(" ");
		for (int state : unexploredStates) {
			writer.printf("%d -> %d%n", state, SINK_STATE);
			sj.add("" + state);
		}

		writer.printf(sj.toString());
		this.writer.close();
	}

	@Override
	public void stateBacktracked(Search search) {
		this.target = search.getStateId();
	}

	@Override
	public void stateRestored(Search search) {
		this.target = search.getStateId();
	}

	/**
	 * Invoked when a state is processed
	 * 
	 * @implNote When `search.getStateId()` has been processed by JPF, it is removed
	 *           from the set of unexploredStates.
	 * 
	 * @param search - the Search instance
	 */
	@Override
	public void stateProcessed(Search search) {
		unexploredStates.remove(search.getStateId());
	}

	private boolean newStatesExceeded() {
		return this.maxNewStates > 0 && this.newStates > this.maxNewStates;
	}
}