package com.conviva.json;

import java.util.Map;

import org.json.simple.JSONValue;

/**
 * Default SimpleJsonInterface provided by Conviva. Alternatively you can
 * develop your own JsonInterface.
 */
public class SimpleJsonInterface implements IJsonInterface {

	@Override
	public String encode(Map<String, Object> map) {
        try {
            return JSONValue.toJSONString(map);
        } catch (Exception e) {
            //err("Failed to encode json object: " + e.toString());
            return null;
        }
	}

	@Override
	public Map<String, Object> decode(String json) {
        try {
            return (Map<String, Object>) JSONValue.parse(json);
        } catch (Exception e) {
            //err("Failed to decode json string: " + e.toString());
        }
        return null;
	}

}
