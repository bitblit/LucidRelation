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
public class EmailHashingModifier extends SaltedHashingModifier {
    private boolean domainMaintained = true;

    public EmailHashingModifier() {
    }

    public EmailHashingModifier(String salt, boolean domainMaintained) {
        super(salt,null);
        this.domainMaintained = domainMaintained;
    }

    @Override
    public String modify(String body) {
        String rval = super.modify(body);
        if (domainMaintained && body!=null)
        {
            int idx = body.indexOf("@");
            if (idx>-1)
            {
                rval = super.modify(body.substring(0,idx))+body.substring(idx);
            }
        }
        return rval;
    }

    public boolean isDomainMaintained() {
        return domainMaintained;
    }

    public void setDomainMaintained(boolean domainMaintained) {
        this.domainMaintained = domainMaintained;
    }
}
