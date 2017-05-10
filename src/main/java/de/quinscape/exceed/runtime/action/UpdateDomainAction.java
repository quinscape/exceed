package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.action.UpdateDomainActionModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.merge.MergeLocation;
import de.quinscape.exceed.model.merge.UpdateDomainResult;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.resource.AppResource;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UpdateDomainAction
    implements Action<UpdateDomainActionModel>
{
    private final static Logger log = LoggerFactory.getLogger(UpdateDomainAction.class);


    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ModelJSONService modelJSONService;

    /**
     * Updates the domain model of the current application with the given updates.
     *
     *
     * @param runtimeContext
     * @param model
     * @return
     * @throws ParseException
     */
    @Override
    //XXX: Synchronized to force serialization of updates. Will not be enough with clustered application contexts targeting the same DB.
    public synchronized Object execute(RuntimeContext runtimeContext, UpdateDomainActionModel model) throws ParseException
    {
        final List<MergeLocation> mergeLocations = checkMerge(runtimeContext, model);
        if (mergeLocations.size() == 0)
        {
            final Map<String, DomainType> newDomainTypes = executeResourceUpdate(runtimeContext, model);

            applicationService.resetRuntimeApplication(servletContext, runtimeContext.getApplicationModel().getName());

            return UpdateDomainResult.createSuccess(newDomainTypes);
        }
        else
        {
            final Map<String, DomainType> domainTypes = runtimeContext.getApplicationModel().getDomainTypes();
            return UpdateDomainResult.createMergeError(mergeLocations);
        }
    }


    /**
     * Executes the actual update from the given action model and collects the new version GUIDs of the created and updated
     * domain types.
     *
     * @param runtimeContext        runtime context
     * @param model                 update action model
     * @return  new GUIDs
     */
    public Map<String, DomainType> executeResourceUpdate(RuntimeContext runtimeContext, UpdateDomainActionModel model)
    {
        final Map<String, DomainType> domainTypes = runtimeContext.getApplicationModel().getDomainTypes();

        final Map<String, DomainType> newDomainTypes = new HashMap<>(domainTypes);
        for (DomainType type : model.getNewTypes())
        {
            log.debug("Update new type {}", type);

            String newVersion = updateType(runtimeContext, type);
            newDomainTypes.put(type.getName(), type);
        }

        for (DomainType type : model.getChangedTypes())
        {
            log.debug("Update changed type {}", type);

            String newVersion = updateType(runtimeContext, type);
            newDomainTypes.put(type.getName(), type);
        }


        for (DomainType type : model.getRemovedTypes())
        {
            log.debug("Delete type {}", type);

            initResource(runtimeContext, type);
            type.getResource().delete();

            newDomainTypes.remove(type.getName());
        }

        return newDomainTypes;
    }


    public List<MergeLocation> checkMerge(RuntimeContext runtimeContext, UpdateDomainActionModel model)
    {
        final List<MergeLocation> mergeLocations = new ArrayList<>();
        final Map<String, DomainType> domainTypes = runtimeContext.getApplicationModel().getDomainTypes();

        for (DomainType domainType : model.getNewTypes())
        {
            domainType.initializeGUIDs();


            final DomainType existing = domainTypes.get(domainType.getName());
            if (existing != null)
            {
              mergeLocations.add(new MergeLocation(domainType, existing));
            }
        }


        for (DomainType domainType : model.getChangedTypes())
        {
            final DomainType existing = findByIdentityGuid(domainTypes, domainType.getIdentityGUID());

            // if we have no existing type here, it was deleted by someone
            if (existing == null || !existing.getVersionGUID().equals(domainType.getVersionGUID()))
            {
                mergeLocations.add(new MergeLocation(domainType, existing));
            }
        }

        for (DomainType domainType : model.getRemovedTypes())
        {
            final DomainType existing = findByIdentityGuid(domainTypes, domainType.getIdentityGUID());

            // if it's not the version we expect, it was edited so it's a merge error
            if (existing != null && !existing.getVersionGUID().equals(domainType.getVersionGUID()))
            {
                mergeLocations.add(new MergeLocation(null, existing));
            }
        }
        return mergeLocations;
    }


    private String updateType(RuntimeContext runtimeContext, DomainType type)
    {
        initResource(runtimeContext, type);

        final String newVersion = UUID.randomUUID().toString();
        type.setVersionGUID(newVersion);

        final String json = JSONUtil.formatJSON(
            modelJSONService.toJSON(type)
        );

        final byte[] data = json.getBytes(RequestUtil.UTF_8);
        final AppResource resource = type.getResource();

        resource.write(data);

        return newVersion;
    }


    private void initResource(RuntimeContext runtimeContext, DomainType type)
    {
        final ResourceRoot resourceRoot = runtimeContext.getRuntimeApplication().getResourceLoader().getExtensions()
            .get(type.getExtension());

        if (!resourceRoot.isWritable())
        {
            throw new IllegalStateException("The extension index of " + type +" points to non-writable resource-root " + resourceRoot);
        }

        final AppResource targetResource = resourceRoot.getResource("/models/domain/" + type.getName() + ".json");

        log.debug("Target resource: {}", targetResource);

        type.setResource(targetResource);
    }



    private DomainType findByIdentityGuid(Map<String, DomainType> domainTypes, String identityGUID)
    {
        for (DomainType domainType : domainTypes.values())
        {
            if (domainType.getIdentityGUID().equals(identityGUID))
            {
                return domainType;
            }
        }
        return null;
    }


    @Override
    public Class<UpdateDomainActionModel> getActionModelClass()
    {
        return UpdateDomainActionModel.class;
    }
}
