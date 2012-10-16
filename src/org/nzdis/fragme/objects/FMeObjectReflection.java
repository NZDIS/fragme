/*
 * Created on 19/09/2005
 */
package org.nzdis.fragme.objects;

import java.io.Serializable;

/**
 * Implements the Reflection method of FMeObject change updating; that is, when
 * only a single field has been changed in the object. This class contains the
 * field to be changed, the value to change it to, and a unique global object
 * id.
 * 
 * @author purda621
 * @refactored Morgan Bruce 16/7/2008
 */
public class FMeObjectReflection implements Serializable {

	/**
	 * Default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of field to be changed
	 */
	private String fieldName;

	/**
	 * Value to change the field to
	 */
	private Object valueObject;

	/**
	 * Globally unique object id, denoting which object this field belongs to.
	 */
	private String objectId;

	/**
	 * Constructs a new FMeObjectReflection object
	 * 
	 * @param fieldName
	 *            name of field to be changed
	 * @param valueObject
	 *            value to change field to
	 * @param objectId
	 *            id of object field belongs to
	 */
	public FMeObjectReflection(String fieldName, Object valueObject,
			String objectId) {
		super();
		this.fieldName = fieldName;
		this.valueObject = valueObject;
		this.objectId = objectId;
	}

	/**
	 * @see org.globalse.fragme.objects.FMeObject#setId(int)
	 */
	public void setId(String id) {
		this.objectId = id;
	}

	/**
	 * @see org.globalse.fragme.objects.FMeObject#getId()
	 */
	public String getId() {
		return this.objectId;
	}

	/**
	 * @return Returns the value to change a field to
	 */
	public Object getValueObject() {
		return this.valueObject;
	}

	/**
	 * @param valueObject
	 *            Sets the value to change the field to
	 */
	public void setValueObject(Object valueObject) {
		this.valueObject = valueObject;
	}

	/**
	 * @return Returns the name of the field being changed
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * @param fieldName
	 *            Sets the value of fieldName
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}
