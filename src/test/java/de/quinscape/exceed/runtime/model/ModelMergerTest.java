package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.config.BaseTemplateConfig;
import de.quinscape.exceed.model.config.ComponentConfig;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.RoutingTable;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ModelMergerTest
{

    @Test
    public void testPrimitives() throws Exception
    {

        final ModelMerger merger = merger();

        assertThat(merger.merge("aab", "bbb"), is("bbb"));
        assertThat(merger.merge(1, 2), is(2));
        assertThat(merger.merge(false, true), is(true));
    }


    private ModelMerger merger()
    {
        return new ModelMerger(null, null, null);
    }


    @Test
    public void mergeRoutingTables() throws Exception
    {
        final ModelMerger merger = merger();

        final RoutingTable routingTableA = new RoutingTable();
        final TreeMap<String, Mapping> mappingsA = new TreeMap<>();
        mappingsA.put("/", new Mapping("Home", null));
        routingTableA.setMappings(mappingsA);

        final RoutingTable routingTableB = new RoutingTable();
        final TreeMap<String, Mapping> mappingsB = new TreeMap<>();
        mappingsB.put("/second", new Mapping("Second", null));
        routingTableB.setMappings(mappingsB);


        final RoutingTable merged = (RoutingTable) merger.merge(routingTableA, routingTableB);

        assertThat(merged.getMappings().size(), is(2));
        assertThat(merged.getMappings().get("/").getViewName(), is("Home"));
        assertThat(merged.getMappings().get("/second").getViewName(), is("Second"));

        // new mapping and new map
        assertThat(merger.getNewInstanceCount(), is((2)));
    }

    @Test
    public void mergeRoutingTables2() throws Exception
    {
        final ModelMerger merger = merger();

        final RoutingTable routingTableA = new RoutingTable();
        final TreeMap<String, Mapping> mappingsA = new TreeMap<>();
        mappingsA.put("/", new Mapping("Home", null));
        routingTableA.setMappings(mappingsA);

        final RoutingTable routingTableB = new RoutingTable();
        final TreeMap<String, Mapping> mappingsB = new TreeMap<>();
        mappingsB.put("/", new Mapping("Second", null));
        routingTableB.setMappings(mappingsB);


        final RoutingTable merged = (RoutingTable) merger.merge(routingTableA, routingTableB);

        assertThat(merged.getMappings().size(), is(1));
        assertThat(merged.getMappings().get("/").getViewName(), is("Second"));

        // new mapping and new map
        assertThat(merger.getNewInstanceCount(), is((2)));
    }


    @Test
    public void testUserMerge() throws Exception
    {
        final ApplicationConfig cfgA = new ApplicationConfig();


        final ApplicationConfig cfgB = new ApplicationConfig();
        Map<String, Set<String>> users = new HashMap<>();
        users.put("user", newSet("ROLE_XXX"));
        cfgB.setDefaultUsers(users);


        final ModelMerger merger = merger();
        final ApplicationConfig merge = merger.merge(cfgA, cfgB);

        assertThat(merge.getDefaultUsers().get("user"),is(newSet("ROLE_USER", "ROLE_XXX")));
    }

    @Test
    public void testLocalesMerge() throws Exception
    {
        final ApplicationConfig cfgA = new ApplicationConfig();
        final ApplicationConfig cfgB = new ApplicationConfig();
        cfgB.setSupportedLocales(Collections.singletonList("en"));


        final ModelMerger merger = merger();
        final ApplicationConfig merge = merger.merge(cfgA, cfgB);

        assertThat(merge.getSupportedLocales(),is(Arrays.asList("en")));
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

        final ModelMerger merger = merger();
        final ApplicationConfig merge = merger.merge(cfgA, cfgB);

        final ComponentConfig mergedComponentCfg = merge.getComponentConfig();
        assertThat(mergedComponentCfg.getBaseTemplateConfig().getHead(),is("HEAD"));
        assertThat(mergedComponentCfg.getBaseTemplateConfig().getContentAfter(),is("AFTER2"));

    }


    public <T> Set<T> newSet(T... values)
    {
        final HashSet<T> set = new HashSet<>();
        for (T value : values)
        {
            set.add(value);
        }
        return set;
    }


}
