package com.hello2mao.xlogging.internal.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class ReflectionUtil {

    /**
     * getFieldFromClass
     *
     * @param clazz Class
     * @param fieldClazz Class
     * @return Field
     * @throws Exception Exception
     */
    public static Field getFieldFromClass(Class<?> clazz, Class<?> fieldClazz) throws Exception {
        Field[] declaredFields = clazz.getDeclaredFields();
        Field field = null;
        for (Field declaredField : declaredFields) {
            if (fieldClazz.isAssignableFrom(declaredField.getType())) {
                if (field != null) {
                    throw new Exception("Field is ambiguous: " + field.getName() + ", "
                            + declaredField.getName());
                }
                field = declaredField;
            }
        }
        if (field == null) {
            throw new Exception("Could not find field matching type: " + fieldClazz.getName());
        }
        field.setAccessible(true);
        return field;
    }

    /**
     * getValueOfField
     *
     * @param field  Field
     * @param c C
     * @param <C> C
     * @param <F> F
     * @return F
     * @throws Exception Exception
     */
    public static <C, F> F getValueOfField(Field field, C c) throws Exception {
        Object value;
        if (field == null) {
            return null;
        }
        field.setAccessible(true);
        try {
            value = field.get(c);
        } catch (ThreadDeath threadDeath) {
            throw threadDeath;
        } catch (Throwable t) {
            throw new Exception("Unable to get value of field", t);
        }
        return (F) value;
    }

    public static void setAccessible(AccessibleObject accessibleObject, AccessibleObject[] array) {
        if (accessibleObject != null) {
            accessibleObject.setAccessible(true);
        }
        if (array != null && array.length > 0) {
            setAccessible(array);
        }
    }

    public static void setAccessible(AccessibleObject[] array) {
        for (AccessibleObject accessibleObject : array) {
            if (accessibleObject != null) {
                accessibleObject.setAccessible(true);
            }
        }
    }

    public static Object getFieldFromObject(Field field, Object object) {
        Object value = null;
        if (field != null) {
            field.setAccessible(true);
            try {
                value = field.get(object);
            } catch (ThreadDeath threadDeath) {
                throw threadDeath;
            } catch (Throwable t) {
                return null;
            }
        }
        return value;
    }
}
