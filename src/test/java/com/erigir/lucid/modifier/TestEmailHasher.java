package com.erigir.lucid.modifier;


import org.junit.Test;
import static org.junit.Assert.*;

public class TestEmailHasher
{
    private EmailHasher eh = new EmailHasher("monosodiumglutamate");

    @Test
    public void testFindAndHash()
    {
        assertEquals("f7ceecd8c24e4aabaafac1ced37072bc@example.com",eh.modify("\"Fred Bloggs\"@example.com"));
        assertEquals("79b8809a33591c6ad224ebc9525c7571@chucknorris.com>",eh.modify("Chuck Norris <gmail@chucknorris.com>"));
        assertEquals("37a808c70bef76de2bd516cd51d5a220@müller.de",eh.modify("webmaster@müller.de"));
        assertEquals("cf4706a3d5f2172b193a1bd7a6df8e6a@78.47.122.114",eh.modify("matteo@78.47.122.114"));

    }


    @Test
    public void testMultiple()
    {
        assertEquals("f7ceecd8c24e4aabaafac1ced37072bc@example.com and 37a808c70bef76de2bd516cd51d5a220@müller.de walked into a bar...",eh.modify("\"Fred Bloggs\"@example.com and webmaster@müller.de walked into a bar..."));
        assertEquals("f7ceecd8c24e4aabaafac1ced37072bc@example.com and 37a808c70bef76de2bd516cd51d5a220@müller.de walked into a bar...",eh.modify("\"Fred Bloggs\"@example.com and webmaster@müller.de walked into a bar..."));
    }


    @Test
    public void testMiss()
    {

        assertEquals("user@.invalid.com",eh.modify("user@.invalid.com"));

    }

}