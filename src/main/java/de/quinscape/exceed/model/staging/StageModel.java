package de.quinscape.exceed.model.staging;

import de.quinscape.exceed.model.AbstractTopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.annotation.DocumentedSubTypes;
import de.quinscape.exceed.model.annotation.MergeStrategy;
import de.quinscape.exceed.model.merge.ModelMergeMode;
import org.svenson.JSONProperty;
import org.svenson.JSONTypeHint;

import java.util.Map;

@MergeStrategy(ModelMergeMode.DEEP)
public class StageModel
    extends AbstractTopLevelModel
{

    private Map<String, DataSourceModel> dataSourceModels;


    public Map<String, DataSourceModel> getDataSourceModels()
    {
        return dataSourceModels;
    }

    /**
     * Contain models for the data-sources of the application.
     * 
     * @param dataSourceModels
     */
    @JSONProperty("dataSources")
    @JSONTypeHint(AbstractDataSourceModel.class)
    @DocumentedSubTypes({
        JOOQDataSourceModel.class,
        AtomikosDataSourceModel.class,
        QueryTypeDataSourceModel.class
    })
    public void setDataSourceModels(
        Map<String, DataSourceModel> dataSourceModels
    )
    {
        for (Map.Entry<String, DataSourceModel> entry : dataSourceModels.entrySet())
        {
            final String name = entry.getKey();
            final DataSourceModel dataSourceModel = entry.getValue();
            dataSourceModel.setName(name);
        }

        this.dataSourceModels = dataSourceModels;
    }

    @Override
    public <I, O> O accept(TopLevelModelVisitor<I, O> visitor, I in)
    {
        return visitor.visit(this, in);
    }
}
