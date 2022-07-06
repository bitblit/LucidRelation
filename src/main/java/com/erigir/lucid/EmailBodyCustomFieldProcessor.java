package com.erigir.lucid;

import com.erigir.lucid.modifier.IScanAndReplace;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * User: chrweiss
 * Date: 12/4/13
 * Time: 1:38 PM
 */
public class EmailBodyCustomFieldProcessor implements ICustomFieldProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(EmailBodyCustomFieldProcessor.class);
    public static final String TEXT_BODY_NAME = "emailTextBody";
    public static final String HTML_BODY_NAME = "emailHtmlBody";
    public static final String HEADER_PREFIX = "emailHeader_";

    private IScanAndReplace postProcessor;


    @Override
    public Map<String, Class> process(String fieldName, Object fieldValue, Document lDoc) {
        LOG.debug("Processing field {} (class {}) as an email body", new Object[]{fieldName, fieldName.getClass()});
        Map<String, Class> addField = new TreeMap<String, Class>();

        try {
            ByteArrayInputStream inputData = new ByteArrayInputStream(String.valueOf(fieldValue).getBytes());

            MimeTokenStream stream = new MimeTokenStream();
            stream.parse(inputData);
            for (EntityState state = stream.getState();
                 state != EntityState.T_END_OF_STREAM;
                 state = stream.next()) {
                switch (state) {
                    case T_BODY:
                        BodyDescriptor bd = stream.getBodyDescriptor();
                        if ("text".equalsIgnoreCase(bd.getMediaType())) {
                            String value = IOUtils.toString(stream.getInputStream());
                            if (bd.getTransferEncoding() != null) {
                                if ("base64".equalsIgnoreCase(bd.getTransferEncoding())) {
                                    value = new String(Base64.decodeBase64(value.getBytes()));
                                } else if ("7bit".equalsIgnoreCase(bd.getTransferEncoding())) {
                                    LOG.debug("Using 7bit encoding straight : {}", value.length());
                                } else if ("8bit".equalsIgnoreCase(bd.getTransferEncoding())) {
                                    LOG.debug("Using 8bit encoding straight : {}", value.length());
                                } else if ("quoted-printable".equalsIgnoreCase(bd.getTransferEncoding())) {
                                    LOG.debug("Using quoted-printable encoding straight : {}", value.length());
                                } else {
                                    LOG.warn("unknown xfer enc : {}", bd.getTransferEncoding());
                                }
                            }

                            if ("plain".equalsIgnoreCase(bd.getSubType())) {
                                LOG.info("Setting email text body");
                                lDoc.add(new TextField(TEXT_BODY_NAME, postProcess(value), Field.Store.YES));
                                addField.put(TEXT_BODY_NAME, String.class);
                            } else if ("html".equalsIgnoreCase(bd.getSubType())) {
                                LOG.info("Setting email html body");
                                lDoc.add(new TextField(HTML_BODY_NAME, postProcess(value), Field.Store.YES));
                                addField.put(HTML_BODY_NAME, String.class);
                            } else {
                                LOG.debug("non plain or html text found : {}", bd.getSubType());
                            }
                        } else {
                            LOG.warn("Non-text body part found : {}", bd);
                        }
                        break;
                    case T_FIELD:
                        org.apache.james.mime4j.stream.Field f = stream.getField();
                        String recName = HEADER_PREFIX + f.getName();

                        lDoc.add(new StringField(recName, postProcess(f.getBody()), Field.Store.YES));
                        addField.put(recName, String.class);

                        LOG.trace("Header field detected: name {} value {}", f.getName(), f.getBody());
                        break;
                    case T_START_MULTIPART:
                        LOG.trace("Multipart message detexted,"
                                + " header data = "
                                + stream.getBodyDescriptor());
                }
            }


        } catch (IOException ioe) {
            LOG.warn("Error processing", ioe);
        } catch (MimeException me) {
            LOG.warn("Error processing", me);
        }

        return addField;
    }

    private String postProcess(String input) {
        return (input == null || postProcessor == null) ? input : postProcessor.performScanAndReplace(input);
    }

    public void setPostProcessor(IScanAndReplace postProcessor) {
        this.postProcessor = postProcessor;
    }
}
