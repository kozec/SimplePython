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

import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.PythonObject;
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
			sfields.put(Arithmetics.__MUL__, new JavaMethodObject(StringObject.class, "mul", PythonObject.class));
			sfields.put(Arithmetics.__MOD__, new JavaMethodObject(StringObject.class, "mod", PythonObject.class));
			sfields.put(Arithmetics.__LT__, new JavaMethodObject(StringObject.class, "lt", PythonObject.class));
			sfields.put(Arithmetics.__LE__, new JavaMethodObject(StringObject.class, "le", PythonObject.class));
			sfields.put(Arithmetics.__EQ__, new JavaMethodObject(StringObject.class, "eq", PythonObject.class));
			sfields.put(Arithmetics.__NE__, new JavaMethodObject(StringObject.class, "ne", PythonObject.class));
			sfields.put(Arithmetics.__GE__, new JavaMethodObject(StringObject.class, "ge", PythonObject.class));
			sfields.put(Arithmetics.__GT__, new JavaMethodObject(StringObject.class, "gt", PythonObject.class));

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
	
	public PythonObject mul(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__MUL__);
	}
	
	public PythonObject mod(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__MOD__);
	}

	public PythonObject lt(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__LT__);
	}
	
	public PythonObject le(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__LE__);
	}
	
	public PythonObject eq(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__EQ__);
	}
	
	public PythonObject ne(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__NE__);
	}
	
	public PythonObject gt(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__GT__);
	}
	
	public PythonObject ge(PythonObject arg){
		return Arithmetics.doOperatorString(this, arg, Arithmetics.__GE__);
	}	
	
	@Override
	public void deleteKey(PythonObject key) {
		throw Utils.throwException("TypeError", "'" + Utils.run("typename", this) + "' object doesn't support item deletion");
	}
}
