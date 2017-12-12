package de.quinscape.exceed.runtime.model;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.config.BaseTemplateConfig;
import de.quinscape.exceed.model.config.ComponentConfig;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.staging.DataSourceModel;
import de.quinscape.exceed.model.staging.JOOQDataSourceModel;
import de.quinscape.exceed.model.staging.StageModel;
import de.quinscape.exceed.model.staging.SystemDataSourceModel;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static de.quinscape.exceed.runtime.model.ModelMerger.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ModelMergerTest
{

    @Test
    public void mergePrimitives() throws Exception
    {
        assertThat( merge("aaa", null), is("aaa"));
        assertThat( merge(null, "aaa"), is("aaa"));
        assertThat( merge("aab", "bbb"), is("bbb"));
        assertThat( merge("bbb", "aab"), is("aab"));
        assertThat( merge(1, 2), is(2));
        assertThat( merge(2, 1), is(1));
        assertThat( merge(false, true), is(true));
        assertThat( merge(true, false), is(false));
    }



    @Test
    public void mergeRoutingTables() throws Exception
    {

        final RoutingTable routingTableA = new RoutingTable();
        final TreeMap<String, Mapping> mappingsA = new TreeMap<>();
        mappingsA.put("/", new Mapping("Home", null));
        routingTableA.setMappings(mappingsA);

        final RoutingTable routingTableB = new RoutingTable();
        final TreeMap<String, Mapping> mappingsB = new TreeMap<>();
        mappingsB.put("/second", new Mapping("Second", null));
        routingTableB.setMappings(mappingsB);


        final RoutingTable merged = merge(routingTableA, routingTableB);

        assertThat(merged.getMappings().size(), is(2));
        assertThat(merged.getMappings().get("/").getViewName(), is("Home"));
        assertThat(merged.getMappings().get("/second").getViewName(), is("Second"));

    }

    @Test
    public void overwriteRoutingEntry() throws Exception
    {
        final RoutingTable routingTableA = new RoutingTable();
        final TreeMap<String, Mapping> mappingsA = new TreeMap<>();
        mappingsA.put("/", new Mapping("Home", null));
        routingTableA.setMappings(mappingsA);

        final RoutingTable routingTableB = new RoutingTable();
        final TreeMap<String, Mapping> mappingsB = new TreeMap<>();
        mappingsB.put("/", new Mapping("Second", null));
        routingTableB.setMappings(mappingsB);


        final RoutingTable merged = merge(routingTableA, routingTableB);

        assertThat(merged.getMappings().size(), is(1));
        assertThat(merged.getMappings().get("/").getViewName(), is("Second"));

    }


    @Test
    public void testUserMerge() throws Exception
    {
        final ApplicationConfig cfgA = new ApplicationConfig();


        final ApplicationConfig cfgB = new ApplicationConfig();
        Map<String, Set<String>> users = new HashMap<>();
        users.put("user", newSet("ROLE_XXX"));
        cfgB.setDefaultUsers(users);


        final ApplicationConfig merge = merge(cfgA, cfgB);

        assertThat(merge.getDefaultUsers().get("user"),is(newSet("ROLE_USER", "ROLE_XXX")));
    }

    @Test
    public void testLocalesMerge() throws Exception
    {
        final ApplicationConfig cfgA = new ApplicationConfig();
        final ApplicationConfig cfgB = new ApplicationConfig();
        cfgB.setSupportedLocales(Collections.singletonList("en"));


        final ApplicationConfig mergedConfig = merge(cfgA, cfgB);

        assertThat(mergedConfig.getSupportedLocales(),is(Arrays.asList("en")));
    }


    @Test
    public void testBaseTemplateConfigMerge() throws Exception
    {

        final ApplicationConfig cfgA = new ApplicationConfig();
        final ApplicationConfig cfgB = new ApplicationConfig();
        final ComponentConfig componentConfigA = new ComponentConfig();
        final ComponentConfig componentConfigB = new ComponentConfig();

        BaseTemplateConfig basetTemplA = new BaseTemplateConfig();
        BaseTemplateConfig basetTemplB = new BaseTemplateConfig();
        componentConfigA.setBaseTemplateConfig(basetTemplA);
        componentConfigB.setBaseTemplateConfig(basetTemplB);


        basetTemplA.setHead("HEAD");
        basetTemplA.setContentAfter("AFTER");
        basetTemplB.setContentAfter("AFTER2");

        cfgA.setComponentConfig(componentConfigA);
        cfgB.setComponentConfig(componentConfigB);

        final ComponentConfig mergedComponentCfg = merge(cfgA, cfgB).getComponentConfig();
        assertThat(mergedComponentCfg.getBaseTemplateConfig().getHead(),is("HEAD"));
        assertThat(mergedComponentCfg.getBaseTemplateConfig().getContentAfter(),is("AFTER2"));

    }


    @Test
    public void testDataSourceMerging() throws Exception
    {

        final JOOQDataSourceModel modelA = new JOOQDataSourceModel();

        modelA.setNamingStrategyName("aaa");
        modelA.setDomainOperationsName("bbb");
        modelA.setSchemaServiceName("ccc");

        final JOOQDataSourceModel modelB = new JOOQDataSourceModel();

        final SystemDataSourceModel systemDataSourceModel = new SystemDataSourceModel();

        final StageModel stageA = new StageModel();
        final StageModel stageB = new StageModel();
        stageA.setDataSourceModels(ImmutableMap.of("test", modelA, "systemDataSource", systemDataSourceModel));
        stageB.setDataSourceModels(ImmutableMap.of("test", modelB));

        final StageModel mergedModel = merge(stageA, stageB);

        final JOOQDataSourceModel mergedSrc = (JOOQDataSourceModel) mergedModel.getDataSourceModels().get("test");
        assertThat(mergedSrc.getNamingStrategyName(), is("aaa"));
        assertThat(mergedSrc.getDomainOperationsName(), is("bbb"));
        assertThat(mergedSrc.getSchemaServiceName(), is("ccc"));

        final DataSourceModel mergedSrc2 = mergedModel.getDataSourceModels().get("systemDataSource");
        assertThat(mergedSrc2,instanceOf(SystemDataSourceModel.class));
    }


    @Test
    public void mergeEmptyLists()
    {
            final BeanWithMergedList configA = new BeanWithMergedList();
            final BeanWithMergedList configB = new BeanWithMergedList();
            final BeanWithMergedList merged = merge(configA,configB);

            assertThat(merged.getValues(), is(Collections.emptyList()));
    }

    @Test
    public void mergeInValue()
    {
        final BeanWithMergedList configA = new BeanWithMergedList();
        final BeanWithMergedList configB = new BeanWithMergedList();

        configB.setValues(Collections.singletonList("myValue"));

        final BeanWithMergedList merged = merge(configA,configB);

        assertThat(merged.getValues(), is(Collections.singletonList("myValue")));
    }

    @Test
    public void mergeValues()
    {
        final BeanWithMergedList configA = new BeanWithMergedList();
        final BeanWithMergedList configB = new BeanWithMergedList();

        configA.setValues(Collections.singletonList("valueA"));
        configB.setValues(Collections.singletonList("valueB"));

        final BeanWithMergedList merged = merge(configA,configB);

        assertThat(merged.getValues(), is(Arrays.asList("valueA", "valueB")));
    }


    @SafeVarargs
    public final <T> Set<T> newSet(T... values)
    {
        final HashSet<T> set = new HashSet<>();
        Collections.addAll(set, values);
        return set;
    }
    
}
