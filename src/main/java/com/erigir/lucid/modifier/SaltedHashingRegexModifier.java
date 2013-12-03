package com.erigir.lucid.modifier;


import org.apache.commons.codec.digest.DigestUtils;

/**
 * Defaults to just salted hashing the match
 * Created with IntelliJ IDEA.
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaltedHashingRegexModifier extends RegexModifier {
    private String salt;
    private String regexToFind;

    public SaltedHashingRegexModifier(String regexToFind,String salt ) {
        this.salt = salt;
        this.regexToFind = regexToFind;
    }

    public static SaltedHashingRegexModifier createSSNModifier(String salt)
    {
        return new SaltedHashingRegexModifier("\\d{3}(-|/)\\d{2}(-|/)\\d{4}",salt);
    }

    public static SaltedHashingRegexModifier createCreditCardModifier(String salt)
    {
        return new SaltedHashingRegexModifier("\\d{4}(-|\\s)?\\d{4}(-|\\s)?\\d{4}(-|\\s)?\\d{4}",salt);
        //return new SaltedHashingRegexModifier("\\d{4}(-|\\s)?\\d{4}(-|\\s)?\\d{4}(-|\\s)?\\d{4}",salt);
    }

    public String regexToFind() {
        return regexToFind;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String processMatch(String match) {
        if (salt==null)
        {
            throw new IllegalArgumentException("Salt cannot be null");
        }
        return DigestUtils.shaHex((salt + match).getBytes());
    }

}
