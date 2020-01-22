/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.domain.xform.dto;

public interface UserControl {

    public String getRef();

    public void setRef(String ref);

    public String getAppearance();

    public void setAppearance(String appearance);

    public Label getLabel();

    public void setLabel(Label label);

    public Hint getHint();

    public void setHint(Hint hint);

    public String getMediatype();

}
