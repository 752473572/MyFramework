package com.dcee.core.data.no;

import java.io.Serializable;

public class NoDefinition implements Cloneable, Serializable {
	private static final long serialVersionUID = 7166614248898685221L;
	private String noid;
	private String noname;
	private int nolength;
	private int notype;
	private String prefix;
	private String postfix;
	private int bufferSize = 0;
	private long initialValue = 1L;
	private int noIncrement = 1;

	public NoDefinition() {
	}

	public NoDefinition(String noid) {
		this(noid, null, 10, 1, null, null, 0);
	}

	public NoDefinition(String noid, int nolength, int notype, String prefix, String postfix) {
		this(noid, null, nolength, notype, prefix, postfix, 0);
	}

	public NoDefinition(String noid, String noname, int nolength, int notype, String prefix, String postfix,
			int bufferSize) {
		this(noid, noname, nolength, notype, prefix, postfix, bufferSize, 0L, 0);
	}

	public NoDefinition(String noid, String noname, int nolength, int notype, String prefix, String postfix,
			int bufferSize, long initialValue, int noIncrement) {
		this.noid = noid;
		this.noname = noname;
		this.nolength = nolength;
		this.notype = notype;
		this.prefix = prefix;
		this.postfix = postfix;
		this.bufferSize = bufferSize;
		this.initialValue = initialValue;
		this.noIncrement = noIncrement;
	}

	public String getNoid() {
		return this.noid;
	}

	public String getNoname() {
		return this.noname;
	}

	public int getNolength() {
		return this.nolength;
	}

	public int getNotype() {
		return this.notype;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getPostfix() {
		return this.postfix;
	}

	public void setNoid(String noid) {
		this.noid = noid;
	}

	public void setNoname(String noname) {
		this.noname = noname;
	}

	public void setNolength(int nolength) {
		this.nolength = nolength;
	}

	public void setNotype(int notype) {
		this.notype = notype;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException ex) {
			System.out.println(o);
		}
		return o;
	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public long getInitialValue() {
		return this.initialValue;
	}

	public void setInitialValue(long initialValue) {
		this.initialValue = initialValue;
	}

	public int getNoIncrement() {
		return this.noIncrement;
	}

	public void setNoIncrement(int noIncrement) {
		this.noIncrement = noIncrement;
	}
}
