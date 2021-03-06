package me.enerccio.sp.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.enerccio.sp.errors.PythonException;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.types.ListTypeObject;

public class Coerce {
	private static final Map<Class<?>, Coercion> COERCIONS = new HashMap<>();

	private interface Coercion {
		public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException;
	}

	/** Coerces PythonObject to specified java class, if possible */
	@SuppressWarnings("unchecked")
	public static <X> X toJava(PythonObject o, Class<X> clazz) throws CastFailedException {
		// 0th, can't do null
		if (o == null)
			throw new CastFailedException("Can't coerce null");
		
		// 1st, return array if requested
		if (clazz.isArray()) {
			ListObject lo = ListTypeObject.make_list(o);
			X rv = (X)Array.newInstance(clazz.getComponentType(), lo.len());
			for (int i=0; i<lo.len(); i++) {
				Array.set(rv, i, Coerce.toJava(lo.get(i), clazz.getComponentType()));
			}
			return (X)rv;
		}
		
		// 2st, return PythonObject if requested
		if (clazz.isAssignableFrom(o.getClass()))
			if (PythonObject.class.isAssignableFrom(clazz))
				return clazz.cast(o);

		// 3nd, coerce None directly
		if (o == NoneObject.NONE) {
			if (clazz.isPrimitive())
				throw new CastFailedException("Can't convert None to " + clazz.getName());
			return null;
		}

		// 4rd, try to coerce pointers directly 
		if (o instanceof PointerObject) {
			Class<?> ptype = ((PointerObject) o).getObject().getClass();
			if (clazz.isAssignableFrom(ptype))
				return clazz.cast(((PointerObject) o).getObject());
		}
		
		// 5th, use Coercion class
		Coercion co = (Coercion) COERCIONS.get(clazz);
		if (co == null) {
			// Can't coerce directly
			for (Entry<Class<?>, Coercion> coer : COERCIONS.entrySet()) {
				if (clazz.isAssignableFrom(coer.getKey())) {
					co = coer.getValue();
					break;
				}
			}
		}
		if (co == null)
			// Coercion class not found
			throw new CastFailedException("Can't convert " + o.toString() + " to " + clazz.getName());
		
		return (X)co.coerce(o, clazz);
	}
	
	/** 
	 * Works like Coerce.toJava, but throws exception appropriate for failing to convert method argument.
	 * Handles missing arguments as well.
	 * 
	 * @param t arguments tuple
	 * @param argNumber argument number to get
	 * @param function function name used in exception
	 * @param clazz requested argument type
	 
	 * */ 
	public static <X> X argument(TupleObject t, int argNumber, String function, Class<X> clazz) throws PythonException {
		try {
			return Coerce.toJava(t.get(argNumber), clazz);
		} catch (CastFailedException e) {
			throw new TypeError(function + ": cannot convert value for argument " + argNumber, e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new TypeError(function + ": value for argument " + argNumber + " missing", e);
		}
	}


	/** 
	 * Coerces object to python type representing provided class
	 */
	public static PythonObject toPython(Object o, Class<?> cls) {
		if (cls == Byte.class || cls == byte.class)
			return NumberObject.valueOf(((Byte) o).byteValue());
		if (cls == Integer.class || cls == int.class)
			return NumberObject.valueOf(((Integer) o).intValue());
		if (cls == Long.class || cls == long.class) {
			if (PythonRuntime.USE_INT_ONLY)
				return NumberObject.valueOf(((Long) o).intValue());
			else
				return NumberObject.valueOf(((Long) o).longValue());
		}
		if (cls == Float.class || cls == float.class) {
			if (PythonRuntime.USE_DOUBLE_FLOAT)
				return NumberObject.valueOf(((Float) o).doubleValue());
			else	
				return NumberObject.valueOf(((Float) o).floatValue());
		}
		if (cls == Double.class || cls == double.class) {
			if (PythonRuntime.USE_DOUBLE_FLOAT)
				return NumberObject.valueOf(((Double) o).doubleValue());
			else
				return NumberObject.valueOf(((Double) o).floatValue());
		}
		if (cls == String.class)
			return new StringObject((String) o);
		if (cls == Void.class)
			return NoneObject.NONE;
		if (cls == Boolean.class || cls == boolean.class)
			return BoolObject.fromBoolean((Boolean) o);
		if (cls.isArray()) {
			ListObject lo = new ListObject();
			lo.newObject();
			for (Object i : (Object[])o)
				lo.objects.add(i == null ? NoneObject.NONE : toPython(i, i.getClass()));
			return lo;
		}
		if (o instanceof Collection){
			ListObject lo = new ListObject();
			for (Object i : (Collection<?>)o){
				lo.objects.add(i == null ? NoneObject.NONE : toPython(i, i.getClass()));
			}
			return lo;
		}
		if (o instanceof Map){
			Map<?, ?> map = (Map<?, ?>)o;
			DictObject dict = new DictObject();
			for (Map.Entry<?, ?> e : map.entrySet()){
				dict.backingMap.put(e.getKey() == null ? NoneObject.NONE : toPython(e.getKey(), e.getKey().getClass()), 
						e.getValue() == null ? NoneObject.NONE : toPython(e.getValue(), e.getValue().getClass()));
			}
			return dict;
		}
		if (o instanceof PythonObject){
			if (o instanceof StringObject)
				return new StringObject(((StringObject) o).value);
			if (o instanceof TupleObject)
				return new TupleObject(((TupleObject) o).getObjects());
			return (PythonObject) o;
		}
		if (o == null)
			return NoneObject.NONE;
		return PythonRuntime.runtime.getJavaClass(o.getClass().getCanonicalName(), o, null);
	}
	
	/** Coerces object to nearest applicable python type */
	public static PythonObject toPython(int i) {
		return NumberObject.valueOf(i);
	}

	/** Coerces object to nearest applicable python type */
	public static PythonObject toPython(long l) {
		return NumberObject.valueOf(l);
	}
	
	/** Coerces object to nearest applicable python type */
	public static PythonObject toPython(String s) {
		return new StringObject(s);
	}
	
	/** Coerces object to nearest applicable python type */
	public static PythonObject toPython(Object o) {
		return PythonRuntime.runtime.getJavaClass(o.getClass().getCanonicalName(), o, null);
	}

	public static TupleObject toTuple(Collection<?> c){
		PythonObject[] values = new PythonObject[c.size()];
		int i=0;
		for (Object o : c){
			values[i++] = Coerce.toPython(o);
		}
		
		TupleObject t = new TupleObject(values);
		t.newObject();
		return t;
	}
	
	public static TupleObject toTuple(Object[] c){
		PythonObject[] values = new PythonObject[c.length];
		int i=0;
		for (Object o : c){
			values[i++] = Coerce.toPython(o);
		}
		
		TupleObject t = new TupleObject(values);
		t.newObject();
		return t;
	}
	
	static {
		
		COERCIONS.put(Integer.class, new Coercion() {
			/** Coerces Integer */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).intValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to Integer");
			}
		});
		
		COERCIONS.put(int.class, new Coercion() {
			/** Coerces int */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).intValue();
				throw new CastFailedException("Can't convert " + o.toString() + " to int");
			}
		});

		COERCIONS.put(Long.class, new Coercion() {
			/** Coerces Long */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).longValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to Long");
			}
		});
		

		COERCIONS.put(long.class, new Coercion() {
			/** Coerces long */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).longValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to long");
			}
		});
		
		COERCIONS.put(Float.class, new Coercion() {
			/** Coerces Float */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).floatValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to Float");
			}
		});

		COERCIONS.put(float.class, new Coercion() {
			/** Coerces float */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).floatValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to float");
			}
		});

		COERCIONS.put(Double.class, new Coercion() {
			/** Coerces Double */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).doubleValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to Double");
			}
		});
		

		COERCIONS.put(double.class, new Coercion() {
			/** Coerces Double */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject)o).doubleValue();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to double");
			}
		});
		
		COERCIONS.put(Boolean.class, new Coercion() {
			/** Coerces Boolean. Everything can be coerced to boolean */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) {
				return o.truthValue() ? Boolean.TRUE : Boolean.FALSE;
			}
		});
		

		COERCIONS.put(boolean.class, new Coercion() {
			/** Coerces boolean. Everything can be coerced to boolean */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) {
				return o.truthValue();
			}
		});
		
		COERCIONS.put(String.class, new Coercion() {
			/** Coerces String. Only StringObject coerced to String 
			 * @throws CastFailedException */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof StringObject)
					return ((StringObject) o).getString();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to string");
			}
		});
		
		COERCIONS.put(Map.class, new Coercion() {
			/** Coerces boolean. Everything can be coerced to boolean */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof DictObject)
					return ((DictObject)o).asRegularDict();
				if (o instanceof StringDictObject)
					return ((StringDictObject)o).asRegularDict();
				
				throw new CastFailedException("Can't convert " + o.toString() + " to Map");
			}
		});
	
		COERCIONS.put(Object.class, new Coercion() {
			/** Coerces to object. Everything is an object, but PointerObject is dereferenced first */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) throws CastFailedException {
				if (o instanceof PointerObject)
					return ((PointerObject) o).getObject();
				else if (o instanceof NumberObject) {
					NumberObject n = (NumberObject)o;
					switch (n.getNumberType()) {
					case BOOL:
						return n.truthValue() ? Boolean.TRUE : Boolean.FALSE;
					case COMPLEX:
						return n;
					case FLOAT:
						if (PythonRuntime.USE_DOUBLE_FLOAT)
							return n.doubleValue();
						else
							return n.floatValue();
					case INT:
						return n.intValue();
					case LONG:
						if (PythonRuntime.USE_INT_ONLY)
							return n.intValue();
						else
							return n.longValue();
					}
				} else if (o instanceof StringObject) {
					return o.toString();
				}
				return o;
			}
		});

}
	
	public static class CannotCoerceException extends RuntimeException {
		private static final long serialVersionUID = 8306084748185702275L;

		public CannotCoerceException(PythonObject type) {
			super("Cannot coerce " + type.toString() + " to java");
		}
	}
}
