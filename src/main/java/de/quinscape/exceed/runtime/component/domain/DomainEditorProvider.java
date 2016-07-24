package de.quinscape.exceed.runtime.component.domain;

import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataProvider;
import de.quinscape.exceed.runtime.component.SelectOption;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.view.DataProviderContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainEditorProvider
    implements DataProvider
{
    private final StorageConfigurationRepository storageConfigurationRepository;
    private final ApplicationService applicationService;


    public DomainEditorProvider(StorageConfigurationRepository storageConfigurationRepository, ApplicationService
        applicationService)
    {
        this.storageConfigurationRepository = storageConfigurationRepository;
        this.applicationService = applicationService;
    }


    @Override
    public Map<String, Object> provide(DataProviderContext dataProviderContext, ComponentModel componentModel,
                                       Map<String, Object> vars)
    {
        Map<String, Object> map = new HashMap<>();
        List<SelectOption> options = new ArrayList<>();

        final RuntimeContext runtimeContext = dataProviderContext.getRuntimeContext();
        options.add(new SelectOption(runtimeContext.getTranslator().translate(runtimeContext, "Default Storage"), null));
        for (String name : storageConfigurationRepository.getConfigurationNames())
        {
            options.add(new SelectOption(name, name));
        }


        final DomainService domainService = dataProviderContext.getRuntimeContext().getDomainService();

        dataProviderContext.registerTranslations(domainService.getDomainType("DomainPropertyModel"));
        dataProviderContext.registerTranslations(domainService.getDomainType("DomainTypeModel"));
        dataProviderContext.registerTranslations(domainService.getDomainType("EnumTypeModel"));
        dataProviderContext.registerTranslations(domainService.getDomainType("ForeignKeyDefinition"));

        map.put("storageOptions", options);
        map.put("domainVersion", applicationService.getApplicationState(runtimeContext.getApplicationModel().getName()).getDomainVersion());
        return map;
    }
}
