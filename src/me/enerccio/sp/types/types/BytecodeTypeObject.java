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
package me.enerccio.sp.types.types;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.runtime.ModuleInfo;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;


/**
 * bytecode()
 * @author Enerccio
 *
 */
public class BytecodeTypeObject extends TypeObject {
	private static final long serialVersionUID = 1434651099156262641L;
	public static final String BYTECODE_CALL = "bytecode";

	@Override
	public String getTypeIdentificator() {
		return "bytecode";
	}
	
	private static ModuleInfo mook = new ModuleInfo() {
		
		@Override
		public String getName() {
			return "<generated-module>";
		}
		
		@Override
		public ModuleProvider getIncludeProvider() {
			return null;
		}
		
		@Override
		public String getFileName() {
			return "<generated-module>";
		}
	};

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (args.len() == 0)
			throw Utils.throwException("TypeError", "bytecode(): incorrect number of parameters, must be >0");
		
		try {
			IntObject byteNum = (IntObject) args.getObjects()[0];
			
			Bytecode b = Bytecode.fromNumber((int) byteNum.intValue());
			if (b == null)
				throw Utils.throwException("TypeError", "bytecode(): unknown bytecode number " + byteNum);
			PythonBytecode bytecode = Bytecode.makeBytecode(b, null, null, mook);
			
			switch (b) {
			case ACCEPT_ITER:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case CALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case DEL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				bytecode.booleanValue = Coerce.toJava(args.get(2), boolean.class);
				break;
			case DELATTR:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case DUP:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case ECALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case GETATTR:
				break;
			case GET_ITER:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case GOTO:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case IMPORT:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				bytecode.object = Coerce.toJava(args.get(2), String.class);
				break;
			case ISINSTANCE:
				break;
			case JUMPIFFALSE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case JUMPIFNONE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case JUMPIFNORETURN:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case JUMPIFTRUE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case KWARG:
				bytecode.object = Coerce.toJava(args.get(1), String[].class);
				break;
			case LOAD:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case LOADBUILTIN:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case LOADGLOBAL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case NOP:
				break;
			case OPEN_LOCALS:
				break;
			case POP:
				break;
			case PUSH:
				bytecode.value = args.get(1);
				break;
			case PUSH_ENVIRONMENT:
				break;
			case PUSH_EXCEPTION:
				break;
			case PUSH_FRAME:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case PUSH_LOCALS:
				break;
			case PUSH_LOCAL_CONTEXT:
				break;
			case RAISE:
				break;
			case RCALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case RERAISE:
				break;
			case RESOLVE_ARGS:
				break;
			case RESOLVE_CLOSURE:
				break;
			case RETURN:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case SAVE:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SAVEGLOBAL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SAVE_LOCAL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SETATTR:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SETUP_LOOP:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case SWAP_STACK:
				break;
			case TRUTH_VALUE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case UNPACK_KWARG:
				break;
			case UNPACK_SEQUENCE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case YIELD:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				bytecode.intValue = Coerce.toJava(args.get(2), int.class);
				break;
			case KCALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			}
			
			bytecode.newObject();
			return bytecode;
		} catch (CastFailedException e){
			throw Utils.throwException("TypeError", "bytecode(): incorrect type of arguments");
		} catch (ArrayIndexOutOfBoundsException e){
			throw Utils.throwException("TypeError", "bytecode(): incorrect number of arguments");
		}
		
	}
}
