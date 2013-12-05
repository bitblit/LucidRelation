package com.erigir.lucid.modifier;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrote this because to get really good email detection is a subject of intense debate and may even
 * be beyond the power of regular expressions (dont even get me started on ICANN allowing special symbols in them
 * I'm gonna use the following logic:
 * 1) Find an @ symbol (everyone agrees this is part of an email address)
 * 2) Scan forward until you find whitespace
 * 3) Looking backward from whitespace, there must be at least 2 characters until the first period
 * 4) There must be at least 1 period between @ and whitespace
 * 5) The period can't be the first character
 *
 * If you reached here, then scan backward!
 * 1) If the first character is a double quote, just jump back to the previous doublequote
 * 2) Otherwise, scan back to the first whitespace
 *
 * At this point what lies between is the email.  If any rule breaks, then repeat, looking for the next @ symbol
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:43 PM
 */
public class EmailStringFinder implements IStringFinder {
    private static final Logger LOG = LoggerFactory.getLogger(EmailStringFinder.class);

    public MatchLocation find(final String body) {
        MatchLocation rval = null;
        if (body!=null)
        {
            rval = findNext(body,0);
        }

        return rval;
    }

    private MatchLocation findNext(String input, int startingFrom)
    {
        MatchLocation rval = null;
        if (input!=null)
        {

            int nextAt = input.indexOf("@",startingFrom);

            if (nextAt>0) // need at least 1 character before the @
            {
                // Scan forward to whitespace
                int nextWhitespace = findNextWhitespaceOrEnd(input, nextAt, 1);
                if (nextWhitespace>(nextAt+4)) // bare minimum of 4 characters from @ till end
                {
                    String domain = input.substring(nextAt+1, nextWhitespace);
                    // No empty sections allowed in domains
                    if (validDomainName(domain))
                    {
                        // If we reached here its a value domain!
                        // Scan backwards
                        int startPoint = 0;
                        if (input.charAt(nextAt-1)=='"')
                        {
                            // The double quote case
                            startPoint = input.lastIndexOf('"', nextAt-2);
                        }
                        else
                        {
                            // Scan for whitespace
                            startPoint = findNextWhitespaceOrEnd(input, nextAt, -1);
                            if (startPoint>0)
                            {
                                startPoint++; // if not start-of-string, we dont want the whitespace character
                            }
                        }

                        // We've found one!
                        rval = new MatchLocation(startPoint, nextWhitespace);
                    }
                }
            }

            if (rval==null && nextAt>-1)
            {
                // We found an at, but not an email address.  Scan further forward
                rval = findNext(input, nextAt+1);
            }

        }

        if (rval!=null && input!=null && rval.getEnd()>0 && rval.getStart()>=0) // catch the bracketed case
        {
            if (input.charAt(rval.getStart())=='<' && input.charAt(rval.getEnd()-1)=='>')
            {
                rval.setStart(rval.getStart()+1);
                rval.setEnd(rval.getEnd()-1);
            }

        }

        return rval;
    }

    public boolean validDomainName(String domain)
    {
        boolean rval = (domain.contains(".") && !domain.contains("..") && !domain.startsWith(".") && !domain.endsWith("."));

        for (int i=0;i<domain.length() && rval;i++)
        {
            char c = domain.charAt(i);
            rval = Character.isLetterOrDigit(c) || c=='.' || (i==domain.length()-1 && c=='>'); // ok, the last one is pretty kludgy
        }

        if (!rval)
        {
            LOG.debug("{} is not a valid domain - rejecting", domain);
        }

        return rval;

    }



    private int findNextWhitespaceOrEnd(String input, int startFrom, int step)
    {
        int rval = startFrom;
        while (input!=null && rval<input.length() && rval>0 && !Character.isWhitespace(input.charAt(rval)))
        {
            rval +=step ;
        }
        return rval;
    }


}
