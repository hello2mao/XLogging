package com.hello2mao.xlogging.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class ReflectionUtil {
    
    public static <C, F> F getValueOfField(Field field, C c) throws CustomException {
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
            throw new CustomException("Unable to get value of field", t);
        }
        return (F) value;
    }
    
    public static Field getFieldFromClass(Class<?> clazz, Class<?> fieldClazz) throws CustomException {
        Field[] declaredFields = clazz.getDeclaredFields();
        Field field = null;
        for (Field declaredField : declaredFields) {
            if (fieldClazz.isAssignableFrom(declaredField.getType())) {
                if (field != null) {
                    throw new CustomException("Field is ambiguous: " + field.getName() + ", "
                            + declaredField.getName());
                }
                field = declaredField;
            }
        }
        if (field == null) {
            throw new CustomException("Could not find field matching type: " + fieldClazz.getName());
        }
        field.setAccessible(true);
        return field;
    }

    public static Field getFieldFromClass(Class clazz, Class fieldClazz, boolean paramBoolean)
            throws CustomException {
        Field[] declaredFields = clazz.getDeclaredFields();
        Field field = null;
        for (Field declaredField : declaredFields) {
            if (fieldClazz.isAssignableFrom(declaredField.getType())) {
                if (field != null) {
                    throw new CustomException("Field is ambiguous: " + field.getName() + ", " +
                            declaredField.getName());
                }
                field = declaredField;
            }
        }
        if (field != null) {
            field.setAccessible(true);
        } else if (paramBoolean) {
            throw new CustomException("Could not find field matching type: " + fieldClazz.getName());
        }
        return field;
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
