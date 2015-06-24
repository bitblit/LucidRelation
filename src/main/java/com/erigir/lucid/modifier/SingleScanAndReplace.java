package com.erigir.lucid.modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:43 PM
 */
public class SingleScanAndReplace implements IScanAndReplace {
    private static final Logger LOG = LoggerFactory.getLogger(SingleScanAndReplace.class);

    private IStringFinder finder;
    private IStringModifier modifier;

    public SingleScanAndReplace() {
    }

    public SingleScanAndReplace(IStringFinder finder, IStringModifier modifier) {
        this.finder = finder;
        this.modifier = modifier;
    }


    public IStringModifier getModifier() {
        return modifier;
    }

    public void setModifier(IStringModifier modifier) {
        this.modifier = modifier;
    }

    public IStringFinder getFinder() {
        return finder;
    }

    public void setFinder(IStringFinder finder) {
        this.finder = finder;
    }

    @Override
    public String performScanAndReplace(String value)
    {
        String rval = value;

        if (value!=null)
        {
            String work = value;
            StringBuilder sb = new StringBuilder();

            MatchLocation next = finder.find(work);
            while (next!=null)
            {
                if (!matchLocationValid(work, next))
                {
                    throw new IllegalStateException("Bad match location");
                }
                sb.append(work.substring(0, next.getStart()));
                sb.append(modifier.modify(work.substring(next.getStart(), next.getEnd())));
                work = (next.getEnd()==work.length())?"":work.substring(next.getEnd());
                next = finder.find(work);
            }

            if (work.length()>0)
            {
                sb.append(work); // the remainder
            }
            rval = sb.toString();

        }
        return rval;
    }

    private boolean matchLocationValid(String string, MatchLocation match)
    {
        boolean rval = true;
        if (string!=null && match!=null)
        {
            rval = match.getStart()>=0 && match.getEnd()<=string.length();
            if (!rval)
            {
                LOG.warn("Warning - invalid match found from finder {} against string '{}' - was {}", new Object[]{finder.getClass(), string, match});
            }
        }

        return rval;
    }

}
