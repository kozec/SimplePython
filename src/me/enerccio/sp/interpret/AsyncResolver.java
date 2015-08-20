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

import java.util.concurrent.Semaphore;

import me.enerccio.sp.runtime.ModuleProvider;

public abstract class AsyncResolver implements PythonDataSourceResolver, IAsyncResolver {
	
	private ModuleProvider p;
	
	@Override
	public ModuleProvider resolve(String name, String resolvePath) {
		Semaphore s = new Semaphore(0);
		
		ResolveModuleAsync rma = new ResolveModuleAsync();
		rma.s = s;
		rma.name = name;
		rma.resolvePath = resolvePath;
		
		getProviderAsync(rma);
		
		try {
			s.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		return p;
	}
	
	@Override
	public void setModuleProvider(ModuleProvider o){
		p = o;
	}

	public abstract void getProviderAsync(ResolveModuleAsync resolve);
}
