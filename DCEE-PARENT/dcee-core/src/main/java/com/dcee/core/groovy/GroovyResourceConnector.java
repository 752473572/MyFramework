package com.dcee.core.groovy;

import java.io.File;

public interface GroovyResourceConnector {
	void updateClassFile(File paramFile, String paramString, CompositeResourceConnector paramCompositeResourceConnector)
			throws ClassNotFoundException;
}
