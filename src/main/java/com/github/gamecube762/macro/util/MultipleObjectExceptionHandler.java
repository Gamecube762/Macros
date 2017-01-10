package com.github.gamecube762.macro.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gamec on 12/12/2016.
 */
/**
 * Creates a combined message of all the exceptions thrown while listing the objects that threw those exceptions.
 *
 * Example:
 *
 * java.lang.IllegalArgumentException: Invalid Parameter
 *  - Identifier
 *  - Identifier2
 *
 * java.lang.IllegalArgumentException: Invalid Parameter
 *  - Identifier
 *  - Identifier2
 *
 *  java.lang.NullPointerException: --
 *  - Identifier
 *  - Identifier2
 */
public class MultipleObjectExceptionHandler<T> {//todo doc

    private HashMap<String, HashMap<T, Exception>> map = new HashMap<>();

    public MultipleObjectExceptionHandler() {}

    public void thrown(Exception e, T object) {
        String k = String.format("%s: %s", e.getClass().getName(), e.getMessage());
        HashMap<T, Exception> inner = map.getOrDefault(k, new HashMap<>());
        inner.put(object, e);
        map.put(k, inner);
    }

    public HashMap<String, HashMap<T, Exception>> getMap() {
        return map;
    }

    public Collection<Exception> getExceptions() {
        Collection<Exception> a = new ArrayList<>();

        map.values().stream()
                .map(HashMap::values)
                .forEach(a::addAll);

        return a;
    }

    public Collection<T> getObjects() {
        Collection<T> a = new ArrayList<>();

        map.values().stream()
                .map(HashMap::keySet)
                .distinct()//No duplicates
                .forEach(a::addAll);

        return a;
    }

    public String getMessage() {
        return getMessage(Object::toString);
    }

    public String getMessage(CustomSerializer2<T> serializer) {
        return getMessage((e, o) -> serializer.asString(o));
    }

    public String getMessage(CustomSerializer<T> serializer) {
        StringBuilder sb = new StringBuilder();

        for (String k : map.keySet()) { //todo sort
            sb.append(k).append("\n");

            for (Map.Entry<T, Exception> entry : map.get(k).entrySet())
                sb.append(" - ").append(serializer.asString(entry.getValue(), entry.getKey())).append('\n');

            sb.append("\n");
        }

        return sb.toString();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public interface CustomSerializer<T> {

        String asString(Exception exc, T obj);

    }

    public interface CustomSerializer2<T> {

        String asString(T o);

    }
}
