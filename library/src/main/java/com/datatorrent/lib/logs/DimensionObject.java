package com.datatorrent.lib.logs;

public class DimensionObject<T> implements Comparable<DimensionObject<T>> {

	private long count;
	private T val;
	
	public DimensionObject(long count,T s){
		this.count = count;
		val=s;
	}
	
	@Override
	public String toString(){
		return count+","+val.toString();
	}

	@Override
	public int compareTo(DimensionObject<T> arg0) {
		if(count> arg0.count)
			return 1;
		if(count < arg0.count)
			return -1;
		return 0;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public T getVal() {
		return val;
	}

	public void setVal(T val) {
		this.val = val;
	}
	
	@Override
	public int hashCode(){
		return (val.toString() + Long.toString(count)).hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null) return false;
		if (!this.getClass().equals(obj.getClass())) return false;
		DimensionObject<T> obj2 = (DimensionObject<T>) obj;
		if(this.val.equals(obj2.val))
			return true;
		return false;
		
	}

}
