package com.example.project.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Sedtawut on 4/23/14 AD.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    int value() default 10;
    Dir dir() default Dir.ASC;
}
