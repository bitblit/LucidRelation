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
public abstract class RegexModifier implements IStringModifier {
    private static final Logger LOG = LoggerFactory.getLogger(RegexModifier.class);

    private Pattern cache;

    @Override
    public String modify(String body) {
        if (cache==null && regexToFind()!=null)
        {
            cache = Pattern.compile(regexToFind(), Pattern.CASE_INSENSITIVE);
        }

        String rval = body;
        if (rval!=null && cache!=null)
        {
            Matcher matcher = cache.matcher(body);
            StringBuilder sb = new StringBuilder();
            int last = 0;
            while (matcher.find())
            {
                int start = matcher.start();
                int end = matcher.end();
                String middle = body.substring(last, start);
                String toReplace = body.substring(start,end);
                String newVal = processMatch(toReplace);

                LOG.debug("Found match, mid='{}' replace='{}' with '{}'", new Object[]{middle, toReplace, newVal});
                // Copy in everything before the match
                sb.append(middle);
                sb.append(newVal);
                last = end;

            }

            if (last<body.length())
            {
                sb.append(body.substring(last));
            }

            rval = sb.toString();

        }

        return rval;
    }

    public abstract String regexToFind();
    public abstract String processMatch(String match);

}
