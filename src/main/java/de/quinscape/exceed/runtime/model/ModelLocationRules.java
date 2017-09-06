package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.DomainEditorViews;
import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.property.PropertyTypeModel;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.domain.type.QueryTypeModel;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the rules to turn a relative path of a JSON resource into the model type to parse that resource into.
 */
public class ModelLocationRules
{
    private final List<ModelLocationRule> modelLocationRules;

    public static final String CONFIG_MODEL_NAME = "/models/config.json";

    public static final String ROUTING_MODEL_NAME = "/models/routing.json";

    public static final String DOMAIN_MODEL_PREFIX = "/models/domain/";

    public static final String DOMAIN_PROPERTY_MODEL_PREFIX = "/models/domain/property/";

    public static final String ENUM_MODEL_PREFIX = "/models/domain/enum/";

    public static final String DOMAIN_VERSION_PREFIX = "/models/domain/version/";

    public static final String LAYOUT_MODEL_PREFIX = "/models/layout/";

    public static final String VIEW_MODEL_PREFIX = "/models/view/";

    public static final String PROCESS_MODEL_PREFIX = "/models/process/";

    public static final String PROCESS_VIEW_MODEL_PATTERN = "/models/process/*/view";

    public static final String DOMAIN_LAYOUT_NAME = "/layout/domain.json";

    public static final String RULE_MODEL_PREFIX = "/models/domain/rule/";

    public static final String DOMAIN_STATE_PREFIX = "/models/domain/state/";
    
    public static final String QUERY_TYPE_PREFIX = "/models/domain/query/";

    public static final String STATE_MACHINE_PREFIX = "/models/domain/state/";

    public ModelLocationRules()
    {

        this.modelLocationRules = new ArrayList<>(
            Arrays.asList(
                new ModelLocationRule( CONFIG_MODEL_NAME, Model.getType(ApplicationConfig.class)),
                new ModelLocationRule( ROUTING_MODEL_NAME, Model.getType(RoutingTable.class)),
                new ModelLocationRule( DOMAIN_VERSION_PREFIX, Model.getType(DomainVersion.class)),
                new ModelLocationRule( DOMAIN_PROPERTY_MODEL_PREFIX, Model.getType(PropertyTypeModel.class)),
                new ModelLocationRule( ENUM_MODEL_PREFIX, Model.getType(EnumType.class)),
                new ModelLocationRule( RULE_MODEL_PREFIX, Model.getType(DomainRule.class)),
                new ModelLocationRule( QUERY_TYPE_PREFIX, Model.getType(QueryTypeModel.class)),
                new ModelLocationRule( STATE_MACHINE_PREFIX, Model.getType(StateMachine.class)),
                new ModelLocationRule( DOMAIN_MODEL_PREFIX, Model.getType(DomainTypeModel.class)),
                new ModelLocationRule( VIEW_MODEL_PREFIX, Model.getType(View.class)),
                new ModelLocationRule( LAYOUT_MODEL_PREFIX, Model.getType(LayoutModel.class)),
                new ModelLocationRule( PROCESS_VIEW_MODEL_PATTERN, Model.getType(View.class)),
                new ModelLocationRule( PROCESS_MODEL_PREFIX, Model.getType(Process.class)),
                new ModelLocationRule( DOMAIN_LAYOUT_NAME, Model.getType(DomainEditorViews.class))
            )
        );
    }

    public void addRule(ModelLocationRule rule)
    {
        modelLocationRules.add(rule);
    }


    public List<ModelLocationRule> getRules()
    {
        return modelLocationRules;
    }

    public Class<TopLevelModel> matchType(String path)
    {
        for (ModelLocationRule rule : modelLocationRules)
        {
            if (rule.matches(path))
            {
                final String type = rule.getType();
                return Model.getType(type);
            }
        }
        return null;
    }
}
