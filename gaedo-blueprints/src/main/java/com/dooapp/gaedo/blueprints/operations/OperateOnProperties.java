package com.dooapp.gaedo.blueprints.operations;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.finders.id.AnnotationsFinder.Annotations;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.TypeProperty;

/**
 * Internal class allowing easy iteration over properties of one object
 * @author ndx
 *
 */
class OperateOnProperties {
	public void execute(Map<Property, Collection<CascadeType>> containedProperties, CascadeType cascade, Operation operation) {
		String operationSimpleName = operation.getClass().getSimpleName();
    	Logger logger = Logger.getLogger(operation.getClass().getName());
        for (Map.Entry<Property, Collection<CascadeType>> entry : containedProperties.entrySet()) {
            Property p = entry.getKey();
            // Static properties are by design not written
            if (!p.hasModifier(Modifier.STATIC) && !Annotations.TRANSIENT.is(p)) {
                // Per default, no operation is cascaded
                CascadeType used = null;
                // However, if property supports that cascade type, we cascade operation
                if (entry.getValue().contains(cascade)) {
                    used = cascade;
                } else {
                	if(logger.isLoggable(Level.FINE)) {
                		// just don't output anything for type or class collections, as they're special properties
                		if(!(TypeProperty.INSTANCE.equals(p) || new ClassCollectionProperty(getClass()).equals(p)))
                		logger.log(Level.FINE,
                						String.format("operation %s not performed on %s has no cascade %s defined\n"
                										+ "To fix that, simply add %s.%s to its JPA annotation",
                										operationSimpleName,
                										p.toGenericString(),
                										cascade,
                										cascade.getClass().getSimpleName(),
                										cascade));
                	}
                }
                // We only perform operations on cascaded fields
                if(used!=null) {
                	operation.operateOn(p, cascade);
                }
            }
        }
	}
}
