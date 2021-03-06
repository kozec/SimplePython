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
package me.enerccio.sp.types.sequences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.IndexError;
import me.enerccio.sp.errors.StopIteration;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.InternalIterator;
import me.enerccio.sp.types.iterators.InternallyIterable;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.utils.Utils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Python list representation.
 * @author Enerccio
 *
 */
public class ListObject extends MutableSequenceObject implements SimpleIDAccessor, InternallyIterable  {
	private static final long serialVersionUID = 16L;
	public List<PythonObject> objects = Collections.synchronizedList(new ArrayList<PythonObject>());
	
	public ListObject(){
		super(false);
	}
	
	public ListObject(SequenceObject o) {
		super(false);
		for (int i = 0; i<o.len(); i++)
			append(o.get(NumberObject.valueOf(i)));
	}
	
	/** If passed object is iterable or has __GETITEM__ defined, creates list filled with objects in this list */ 
	public ListObject(PythonObject o) {
		super(false);
		PythonObject iter = o.get(__ITER__, null);
		try {
			PythonObject iterator;
			if (iter == null) {
				// Use iter() function to grab iterator
				iterator = Utils.run("iter", o);
			} else {
				iterator = PythonInterpreter.interpreter.get().execute(true, iter, null);
				if (iterator instanceof InternalIterator) {
					InternalIterator ii = (InternalIterator)iterator;
					PythonObject item = ii.next();
					while (item != null) {
						append(item);
						item = ii.next();
					}
					return;
				}
			}
			PythonObject next = iterator.get("next", null);
			if (next == null)
				throw new TypeError("iterator of " + o.toString() + " has no next() method");
			while (true) {
				PythonObject item = PythonInterpreter.interpreter.get().execute(true, next, null);
				append(item);
			}
		} catch (StopIteration e){
			return;
		} catch (PythonExecutionException e) {
			if (PythonRuntime.isinstance(e.getException(), PythonRuntime.STOP_ITERATION).truthValue())
				; // nothing
			else if (PythonRuntime.isinstance(e.getException(), PythonRuntime.INDEX_ERROR).truthValue())
				; // still nothing
			else
				throw e;
		}
	}

	public ListObject(String... strings) {
		this();
		for (String s : strings)
			objects.add(new StringObject(s));
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(MutableSequenceObject.getSFields());
			sfields.put("append", new JavaMethodObject(ListObject.class, "append", PythonObject.class));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	protected static Map<String, JavaMethodObject> getSFields(){ return sfields; }
	
	@Override
	public Set<String> getGenHandleNames() {
		return sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return sfields;
	}
	
	@Override
	public void newObject() {
		super.newObject();
	}
	
	public synchronized PythonObject append(PythonObject value){
		objects.add(value);
		return this;
	}
	
	public PythonObject add(PythonObject b) {
		if (b instanceof ListObject) {
			ListObject l = new ListObject();
			for (PythonObject o : objects)
				l.objects.add(o);
			for (PythonObject o : ((ListObject)b).objects)
				l.objects.add(o);
			return l;
		}
		throw new TypeError("can only concatenate list (not '" + b.toString() + "') to list");
	}
	
	@Override
	public int len() {
		return objects.size();
	}
	
	@Override
	protected String doToString() {
		return objects.toString();
	}

	@Override
	public int hashCode(){
		return super.hashCode();
	}

	@Override
	public PythonObject get(int i) {
		if (i >= objects.size() || i<-(objects.size()))
			throw new IndexError("Incorrect index, expected (" + -objects.size() + ", " + objects.size() + "), got " + i);
		return objects.get(morphAround(i, objects.size()));
	}
	
	@Override
	public PythonObject get(PythonObject key) {
		if (key instanceof SliceObject){
			ListObject lo = new ListObject();
			
			int[] slicedata = getSliceData(objects.size(), key);
			int sav = slicedata[0];
			int sov = slicedata[1];
			int stv = slicedata[2];
			boolean reverse = slicedata[3] == 1;
			
			synchronized (objects){
				if (sav <= sov)
					for (int i=sav; i<sov; i+=stv)
						lo.objects.add(objects.get(i));
				else
					for (int i=sov; i<sav; i+=stv)
						lo.objects.add(objects.get(i));
				if (reverse)
					synchronized (lo.objects){
						Collections.reverse(lo.objects);
					}
			}
			
			return lo;
		} else 
		return doGet(this, key);
	}

	@Override
	public PythonObject __iter__() {
		return new OrderedSequenceIterator(this);
	}
	
	@Override
	public PythonObject valueAt(int idx) {
		return objects.get(idx);
	}

	@Override
	public PythonObject set(PythonObject key, PythonObject value) {
		
		if (NumberObject.isInteger(key)) {
			int i = ((NumberObject)key).intValue();
			if (i >= len() || i<-(len()))
				throw new IndexError("incorrect index, expected (" + -len() + ", " + len() + "), got " + i);
			int idx = morphAround(i, len());
			objects.set(idx, value);
		} else if (key instanceof SliceObject){
			
		} else {
			throw new TypeError("key must be int or slice");
		}
		
		return this;
	}

	@Override
	public boolean containsItem(PythonObject o) {
		return objects.contains(o);
	}

	@Override
	public synchronized void deleteKey(PythonObject key) {
		if (key instanceof SliceObject){
			throw new NotImplementedException();
		}
		PythonObject idx = key;
		if (!NumberObject.isInteger(idx))
			throw new TypeError("Index must be int");
		int i = ((NumberObject)idx).intValue();
		if (i >= len() || i<-(len()))
			throw new IndexError("Incorrect index, expected (" + -len() + ", " + len() + "), got " + i);
		objects.remove((morphAround(i, len())));
	}

	@Override
	public PythonObject mul(PythonObject b) {
		if (b instanceof NumberObject && NumberObject.isInteger(b)){
			NumberObject no = (NumberObject)b;
			int cnt = no.intValue();
			ListObject ret = new ListObject();
			synchronized (objects){
				for (int i=0; i<cnt; i++){
					ret.objects.addAll(objects);
				}
			}
			return ret;
		}
		throw new TypeError("unsupported operand type(s) for *: '" + this + "' and '" + b + "'");
	}
}