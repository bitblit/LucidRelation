package com.erigir.lucid.modifier;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSaltedHashingRegexModifier
{
    private SaltedHashingRegexModifier ssnHasher = SaltedHashingRegexModifier.createSSNModifier("monosodiumglutamate");
    private SaltedHashingRegexModifier ccardHasher = SaltedHashingRegexModifier.createCreditCardModifier("monosodiumglutamate");

    @Test
    public void testFindAndHashCreditCard()
    {
        assertEquals("e0e0f870762bfe5e71135d887df857c2359514e1",ccardHasher.modify("5555-5555-5555-5555"));
        assertEquals("751d12d2301d2644df38d25092e8203f98608c13",ccardHasher.modify("2222222222222222"));
        assertEquals("3cbecbcce06300605a952b374438b1ab811fec2d",ccardHasher.modify("1111 1111 1111 1111"));
    }

    @Test
    public void testMultipleCreditCard()
    {
        assertEquals("My card numbers are e0e0f870762bfe5e71135d887df857c2359514e1, 751d12d2301d2644df38d25092e8203f98608c13, and 3cbecbcce06300605a952b374438b1ab811fec2d. Please dont use them",ccardHasher.modify("My card numbers are 5555-5555-5555-5555, 2222222222222222, and 1111 1111 1111 1111. Please dont use them"));
    }


    @Test
    public void testFindAndHashSSN()
    {
        assertEquals("0a7764558a62cb0ad1fdd86e865ae3880064f8ed",ssnHasher.modify("555-55-5555"));
        assertEquals("cc4a09bc095932b26c8b5a46725fd261c9a18c01",ssnHasher.modify("111/11/1111"));

    }


    @Test
    public void testMultipleSSN()
    {
        assertEquals("0a7764558a62cb0ad1fdd86e865ae3880064f8ed and cc4a09bc095932b26c8b5a46725fd261c9a18c01 walked into a bar...",ssnHasher.modify("555-55-5555 and 111/11/1111 walked into a bar..."));
    }


    @Test
    public void testMissSSN()
    {
        assertEquals("111-11 1111",ssnHasher.modify("111-11 1111"));

    }

}