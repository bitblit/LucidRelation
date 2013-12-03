package com.erigir.lucid.modifier;


import org.junit.Test;
import static org.junit.Assert.*;

public class TestEmailHasher
{
    private EmailHasher eh = new EmailHasher("monosodiumglutamate");

    @Test
    public void testFindAndHash()
    {
        assertEquals("5fda182aac7cf34273988e0e7313788fed36bdaa@example.com",eh.modify("\"Fred Bloggs\"@example.com"));
        assertEquals("a3292418335605cddc0a97f8340130412a329e08@chucknorris.com>",eh.modify("Chuck Norris <gmail@chucknorris.com>"));
        assertEquals("d7befd0884737e724745200763c7458703edb5cd@müller.de",eh.modify("webmaster@müller.de"));
        assertEquals("47053cc4fe42ee50516cce8eb3c8ee722fb0f4cb@78.47.122.114",eh.modify("matteo@78.47.122.114"));

    }


    @Test
    public void testMultiple()
    {
        assertEquals("5fda182aac7cf34273988e0e7313788fed36bdaa@example.com and d7befd0884737e724745200763c7458703edb5cd@müller.de walked into a bar...",eh.modify("\"Fred Bloggs\"@example.com and webmaster@müller.de walked into a bar..."));
        assertEquals("5fda182aac7cf34273988e0e7313788fed36bdaa@example.com and d7befd0884737e724745200763c7458703edb5cd@müller.de walked into a bar...",eh.modify("\"Fred Bloggs\"@example.com and webmaster@müller.de walked into a bar..."));
    }


    @Test
    public void testMiss()
    {

        assertEquals("user@.invalid.com",eh.modify("user@.invalid.com"));

    }

}