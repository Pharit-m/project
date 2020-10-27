package com.example.project.common;


import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class Sort {
    public static final Map<Class, List<Field>> clazzCaches = new ConcurrentHashMap<>();

    @SuppressWarnings("uncheckrd")
    public static <T> void sort(List<T> list, Class<T> clazz) {
        List<Field> orderField = null;
        if(!clazzCaches.containsKey(clazz)){
            orderField =  new ArrayList<>();
            final Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Order annotation = field.getAnnotation(Order.class);
                if (annotation != null) {
                    orderField.add(field);
                }
            }

            Collections.sort(orderField, new Comparator<Field>() {
                @Override
                public int compare(Field o1, Field o2) {
                    Order or1 = o1.getAnnotation(Order.class);
                    Order or2 = o2.getAnnotation(Order.class);
                    return or1.value() - or2.value();
                }
            });

            clazzCaches.put(clazz, orderField);
        }else {
            orderField = clazzCaches.get(clazz);
        }

        final List<Field> finalOrderField = orderField;
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                for (Field field : finalOrderField) {
                    Order order = field.getAnnotation(Order.class);
                    Dir dir = order.dir();
                    int result = compareField(o1, o2, field);
                    if (result != 0) {
                        return dir == Dir.ASC ? result : -result;
                    }
                }
                return 0;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> int compareField(T o1, T o2, Field field){
        field.setAccessible(true);
        Comparable c1 = null;
        Comparable c2 = null;
        try {
            c1 = (Comparable) field.get(o1);
            c2 = (Comparable) field.get(o2);
        } catch (IllegalAccessException e) {
            log.info("Sort fail", e);
        }
        if (c1 == null) return -1;
        if (c2 == null) return 1;

        return c1.compareTo(c2);
    }

    @ToString
    public static class SomeClass {
        @Order(value = 2, dir = Dir.ASC)
        public int field1;
        @Order(value = 1)
        public int field2;
        // no annotation
        public int field3;

        public SomeClass(int field1, int field2, int field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }
    }
}

