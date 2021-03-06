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
package me.enerccio.sp.types.callables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents user function (compiled).
 * @author Enerccio
 *
 */
public class UserFunctionObject extends CallableObject {
	private static final long serialVersionUID = 22L;
	
	/** Bytecode of this function */
	public CompiledBlockObject block;
	/** Arguments this function has */
	public List<String> args;
	/** Whether this function is vararg */
	public boolean isVararg;
	/** Vararg name */
	public String vararg;

	public boolean isKvararg = false;
	public String kvararg;
	
	public UserFunctionObject(){
		
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, "call"));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	/**
	 * Calls this function. Will insert onto frame stack and returns None.
	 * @param args
	 * @return
	 */
	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		TupleObject oargs = args;
		args = refillArgs(args, kwargs);
		int argc = args.len();
		int rargs = this.args.size();
		
		if (argc < rargs)
			throw new TypeError(fields.get("__name__").object + "(): incorrect amount of arguments, expected at least " + rargs + ", got " + argc);
		
		if (!isVararg && argc > rargs)
			throw new TypeError(fields.get("__name__").object + "(): incorrect amount of arguments, expected at most " + rargs + ", got " + argc);
			
		InternalDict a = new StringDictObject();
		
		List<PythonObject> vargs = new ArrayList<PythonObject>();
		for (int i=0; i<Math.max(oargs.len(), args.len()); i++){
			if (i < this.args.size())
				a.putVariable(this.args.get(i), args.getObjects()[i]);
			else
				vargs.add(oargs.getObjects()[i]);
		}
		
		if (isVararg){
			TupleObject t = (TupleObject) Utils.list2tuple(vargs, false);
			a.putVariable(vararg, t);
		}
		
		if (isKvararg){
			DictObject kwdict = args.convertKwargs(kwargs);
			a.putVariable(kvararg, kwdict);
		}
		
		PythonInterpreter.interpreter.get().setArgs(a);
		PythonInterpreter.interpreter.get().setClosure(closure);
		PythonInterpreter.interpreter.get().executeBytecode(block);
		
		return NoneObject.NONE; // returns immediately
	}

	/**
	 * Adds variables from defaults
	 * @param args
	 * @return
	 */
	public TupleObject refillArgs(TupleObject args, KwArgs kwargs) {
		InternalDict m = (InternalDict) fields.get("function_defaults").object;
		PythonObject[] pl = new PythonObject[this.args.size()];
		for (int i=0; i<pl.length; i++) {
			String key = this.args.get(i);
			if (i < args.len()) {
				// Argument passed in tuple
				pl[i] = args.get(i);
				if (kwargs != null && kwargs.contains(key))
					throw new TypeError(fields.get("__name__").object + "() got multiple values for keyword argument '" + key + "'");
			} else if ((kwargs != null) && (kwargs.contains(key))) {
				// Argument passed in kwargs
				pl[i] = kwargs.consume(key);
			} else if (m.containsVariable(key)) {
				// Argument filled from defaults
				pl[i] = m.getVariable(key);
			} else {
				// Missing argument
				throw new TypeError(fields.get("__name__").object + "() required argument '" + key + "' missing");
			}
		}
		if (kwargs != null && !isKvararg)
			kwargs.checkEmpty(fields.get("__name__").object + "()");
		return new TupleObject(true, pl); // pl.toArray(new PythonObject[pl.size()]));
	}

	@Override
	protected String doToString() {
		if (fields.get("__location__") != null)
			return "<function " + fields.get("__name__").object + " at " + fields.get("__location__").object.toString()  + ">";
		return "<function " + fields.get("__name__").object + ">";
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	private List<InternalDict> closure;
	public void setClosure(List<InternalDict> closure) {
		this.closure = closure;
	}
	
	public List<InternalDict> getClosure(){
		return closure;
	}
	
	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}
}
