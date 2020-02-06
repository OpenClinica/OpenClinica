/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.rule;

import java.io.File;
import java.io.InputStream;

public interface FileRenamePolicy {

    public File rename(File f, InputStream content);

}
