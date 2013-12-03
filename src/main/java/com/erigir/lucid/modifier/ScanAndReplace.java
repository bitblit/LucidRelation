package com.erigir.lucid.modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:43 PM
 */
public class ScanAndReplace  {
    private static final Logger LOG = LoggerFactory.getLogger(ScanAndReplace.class);

    private IStringFinder finder;
    private IStringModifier modifier;

    public ScanAndReplace() {
    }

    public ScanAndReplace(IStringFinder finder, IStringModifier modifier) {
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


}
