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
package me.enerccio.sp;

import java.util.Collection;

import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerFinalizer;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Coerce;

public class SimplePython {

	private static PythonRuntime r;
	
	public static void initialize(){
		r = PythonRuntime.runtime;
	}

	public static void addResolver(PythonDataSourceResolver resolver){
		r.addResolver(resolver);
	}
	
	public static void addAlias(Class<?> cls, String alias){
		addAlias(cls.getName(), alias);
	}

	public static void addAlias(String name, String alias) {
		r.addAlias(name, alias);
	}
	
	public static String serialize() throws Exception{
		return r.serializeRuntime();
	}
	
	public static ModuleObject getModule(String pythonPath){
		return r.getModule(pythonPath);
	}
	
	public static void addExcludePackageOrClass(String path){
		r.addExcludePackageOrClass(path);
	}
	
	public static void setAllowAutowraps(boolean autowraps){
		r.setAllowAutowraps(autowraps);
	}
	
	public static void addFactory(String packagePath, Class<? extends PointerFactory> clazz){
		r.addFactory(packagePath, clazz);
	}
	
	public static PythonObject asTuple(Collection<?> c){
		PythonObject[] values = new PythonObject[c.size()];
		int i=0;
		for (Object o : c){
			values[i++] = Coerce.toPython(o);
		}
		
		TupleObject t = new TupleObject(values);
		t.newObject();
		return t;
	}
	
	public static void setField(PythonObject object, String fieldName, PythonObject value){
		PythonRuntime.setattr(object, fieldName, value);
	}
	
	public static PythonObject getField(PythonObject o, String fieldName){
		return PythonRuntime.getattr(o, fieldName);
	}
	
	public static PythonObject executeFunction(String module, String function){
		return executeFunction(getModule(module), function);
	}
	
	public static PythonObject executeFunction(ModuleObject module, String function){
		CallableObject c = (CallableObject) getField(module, function);
		if (c == null)
			return null;
		
		c.call(new TupleObject(), null);
		return PythonInterpreter.interpreter.get().executeAll(0);
	}
	
	public static void addFinalizer(Class<?> cls, PointerFinalizer finalizer){
		addFinalizer(cls.getName(), finalizer);
	}

	public static void addFinalizer(String name, PointerFinalizer finalizer) {
		r.addPointerFinalizer(name, finalizer);
	}

	public static PythonRuntime getRuntime() {
		return r;
	}
	
}
