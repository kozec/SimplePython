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
package me.enerccio.sp.types.iterators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * xrange implementation
 */
public class XRangeIterator extends PythonObject {
	private static final long serialVersionUID = -543998207864616108L;
	public static final String __ITER__ = SequenceObject.__ITER__;
	public static final String NEXT =  "next";
			
	private int i, end, step;

	public XRangeIterator(int start, int end, int step) {
		this.i = start;
		this.end = end;
		this.step = step;
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __ITER__, new JavaMethodObject(null, XRangeIterator.class.getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, NEXT, new JavaMethodObject(null, XRangeIterator.class.getMethod("next", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __ITER__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = NEXT;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	@Override
	protected String doToString() {
		return "<rangeiterator object at " + this.getId() + ">";
	}

	public PythonObject __iter__(TupleObject t) {
		return this;
	}
	
	/** 
	 * Internal version.
	 * Works as next(TupleObject t) called from python, but return null instead of calling StopIteration.
	 */
	public PythonObject next() {
		if (step > 0) {
			// Goes up
			if (i >= end)
				return null;
		} else {
			// Goes down
			if (i <= end)
				return null;
		}
		IntObject rv = IntObject.valueOf(i);
		i += step;
		return rv;
	}
	
	public PythonObject next(TupleObject t) {
		PythonObject rv = next();
		if (rv == null)
			throw Utils.throwException("StopIteration");
		return rv;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

}