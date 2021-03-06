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
package me.enerccio.sp.compiler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import me.enerccio.sp.errors.RuntimeError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.interpret.ModuleResolver;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.EllipsisObject;
import me.enerccio.sp.types.base.LabelObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Pair;
import me.enerccio.sp.utils.StaticTools.IOUtils;
import me.enerccio.sp.utils.Utils;

public class BlockDefinition {
	
	
	public enum DataTag {
		MODULE('M'), NONE('N'), INT('I'), LONG('L'), REAL('R'), COMPLEX('C'), BUILTINS('b'), STRING('S'), 
		ARRAY('A'), STRINGDICT_EMPTY_NEW('D'), ELLIPSIS('E'), BLOCK('B'), FNC('F'), LABEL('l')
		
		;
		
		DataTag(int id){
			this.id = (byte) id;
		}
		
		public final byte id;
		
		private static final DataTag[] tags;
		static {
			tags = new DataTag[255];
			for (DataTag dt : DataTag.values())
				tags[dt.id] = dt;
		}

		public static DataTag fromByte(byte readByte) {
			return tags[readByte];
		}
	}

	private byte[] bytecode;
	private ArrayList<String> idList = new ArrayList<String>();
	private Map<Integer, Pair<DataTag, Object>> di = new TreeMap<Integer, Pair<DataTag, Object>>();
	private Map<Integer, Pair<DataTag, Object>> tagobjects = new TreeMap<Integer, Pair<DataTag, Object>>();
	
	public BlockDefinition(CompiledBlockObject frame) {
		bytecode = frame.getBytedata().clone();
		
		for (Integer i : frame.dmap.keySet()){
			processDebug(i, frame.dmap.get(i));
		}
		
		for (Integer i : frame.mmap.keySet()){
			processEntry(i, frame.mmap.get(i));
		}
	}
	
	public CompiledBlockObject toFrame(final ModuleData mi) {
		CompiledBlockObject c = new CompiledBlockObject(bytecode);
		
		for (Integer i : tagobjects.keySet()){
			add(c.mmap, i, tagobjects.get(i), mi);
		}
		
		for (Integer i : di.keySet()){
			add(c.dmap, i, di.get(i), mi);
		}
		
		return c;
	}

	@SuppressWarnings("unchecked")
	private void add(NavigableMap<Integer, DebugInformation> dmap, Integer i, Pair<DataTag, Object> pair, final ModuleData mi) {
		Object[] arr = (Object[]) pair.getSecond();
		
		final String name = idList.get((Integer)((Pair<DataTag, Object>)arr[0]).getSecond());
		final String fname = idList.get((Integer)((Pair<DataTag, Object>)arr[1]).getSecond());
		
		DebugInformation di = new DebugInformation();
		di.function = idList.get((Integer)((Pair<DataTag, Object>)arr[2]).getSecond());
		di.lineno = (Integer)((Pair<DataTag, Object>)arr[3]).getSecond();
		di.charno = (Integer)((Pair<DataTag, Object>)arr[4]).getSecond();
		ModuleData mf = new ModuleData(){

			@Override
			public ModuleResolver getResolver() {
				return mi.getResolver();
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getFileName() {
				return fname;
			}

			@Override
			public String getPackageResolve() {
				return mi.getPackageResolve();
			}

			@Override
			public boolean isPackage() {
				return mi.isPackage();
			}
		};
		
		di.module = mf;
		dmap.put(i, di);
	}

	@SuppressWarnings("unchecked")
	private void add(Map<Integer, PythonObject> mmap, Integer i, Pair<DataTag, Object> pair, final ModuleData mi) {
		
		PythonObject v = null;
		switch (pair.getFirst()){
		case ARRAY:
			// won't be read here
			break;
		case BLOCK:
			// won't be read here
			break;
		case BUILTINS:
			v = PythonRuntime.runtime.getGlobals();
			break;
		case COMPLEX:
			Pair<DataTag, Object> a = (Pair<DataTag, Object>) ((Pair<DataTag, Object>)pair.getSecond()).getSecond();
			Object[] arr = (Object[]) a.getSecond();
			v = new ComplexObject(NumberObject.valueOf((Float)((Pair<DataTag, Object>) arr[0]).getSecond()), 
					NumberObject.valueOf((Float)((Pair<DataTag, Object>) arr[1]).getSecond()));
			break;
		case ELLIPSIS:
			v = EllipsisObject.ELLIPSIS;
			break;
		case FNC:
			a = (Pair<DataTag, Object>) (pair.getSecond());
			arr = (Object[]) a.getSecond();
			String name = (String) ((Pair<DataTag, Object>) arr[0]).getSecond();
			Object[] fncArgs = (Object[]) (((Pair<DataTag, Object>) arr[1]).getSecond());
			
			List<String> args = new ArrayList<String>();
			for (Object o : fncArgs){
				args.add((String) ((Pair<DataTag, Object>) o).getSecond());
			}
			
			Pair<DataTag, Object> docString = (Pair<DataTag, Object>) arr[2];
			
			CompiledBlockObject cbc = ((BlockDefinition)((Pair<DataTag, Object>) arr[3]).getSecond()).toFrame(mi);
			
			boolean hasVararg = ((Pair<DataTag, Object>) arr[4]).getSecond().equals(new Integer(1));
			String vararg = (String) ((Pair<DataTag, Object>) arr[5]).getSecond();
			boolean hasKWarg = ((Pair<DataTag, Object>) arr[6]).getSecond().equals(new Integer(1));
			String kwararg = (String) ((Pair<DataTag, Object>) arr[7]).getSecond();
			
			UserFunctionObject uof = new UserFunctionObject();
			Utils.putPublic(uof, "function_defaults", new StringDictObject());
			Utils.putPublic(uof, "__name__", new StringObject(name));
			if (docString.getFirst() == DataTag.NONE){
				Utils.putPublic(uof, "__doc__", NoneObject.NONE);
			} else {
				Utils.putPublic(uof, "__doc__", new StringObject((String)docString.getSecond()));
			}
			uof.block = cbc;
			uof.args = args;
			uof.isKvararg = hasKWarg;
			uof.isVararg = hasVararg;
			uof.vararg = vararg;
			uof.kvararg = kwararg;
			
			v = uof;
			break;
		case INT:
			v = NumberObject.valueOf((Integer)pair.getSecond());
			break;
		case LONG:
			v = NumberObject.valueOf((Long)pair.getSecond());
			break;
		case MODULE:
			// won't be read here
			break;
		case NONE:
			v = NoneObject.NONE;
			break;
		case REAL:
			v = NumberObject.valueOf((Float)pair.getSecond());
			break;
		case STRING:
			v = new StringObject((String)pair.getSecond(), false);
			break;
		case LABEL:
			arr = (Object[]) pair.getSecond();
			v = new LabelObject(
					(String)((Pair<DataTag, Object>)arr[1]).getSecond(),
					(Integer)((Pair<DataTag, Object>)arr[0]).getSecond()
				);
			break;
		case STRINGDICT_EMPTY_NEW:
			v = new StringDictObject();
			break;
		
		}
		mmap.put(i, v);
	}

	private void processDebug(Integer i, DebugInformation debugInformation) {
		di.put(i, toTagPair(debugInformation));
	}

	private void processEntry(Integer i, PythonObject pythonObject) {
		tagobjects.put(i, processEntry(pythonObject));
	}

	private Pair<DataTag, Object> processEntry(PythonObject o) {
		
		if (o == NoneObject.NONE){
			return Pair.makePair(DataTag.NONE, null);
		} else if (o instanceof NumberObject){
			NumberObject no = (NumberObject)o;
			if (NumberObject.isInteger(o)){
				return Pair.makePair(DataTag.INT, (Object)no.intValue());
			}
			if (NumberObject.isLong(o)){
				return Pair.makePair(DataTag.LONG, (Object)no.longValue());
			}
			if (NumberObject.isFloat(o)){
				return Pair.makePair(DataTag.REAL, (Object)no.floatValue());
			}
			if (NumberObject.isComplex(o)){
				Object[] components = new Object[2];
				ComplexObject co = (ComplexObject)no;
				components[0] = Pair.makePair(DataTag.REAL, (float)co.getRealValue());
				components[1] = Pair.makePair(DataTag.REAL, (float)co.getImaginaryValue());
				return Pair.makePair(DataTag.COMPLEX, (Object)Pair.makePair(DataTag.ARRAY, (Object)components));
			}
		} else if (o == PythonRuntime.runtime.getGlobals()){
			return Pair.makePair(DataTag.BUILTINS, null);
		} else if (o instanceof StringObject){
			return Pair.makePair(DataTag.STRING, (Object)((StringObject)o).value);
		} else if (o instanceof UserFunctionObject){
			
			UserFunctionObject fnc = (UserFunctionObject)o;
			
			Object fncName = ((StringObject)fnc.get("__name__", fnc)).value;
			Object[] args = new Object[fnc.args.size()];
			int i = 0;
			for (String arg : fnc.args){
				args[i] = Pair.makePair(DataTag.STRING, arg);
				++i;
			}
			Object docstring;
			if (fnc.get("__doc__", fnc) == null)
				docstring = Pair.makePair(DataTag.NONE, null);
			else {
				PythonObject pb = fnc.get("__doc__", fnc);
				if (pb == NoneObject.NONE)
					docstring = Pair.makePair(DataTag.NONE, null);
				else
					docstring = Pair.makePair(DataTag.STRING, ((StringObject)pb).value);
			}
			Object block = new BlockDefinition(fnc.block);
			Object hasArgs = Pair.makePair(DataTag.INT, fnc.isVararg ? 1 : 0);
			Object vararg = Pair.makePair(DataTag.STRING, fnc.vararg  == null ? "" : fnc.vararg);
			Object hasKwArgs = Pair.makePair(DataTag.INT, fnc.isKvararg ? 1 : 0);
			Object kwargarg = Pair.makePair(DataTag.STRING, fnc.kvararg == null ? "" : fnc.kvararg);
			
			Object[] data = new Object[]{Pair.makePair(DataTag.STRING, fncName), 
					Pair.makePair(DataTag.ARRAY, args), docstring, 
					Pair.makePair(DataTag.BLOCK, block),
					hasArgs, vararg, hasKwArgs, kwargarg, };
			return Pair.makePair(DataTag.FNC, (Object)Pair.makePair(DataTag.ARRAY, (Object)data));
		} else if (o instanceof StringDictObject && ((StringDictObject)o).len() == 0){
			return Pair.makePair(DataTag.STRINGDICT_EMPTY_NEW, null);
		} else if (o instanceof EllipsisObject){
			return Pair.makePair(DataTag.ELLIPSIS, null);
		} else if (o instanceof LabelObject){
			return Pair.makePair(DataTag.LABEL, (Object)(new Object[]{
					Pair.makePair(DataTag.INT, ((LabelObject)o).getPosition()),
					Pair.makePair(DataTag.STRING, ((LabelObject)o).getName())
			}));
		}
		
		throw new RuntimeError("Failed to compile, unknown data type " + o);
	}
	
	@SuppressWarnings("unchecked")
	public BlockDefinition(DataInputStream dis) throws Exception {
		int bclen = dis.readInt();
		bytecode = IOUtils.toByteArray(dis, bclen);
		
		Pair<DataTag, Object> pp = BlockDefinition.unpackTaggedData(dis);
		Object[] arr = (Object[])pp.getSecond();
		
		for (Object o : arr){
			idList.add((String) ((Pair<DataTag, Object>)o).getSecond());
		}
		
		int c = dis.readInt();
		for (int i=0; i<c; i++){
			Integer key = dis.readInt();
			Pair<DataTag, Object> data = BlockDefinition.unpackTaggedData(dis);
			di.put(key, data);
		}
		
		c = dis.readInt();
		for (int i=0; i<c; i++){
			Integer key = dis.readInt();
			Pair<DataTag, Object> data = BlockDefinition.unpackTaggedData(dis);
			tagobjects.put(key, data);
		}
		
	}
	
	private byte[] toBytes(int protocolVersion) throws Exception {
		ByteArrayOutputStream bof = new ByteArrayOutputStream();
		DataOutputStream wr = new DataOutputStream(bof);
		
		wr.writeInt(bytecode.length);
		wr.write(bytecode);
		
		Object[] idAr = new Object[idList.size()];
		for (int i=0; i<idAr.length; i++){
			idAr[i] = Pair.makePair(DataTag.STRING, (Object)idList.get(i));
		}
		wr.write(asBytes(Pair.makePair(DataTag.ARRAY, (Object)idAr), protocolVersion));
		
		wr.writeInt(di.size());
		for (Integer key : di.keySet()){
			wr.writeInt(key);
			Pair<DataTag, Object> d = di.get(key);
			
			wr.write(asBytes(d, protocolVersion));
		}
		
		wr.writeInt(tagobjects.size());
		for (Integer key : tagobjects.keySet()){
			wr.writeInt(key);
			wr.write(asBytes(tagobjects.get(key), protocolVersion));
		}
		
		wr.flush();
		return bof.toByteArray();
	}

	private Pair<DataTag, Object> toTagPair(DebugInformation d) {
		Object name = Pair.makePair(DataTag.INT, cache(d.module.getName()));
		Object filename = Pair.makePair(DataTag.INT, cache(d.module.getFileName()));
		Object function = Pair.makePair(DataTag.INT, cache(d.function));
		Object lineno = Pair.makePair(DataTag.INT, d.lineno);
		Object charno = Pair.makePair(DataTag.INT, d.charno);
		return Pair.makePair(DataTag.ARRAY, (Object)new Object[]{name, filename, function, lineno, charno});
	}

	private int cache(String text) {
		int id = idList.indexOf(text);
		if (id == -1){
			id = idList.size();
			idList.add(text);
		}
		return id;
	}

	@SuppressWarnings("unchecked")
	public static byte[] asBytes(Pair<DataTag, Object> o, int protocolVersion) throws Exception {
		ByteArrayOutputStream bof = new ByteArrayOutputStream();
		DataOutputStream wr = new DataOutputStream(bof);
		
		DataTag tag = o.getFirst();
		Object value = o.getSecond();
		
		wr.writeByte(tag.id);
		
		switch (tag){
		case ARRAY:
			Object[] arr = (Object[])value;
			wr.writeInt(arr.length);
			for (Object el : arr){
				wr.write(asBytes((Pair<DataTag, Object>) el, protocolVersion));
			}
			break;
		case BLOCK:
			BlockDefinition bd = (BlockDefinition)value;
			wr.write(bd.toBytes(protocolVersion));
			break;
		case COMPLEX:
			wr.write(asBytes((Pair<DataTag, Object>) value, protocolVersion));
			break;
		case FNC:
			wr.write(asBytes((Pair<DataTag, Object>) value, protocolVersion));
			break;
		case INT:
			wr.writeInt((Integer)value);
			break;
		case LONG:
			wr.writeLong((Long)value);
			break;
		case MODULE:
			wr.write(asBytes((Pair<DataTag, Object>) value, protocolVersion));
			break;
		case REAL:
			wr.writeFloat((Float)value);
			break;
		case STRING:
			byte[]  sb = ((String)value).getBytes("utf-8");
			wr.writeInt(sb.length);
			wr.write(sb);
			break;
		case LABEL:
			wr.writeInt( (Integer)((Pair<DataTag, Object>)(((Object[])value)[0])).getSecond() );
			sb = ((String)((Pair<DataTag, Object>)(((Object[])value)[1])).getSecond()).getBytes("utf-8");
			wr.writeInt(sb.length);
			wr.write(sb);
			break;
		case NONE:
		case ELLIPSIS:	
		case BUILTINS:
		case STRINGDICT_EMPTY_NEW:
			break;
		}
		
		wr.flush();
		return bof.toByteArray();
	}

	public static Pair<DataTag, Object> unpackTaggedData(DataInputStream dis) throws Exception {
		DataTag t = DataTag.fromByte(dis.readByte());
		Pair<DataTag, Object> pp = null;
		
		switch (t){
		case ARRAY:
			int c = dis.readInt();
			Object[] arr = new Object[c];
			for (int i=0; i<c; i++){
				arr[i] = unpackTaggedData(dis);
			}
			pp = Pair.makePair(t, (Object)arr);
			break;
		case BLOCK:
			pp = Pair.makePair(t, (Object)new BlockDefinition(dis));
			break;
		case COMPLEX:
			pp = Pair.makePair(t, (Object)unpackTaggedData(dis));
			break;
		case FNC:
			pp = Pair.makePair(t, (Object)unpackTaggedData(dis));
			break;
		case INT:
			pp = Pair.makePair(t, (Object)dis.readInt());
			break;
		case LONG:
			pp = Pair.makePair(t, (Object)dis.readLong());
			break;
		case MODULE:
			pp = Pair.makePair(t, (Object)unpackTaggedData(dis));
			break;
		case REAL:
			pp = Pair.makePair(t, (Object)dis.readFloat());
			break;
		case STRING:
			c = dis.readInt();
			byte[] bd = IOUtils.toByteArray(dis, c);
			pp = Pair.makePair(t, (Object)new String(bd, "utf-8"));
			break;
		case LABEL:
			int p = dis.readInt();
			c = dis.readInt();
			bd = IOUtils.toByteArray(dis, c);
			pp = Pair.makePair(DataTag.LABEL, (Object)(new Object[]{
					Pair.makePair(DataTag.INT, p),
					Pair.makePair(DataTag.STRING, (Object)new String(bd, "utf-8"))
			}));
			break;
		case NONE:
		case ELLIPSIS:	
		case BUILTINS:
		case STRINGDICT_EMPTY_NEW:
			pp = Pair.makePair(t, null);
		}
		
		return pp;
	}

}
