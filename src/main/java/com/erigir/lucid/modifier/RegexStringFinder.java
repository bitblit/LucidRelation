package com.erigir.lucid.modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds a regular expression and then performs some proces on the match
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:43 PM
 */
public class RegexStringFinder implements IStringFinder {
    private static final Logger LOG = LoggerFactory.getLogger(RegexStringFinder.class);

    public static RegexStringFinder SSN_FINDER = new RegexStringFinder("\\d{3}(-|/)\\d{2}(-|/)\\d{4}");
    public static RegexStringFinder CREDIT_CARD_FINDER = new RegexStringFinder("\\d{4}(-|\\s)?\\d{4}(-|\\s)?\\d{4}(-|\\s)?\\d{4}");


    private Pattern regex;

    public RegexStringFinder(String regex) {
        setRegex(regex);
    }

    @Override
    public MatchLocation find(String body) {
        MatchLocation rval = null;

        if (body!=null)
        {
            Matcher matcher = regex.matcher(body);
            if (matcher.find())
            {
                rval = new MatchLocation(matcher.start(), matcher.end());
            }
        }

        return rval;
    }

    public void setRegex(String regex)
    {
        this.regex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }


}
