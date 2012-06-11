package com.dooapp.gaedo.finders.id;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A local id, to avoid the requirement of having a javax.persistence provider
 * @author ndx
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

}
