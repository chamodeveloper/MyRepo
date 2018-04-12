package com.conviva.json;

import java.util.Map;

/**
 * Interface IJsonInterface used by the Conviva library to handle JSON encoding/decoding.
 */
public interface IJsonInterface {
    /** 
     * Encode an object into a JSON string.
     * @param map An object to be encoded to JSON.
     * @return    The resulting JSON string.
     */
    public String encode(Map<String, Object> map);
    
    /** 
     * Decode a JSON string into an object.
     * @param json The JSON string.
     * @return  The resulting decoded map.
     */
    public Map<String, Object> decode(String json);

}
