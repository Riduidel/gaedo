package com.dooapp.gaedo.blueprints;

import com.dooapp.gaedo.finders.Informer;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;

/**
 * Transaction supporting "closure" base : it decorates given operation with transaction support.
 * @author ndx
 *
 * @param <ResultType>
 */
public abstract class TransactionalOperation<ResultType, DataType, InformerType extends Informer<DataType>> {
	/**
	 * 
	 */
	private final AbstractBluePrintsBackedFinderService<?, DataType, InformerType> service;

	/**
	 * @param bluePrintsBackedFinderService
	 */
	public TransactionalOperation(AbstractBluePrintsBackedFinderService<?, DataType, InformerType> bluePrintsBackedFinderService) {
		service = bluePrintsBackedFinderService;
	}

	public ResultType perform() {
		// disabled for test purpose
		if(false && service.transactionSupport!=null) {
			try {
//				service.transactionSupport.startTransaction();
				try {
					ResultType returned = doPerform();
					service.transactionSupport.stopTransaction(Conclusion.SUCCESS);
					return returned;
				} catch(RuntimeException e) {
					service.transactionSupport.stopTransaction(Conclusion.FAILURE);
					throw e;
				}
			} catch(RuntimeException e) {
				/*
				 * People of tinkerpop : I hate you for that code block !
				 * Why on earth didn't you send a specific exception conveying that very purpose ? I could have handled it clearly.
				 * Instead, I have to rely on messy and slow string comparison. OMG
				 */
				if("Stop current transaction before starting another".equals(e.getMessage())) {
					// Anyway, perform operation in transaction
					return doPerform();
					// And obviously, I won't close a transaction I didn't opened
				} else {
					throw e;
				}
			}
		} else {
			return doPerform();
		}
	}

	/**
	 * Operation that will be performed (in transactional context or not)
	 * @return effective result
	 */
	protected abstract ResultType doPerform();
}