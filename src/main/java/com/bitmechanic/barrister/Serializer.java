package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Serializer {

    //public byte[] serialize(Object o) throws IOException;

    public List<Map<String,Object>> readList(InputStream is) throws IOException;
    //public Map<String,Object> readMap(InputStream is) throws IOException;

    RpcRequest readRequest(byte[] input) throws IOException;
    byte[] writeResponse(RpcResponse resp) throws IOException;

}