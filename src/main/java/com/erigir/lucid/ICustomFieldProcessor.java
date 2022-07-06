package com.erigir.lucid;

import com.erigir.lucid.modifier.IScanAndReplace;
import org.apache.lucene.document.Document;

import java.util.Map;

/**
 * User: chrweiss
 * Date: 12/4/13
 * Time: 1:37 PM
 */
public interface ICustomFieldProcessor {
    void setPostProcessor(IScanAndReplace postProcessor);

    /**
     * Can add fields to the provided document - should return a map of the field
     * names and types added to the doc
     *
     * @param fieldName
     * @param fieldValue
     * @param lDoc
     * @return
     */
    Map<String, Class> process(String fieldName, Object fieldValue, Document lDoc);
}
