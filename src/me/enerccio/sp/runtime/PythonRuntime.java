package me.enerccio.sp.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import me.enerccio.sp.interpret.EnvironmentObject;
import me.enerccio.sp.interpret.FrameObject;
import me.enerccio.sp.interpret.NoGetattrException;
import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.pointer.WrapNoMethodsFactory;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.types.BytecodeTypeObject;
import me.enerccio.sp.types.types.IntTypeObject;
import me.enerccio.sp.types.types.JavaInstanceTypeObject;
import me.enerccio.sp.types.types.ListTypeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.types.types.SliceTypeObject;
import me.enerccio.sp.types.types.StringTypeObject;
import me.enerccio.sp.types.types.TupleTypeObject;
import me.enerccio.sp.types.types.TypeTypeObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

public class PythonRuntime {
	
	public static final PythonRuntime runtime = new PythonRuntime();
	public static final String IS = "is";
	public static final String GO = "go";
	public static final String SUPER = "super";
	public static final String GETATTR = "getattr";
	public static final String SETATTR = "setattr";
	public static final String ISINSTANCE = "isinstance";
	public static final String PRINT_JAVA = "print_java";
	public static final String PRINT_JAVA_EOL = "print_java_eol";
	
	private PythonRuntime(){
		addFactory("", WrapNoMethodsFactory.class);
	}
	
	public Map<String, ModuleObject> root = Collections.synchronizedMap(new HashMap<String, ModuleObject>());
	private Map<Long, PythonObject> instances1 = new HashMap<Long, PythonObject>();
	private Map<PythonObject, Long> instances2 = new HashMap<PythonObject, Long>();
	private List<PythonDataSourceResolver> resolvers = new ArrayList<PythonDataSourceResolver>();
	
	private long key = Long.MIN_VALUE;
	
	private CyclicBarrier awaitBarrierEntry;
	private CyclicBarrier awaitBarrierExit;
	private volatile boolean isSaving = false;
	private volatile boolean allowedNewInterpret = true;
	
	public void waitForNewInterpretAvailability() throws InterruptedException{
		if (!allowedNewInterpret)
			Thread.sleep(10);
	}
	
	public void waitIfSaving(PythonInterpret i) throws InterruptedException {
		if (!isSaving)
			return;
		if (!i.isInterpretStoppable())
			return; // continue working
		try {
			awaitBarrierEntry.await();
			awaitBarrierExit.await();
		} catch (BrokenBarrierException e) {
			throw new InterruptedException(e.getMessage());
		}
	}
	
	public synchronized String serializeRuntime() throws Exception{
		allowedNewInterpret = false;
		int numInterprets = PythonInterpret.interprets.size();
		awaitBarrierEntry = new CyclicBarrier(numInterprets + 1); // include self
		awaitBarrierExit = new CyclicBarrier(numInterprets + 1); // include self
		isSaving = true;
		
		awaitBarrierEntry.await();
		String content = doSerializeRuntime();
		awaitBarrierExit.await();
		
		return content;
	}
	
	private String doSerializeRuntime() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized void addResolver(PythonDataSourceResolver resolver){
		resolvers.add(resolver);
	}
	
	public synchronized void newInstanceInitialization(PythonObject o){
		instances2.put(o, key);
		instances1.put(key++, o);
	}
	
	public synchronized long getInstanceId(PythonObject o){
		return instances2.get(o);
	}
	
	public synchronized ModuleObject getRoot(String key) {
		if (!root.containsKey(key)){
			root.put(key, getModule(key, null));
		}
		return root.get(key);
	}
	
	private ModuleObject loadModule(ModuleProvider provider){
		MapObject globals = generateGlobals();
		ModuleObject mo = new ModuleObject(globals, provider);
		return mo;
	}
	
	public synchronized ModuleObject getModule(String name, StringObject moduleResolvePath){
		if (moduleResolvePath == null)
			moduleResolvePath = new StringObject("");
		ModuleObject mo = resolveModule(name, moduleResolvePath);
		if (mo == null)
			throw Utils.throwException("ImportError", "unknown module '" + name + "' with resolve path '" + moduleResolvePath.value + "'");
		String pp = moduleResolvePath.value;
		mo.newObject();
		if (pp.equals(""))
			root.put(name, mo);
		mo.initModule();
		return mo;
	}

	private ModuleObject resolveModule(String name,
			StringObject moduleResolvePath) {
		ModuleProvider provider = null;
		for (PythonDataSourceResolver resolver : resolvers){
			provider = resolver.resolve(name, moduleResolvePath.value);
		}
		if (provider != null)
			return loadModule(provider);
		return null;
	}

	private static volatile MapObject globals = null;
	public MapObject generateGlobals() {
		if (globals == null)
			synchronized (this){
				if (globals == null){
					globals = new MapObject();
					
					EnvironmentObject e = new EnvironmentObject();
					e.newObject();
					e.add(globals);
					
					PythonInterpret.interpret.get().currentEnvironment.push(e);
					PythonObject o;
					
					globals.put(GETATTR, Utils.staticMethodCall(PythonRuntime.class, GETATTR, PythonObject.class, String.class));
					globals.put(SETATTR, Utils.staticMethodCall(PythonRuntime.class, SETATTR, PythonObject.class, String.class, PythonObject.class));
					globals.put(ISINSTANCE, Utils.staticMethodCall(PythonRuntime.class, ISINSTANCE, PythonObject.class, PythonObject.class));
					globals.put(PRINT_JAVA, Utils.staticMethodCall(PythonRuntime.class, PRINT_JAVA, PythonObject.class));
					globals.put(PRINT_JAVA_EOL, Utils.staticMethodCall(PythonRuntime.class, PRINT_JAVA_EOL));
					globals.put(IS, Utils.staticMethodCall(PythonRuntime.class, IS, PythonObject.class, PythonObject.class));
					globals.put(GO, Utils.staticMethodCall(PythonRuntime.class, GO, String.class));
					globals.put(SUPER, Utils.staticMethodCall(PythonRuntime.class, "superClass", ClassObject.class));
					globals.put(TypeTypeObject.TYPE_CALL, o = new TypeTypeObject());
					o.newObject();
					globals.put(StringTypeObject.STRING_CALL, o = new StringTypeObject());
					o.newObject();
					globals.put(IntTypeObject.INT_CALL, o = new IntTypeObject());
					o.newObject();
					globals.put(BytecodeTypeObject.BYTECODE_CALL, o = new BytecodeTypeObject());
					o.newObject();
					globals.put(TupleTypeObject.TUPLE_CALL, o = new TupleTypeObject());
					o.newObject();
					globals.put(ListTypeObject.LIST_CALL, o = new ListTypeObject());
					o.newObject();
					globals.put(ObjectTypeObject.OBJECT_CALL, o = ObjectTypeObject.inst);
					o.newObject();
					globals.put(SliceTypeObject.SLICE_CALL, o = new SliceTypeObject());
					o.newObject();
					globals.put(JavaInstanceTypeObject.JAVA_CALL, o = new JavaInstanceTypeObject());
					o.newObject();
					
					addExceptions(globals);
					
					PythonInterpret.interpret.get().currentEnvironment.pop();
				}
			}
		
		return globals.cloneMap();
	}
	
	public static PythonObject go(String where){
		PythonInterpret current = PythonInterpret.interpret.get();
		FrameObject frame = current.frame();
		if (!frame.labelMap.containsKey(where))
			throw Utils.throwException("Error", "label '" + where + "' undefined");
		frame.pc = frame.labelMap.get(where);
		return NoneObject.NONE;
	}
	
	public static PythonObject superClass(ClassObject clazz){
		List<ClassObject> ll = Utils.resolveDiamonds(clazz);
		if (ll.size() == 1)
			return NoneObject.NONE;
		return ll.get(ll.size()-2);
	}
	
	public static PythonObject is(PythonObject a, PythonObject b){
		return BoolObject.fromBoolean(a == b);
	}
	
	public static PythonObject print_java(PythonObject a){
		System.out.print(a);
		return NoneObject.NONE;
	}
	
	public static PythonObject print_java_eol(){
		System.out.println();
		return NoneObject.NONE;
	}
	
	public static PythonObject isinstance(PythonObject testee, PythonObject clazz){
		if (testee instanceof ClassInstanceObject && clazz instanceof ClassObject)
			return isClassInstance((ClassInstanceObject)testee, (ClassObject)clazz) ? BoolObject.TRUE : BoolObject.FALSE;
		return Utils.run("type", testee).equals(Utils.run("type", clazz)) ? BoolObject.TRUE : BoolObject.FALSE;
	}

	private static boolean isClassInstance(ClassInstanceObject testee,
			ClassObject clazz) {
		ClassObject cls = (ClassObject) Utils.get(testee, "__class__");
		return checkClassAssignable(cls, clazz);
	}

	private static boolean checkClassAssignable(ClassObject cls, ClassObject clazz) {
		if (Utils.equals(cls, clazz))
			return true;
		for (PythonObject o : ((TupleObject)Utils.get(clazz, "__bases__")).getObjects())
			if (o instanceof ClassObject){
				if (checkClassAssignable(cls, (ClassObject)o)){
					return true;
				}
			}
			
		return false;
	}

	private static final ThreadLocal<Stack<PythonObject>> accessor = new ThreadLocal<Stack<PythonObject>>(){

		@Override
		protected Stack<PythonObject> initialValue() {
			return new Stack<PythonObject>();
		}
		
	};
	
	public static PythonObject getattr(PythonObject o, String attribute) {
		PythonObject value = o.get(attribute, PythonInterpret.interpret.get().getLocalContext());
		if (value == null){
			if (accessor.get().size() != 0 && accessor.get().peek() == o){
				accessor.get().pop();
				throw new NoGetattrException();
			}
			accessor.get().push(o);
			try {
				PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTR__);
				value = PythonInterpret.interpret.get().execute(false, getattr, new StringObject(attribute));
			} catch (NoGetattrException e) {
				throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", Utils.run("type", o), attribute));
			} finally {

				accessor.get().pop();
			}
		}
		return value;
	}
	
	public static PythonObject setattr(PythonObject o, String attribute, PythonObject v){
		if (o.get("__setattr__", PythonInterpret.interpret.get().getLocalContext()) != null){
			return PythonInterpret.interpret.get().execute(false, o.get("__setattr__", PythonInterpret.interpret.get().getLocalContext())
					, new StringObject(attribute), v);
		}
		if (o.get(attribute, PythonInterpret.interpret.get().getLocalContext()) == null)
			o.create(attribute, attribute.startsWith("__") && !attribute.endsWith("__") ? AccessRestrictions.PRIVATE : AccessRestrictions.PUBLIC);
		o.set(attribute, PythonInterpret.interpret.get().getLocalContext(), v);
		return NoneObject.NONE;
	}
	
	private void addExceptions(MapObject globals) {
		MapObject base = addException(globals, "Error", null, false);
		ListObject lo = new ListObject();
		lo.newObject();
		base.backingMap.put(new StringObject("stack"), lo);
		JavaFunctionObject str = (JavaFunctionObject) Utils.staticMethodCall(PythonRuntime.class, "baseExcToStr", PythonObject.class);
		str.setWrappedMethod(true);
		base.backingMap.put(new StringObject("__str__"), str);
		
		addException(globals, "BaseException", "Error", false);
		addException(globals, "Exception", "BaseException", false);
		addException(globals, "TypeError", "Exception", true);
		addException(globals, "SyntaxError", "Exception", true);
		addException(globals, "ValueError", "Exception", true);
		addException(globals, "AttributeError", "Exception", true);
		addException(globals, "ImportError", "Exception", true);
		addException(globals, "NameError", "Exception", true);
		addException(globals, "ParseError", "Exception", true);
		addException(globals, "IndexError", "Exception", true);
		addException(globals, "StopIteration", "Exception", false);
	}


	private MapObject addException(MapObject globals, String exceptionName, String exceptionBase, boolean stringArg) {
		TypeTypeObject classCreator = (TypeTypeObject) globals.doGet(TypeTypeObject.TYPE_CALL);
		MapObject dict = new MapObject();
		
		JavaFunctionObject init = null; 
		if (!stringArg)
			init = (JavaFunctionObject) Utils.staticMethodCall(PythonRuntime.class, "initException", PythonObject.class);
		else
			init = (JavaFunctionObject) Utils.staticMethodCall(PythonRuntime.class, "initException", PythonObject.class, PythonObject.class);
		init.setWrappedMethod(true);
		
		dict.backingMap.put(new StringObject("__init__"), init);
		
		globals.put(exceptionName, classCreator.call(new TupleObject(new StringObject(exceptionName), exceptionBase == null ? new TupleObject() :
				new TupleObject(globals.doGet(exceptionBase)), dict)));
		return dict;
	}
	
	public static PythonObject baseExcToStr(PythonObject e){
		return new StringObject(Utils.run("str", e.get("__CLASS__", e)) + ": " + Utils.run("str", e.get("__msg__", e)));
	}
	
	public static PythonObject initException(PythonObject e){
		return initException(e, NoneObject.NONE);
	}
	
	public static PythonObject initException(PythonObject e, PythonObject text){
		Utils.putPublic(e, "__msg__", text);
		return NoneObject.NONE;
	}
	
	/*
	 * How to add new classes to the system.
	 * 
	 * First, if allowAutowraps is true, SP will try to wrap all classes returned from java (or asked to instantiate via javainstance() type)
	 * You can specify packages to disallow this autowrapping by using addExcludePackageOrClass method.
	 * 
	 * To provide which wrapping factory to be used, use addFactory method. This is hierarchical. To set for root package, use addFactory with empty string.
	 * Otherwise specify package (or class) from which the runtime will apply your specified PointerFactory.
	 * 
	 * Example:
	 * 
	 * addFactory("", WrapNoMethodsFactory.class)
	 * addFactory("org", WrapPublicFactory.class)
	 * addFactory("org.i.dont.trust.thispackage", WrapAnnotationFactory.class)
	 * 
	 * if set up like this, every class outside org package will have no methods available in python, all classes in org except for org.i.dont.trust.thispackage
	 * will have all public methods available in python, and classes in org.i.dont.trust.thispackage will only have methods with annotation available. 
	 * 
	 * You can also set up aliases for classes using addAlias.
	 * 
	 * addAlias("com.example.Class", "example") will alias example into com.example.Class. in python user can:
	 * 		
	 * 		x = javainstance("example") 
	 * 
	 * instead of 
	 * 		
	 * 		x = javainstance("com.example.Class") 
	 */
	
	private Map<String, PointerFactory> factories = Collections.synchronizedMap(new TreeMap<String, PointerFactory>());
	private boolean allowAutowraps;
	private List<String> excludedPackages = new ArrayList<String>();
	private Map<String, String> aliases = Collections.synchronizedMap(new HashMap<String, String>());
	
	public synchronized void addExcludePackageOrClass(String packageOrClass){
		excludedPackages.add(packageOrClass);
	}
	
	public synchronized void addAlias(String fullName, String alias){
		aliases.put(alias, fullName);
	}
	
	public synchronized void setAllowAutowraps(boolean allowAutowraps){
		this.allowAutowraps = allowAutowraps;
	}	
	
	public void addFactory(String packagePath, Class<? extends PointerFactory> clazz) {
		try {
			factories.put(packagePath, clazz.newInstance());
		} catch (Exception e) {
			throw Utils.throwException("TypeError", "failed to instantiate pointer factory");
		}
	}

	public PointerObject getJavaClass(String cls, Object pointedObject, PythonObject... args) {
		if (!aliases.containsKey(cls) && !allowAutowraps)
			throw Utils.throwException("TypeError", "javainstance(): unknown java type '" + cls + "'. Type is not wrapped");
		if (!aliases.containsKey(cls))
				synchronized (aliases){
					for (String s : excludedPackages)
						if (cls.startsWith(s))
							throw Utils.throwException("TypeError", "package '" + s + "' is not allowed for automatic wrapping");
					aliases.put(cls, cls);
				}
		
		cls = aliases.get(cls);
		
		Object o;
		if (pointedObject != null)
			o = pointedObject;
		else {
			Class<?> clazz;
			try {
				clazz = Class.forName(cls);
			} catch (ClassNotFoundException e1) {
				throw Utils.throwException("TypeError", "javainstance(): unknown java type " + cls);
			}
			
			Object[] jargs = new Object[args.length];
			
			Constructor<?> selected = null;
			outer:
			for (Constructor<?> c : clazz.getConstructors()){
				Class<?>[] types = c.getParameterTypes();
				if (types.length != jargs.length)
					continue;
				
				int i=0;
				for (PythonObject oo : args){
					try {
						jargs[i] = Utils.asJavaObject(types[i], oo);
						++i;
					} catch (PointerMethodIncompatibleException e){
						continue outer;
					}
				}
				
				selected = c;
				break;
			}
			
			if (selected == null)
				throw Utils.throwException("TypeError", "javainstance(): no compatibile constructor for type " + cls + " found ");
			
			try {
				o = selected.newInstance(jargs);
			} catch (PythonExecutionException e){
				throw e;
			} catch (InvocationTargetException e){
				if (e.getTargetException() instanceof PythonExecutionException)
					throw (RuntimeException)e.getTargetException();
				throw Utils.throwException("TypeError", "javainstance(): failed java constructor call");
			} catch (Exception e) {
				throw Utils.throwException("TypeError", "javainstance(): failed java constructor call");
			}
		}
		
		PointerFactory factory = getFactory(cls);
		if (factory == null){
			throw Utils.throwException("TypeError", "javainstance(): no available factory for class " + cls);
		}
		return factory.doInitialize(o);
	}

	private PointerFactory getFactory(String cls) {
		String[] components = cls.split("\\.");
		List<String> c = new ArrayList<String>();
		c.add("");
		c.addAll(Arrays.asList(components));
		return doGetFactory(c);
	}

	private PointerFactory doGetFactory(List<String> c) {
		String pkgName = "";
		PointerFactory fac = null;
		for (String component : c){
			pkgName += component;
			PointerFactory ff = factories.get(pkgName);
			if (ff == null)
				break;
			fac = ff;
		}
		return fac;
	}

	public synchronized ClassObject getObject() {
		ObjectTypeObject o = (ObjectTypeObject) globals.doGet(ObjectTypeObject.OBJECT_CALL);
		return o;
	}
	
	public PythonObject runtimeWrapper() {
		// TODO Auto-generated method stub
		return null;
	}
}

