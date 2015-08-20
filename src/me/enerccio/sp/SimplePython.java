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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.ModuleResolver;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerFinalizer;
import me.enerccio.sp.types.sequences.TupleObject;

public class SimplePython {

	private static PythonRuntime r;
	public static List<File> pycCaches = new ArrayList<>();
	
	public static void initialize(){
		r = PythonRuntime.runtime;
	}

	/** 
	 * Adds new pyc files cache directory. Specified path must exists and must be writable.
	 * Last path added will be used for writing cache files. When cached file is being searched,
	 * last added path takes precedence and former is searched only if file is not found. 
	 * */ 
	public static void addPycCache(File path) {
		pycCaches.add(0, path);
	}
	
	/**
	 * Adds the data source resolver. Data source resolver is the way for Simple Python to load modules.
	 * Every resolver acts as a root for some part of the python path. If you need standard python path,
	 * use PythonPathResolver. Internally, SimplePython uses InternalJavaPathResolver.
	 * @param resolver PythonDataSourceResolver instance that will be queried for a module, in the order of the 
	 * insertion. First resolver that returns the module will be the one used.
	 * @see me.enerccio.sp.interpret.FilesystemResolver
	 */
	public static void addResolver(ModuleResolver resolver){
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
	
	public static PythonObject executeFunction(String module, String function){
		return executeFunction(getModule(module), function);
	}
	
	public static PythonObject executeFunction(ModuleObject module, String function){
		CallableObject c = (CallableObject) module.getField(function);
		if (c == null)
			throw new TypeError("Module has no function '" + function + "'");
		
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
