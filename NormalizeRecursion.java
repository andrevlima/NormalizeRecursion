package celfocus.omnichannel.Common;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Treat recursion known issues
 *
 * @author NB24054
 */
public class NormalizeRecursion {

    static final Set<Class> WRAPPER_TYPES = new HashSet(Arrays.asList(
            Boolean.class, Character.class, Byte.class, Short.class, Date.class, Integer.class,
            Long.class, Float.class, Double.class, Void.class, String.class, Temporal.class));

    /**
     * Guess if the object is a "common/primitive Java value"
     * @param T
     * @return
     */
    public static boolean isBasicType(Class T) {
        return T == null || T.isPrimitive() || ClassUtils.isPrimitiveOrWrapper(T) || WRAPPER_TYPES.stream().anyMatch((type) -> T.isAssignableFrom(type) || T.isEnum());
    }

    /**
     * The next algorithm cleans all recursion problems on object serialization, for example,
     * to a plain JSON. <b>Pay attention, the object cleaned will suffer changes, Java works byRef
     * if you really need to preserve it, try to find a way to clone it before.</b><br><br>
     *
     * A problem is when you have an object that has some property that stores an object that
     * is a possible parent, when is during a serialization the error occurred is a StackOverflow
     * caused by the loop.<br><br>
     *
     * This function goes through an object properties recursively and tries to find any others pointing
     * to some ancestor, for this "clean effect" we call the "setter" of property with a NULL parameter.
     * <br><br>
     *
     * Known-issue: When the object is an native array is not foreseen a solution to iterate and search inner and
     * also was never test with, unexpected behaviour can occurs.
     * @author NB24054
     * @param obj Target
     */
    public static void cleanRecursion(Object obj) {
        cleanRecursion(obj, new HashSet<>());
    }

    protected static void cleanRecursion(Object obj, Set<Object> visitedList) {
        Set<Object> tempList = visitedList;
        visitedList = new HashSet<Object>();
        visitedList.addAll(tempList);
        visitedList.add(obj);

        if(List.class.isAssignableFrom(obj.getClass())) {
            Set<Object> finalVisitedList = visitedList;
            ((List) obj).stream().forEach(item -> {
                cleanRecursion(item, finalVisitedList);
            });
            return;
        }
        //If is basic...
        if(obj == null || isBasicType(obj.getClass())) {
            return;
        }

        for (Field getter: obj.getClass().getDeclaredFields()) {
            try {
                if(!isBasicType(getter.getType())) {
                    getter.setAccessible(true);
                    Object value = getter.get(obj);

                    if(value != null && !visitedList.contains(value)) {
                        cleanRecursion(value, visitedList);
                    } else if(visitedList.contains(value)) {
                        new org.apache.commons.beanutils.PropertyUtilsBean().setProperty(obj, getter.getName(), null);
                        //getter.set(obj, null); //Dangerous because some properties is not interesting to be nullable (like native properties, enums...)
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }
}
