package it.utility;

public class MutableBoolean {
	
	private boolean flag;

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public MutableBoolean(Boolean flag) {
this.flag = flag;	}
	

}
