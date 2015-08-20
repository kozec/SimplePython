import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.FilesystemResolver;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.utils.Coerce;


public class Test {
	
	public static void main(String[] args) throws Exception {
		
		long c = System.currentTimeMillis();
		long c2 = 0;
		
		try {
			File cache = new File("__pycache__");
			cache.mkdir();
			SimplePython.addPycCache(cache);
			
			SimplePython.initialize();
			SimplePython.setAllowAutowraps(true);
			SimplePython.addResolver(new FilesystemResolver(Paths.get("").toAbsolutePath().toString() + File.separator + "bin" + File.separator + "t"));
			
			ModuleObject x = SimplePython.getModule("x");
			c2 = System.currentTimeMillis();
//			System.in.read();
			if (x.getField("test") != null)
				SimplePython.executeFunction("x", "test");

		} finally {
			System.out.println("Took " + (System.currentTimeMillis() - c) + " ms");
			System.out.println("Took pure runtime " + (System.currentTimeMillis() - c2) + " ms");
		}
	}

}