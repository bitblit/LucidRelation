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
public class SaltedHashingModifier implements IStringModifier {
    private String salt;
    private String prefix;

    public SaltedHashingModifier() {
    }

    public SaltedHashingModifier(String salt, String prefix) {
        this.salt = salt;
        this.prefix = prefix;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String modify(String body) {
        if (salt == null) {
            throw new IllegalArgumentException("Salt cannot be null");
        }
        String hash = DigestUtils.shaHex((salt + body).getBytes());
        return (prefix == null) ? hash : prefix + hash;
    }


}
