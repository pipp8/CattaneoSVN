/*
 * $Id: ConsensusWorker.java 878 2017-04-08 22:31:23Z cattaneo $
 * $LastChangedDate: 2017-04-09 00:31:23 +0200(Dom, 09 Apr 2017) $
 */

package valworkbench.utils;

import valworkbench.measures.internal.ConsensusC.ConsensusClustering;

import java.util.logging.*;



public class ConsensusWorker implements Runnable {

	private ConsensusClustering topClass = null;
	private int		clusterIndex = -1;
	private static final Logger LOGGER = Logger.getLogger( ConsensusWorker.class.getName() );

	public ConsensusWorker( ConsensusClustering context, int clusterIndex) {
		this.topClass = context;
		this.clusterIndex = clusterIndex;
	}
	
	public void run() {

		LOGGER.log( Level.INFO, "Starting thread: {0} for clusterIndex: {1}",
				new Object[]{ Thread.currentThread().getName(), clusterIndex});

	    processAsynchronousMethod();

		LOGGER.log( Level.INFO, "Thread: {0} terminated for clusterIndex: {1}",
				new Object[]{ Thread.currentThread().getName(), clusterIndex});

	}

	private void processAsynchronousMethod() {
	    try {
	        this.topClass.executeSingleIteration( this.clusterIndex);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
