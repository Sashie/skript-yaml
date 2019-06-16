package example.src.my.awesome.plugin;

import java.util.List;
import java.util.Map;

import me.sashie.skriptyaml.api.ConstructedClass;

public class ExampleConstructedClass extends ConstructedClass<ExampleClass> {

	@SuppressWarnings("unchecked")
	@Override
	public ExampleClass construct(Map<Object, Object> values) {
		String ex1 = (String) values.get("ex1");
		String ex2 = (String) values.get("ex2");
		List<String> ex3 = (List<String>) values.get("ex3");
		
		if (ex1 == null || ex2 == null || ex3 == null)
			return null;

		return new ExampleClass(ex1, ex2, ex3);
	}
}
