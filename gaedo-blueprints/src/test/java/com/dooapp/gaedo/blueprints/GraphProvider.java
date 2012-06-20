package com.dooapp.gaedo.blueprints;

import com.tinkerpop.blueprints.pgm.IndexableGraph;

interface GraphProvider {
	IndexableGraph get();
}