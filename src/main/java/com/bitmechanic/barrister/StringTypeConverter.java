package com.bitmechanic.barrister;

public class StringTypeConverter implements TypeConverter {

    public Class getTypeClass() {
        return String.class;
    }

    public Object fromRequest(String pkg, Object o) throws RpcException {
        if (o == null || o.getClass() == String.class)
            return o;
        else
            throw RpcException.Error.INVALID_PARAMS.exc("Expected string, got: " +
                                                        o.getClass().getSimpleName());
    }

    public Object toResponse(Object o) throws RpcException {
        return fromRequest(null, o);
    }

}