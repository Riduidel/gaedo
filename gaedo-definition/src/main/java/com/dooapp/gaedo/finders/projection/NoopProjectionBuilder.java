package com.dooapp.gaedo.finders.projection;

import com.dooapp.gaedo.finders.Informer;

/**
 * in some cases (typically GAE), it is easier to always rely upon projector than to avoid it, so use this one in that case
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public class NoopProjectionBuilder<DataType, InformerType extends Informer<DataType>> implements ProjectionBuilder<DataType, DataType, InformerType> {

	@Override
	public DataType project(InformerType informer, ValueFetcher fetcher) {
		return fetcher.getValue(informer);
	}

}
