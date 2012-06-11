package com.dooapp.gaedo.extensions.hierarchy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes an object that is to be considered as a child of given object. This may change the way child object is stored, but it is storage dependant.
 * @author ndx
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Child {

}
