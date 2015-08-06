package me.enerccio.sp.interpret;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

public interface KwArgs {
	public static final KwArgs EMPTY = new KwArgs.HashMapKWArgs();
	
	/** 
	 * Removes argument from list and returns its value. 
	 * Returns null if argument is not found
	 */
	public PythonObject consume(String arg);

	/** 
	 * Removes argument from list and returns its value as instance of specified class.
	 * Returns null if argument is not found.
	 * Throws TypeError if value cannot be converted. 
	 */
	public <T> T consume(String arg, Class<T> cls);

	/** 
	 * Removes argument from list and returns its value as String. 
	 * Returns null if argument is not found.
	 */
	public String consumeString(String arg);

	/** 
	 * Check if kwarg list is empty and throws exception if not.
	 * Passed name is used as function name in error message.  
	 */
	public void checkEmpty(String arg);

	/** 
	 * Throws exception if there is any kwarg in list.  
	 */
	public void notExpectingKWArgs();

	/**
	 * Returns true if argument is defined
	 */
	public boolean contains(String string);

	/**
	 * Returns content as Map
	 */
	public Map<String, PythonObject> getAll();

	static class HashMapKWArgs extends HashMap<String, PythonObject>  implements KwArgs {
		private static final long serialVersionUID = 7370108455070437208L;

		@Override
		public PythonObject consume(String arg) {
			return remove(arg);
		}

		@Override
		public <T> T consume(String arg, Class<T> cls) {
			if (contains(arg)) {
				try {
					return Coerce.toJava(remove(arg), cls);
				} catch (CastFailedException e) {
					throw Utils.throwException("TypeError", "cannot convert value for argument '" + arg + "'", e);
				}
			}
			return null;
		}
		
		@Override
		public String consumeString(String arg) {
			if (containsKey(arg))
				return remove(arg).toString();
			return null;
		}

		@Override
		public void checkEmpty(String name) {
			if (size() == 0)
				return;
			String key = keySet().iterator().next();
			throw Utils.throwException("TypeError", name + " got an unexpected keyword argument '" + key + "'");
		}

		@Override
		public void notExpectingKWArgs() {
			checkEmpty("function");
		}

		@Override
		public boolean contains(String arg) {
			return containsKey(arg);
		}

		@Override
		public Map<String, PythonObject> getAll() {
			return this;
		}

		@Override
		public DictObject toDict() {
			DictObject dict = new DictObject();
			for (String key : new HashSet<String>(keySet())){
				dict.put(key, consume(key));
			}
			return dict;
		}
	}


	public DictObject toDict();
}