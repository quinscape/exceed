package de.quinscape.exceed.model.context;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.scope.ScopeType;

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
 *     <li>View-Context as defined by <ViewContext/> definitions (in either view or layout)</li>
 *     <li>Process context execution contexts that are happening in a process</li>
 *     <li>Session context</li>
 *     <li>Application context</li>
 * </ul>
 *
 */
public class ScopeMetaModel
{
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
    private Map<String, ScopeDeclarations> definitions = new HashMap<>();


    public ScopeMetaModel(ApplicationModel applicationModel)
    {
        this.applicationModel = applicationModel;

        definitions.put(SYSTEM, createDefinitionMap(
            SYSTEM,
            null,
            null,
            applicationModel.getApplicationContextModel()
        ));


        definitions.put(ACTION, createDefinitionMap(ACTION,
            null,
            applicationModel.getSessionContextModel(),
            applicationModel.getApplicationContextModel()
        ));
    }


    /**
     * Adds definitions for the given process state
     *
     * @param processState process state
     */
    public void addDeclarations(ProcessState processState)
    {
        final Process process = processState.getProcess();
        final String key = key(processState);
        definitions.put(key, createDefinitionMap(
            key,
            processState instanceof ViewState ? process.getView(processState.getName()).getContextModel() : null,
            process.getContextModel(),
            applicationModel.getSessionContextModel(),
            applicationModel.getApplicationContextModel()
        ));
    }


    /**
     * Adds definitions for the given non-process view
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

        final String key = key(view);
        definitions.put(key, createDefinitionMap(
            key,
            view.getContextModel(),
            null,
            applicationModel.getSessionContextModel(),
            applicationModel.getApplicationContextModel()
        ));
    }


    private String key(ProcessState processState)
    {
        final Process process = processState.getProcess();
        return Process.getProcessViewName(process.getName(), processState.getName());
    }


    private String key(View view)
    {
        return view.getName();
    }


    private ScopeDeclarations createDefinitionMap(String key, ContextModel... contexts)
    {
        final ScopeDeclarations scopeDeclarations = new ScopeDeclarations(key);

        for (int i = 0, contextsLength = contexts.length; i < contextsLength; i++)
        {
            ContextModel context = contexts[i];

            if (context != null)
            {
                final ScopeType scopeType = ScopeType.values()[i];
                scopeDeclarations.add(context, scopeType);
            }
        }

        return scopeDeclarations;
    }


    /**
     * Returns the valid scope declarations for the given process state
     *
     * @param processState      process state
     * @return
     */
    public ScopeDeclarations lookup(ProcessState processState)
    {
        final String key = key(processState);
        return lookup(key);
    }


    /**
     * Returns the valid scope declarations for the non-process view.
     *
     * @param view
     * @return
     */
    public ScopeDeclarations lookup(View view)
    {
        final String key = key(view);
        return lookup(key);
    }



    /**
     * Returns the valid scope declarations for the given string key.
     *
     * @param scopeKey  location identifier
     * @return
     */
    public ScopeDeclarations lookup(String scopeKey)
    {
        final ScopeDeclarations definitions = this.definitions.get(scopeKey);
        if (definitions == null)
        {
            throw new IllegalStateException("No context data for key " + scopeKey);
        }
        return definitions;
    }
}
