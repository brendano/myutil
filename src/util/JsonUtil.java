package util;

import java.util.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.*;

import com.google.common.collect.Multiset;

/** simplified wrapper functions for the Jackson JSON library */
public class JsonUtil {
	
	public static <T> ObjectNode toJson(Multiset<T> counts) {
		ObjectNode jmap = newObject();
		for (Multiset.Entry<T> e : counts.entrySet()) {
			jmap.put(e.getElement().toString(), e.getCount());
		}
		return jmap;
	}
	
    public static <T> JsonNode toJson(final List<T> data) {
    	ArrayNode jlist = new ObjectMapper().createArrayNode();
    	for (T elt : data) {
    		jlist.add( toJson(elt) );
    	}
    	return jlist;
    }

    ///////// from Play framework below
    
    /**
     * Convert an object to JsonNode.
     *
     * @param data Value to convert in Json.
     */
    public static JsonNode toJson(final Object data) {
        try {
            return new ObjectMapper().valueToTree(data);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
   
    /**
     * Convert a JsonNode to a Java value
     *
     * @param json Json value to convert.
     * @param clazz Expected Java value type.
     */
    public static <A> A fromJson(JsonNode json, Class<A> clazz) {
        try {
            return new ObjectMapper().treeToValue(json, clazz);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Creates a new empty ObjectNode.
     */ 
    public static ObjectNode newObject() {
        return new ObjectMapper().createObjectNode();
    }
    
    /**
     * Convert a JsonNode to its string representation.
     */
    public static String stringify(JsonNode json) {
        return json.toString();
    }
    
    /**
     * Parse a String representing a json, and return it as a JsonNode.
     */
    public static JsonNode parse(String src) {
        try {
            return new ObjectMapper().readValue(src, JsonNode.class);
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
