package de.quinscape.exceed.model.state;

import de.quinscape.exceed.runtime.domain.InvalidMachineStateException;
import org.junit.Test;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.SvensonRuntimeException;
import org.svenson.tokenize.InputStreamSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * {
 "name" : "OrderStatus",
 "startState": "ACCEPTED",
 "states": {
 "ACCEPTED": [
 "READY",
 "CANCELED"
 ],
 "READY": [
 "SENT",
 "CANCELED"
 ],
 "SENT": [
 "DELIVERED",
 "LOST_ON_DELIVERY"
 ],
 "DELIVERED": [
 "PAID",
 "RETOUR",
 "LOST_ON_RETOUR"
 ],
 "RETOUR": [
 "CANCELED"
 ],
 "LOST_ON_DELIVERY": [],
 "LOST_ON_RETOUR": [],
 "PAID": [],
 "CANCELED": []
 }
 }

 */
public class StateMachineTest
{

    @Test
    public void test() throws Exception
    {
        final StateMachine machine = load("./src/test/java/de/quinscape/exceed/model/state/OrderStatus.json");

        assertThat(machine.isValidTransition("ACCEPTED", "READY"), is(true));
        assertThat(machine.isValidTransition("ACCEPTED", "LOST_ON_RETOUR"), is(false));
        assertThat(machine.isValidTransition("DELIVERED", "PAID"), is(true));
        assertThat(machine.isValidTransition("DELIVERED", "ACCEPTED"), is(false));


        assertThat(JSON.defaultJSON().forValue(machine), containsString("\"startState\":\"ACCEPTED\"," +
            "\"states\":{\"READY\":[\"SENT\",\"CANCELED\"],\"DELIVERED\":[\"PAID\",\"RETOUR\",\"LOST_ON_RETOUR\"]," +
            "\"ACCEPTED\":[\"READY\",\"CANCELED\"],\"LOST_ON_DELIVERY\":[],\"LOST_ON_RETOUR\":[],\"CANCELED\":[]," +
            "\"PAID\":[],\"RETOUR\":[\"CANCELED\"],\"SENT\":[\"DELIVERED\",\"LOST_ON_DELIVERY\"]}"));
    }


    @Test(expected = InvalidMachineStateException.class)
    public void testInvalid() throws Exception
    {
        final StateMachine machine = load("./src/test/java/de/quinscape/exceed/model/state/OrderStatus.json");
        machine.isValidTransition("INVALID", "READY");
    }

    @Test(expected = SvensonRuntimeException.class)
    public void testInvalidStart() throws Exception
    {
        load("./src/test/java/de/quinscape/exceed/model/state/ERR-InvalidStart.json");
    }

    @Test(expected = SvensonRuntimeException.class)
    public void testInvalidTransition() throws Exception
    {
        load("./src/test/java/de/quinscape/exceed/model/state/ERR-InvalidTransition.json");
    }

    @Test(expected = SvensonRuntimeException.class)
    public void testUnreachable() throws Exception
    {
        load("./src/test/java/de/quinscape/exceed/model/state/ERR-Unreachable.json");
    }


    @Test(expected = SvensonRuntimeException.class)
    public void testReservedStateNameSTART() throws Exception
    {
        load("./src/test/java/de/quinscape/exceed/model/state/ERR-Reserved.json");
    }

    @Test
    public void testReservedStateNameSTART2() throws Exception
    {
        load("./src/test/java/de/quinscape/exceed/model/state/StartNamedSTART.json");
    }


    private StateMachine load(String name) throws FileNotFoundException
    {
        return JSONParser.defaultJSONParser().parse(
            StateMachine.class,
            new InputStreamSource(
                new FileInputStream(name),
                true
            )
        );
    }
}
