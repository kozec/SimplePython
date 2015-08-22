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

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;

/**
 * Callable objects that interpret can call
 * @author Enerccio
 *
 */
public abstract class CallableObject extends PythonObject {
	private static final long serialVersionUID = 21L;
	public static final String __CALL__ = "__call__";
	
	public CallableObject(){
		super(false);
	}
	
	/**
	 * Calls this object with arguments in a tuple.
	 * @param args
	 * @return
	 */
	public abstract PythonObject call(TupleObject args, KwArgs kwargs);

	@Override
	public boolean truthValue() {
		return true;
	}
	
	public <T> T exec(Class<? extends T> cls, KwArgs kw, PythonObject... a) throws CastFailedException {
		return Coerce.toJava(
				PythonInterpreter.interpreter.get().execute(true, this, kw, a),
				cls);
	}
	
	public PythonObject exec(KwArgs kw, PythonObject... a) {
		return PythonInterpreter.interpreter.get().execute(true, this, kw, a);
	}

	public <T> T exec(Class<? extends T> cls, PythonObject... a) throws CastFailedException {
		return Coerce.toJava(
				PythonInterpreter.interpreter.get().execute(true, this, KwArgs.EMPTY, a),
				cls);
	}
	
	public PythonObject exec(PythonObject... a) {
		return PythonInterpreter.interpreter.get().execute(true, this, KwArgs.EMPTY, a);
	}
}
