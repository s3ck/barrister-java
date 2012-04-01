package com.bitmechanic.barrister;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.FileInputStream;
import java.util.List;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

/**
 * A Contract represents a single Barrister IDL file.  It contains all the
 * Interfaces, Structs, and Enums defined in the file.
 *
 * Note that when "IDL File" is referred to in this doc, we're referring to the 
 * JSON representation of the IDL as generated by the barrister tool, not the 
 * human readable text version.
 */
public class Contract extends BaseEntity {

    /**
     * Loads the IDL JSON file, parses it, and returns a Contract.
     * Uses the JacksonSerializer.
     *
     * @param idlJson Barrister IDL JSON file to load
     * @return Contract based on the IDL JSON file
     * @throws IOException If there is a problem reading the file, or if JSON 
     *         deserialization fails
     */
    public static Contract load(File idlJson) throws IOException {
        FileInputStream fis = new FileInputStream(idlJson);
        Contract c = load(fis);
        fis.close();
        return c;
    }

    /**
     * Loads the IDL JSON from the given stream, parses it, and returns a Contract.
     * Uses the JacksonSerializer.
     *
     * @param idlJson InputStream of the IDL JSON to parse
     * @return Contract based on the IDL JSON stream
     * @throws IOException if there is a problem reading the stream, or if JSON
     *         deserialization fails
     */
    public static Contract load(InputStream idlJson) throws IOException {
        return load(idlJson, new JacksonSerializer());
    }

    /**
     * Loads the IDL from the given stream using an arbitrary serializer and returns
     * a Contract.
     *
     * @param idlJson Stream containing serialized IDL
     * @param ser Serializer implementation to use
     * @return Contract based on the IDL stream
     * @throws IOException if there is a problem reading the stream, or if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public static Contract load(InputStream idlJson, Serializer ser) throws IOException {
        return new Contract(ser.readList(idlJson));
    }

    //////////////////////////////

    private Map<String, Interface> interfaces;
    private Map<String, Struct> structs;
    private Map<String, Enum> enums;

    private List<Map<String,Object>> idl;

    private String packageName;

    public Contract() {
        interfaces = new HashMap<String, Interface>();
        structs    = new HashMap<String, Struct>();
        enums      = new HashMap<String, Enum>();
    }

    /**
     * Creates a new Contract based on the Map representation of the IDL
     */
    public Contract(List<Map<String,Object>> idl) {
        this();
        this.idl = idl;

        for (Map<String,Object> e : idl) {
            String type = String.valueOf(e.get("type"));
            if (type.equals("interface")) {
                Interface i = new Interface(e);
                i.setContract(this);
                interfaces.put(i.getName(), i);
            }
            else if (type.equals("struct")) {
                Struct s = new Struct(e);
                s.setContract(this);
                structs.put(s.getName(), s);
            }
            else if (type.equals("enum")) {
                Enum en = new Enum(e);
                en.setContract(this);
                enums.put(en.getName(), en);
            }
        }
    }

    /**
     * Sets the Java package associated with this Contract. This is 
     * used to resolve full generated Java class names when unmarshaling data
     * from requests.
     */
    public void setPackage(String pkgName) {
        this.packageName = pkgName;
    }
    
    /**
     * Returns the Java package associated with this Contract
     */
    public String getPackage() {
        return packageName;
    }

    /**
     * Returns the IDL associated with this Contract as passed to the constructor.
     */
    public List<Map<String,Object>> getIdl() {
        return idl;
    }

    /**
     * Returns the interfaces associated with this Contract. 
     * Keys: interface name from IDL.  Value: Interface instance.
     */
    public Map<String, Interface> getInterfaces() {
        return interfaces;
    }

    /**
     * Returns the structs associated with this Contract. 
     * Keys: struct name from IDL.  Value: Struct instance.
     */
    public Map<String, Struct> getStructs() {
        return structs;
    }

    /**
     * Returns the enums associated with this Contract. 
     * Keys: enum name from IDL.  Value: Enum instance.
     */
    public Map<String, Enum> getEnums() {
        return enums;
    }

    /**
     * Returns the Function associated with the given interface and function name.
     *
     * @param iface Interface name
     * @param func Function name in the interface
     * @return Function that matches iface and func on this Contract
     * @throws RpcException If no Function matches
     */
    public Function getFunction(String iface, String func) throws RpcException {
        Interface i = interfaces.get(iface);
        if (i == null) {
            String msg = "Interface '" + iface + "' not found";
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        Function f = i.getFunction(func);
        if (f == null) {
            String msg = "Function '" + iface + "." + func + "' not found";
            throw RpcException.Error.METHOD_NOT_FOUND.exc(msg);
        }

        return f;
    }

}