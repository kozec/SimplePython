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
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.utils.Utils;

/**
 * PythonObject representing java strings
 * @author Enerccio
 *
 */
public class StringObject extends ImmutableSequenceObject implements SimpleIDAccessor {
	private static final long serialVersionUID = 11L;

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(ImmutableSequenceObject.getSFields());
			// __ADD__ is defined in SequenceObject
			sfields.put(NumberObject.__MUL__, new JavaMethodObject(StringObject.class, "mul", PythonObject.class));
			sfields.put(NumberObject.__MOD__, new JavaMethodObject(StringObject.class, "mod", PythonObject.class));
			sfields.put(NumberObject.__LT__, new JavaMethodObject(StringObject.class, "lt", PythonObject.class));
			sfields.put(NumberObject.__LE__, new JavaMethodObject(StringObject.class, "le", PythonObject.class));
			sfields.put(NumberObject.__EQ__, new JavaMethodObject(StringObject.class, "eq", PythonObject.class));
			sfields.put(NumberObject.__NE__, new JavaMethodObject(StringObject.class, "ne", PythonObject.class));
			sfields.put(NumberObject.__GE__, new JavaMethodObject(StringObject.class, "ge", PythonObject.class));
			sfields.put(NumberObject.__GT__, new JavaMethodObject(StringObject.class, "gt", PythonObject.class));
		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
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
	
	public StringObject(){
		newObject();
	}
	
	public StringObject(String v){
		newObject();
		value = v;
	}
	
	@Override
	public void newObject() {
		super.newObject();
	}
	
	public String value;
	
	@Override
	public int len() {
		return value.length();
	}
	
	@Override
	public int getId(){
		return value.hashCode();
	}

	public String getString() {
		return value;
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof StringObject)
			return value.equals(((StringObject)o).value);
		return false;
	}
	
	@Override
	protected String doToString() {
		return value;
	}

	@Override
	public PythonObject get(PythonObject key) {
		return doGet(this, key);
	}

	@Override
	public PythonObject __iter__() {
		PythonObject o = new OrderedSequenceIterator(this);
		o.newObject();
		return o;
	}
	
	@Override
	public PythonObject get(int i) {
		return valueAt(i);
	}

	@Override
	public PythonObject valueAt(int idx) {
		return new StringObject(Character.toString(value.charAt(idx)));
	}

	@Override
	public boolean containsItem(PythonObject o) {
		if (o instanceof StringObject)
			return value.contains(((StringObject)o).value);
		return false;
	}
	
	public String capitalize(){
		return value.toUpperCase();
	}

	@Override
	public void deleteKey(PythonObject key) {
		throw new TypeError("'" + Utils.run("typename", this) + "' object doesn't support item deletion");
	}

	public PythonObject mul(PythonObject b) {
		if (b instanceof NumberObject) {
			if (((NumberObject)b).getNumberType() == NumberObject.NumberType.INT) {
				// "a" * 5 -> "aaaaa"
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<((NumberObject)b).intValue(); i++)
					sb.append(value);
				return new StringObject(sb.toString());
			}
		}
		throw new TypeError("can't multiply sequence by non-int of type '" + b + "'");
	}

	public PythonObject mod(PythonObject b) {
		throw new TypeError("string format not yet supported"); // :(
	}

	public PythonObject add(PythonObject b) {
		if (b instanceof NumberObject)
			return new StringObject(value + b.toString());
		if (b instanceof StringObject)
			return new StringObject(value + b.toString());
		throw new TypeError("cannot concatenate 'str' and " + b);
	}

	public PythonObject lt(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) < 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;

	}

	public PythonObject le(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) <= 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}

	public PythonObject eq(PythonObject b) {
		if (b instanceof StringObject)
			return ((StringObject)b).value.equals(value) ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject ne(PythonObject b) {
		if (b instanceof StringObject)
			return ((StringObject)b).value.equals(value) ? BoolObject.FALSE : BoolObject.TRUE;
		return BoolObject.FALSE;
	}

	public PythonObject gt(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) > 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}

	public PythonObject ge(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) >= 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}
}
