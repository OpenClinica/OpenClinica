package org.akaza.openclinica.domain.xform.dto;

import java.util.List;

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

    public List<Item> getItem();

}
