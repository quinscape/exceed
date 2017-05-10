package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.domain.DomainProperty;
import org.svenson.info.JSONClassInfo;

import java.util.Map;

public class DataGraphUtil
{
    public Map<String,DomainProperty> columnsFromModel(Model model)
    {
        final JSONClassInfo classInfo = JSONUtil.OBJECT_SUPPORT.createClassInfo(model.getClass());

        return null;
    }
}
