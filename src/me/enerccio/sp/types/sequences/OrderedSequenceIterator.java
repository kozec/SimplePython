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

import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.GeneratorObject;
import me.enerccio.sp.types.iterators.InternalIterator;
import me.enerccio.sp.utils.Utils;

/**
 * Sequence iterator for system classes list and tuple
 * @author Enerccio
 *
 */
public class OrderedSequenceIterator extends PythonObject implements InternalIterator {
	private static final long serialVersionUID = 4746975236443204424L;
	private SequenceObject sequence;
	private int cp = 0;
	private int len = 0;

	public OrderedSequenceIterator(SequenceObject sequenceObject) {
		this.sequence = sequenceObject;
		this.len = sequence.len();
	}
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put(SequenceObject.__ITER__,	JavaMethodObject.noArgMethod(OrderedSequenceIterator.class, "__iter__"));
			sfields.put(GeneratorObject.NEXT,		JavaMethodObject.noArgMethod(OrderedSequenceIterator.class, "next"));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		bindMethods(sfields);
	}
	
	@Override
	public PythonObject __iter__() {
		return this;
	}
	
	@Override
	public PythonObject next() {
		if (cp >= len)
			throw Utils.throwException("StopIteration");
		PythonObject value = PythonInterpreter.interpret.get().execute(false, Utils.get(sequence, SequenceObject.__GETITEM__), null, IntObject.valueOf(cp++));
		return value;
	}
	
	@Override
	public PythonObject nextInternal() {
		if (cp >= len)
			return null;
		PythonObject value = PythonInterpreter.interpret.get().execute(false, Utils.get(sequence, SequenceObject.__GETITEM__), null, IntObject.valueOf(cp++));
		return value;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<iterator of " + sequence.toString()  + ">";
	}

}
