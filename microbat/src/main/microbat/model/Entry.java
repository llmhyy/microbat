package microbat.model;


public class Entry {
	private String className;
	private int startLine;
	private int endLine;
    public final static String COLUMN_SEPARATOR=",";
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Entry) {
			Entry entry = (Entry) o;
			return this.className.equals(entry.getClassName()) && this.startLine == entry.getStartLine()
					&& this.endLine == entry.getEndLine();
		}
		return super.equals(o);
	}
	
	@Override
	public String toString() {
	return className+COLUMN_SEPARATOR+startLine+COLUMN_SEPARATOR+endLine;
	}
}

