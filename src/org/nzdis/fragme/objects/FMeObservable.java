package org.nzdis.fragme.objects;

import java.util.ArrayList;

public class FMeObservable {

	private transient ArrayList<ChangeObserver> changeObservers = new ArrayList<ChangeObserver>();
	private transient ArrayList<ChangePermissionHandler> changeHandlers = new ArrayList<ChangePermissionHandler>();
	private transient ArrayList<DelegateOwnershipPermissionHandler> delegateOwnershipHandlers = new ArrayList<DelegateOwnershipPermissionHandler>();
	private transient ArrayList<DeletePermissionHandler> deleteHandlers = new ArrayList<DeletePermissionHandler>();
	private transient ArrayList<NewFMeObjectObserver> newFMeObjectObservers = new ArrayList<NewFMeObjectObserver>();
	
	public final void register(FMeObserver observer){
		if (observer instanceof ChangeObserver) {
			changeObservers.add((ChangeObserver)observer);
		}
		if (observer instanceof ChangePermissionHandler) {
			changeHandlers.add((ChangePermissionHandler)observer);
		}
		if (observer instanceof DelegateOwnershipPermissionHandler) {
			delegateOwnershipHandlers.add((DelegateOwnershipPermissionHandler)observer);
		}
		if (observer instanceof DeletePermissionHandler) {
			deleteHandlers.add((DeletePermissionHandler)observer);
		}
		if (observer instanceof NewFMeObjectObserver) {
			newFMeObjectObservers.add((NewFMeObjectObserver)observer);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Changes
	
	/**
	 * Called from within FragMe when a change has been successful
	 * Calls changed() on all relevant observers
	 */
	public final void informChangeObserversChanged() {
		for (ChangeObserver observer : changeObservers) {
			observer.changed((FMeObject)this);
		}
	}
	
	/**
	 * Called when a delegation of ownership has been successful
	 * Calls delegatedOwnership() on all relevant observers
	 */
	public final void informChangeObserversDelegatedOwnership() {
		for (ChangeObserver observer : changeObservers) {
			observer.delegatedOwnership((FMeObject)this);
		}
	}
	
	/**
	 * Called when a delete has been successful
	 * Calls deleted() on all relevant observers
	 */
	public final void informChangeObserversDeleted() {
		for (ChangeObserver observer : changeObservers) {
			observer.deleted((FMeObject)this);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Change permissions
	
	/**
	 * Called when a change is being requested
	 * Calls allowChange() on all relevant handlers and returns
	 * false if any handler returns false
	 */
	public final boolean askChangeHandlersAllowChange(FMeObject newObject, String changeRequester) {
		for (ChangePermissionHandler handler : changeHandlers) {
			if (!handler.allowChange((FMeObject)this, newObject, changeRequester)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Called when a change is being requested
	 * Calls allowChangeField() on all relevant handlers and returns
	 * false if any handler returns false
	 */
	public final boolean askChangeHandlersAllowChangeField(FMeObjectReflection objectReflection, String changeRequester) {
		for (ChangePermissionHandler handler : changeHandlers) {
			if (!handler.allowChangeField((FMeObject)this, objectReflection, changeRequester)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Called when a change has failed
	 * Calls changeFailed() on all relevant observers
	 */
	public final void informChangeHandlersChangeFailed() {
		for (ChangePermissionHandler handler : changeHandlers) {
			handler.changeFailed((FMeObject)this);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Delegate ownership permissions
	
	/**
	 * Called when a change of ownership is being requested
	 * Calls allowDelegateOwnership() on all relevant handlers and returns
	 * false if any handler returns false
	 */
	public final boolean askDelegateHandlersAllowDelegateOwnership(String delegateOwnershipRequester) {
		for (DelegateOwnershipPermissionHandler handler : delegateOwnershipHandlers) {
			if (!handler.allowDelegateOwnership((FMeObject)this, delegateOwnershipRequester)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Called when a delegate ownership request has failed
	 * Calls delegateOwnershipFailed() on all relevant observers
	 */
	public final void informDelegateHandlersDelegateOwnershipFailed() {
		for (DelegateOwnershipPermissionHandler handler : delegateOwnershipHandlers) {
			handler.delegateOwnershipFailed((FMeObject)this);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Delete permissions
	
	/**
	 * Called when a delete is being requested
	 * Calls allowDelete() on all relevant handlers and returns
	 * false if any handler returns false
	 */
	public final boolean askDeleteHandlersAllowDelete(String deleteRequester) {
		for (DeletePermissionHandler handler : deleteHandlers) {
			if (!handler.allowDelete((FMeObject)this, deleteRequester)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Called when a delete request has failed
	 * Calls deleteFailed() on all relevant observers
	 */
	public final void informDeleteHandlersDeleteFailed() {
		for (DeletePermissionHandler handler : deleteHandlers) {
			handler.deleteFailed((FMeObject)this);
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// New object
	
	/**
	 * Called from within FragMe when a new object has been loaded into the object manager
	 * Calls newFMeObject() on all relevant observers
	 * @param obj The new FMeObject
	 */
	public final void informNewFMeObjectObservers(FMeObject obj) {
		for (NewFMeObjectObserver observer : newFMeObjectObservers) {
			observer.newFMeObject(obj);
		}
	}
	
}
