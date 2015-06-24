package com.erigir.lucid;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;

/**
 * This is just an easy way to get a correctly configured ObjectMapper
 * cweiss : 6/24/12 6:25 PM
 */
public class ObjectMapperFactory implements FactoryBean<ObjectMapper> {

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public ObjectMapper getObject() throws BeansException {
        ObjectMapper rval = new ObjectMapper();
        rval.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        rval.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        rval.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, true);

        return rval;
    }
}
