package server.infrastructure.mapper;

import java.util.Map;

public interface GenericDataMapper {

    Map<String, Object> toMap(Object object);

    <T> T toObject(Map<String, Object> object, Class<T> clazz);
}
