package com.erigir.lucid.modifier;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class TestScanAndReplace
{
    private SingleScanAndReplace ssnScanner = new SingleScanAndReplace(RegexStringFinder.SSN_FINDER, new SaltedHashingModifier("monosodiumglutamate"));
    private SingleScanAndReplace ccardScanner = new SingleScanAndReplace(RegexStringFinder.CREDIT_CARD_FINDER, new SaltedHashingModifier("monosodiumglutamate"));
    private SingleScanAndReplace emailScanner = new SingleScanAndReplace(new EmailStringFinder(), new EmailHashingModifier("monosodiumglutamate",true));
    private AtomicLong counter = new AtomicLong(0);
    private SingleScanAndReplace emailCountScanner = new SingleScanAndReplace(new EmailStringFinder(), new CountingStringModifier("EMAIL:",counter));

    @Test
    public void testFindAndHashCreditCard()
    {
        assertEquals("e0e0f870762bfe5e71135d887df857c2359514e1", ccardScanner.performScanAndReplace("5555-5555-5555-5555"));
        assertEquals("751d12d2301d2644df38d25092e8203f98608c13", ccardScanner.performScanAndReplace("2222222222222222"));
        assertEquals("3cbecbcce06300605a952b374438b1ab811fec2d", ccardScanner.performScanAndReplace("1111 1111 1111 1111"));
    }

    @Test
    public void testMultipleCreditCard()
    {
        assertEquals("My card numbers are e0e0f870762bfe5e71135d887df857c2359514e1, 751d12d2301d2644df38d25092e8203f98608c13, and 3cbecbcce06300605a952b374438b1ab811fec2d. Please dont use them", ccardScanner.performScanAndReplace("My card numbers are 5555-5555-5555-5555, 2222222222222222, and 1111 1111 1111 1111. Please dont use them"));
    }


    @Test
    public void testFindAndHashSSN()
    {
        assertEquals("0a7764558a62cb0ad1fdd86e865ae3880064f8ed", ssnScanner.performScanAndReplace("555-55-5555"));
        assertEquals("cc4a09bc095932b26c8b5a46725fd261c9a18c01", ssnScanner.performScanAndReplace("111/11/1111"));

    }


    @Test
    public void testMultipleSSN()
    {
        assertEquals("0a7764558a62cb0ad1fdd86e865ae3880064f8ed and cc4a09bc095932b26c8b5a46725fd261c9a18c01 walked into a bar...", ssnScanner.performScanAndReplace("555-55-5555 and 111/11/1111 walked into a bar..."));
    }


    @Test
    public void testMissSSN()
    {
        assertEquals("111-11 1111", ssnScanner.performScanAndReplace("111-11 1111"));

    }

    @Test
    public void testFindAndHash()
    {
        assertEquals("5fda182aac7cf34273988e0e7313788fed36bdaa@example.com", emailScanner.performScanAndReplace("\"Fred Bloggs\"@example.com"));
        assertEquals("Chuck Norris <5b65955f532b4be8781237973e98ec99abbebbc5@chucknorris.com>", emailScanner.performScanAndReplace("Chuck Norris <gmail@chucknorris.com>"));
        assertEquals("d7befd0884737e724745200763c7458703edb5cd@müller.de", emailScanner.performScanAndReplace("webmaster@müller.de"));
        assertEquals("47053cc4fe42ee50516cce8eb3c8ee722fb0f4cb@78.47.122.114", emailScanner.performScanAndReplace("matteo@78.47.122.114"));

    }

    @Test
    public void testFindAndHashCounter()
    {
        assertEquals("EMAIL:0", emailCountScanner.performScanAndReplace("\"Fred Bloggs\"@example.com"));
        assertEquals("Chuck Norris <EMAIL:1>", emailCountScanner.performScanAndReplace("Chuck Norris <gmail@chucknorris.com>"));
        assertEquals("EMAIL:2", emailCountScanner.performScanAndReplace("webmaster@müller.de"));
        assertEquals("EMAIL:3", emailCountScanner.performScanAndReplace("matteo@78.47.122.114"));

    }


    @Test
    public void testMultiple()
    {
        assertEquals("5fda182aac7cf34273988e0e7313788fed36bdaa@example.com and d7befd0884737e724745200763c7458703edb5cd@müller.de walked into a bar...", emailScanner.performScanAndReplace("\"Fred Bloggs\"@example.com and webmaster@müller.de walked into a bar..."));
        assertEquals("5fda182aac7cf34273988e0e7313788fed36bdaa@example.com and d7befd0884737e724745200763c7458703edb5cd@müller.de walked into a bar...", emailScanner.performScanAndReplace("\"Fred Bloggs\"@example.com and webmaster@müller.de walked into a bar..."));
    }


    @Test
    public void testMiss()
    {

        assertEquals("user@.invalid.com", emailScanner.performScanAndReplace("user@.invalid.com"));

    }


}