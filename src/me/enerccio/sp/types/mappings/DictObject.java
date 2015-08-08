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
package me.enerccio.sp.types.mappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Implementation of the python dict
 * @author Enerccio
 *
 */
public class DictObject extends ContainerObject {
	private static final long serialVersionUID = 20L;
	public static final String __GETITEM__ = "__getitem__";
	public static final String __SETITEM__ = "__setitem__";
	public static final String __LEN__ = "__len__";
	
	public DictObject(){
		newObject();
	}
	
	public DictObject(Map<Integer, PythonObject> mmap) {
		newObject();
		for (Integer k : mmap.keySet())
			backingMap.put(IntObject.valueOf(k), mmap.get(k));
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put(__GETITEM__,	new JavaMethodObject(DictObject.class, "getItem"));
			sfields.put(__SETITEM__,	new JavaMethodObject(DictObject.class, "setItem")); 
			sfields.put("keys",			new JavaMethodObject(DictObject.class, "keys")); 
			sfields.put("values",		new JavaMethodObject(DictObject.class, "values"));
		} catch (NoSuchMethodException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		bindMethods(sfields);
	};
	
	public HashHashMap<PythonObject> backingMap = new HashHashMap<PythonObject>();
	
	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("typename", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions, PythonObject localContext) {
		
	}

	// Internal use only
	public synchronized boolean contains(String key) {
		synchronized (backingMap){
			return backingMap.containsKey(new StringObject(key));
		}
	}

	public synchronized void put(String key, PythonObject value) {
		synchronized (backingMap){
			backingMap.put(new StringObject(key), value);
		}
	}
	
	public synchronized PythonObject getItem(String key) {
		StringObject so = new StringObject(key);
		synchronized (backingMap){
			if (!backingMap.containsKey(so))
				return null;
			return backingMap.get(so);
		}
	}
	
	public synchronized PythonObject getItem(TupleObject a, KwArgs kwargs){
		if (kwargs != null) kwargs.checkEmpty(__GETITEM__);
		if (a.len() != 1)
			throw Utils.throwException("TypeError", "__getitem__(): requires 1 parameter");
		PythonObject key = a.getObjects()[0];
		synchronized (backingMap){
			if (!backingMap.containsKey(key))
				throw Utils.throwException("KeyError", "Unknown key " + key);
			return backingMap.get(key);
		}
	}
	
	public synchronized PythonObject setItem(TupleObject a, KwArgs kwargs){
		if (kwargs != null) kwargs.checkEmpty(__SETITEM__);
		if (a.len() != 2)
			throw Utils.throwException("TypeError", "__setitem__(): requires 2 parameters");
		PythonObject key = a.getObjects()[0];
		PythonObject value = a.getObjects()[1];
		synchronized (backingMap){
			return backingMap.put(key, value);
		}
	}
	
	@Override
	public int len() {
		synchronized (backingMap){
			return backingMap.size();
		}
	}

	public synchronized PythonObject doGet(String str) {
		return doGet(new StringObject(str));
	}

	public synchronized PythonObject doGet(PythonObject key) {
		return backingMap.get(key);
	}
	
	public PythonObject keys(TupleObject t, KwArgs kwargs) {
		if (kwargs != null) kwargs.checkEmpty("keys");
		ListObject o = new ListObject();
		o.newObject();
		synchronized (backingMap) {
			for (PythonProxy k : backingMap.keySet())
				o.append(k.o);
		}
		return o;
	}
	
	public Set<String> keys() {
		Set<String> rv = new HashSet<>();
		synchronized (backingMap) {
			for (PythonProxy k : backingMap.keySet())
				rv.add(k.toString());
		}
		return rv;
	}

	
	public PythonObject values(TupleObject t, KwArgs kwargs) {
		if (kwargs != null) kwargs.checkEmpty("values");
		ListObject o = new ListObject();
		o.newObject();
		synchronized (backingMap) {
			for (PythonObject k : backingMap.values())
				o.append(k);
		}
		return o;
	}
	
	private static ThreadLocal<Set<DictObject>> printMap = new ThreadLocal<Set<DictObject>>(){

		@Override
		protected Set<DictObject> initialValue() {
			return new HashSet<DictObject>();
		}
		
	};
	@Override
	protected String doToString() {
		if (printMap.get().contains(this))
			return "...";
		else try {
			printMap.get().add(this);
			return backingMap.toString();
		} finally {
			printMap.get().remove(this);
		}
	}

	public DictObject cloneMap() {
		DictObject c = new DictObject();
		synchronized (backingMap){
			c.backingMap.putAll(backingMap);
		}
		return c;
	}
	
	@Override
	public int getId(){
		throw Utils.throwException("TypeError", "unhashable type '" + Utils.run("typename", this) + "'");
	}

	@Override
	public boolean containsItem(PythonObject o) {
		synchronized (backingMap){
			return backingMap.containsKey(o);
		}
	}

	public HashMap<String, PythonObject> asStringDict() {
		HashMap<String, PythonObject> strDict = new HashMap<String, PythonObject>();
		synchronized (backingMap){
			for (PythonProxy pp : backingMap.keySet()){
				strDict.put(((StringObject)Utils.run("str", pp.o)).value, backingMap.get(pp));
			}
		}
		return strDict;
	}

	@Override
	public synchronized void deleteKey(PythonObject key) {
		if (!backingMap.containsKey(key))
			throw Utils.throwException("NameError", "__delkey__(): unknown key '" + key.toString() + "'");
		backingMap.remove(key);
	}
}
