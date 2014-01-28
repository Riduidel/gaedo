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
                	Logger logger = Logger.getLogger(operation.getClass().getName());
                	if(logger.isLoggable(Level.FINE)) {
                		// just don't output anything for type or class collections, as they're special properties
                		if(!(TypeProperty.INSTANCE.equals(p) || new ClassCollectionProperty(getClass()).equals(p)))
                		logger.log(Level.FINE, "operation not performed on "+p.toGenericString()+" has no cascade "+cascade+" defined");
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
