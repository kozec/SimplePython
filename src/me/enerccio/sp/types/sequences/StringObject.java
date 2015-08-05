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

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.utils.ArgumentConsumer;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

/**
 * PythonObject representing java strings
 * @author Enerccio
 *
 */
public class StringObject extends ImmutableSequenceObject implements SimpleIDAccessor {
	private static final long serialVersionUID = 11L;
	
	public StringObject(){
		newObject();
	}
	
	public StringObject(String v){
		newObject();
		value = v;
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
		return Utils.doGet(this, key);
	}

	@Override
	public PythonObject __iter__() {
		PythonObject o = new OrderedSequenceIterator(this);
		o.newObject();
		return o;
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
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put("capitalize", JavaMethodObject.noArgMethod(StringObject.class, "capitalize"));
			sfields.put("center", new JavaMethodObject(StringObject.class, "center"));
			sfields.put("count", new JavaMethodObject(StringObject.class, "count"));
			sfields.put("endswith", new JavaMethodObject(StringObject.class, "endswith"));
			sfields.put("expandtabs", new JavaMethodObject(StringObject.class, "expandtabs"));
		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
		}
	}
	
	public String capitalize(){
		return value.toUpperCase();
	}
	
	public PythonObject center(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 2)
			throw Utils.throwException("TypeError", "center(): requires 1 or 2 arguments, " + to.len() + " provided");
		
		int llen = Coerce.argument(to, 0, "endwith", int.class);
		String fill = ArgumentConsumer.consumeArgument("center", to, kwargs, 1, "fillchar", String.class, " ");
		if (llen <= value.length())
			return new StringObject(value);
		StringBuilder strb = new StringBuilder();
		int ldiff = llen - value.length();
		for (int i=0; i<ldiff/2; i++)
			strb.append(fill);
		strb.append(value);
		for (int i=ldiff/2; i<ldiff; i++)
			strb.append(fill);
		return new StringObject(strb.toString());
	}
	
	public PythonObject count(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 3)
			throw Utils.throwException("TypeError", "center(): requires from 1 to 3 arguments, " + to.len() + " provided");
		
		String sub = Coerce.argument(to, 0, "endwith", String.class);
		int start = ArgumentConsumer.consumeArgument("count", to, kwargs, 1, "start", int.class, 0);
		int end = ArgumentConsumer.consumeArgument("count", to, kwargs, 2, "end", int.class, value.length()); 
		
		if (start < 0)
			start = Math.max(0, value.length()-(-(start+1)));
		if (end < 0)
			end = Math.max(0, value.length()-(-(end)));
		
		start = Math.max(start, 0);
		end = Math.min(value.length(), end);
		
		String substr = value.substring(0, end);
		int c = 0;
		for (int i=start; i<end; i++){
			if (substr.regionMatches(i, sub, 0, sub.length())){
				++c;
				i += sub.length();
			}
		}
		return IntObject.valueOf(c);
	}
	
	public PythonObject endswith(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 3)
			throw Utils.throwException("TypeError", "substring(): requires from 1 to 3 arguments, " + to.len() + " provided");

		String suffix = Coerce.argument(to, 0, "endwith", String.class);
		int start = ArgumentConsumer.consumeArgument("count", to, kwargs, 1, "start", int.class, 0);
		int end = ArgumentConsumer.consumeArgument("count", to, kwargs, 2, "end", int.class, value.length()); 
		
		if (start < 0)
			start = Math.max(0, value.length()-(-(start+1)));
		if (end < 0)
			end = Math.max(0, value.length()-(-(end)));
		
		start = Math.max(start, 0);
		end = Math.min(value.length(), end);
		
		String substr = value.substring(start, end);
		return BoolObject.fromBoolean(substr.endsWith(suffix));	
	}
	
	public PythonObject expandtabs(TupleObject to, KwArgs kwargs){
		if (to.len() > 1)
			throw Utils.throwException("TypeError", "substring(): requires at most 1 argument, " + to.len() + " provided");
		int tabs = ArgumentConsumer.consumeArgument("expandtabs", to, kwargs, 0, "tabsize", int.class, 8);
		
		StringBuilder bd = new StringBuilder();
		int column = 0;
		for (int i=0; i<value.length(); i++){
			char c = value.charAt(i);
			if (c == '\n' || c == '\r'){
				bd.append(c);
				column = 0;
				continue;
			}
			
			if (c == '\t'){
				int rem = tabs - (column%tabs);
				for (int j=0; j<rem; j++)
					bd.append(" ");
				column = 0;
				continue;
			}
			
			bd.append(c);
			++column;
		}
		
		return new StringObject(bd.toString());
	}
		
	
	@Override
	public void newObject() {	
		super.newObject();
		bindMethods(sfields);
	};
}
