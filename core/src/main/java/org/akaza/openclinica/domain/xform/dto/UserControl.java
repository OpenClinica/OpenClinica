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
