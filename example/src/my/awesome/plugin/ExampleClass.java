package example.src.my.awesome.plugin;

import java.util.LinkedList;
import java.util.List;

public class ExampleClass {

	private String example1;
	private String example2;
	private List<String> example3;

	public ExampleClass(String example1, String example2, List<String> example3) {
		this.example1 = example1;
		this.example2 = example2;
		this.example3 = example3;
	}

	public String getExample1() {
		return example1;
	}

	public void setExample1(String example1) {
		this.example1 = example1;
	}

	public String getExample2() {
		return example2;
	}

	public void setExample2(String example2) {
		this.example2 = example2;
	}

	public List<String> getExample3() {
		if (example3 != null)
			return example3;
		List<String> list = new LinkedList<String>();
		list.add("example line one");
		list.add("example line two");
		list.add("example line three");
		return list;
	}

	public void setExample3(List<String> example3) {
		this.example3 = example3;
	}
}
