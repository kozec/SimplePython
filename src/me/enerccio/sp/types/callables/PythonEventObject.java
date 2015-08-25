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

import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject.SpyDoc;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonEventObject extends CallableObject {
	private static final long serialVersionUID = 894516515215151L;
	public static final String __HANDLERS__ = "__handlers__";
	public static final String __ADDHANDLER__ = "__addhandler__";
	private final String name;
	private final List<String> args;
	private final ListObject handlerList = new ListObject();
	private final int argCount;
	
	public PythonEventObject(String name, List<String> args) {
		this.name = name;
		this.args = args;
		this.argCount = this.args.size();
		Utils.putPublic(this, PythonEventObject.__HANDLERS__, handlerList);
	}
	
	@Override
	protected void newObject() {
		super.newObject();	
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, "call"));
			JavaMethodObject ah = new JavaMethodObject(this, "addHandler");
			Utils.putPublic(this, PythonEventObject.__ADDHANDLER__, ah);
			Utils.putPublic(this, NumberObject.__LSHIFT__, ah);
		} catch (NoSuchMethodException e){
			throw new RuntimeException("This shouldn't happen", e);
		}
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.getSFields().keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.getSFields();
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<event " + name + ", " + handlerList.len() + " handlers >";
	}

	@SpyDoc({
		"Adds new handler to event. Same handler can be registered only once.",
		"",
		"Returns True if handler was added or False if handler was already registered for same event"
	})
	public PythonObject addHandler(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.notExpectingKWArgs();
		if (args.len() != 1)
			throw new TypeError(__ADDHANDLER__ + "() takes exactly 1 argument");
		PythonObject handler = args.valueAt(0);
		if (!Utils.run("callable", handler).truthValue())
			throw new TypeError(__ADDHANDLER__ + "() 1st argument has to be callable");
		if (handlerList.containsItem(handler))
			return BoolObject.FALSE;
		handlerList.append(handler);
		return BoolObject.FALSE;
	}


	@Override
	@SpyDoc({
		"Fires event. Calls all handlers in order, last added first, until one of handlers returns True.",
		"",
		"Returns True if any handler did so or False to signalize that none of hanlders handled event."
	})
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			throw new TypeError("event cannot be fired using keyword arguments");
		if (args.len() != this.args.size())
			throw new TypeError("event " + name + " is fired with exactly " + this.args.size() + " arguments (" + args.len() + " given)");
		
		PythonObject[] eargs = new PythonObject[argCount + 1];
		eargs[0] = this;
		for (int i=0; i<argCount; i++)
			eargs[i + 1] = args.valueAt(i);
		PythonInterpreter i = PythonInterpreter.interpreter.get();
		for (PythonObject o : handlerList.objects)
			if (i.execute(true, o, KwArgs.EMPTY, eargs).truthValue())
				return BoolObject.TRUE;
		
		return BoolObject.FALSE;
	}

	
}
