package org.openpnp.spi.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;

import org.openpnp.spi.Camera;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.Head;
import org.openpnp.spi.JobPlanner;
import org.openpnp.spi.JobProcessor;
import org.openpnp.spi.JobProcessor.Type;
import org.openpnp.spi.Machine;
import org.openpnp.spi.MachineListener;
import org.openpnp.util.IdentifiableList;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Commit;

import com.google.common.util.concurrent.FutureCallback;

public abstract class AbstractMachine implements Machine {
    /**
     * History:
     * 
     * Note: Can't actually use the @Version annotation because of a bug
     * in SimpleXML. See http://sourceforge.net/p/simple/mailman/message/27887562/
     *  
     * 1.0: Initial revision.
     * 1.1: Added jobProcessors Map and deprecated JobProcesor and JobPlanner.
     */

    @ElementList
    protected IdentifiableList<Head> heads = new IdentifiableList<Head>();
    
    @ElementList(required=false)
    protected IdentifiableList<Feeder> feeders = new IdentifiableList<Feeder>();
    
    @ElementList(required=false)
    protected IdentifiableList<Camera> cameras = new IdentifiableList<Camera>();
    
    @Deprecated
    @Element(required=false)
    protected JobPlanner jobPlanner;
    
    @Deprecated
    @Element(required=false)
    protected JobProcessor jobProcessor;
    
    @ElementMap(entry="jobProcessor", key="type", attribute=true, inline=false, required=false)
    protected Map<JobProcessor.Type, JobProcessor> jobProcessors = new HashMap<>();
    
    protected Set<MachineListener> listeners = Collections.synchronizedSet(new HashSet<MachineListener>());
    
    protected ThreadPoolExecutor executor;
    
    protected AbstractMachine() {
    }
    
    @SuppressWarnings("unused")
    @Commit
    private void commit() {
        if (jobProcessors.isEmpty()) {
            jobProcessors.put(JobProcessor.Type.PickAndPlace, jobProcessor);
            jobProcessor = null;
            jobPlanner = null;
        }
    }
    
    @Override
    public List<Head> getHeads() {
        return Collections.unmodifiableList(heads);
    }

    @Override
    public Head getHead(String id) {
        return heads.get(id);
    }

    @Override
    public List<Feeder> getFeeders() {
        return Collections.unmodifiableList(feeders);
    }
    
    @Override
    public Feeder getFeeder(String id) {
        return feeders.get(id);
    }
    
    @Override
    public List<Camera> getCameras() {
        return Collections.unmodifiableList(cameras);
    }

    @Override
    public Camera getCamera(String id) {
        return cameras.get(id);
    }

    @Override
    public void home() throws Exception {
        for (Head head : heads) {
            head.home();
        }
    }

    @Override
    public void addListener(MachineListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(MachineListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void addFeeder(Feeder feeder) throws Exception {
        feeders.add(feeder);
    }

    @Override
    public void removeFeeder(Feeder feeder) {
        feeders.remove(feeder);
    }

    @Override
    public void addCamera(Camera camera) throws Exception {
        cameras.add(camera);
    }

    @Override
    public void removeCamera(Camera camera) {
        cameras.remove(camera);
    }
    
    @Override
    public Map<Type, JobProcessor> getJobProcessors() {
        return Collections.unmodifiableMap(jobProcessors);
    }

    public void fireMachineHeadActivity(Head head) {
        for (MachineListener listener : listeners) {
            listener.machineHeadActivity(this, head);
        }
    }
    
    public void fireMachineEnabled() {
        for (MachineListener listener : listeners) {
            listener.machineEnabled(this);
        }
    }
    
    public void fireMachineEnableFailed(String reason) {
        for (MachineListener listener : listeners) {
            listener.machineEnableFailed(this, reason);
        }
    }
    
    public void fireMachineDisabled(String reason) {
        for (MachineListener listener : listeners) {
            listener.machineDisabled(this, reason);
        }
    }
    
    public void fireMachineDisableFailed(String reason) {
        for (MachineListener listener : listeners) {
            listener.machineDisableFailed(this, reason);
        }
    }

    public void fireMachineBusy(boolean busy) {
        for (MachineListener listener : listeners) {
            listener.machineBusy(this, busy);
        }
    }

    @Override
    public Icon getPropertySheetHolderIcon() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Future<Object> submit(Runnable runnable) {
        return submit(Executors.callable(runnable));
    }
    
    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return submit(callable, null);
    }
    
    @Override
    public <T> Future<T> submit(final Callable<T> callable, final FutureCallback<T> callback) {
        return submit(callable, callback, false);
    }
    
    @Override
    public <T> Future<T> submit(final Callable<T> callable, final FutureCallback<T> callback, final boolean ignoreEnabled) {
        synchronized(this) {
            if (executor == null || executor.isShutdown()) {
                executor = new ThreadPoolExecutor(
                        1, 
                        1, 
                        1,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>());
            }
        }
        
        Callable<T> wrapper = new Callable<T>() {
            public T call() throws Exception {
                // TODO: lock driver
                
                // Notify listeners that the machine is now busy
                fireMachineBusy(true);
                
                // Call the task, storing the result and exception if any
                T result = null;
                Exception exception = null;
                try {
                    if (!ignoreEnabled && !isEnabled()) {
                        throw new Exception("Machine is not enabled.");
                    }
                    result = callable.call();
                }
                catch (Exception e) {
                    exception = e;
                }
                
                // If a callback was supplied, call it with the results
                if (callback != null) {
                    if (exception != null) {
                        callback.onFailure(exception);
                    }
                    else {
                        callback.onSuccess(result);
                    }
                }
                
                // If there was an error cancel all pending tasks.
                if (exception != null) {
                    executor.shutdownNow();
                }
                
                // TODO: unlock driver
  
                // If no more tasks are scheduled notify listeners that
                // the machine is no longer busy
                if (executor.getQueue().isEmpty()) {
                    fireMachineBusy(false);
                }
                
                // Finally, fulfill the Future by either throwing the
                // exception or returning the result.
                if (exception != null) {
                    throw exception;
                }
                return result;
            }
        };
        
        return executor.submit(wrapper); 
    }

	@Override
	public Head getDefaultHead() {
		List<Head> heads = getHeads();
		if (heads == null || heads.isEmpty()) {
			return null;
		}
		return heads.get(0);
	}
}
