package server.infrastructure.mapper.impl;

import server.infrastructure.mapper.GenericDataMapper;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class JacksonDataMapper implements GenericDataMapper {
    private ObjectMapper mapper;

    public JacksonDataMapper() {
        mapper = new ObjectMapper();
    }

    @Override
    public Map<String, Object> toMap(Object object) {
        if (object == null) return null;

        return mapper.convertValue(object, Map.class);
    }

    @Override
    public <T> T toObject(Map<String, Object> data, Class<T> clazz) {
        if (data == null) return null;

        return mapper.convertValue(data, clazz);
    }
}
