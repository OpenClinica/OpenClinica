package org.akaza.openclinica.job;

import java.io.File;
import java.io.FilenameFilter;

public class XMLFileFilter implements FilenameFilter {

	public boolean accept(File arg0, String name) {
		// TODO Auto-generated method stub
		return (name.endsWith(".xml"));

	}

}
