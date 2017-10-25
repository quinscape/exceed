package de.quinscape.exceed.runtime.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class UtilTestCase
{
    private final static Logger log = LoggerFactory.getLogger(UtilTestCase.class);

    private final Pattern BASE32 = Pattern.compile("^[0-7][0-9a-v]{25}$");

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

        assertThat(
            Util.base32(
                0L,
                0L
            )
            , is("00000000000000000000000000"));
        assertThat(
            Util.base32(
                -1L,
                -1L
            )
            // we run out of bits after 25 * 5 = 125 bits, leaving only 3 bits => 7
            , is("7vvvvvvvvvvvvvvvvvvvvvvvvv"));

        // alternating in groups of 5 bits
        assertThat(
            Util.base32(
                0b0000111110000011111000001111100000111110000011111000001111100000L,
                0b1110000011111000001111100000111110000011111000001111100000111110L
            )
        , is("70v0v0v0v0v0v0v0v0v0v0v0v0"));

        // only carry-over bits set
        assertThat(
            Util.base32(
                0xf000000000000000L,
                1
            )
        , is("0000000000000v000000000000"));



        for (int i = 0 ; i < 1000; i++)
        {
            assertThat(BASE32.matcher(Util.base32UUID()).matches(), is(true));
        }
    }
}
