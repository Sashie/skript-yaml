package example.src.my.awesome.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import me.sashie.skriptyaml.api.RepresentedClass;

public class ExampleRepresentedClass extends RepresentedClass<ExampleClass> {

	@Override
	public Map<String, Object> represent(ExampleClass data) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
		out.put("ex1", data.getExample1());
		out.put("ex2", data.getExample2());
		out.put("ex3", data.getExample3());

		return out;
	}
}
