package com.dcee.core.data.no;

import java.util.List;

public interface Sequence {
	boolean isExist(String paramString);

	void create(NoDefinition paramNoDefinition);

	void update(NoDefinition paramNoDefinition);

	void delete(String paramString);

	void resetNo(String paramString);

	void resetNoTo(String paramString, long paramLong);

	String generate(String paramString);

	@SuppressWarnings("rawtypes")
	List batchGenerate(String paramString, int paramInt);
}