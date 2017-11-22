package de.quinscape.exceed.runtime.component.domain;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.SelectOption;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.resource.ResourceRoot;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.view.DataProviderContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DomainEditorProvider
    implements DataProvider
{

    private final ApplicationService applicationService;


    public DomainEditorProvider(ApplicationService applicationService)
    {
        this.applicationService = applicationService;
    }


    @Override
    public Map<String, Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel,
                                       Map<String, Object> vars)
    {
        Map<String, Object> map = new HashMap<>();
        List<SelectOption> options = new ArrayList<>();

        final RuntimeContext runtimeContext = dataProviderContext.getRuntimeContext();
        options.add(new SelectOption(runtimeContext.getTranslator().translate(runtimeContext, "Default DataSource"),
            null));
        for (String name : runtimeContext.getDomainService().getDataSources().keySet())
        {
            options.add(new SelectOption(name, name));
        }

        final DomainService domainService = runtimeContext.getDomainService();
        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();
        final String appName = applicationModel.getName();

        dataProviderContext.registerTranslations(runtimeContext, domainService.getDomainType("DomainPropertyModel"));
        dataProviderContext.registerTranslations(runtimeContext, domainService.getDomainType("DomainTypeModel"));
        dataProviderContext.registerTranslations(runtimeContext, domainService.getDomainType("EnumTypeModel"));
        dataProviderContext.registerTranslations(runtimeContext, domainService.getDomainType("ForeignKeyDefinition"));

        final List<? extends ResourceRoot> extensions = runtimeContext.getRuntimeApplication().getResourceLoader()
            .getExtensions();
        map.put("extensions",
            extensions.stream()
                .map(ExtensionInfo::new)
                .collect(Collectors.toList())
        );
        map.put("dataSourceOptions", options);
        map.put("domainVersion", applicationService.getApplicationState(appName).getDomainVersion());
        return map;
    }


    public static class ExtensionInfo
    {
        private final String name;

        private final boolean writable;


        public ExtensionInfo(ResourceRoot resourceRoot)
        {
            this.name = resourceRoot.getName();
            this.writable = resourceRoot.isWritable();
        }


        public String getName()
        {
            return name;
        }


        public boolean isWritable()
        {
            return writable;
        }
    }
}
