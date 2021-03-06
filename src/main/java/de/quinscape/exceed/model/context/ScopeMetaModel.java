package de.quinscape.exceed.model.context;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.InconsistentModelException;
import de.quinscape.exceed.runtime.scope.ScopeType;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates references to all existing scoped value declarations about all application locations.
 * <p>
 * Each application location is identified by a string key represented an application execution location.
 * </p>
 * <ul>
 *     <li>The view name of a non-process view</li>
 *     <li>"process/viewName" for a process view</li>
 *     <li>"process/stateName" for other process states</li>
 *     <li>{@link #SYSTEM} for the system context</li>
 *     <li>{@link #ACTION} for action execution</li>
 * </ul>
 *
 * <p>
 *      Each location has its own set of valid scoped context values resulting from the combination of all scopes applicable
 *      to the application location.
 * </p>
 *
 * <ul>
 *     <li>View-Context as defined by <ViewContext/> declarations (in either view or layout)</li>
 *     <li>Process context execution contexts that are happening in a process</li>
 *     <li>Session context</li>
 *     <li>Application context</li>
 * </ul>
 *
 */
public class ScopeMetaModel
{
    private final static Logger log = LoggerFactory.getLogger(ScopeMetaModel.class);

    public final static String SYSTEM = "_system/system";

    public final static String ACTION = "_system/action";


    private final ApplicationModel applicationModel;

    /**
     * Map of ScopedContextDefinition maps
     * <p>
     * qualifiedName -> scoped value name -> ScopedContextDefinition
     * <p>
     * qualifiedName = "processName/stateName" or "viewName"
     */
    private Map<String, ScopeDeclarations> declarations = new HashMap<>();
    private Collection<ScopeDeclarations> allDeclarations = Collections.unmodifiableCollection(declarations.values());

    private Definitions applicationDefinitions;


    public ScopeMetaModel(ApplicationModel applicationModel, Definitions systemDefinitions)
    {
        this.applicationModel = applicationModel;
    }

    public void init(Definitions applicationDefinitions)
    {
        this.applicationDefinitions = applicationDefinitions;

        addDeclarations(
            SYSTEM,
            applicationDefinitions,
            null,
            null,
            null,
            applicationModel.getConfigModel().getApplicationContextModel()
        );


        addDeclarations(
            ACTION,
            applicationDefinitions,
            null,
            null,
            applicationModel.getConfigModel().getUserContextModel(),
            applicationModel.getConfigModel().getSessionContextModel(),
            applicationModel.getConfigModel().getApplicationContextModel()
        );

        for (Process process : applicationModel.getProcesses().values())
        {
            final String key = process.getScopeLocation();
            addDeclarations(
                key,
                applicationDefinitions,
                null,
                process.getContextModel(),
                applicationModel.getConfigModel().getUserContextModel(),
                applicationModel.getConfigModel().getSessionContextModel(),
                applicationModel.getConfigModel().getApplicationContextModel()
            );
            
            for (ProcessState processState : process.getStates().values())
            {
                if (!(processState instanceof ViewState))
                {
                    addDeclarations(processState);
                }
            }
        }

        for (View view : applicationModel.getViews().values())
        {
            final String processName = view.getProcessName();
            if (processName != null)
            {
                final Process process = applicationModel.getProcess(processName);
                final ProcessState processState = process.getStates().get(view.getLocalName());

                if (processState == null)
                {
                    throw new InconsistentModelException("No view state with the name '" + view.getName() + " in " +
                        process);
                }
                addDeclarations(processState);
            }
            else
            {
                addDeclarations(view);
            }
        }

        if (log.isDebugEnabled())
        {
            for (ScopeDeclarations scopeDeclarations : getAllDeclarations())
            {
                log.debug("-- Location '{}':\n" +
                    "{}", scopeDeclarations.getScopeLocation(), Util.join(scopeDeclarations.getLocalDefinitions().getDefinitions().values(), "\n"));
            }
        }
    }


    /**
     * Adds declarations for the given process state
     *
     * @param processState process state
     */
    public void addDeclarations(ProcessState processState)
    {
        final Process process = processState.getProcess();
        final String key = getScopeKey(processState);
        addDeclarations(
            key,
            applicationDefinitions,
            processState instanceof ViewState ? process.getView(processState.getName()).getContextModel() : null,
            process.getContextModel(),
            applicationModel.getConfigModel().getUserContextModel(),
            applicationModel.getConfigModel().getSessionContextModel(),
            applicationModel.getConfigModel().getApplicationContextModel()
        );
    }


    /**
     * Adds declarations for the given non-process view
     *
     * @param view view
     * @throws UnsupportedOperationException if the view is a process view
     */
    public void addDeclarations(View view)
    {
        if (view.isContainedInProcess())
        {
            throw new UnsupportedOperationException("Invalid process view " + view + ". Use the addDefinitions" +
                "(ApplicationModel,ProcessState) method.");
        }

        final String scopeLocation = getScopeKey(view);
        addDeclarations(
            scopeLocation,
            applicationDefinitions, view.getContextModel(),
            null,
            applicationModel.getConfigModel().getUserContextModel(),
            applicationModel.getConfigModel().getSessionContextModel(),
            applicationModel.getConfigModel().getApplicationContextModel()
        );
    }


    /**
     * Adds the given context declarations to the given scope location. This method is mostly for internal use and tests.
     * The given context models must be in the order defined by {@link ScopeType}, replacing <code>null</code> for
     * missing locations.
     *  @param scopeLocation     scope location
     * @param applicationDefinitions
     * @param contexts          context varargs. arguments
     */
    private void addDeclarations(String scopeLocation, Definitions applicationDefinitions, ContextModel... contexts)
    {
        if (contexts.length > ScopeType.LAYOUT.ordinal())
        {
            throw new IllegalArgumentException("There should be at most " + ScopeType.APPLICATION.ordinal() + " context models");
        }

        final ScopeDeclarations scopeDeclarations = new ScopeDeclarations(scopeLocation, contexts, applicationDefinitions);

        declarations.put(scopeLocation, scopeDeclarations);
    }

    /**
     * Returns the valid scope declarations for the given string key.
     *
     * @param scopeKey  location identifier
     * @return
     */
    public ScopeDeclarations lookup(String scopeKey)
    {
        final ScopeDeclarations definitions = this.declarations.get(scopeKey);
        if (definitions == null)
        {
            throw new IllegalStateException("No context data for key " + scopeKey);
        }
        return definitions;
    }

    public static String getScopeKey(ProcessState processState)
    {
        return processState.getProcess().getProcessStateName(processState.getName());
    }

    public static String getScopeKey(View view)
    {
        return view.getName();
    }

    public Collection<ScopeDeclarations> getAllDeclarations()
    {
        return allDeclarations;
    }
}
