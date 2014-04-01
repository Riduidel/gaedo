package com.dooapp.gaedo.blueprints.indexable;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.VertexHasNoPropertyException;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.strategies.UnableToGetVertexTypeException;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Browse over an index to find vertex matching given criterias
 * @author ndx
 *
 */
public class IndexBrowser {
    public static interface VertexMatcher {

        String getIdOf(Vertex vertex);

        Kind getKindOf(Vertex vertex);

        String getTypeOf(Vertex vertex);

    }

    private static final Logger logger = Logger.getLogger(IndexBrowser.class.getName());

    public Vertex browseFor(IndexableGraph database, String objectVertexId, String className, VertexMatcher matcher) {
        Vertex defaultVertex = null;
        // If there is no vertex id, don't even
        if(objectVertexId==null)
            return null;
        CloseableIterable<Vertex> matchingIterable = getVerticesWithId(database, objectVertexId);
        Iterator<Vertex> matching = matchingIterable.iterator();
        if (matching.hasNext()) {
            while (matching.hasNext()) {
                Vertex vertex = matching.next();
                String vertexTypeName = null;
                Kind vertexKind = null;
                String vertexId = null;
                try {
                    /// BEWARE : order is signifiant here : read id then kind then type for catch clauses to work correctly
                    vertexId = matcher.getIdOf(vertex);
                    vertexKind = matcher.getKindOf(vertex);
                    vertexTypeName = matcher.getTypeOf(vertex);
                    // this slow-down is a direct consequence of https://github.com/Riduidel/gaedo/issues/66
                    if (objectVertexId.equals(vertexId)) {
                        switch (vertexKind) {
                        case literal:
                        case bnode:
                        case uri:
                            if (Literals.classes.getTransformer().areEquals(className, vertexTypeName)) {
                                return vertex;
                            }
                            break;
                        default:
                            return vertex;
                        }
                    }
                } catch (VertexHasNoPropertyException e) {
                    // vertex is clearly not the match we expected so just navigate to next in index
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING,
                                        "your index may be corrupted...\nWhen looking for value=\"" + objectVertexId + "\", that vertex loaded badly", e);
                    }
                } catch (UnableToGetVertexTypeException e) {
                    if (GraphMappingStrategy.STRING_TYPE.equals(className)) {
                        // in that very case, we can use a type-less vertex as our result
                        defaultVertex = vertex;
                    } else {
                        if (Kind.uri == vertexKind && objectVertexId.equals(vertexId))
                            defaultVertex = vertex;
                    }
                }
            }
        }
        return defaultVertex;
    }

	protected CloseableIterable<Vertex> getVerticesWithId(IndexableGraph database, String objectVertexId) {
		return database.getIndex(IndexNames.VERTICES.getIndexName(), Vertex.class).get(Properties.value.name(),
                        objectVertexId);
	}

}
