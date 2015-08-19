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
package me.enerccio.sp.interpret;

import java.io.InputStream;

import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.utils.StaticTools.IOUtils;

/**
 * PathResolver which searches root of the jar/java path for .spy.
 * @author Enerccio
 *
 */
public class InternalJavaPathResolver implements PythonDataSourceResolver {

	public InternalJavaPathResolver(){
		
	}
	
	@Override
	public ModuleProvider resolve(String name, String resolvePath) {
		if (name.contains("."))
			return null;
		try {
			InputStream iss = PythonRuntime.runtime.getClass().getClassLoader().getResourceAsStream(name + ".pyc");
			return doResolvePyc(iss, name+".pyc", name);
		} catch (Exception e) {
			try {
				InputStream is = PythonRuntime.runtime.getClass().getClassLoader().getResourceAsStream(name + ".py");
				return doResolve(is, name+".py", name);
			} catch (Exception e2){
				
			}
			return null;
		}
	}

	private ModuleProvider doResolvePyc(InputStream is, String fname, String mname) throws Exception {
		if (is == null) return null;
		return new ModuleProvider(mname, null, fname, "", false, true, false, null, IOUtils.toByteArray(is));
	}

	private ModuleProvider doResolve(InputStream is, String fname, String mname) throws Exception {
		if (is == null) return null;
		return new ModuleProvider(mname, IOUtils.toByteArray(is), fname, "", false, false, false, null, null);
	}

}
