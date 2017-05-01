import java.util.HashMap;

public class SymbolTable {
	
	private HashMap st = new HashMap();
	
	public void put(String key, Object val){
		st.put(key,val);
	}
	public Object get(String key){
		return st.get(key);
	}
	
	public boolean contains(String key){
		return st.containsKey(key);
	}
	
	public void remove(String key){
		st.remove(key);
	}
	
}
