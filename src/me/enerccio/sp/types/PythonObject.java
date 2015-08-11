/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.types;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents every single python object. Root of the hierarchy.
 * @author Enerccio
 *
 */
public abstract class PythonObject implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String __CLASS__ = "__class__";
	public static final String __FORMAT__ = "__format__";
	public static final String __EQ__ = "__eq__";
	public static final String __NE__ = "__ne__";
	
	public PythonObject(){
		newObject();
	}
	
	protected static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	static {
		try {
			sfields.put(__EQ__, 		 new JavaMethodObject(PythonObject.class, "eq", PythonObject.class));
			sfields.put(__NE__, 		 new JavaMethodObject(PythonObject.class, "ne", PythonObject.class));
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
	
	protected static Map<String, JavaMethodObject> getSFields(){
		return sfields;
	}
	
	public abstract Set<String> 					 getGenHandleNames();
	protected abstract Map<String, JavaMethodObject> getGenHandles();
	
	protected void bindMethod(String name, JavaMethodObject m) {
		fields.put(name, new AugumentedPythonObject(m.cloneWithThis(this), AccessRestrictions.PUBLIC));
	}
	
	/**
	 * Should be called only once to initialize methods of the object
	 */
	protected void newObject(){
		registerObject();
		if (getType() == null)
			throw new NullPointerException("Type for " + this.getClass().getName() + " is NULL");
		Utils.putPublic(this, __CLASS__, getType());
	}

	/**
	 * Initializes basic method of all objects, EQ, NE and NOT
	 */
	protected void registerObject(){
		PythonRuntime.runtime.newInstanceInitialization(this);
	}
	
	public PythonObject eq(PythonObject other){
		return BoolObject.fromBoolean(other == this);
	}
	
	public PythonObject ne(PythonObject other){
		return BoolObject.fromBoolean(other != this);
	}

	/**
	 * Returns type of this object
	 * @return
	 */
	public ClassObject getType(){
		return PythonRuntime.getType(this);
	}
	
	/**
	 * Returns id of this object
	 * @return
	 */
	public int getId(){
		return hashCode();
	}
	
	/**
	 * Returns truth value of this object
	 * @return
	 */
	public abstract boolean truthValue();
	
	/**
	 * Fields of this object are stored here.
	 */
	public Map<String, AugumentedPythonObject> fields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	private Map<String, SoftReference<JavaMethodObject>> genFields = 
			Collections.synchronizedMap(new HashMap<String, SoftReference<JavaMethodObject>>());
	
	/**
	 * Returns the field value for the key and local context
	 * @param key
	 * @param localContext
	 * @return
	 */
	public synchronized PythonObject get(String key, PythonObject localContext) {
		if (!fields.containsKey(key))
			return possiblyGenHandle(key);
		AugumentedPythonObject field = fields.get(key);
		if (field == null)
			return null;
		if (field.restrictions == AccessRestrictions.PRIVATE && !isPrivate(localContext, field))
			throw new AttributeError("access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("type", this)) + "'");
		return field.object;
	}

	/**
	 * Returns the field value, ignoring AccessRestrictions
	 * @param key
	 * @return
	 */
	public synchronized PythonObject get(String key) {
		AugumentedPythonObject field = fields.get(key);
		if (field == null)
			return null;
		return field.object;
	}

	protected PythonObject possiblyGenHandle(String key) {
		SoftReference<JavaMethodObject> ref = genFields.get(key);
		JavaMethodObject holder = null;
		holder = ref != null ? ref.get() : null;
		if (holder == null){
			if (getGenHandleNames().contains(key)){
				holder = getGenHandles().get(key).cloneWithThis(this);
				ref = new SoftReference<JavaMethodObject>(holder);
				genFields.put(key, ref);
			}
		}
		return ref == null ? null : ref.get();
	}

	/**
	 * Sets the value of the field into value with local context
	 * @param key
	 * @param localContext
	 * @param value
	 * @return
	 */
	public synchronized PythonObject set(String key, PythonObject localContext, PythonObject value){
		if (!fields.containsKey(key))
			if (!create(key))
				throw new AttributeError("'" + 
						Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && !isPrivate(localContext, field))
			throw new AttributeError("access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("type", this)) + "'");
		field.object = value;
		if (value == null)
			fields.remove(key);
		return NoneObject.NONE;
	}
	
	/**
	 * Removes the value of the field into value with local context
	 */
	public PythonObject delete(String key, PythonObject localContext) {
		if (!fields.containsKey(key))
			throw new AttributeError("'" + Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		fields.get(key);	// Throws exception if key is private
		fields.remove(key);
		return null;
	}

	/**
	 * Checks if field is private
	 * @param localContext
	 * @param p
	 * @return
	 */
	private boolean isPrivate(PythonObject localContext, AugumentedPythonObject p) {
		if (localContext == null)
			return false;
		
		if (this instanceof ClassInstanceObject) {
			if (localContext instanceof ClassObject){
				return p.owner == null ? true : (localContext == p.owner);
			}
			return false;
		}
		
		return true;
	}

	/**
	 * Creates new empty field. Should return false if creating fields is not allowed
	 */
	protected synchronized boolean create(String key) {
		if (fields.containsKey(key))
			throw new AttributeError("'" + Utils.run("str", Utils.run("type", this)) + "' object already has a attribute '" + key + "'");
		AugumentedPythonObject field = new AugumentedPythonObject(NoneObject.NONE, AccessRestrictions.PUBLIC);
		fields.put(key, field);
		return true;
	}

	/** Marks field as private */
	final public void setPrivate(String key, PythonObject owner) {
		if (!fields.containsKey(key))
			throw new AttributeError("'" + Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		fields.put(key, new AugumentedPythonObject(fields.get(key).object, AccessRestrictions.PRIVATE, owner));
	}
	
	@Override
	public boolean equals(Object o){
		return o == this;
	}
	
	@Override
	public final String toString(){
		return doToString();
	}

	/**
	 * Returns string representation of the object
	 * @return
	 */
	protected abstract String doToString();
	
	public volatile boolean mark = false;
	public long linkName;
}
