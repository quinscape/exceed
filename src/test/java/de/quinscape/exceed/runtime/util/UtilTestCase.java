package de.quinscape.exceed.runtime.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class UtilTestCase
{
    private final static Logger log = LoggerFactory.getLogger(UtilTestCase.class);


    @Test
    public void testBase32() throws Exception
    {
        assertThat(Util.BASE32_ALPHABET.length, is(32));

        for (long l = 0 ; l < 10; l++)
        {
            assertThat(Util.base32(l), is(String.valueOf((char) ('0' + (char)l) )));
        }

        for (long l = 0 ; l < 22; l++)
        {
            assertThat(Util.base32(10 + l), is(String.valueOf((char) ('a' + (char)l) )));
        }

        assertThat(Util.base32( (2 << 15) + (1 << 10) + (27 << 5) + 4 ), is("21r4"));
    }
}
