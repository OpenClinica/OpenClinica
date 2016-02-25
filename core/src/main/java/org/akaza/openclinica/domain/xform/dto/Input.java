package org.akaza.openclinica.domain.xform.dto;

public class Input implements UserControl {
    private String ref;
    private String appearance;
    private Label label = null;
    private Hint hint = null;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Hint getHint() {
        return hint;
    }

    public void setHint(Hint hint) {
        this.hint = hint;
    }

    @Override
    public String getMediatype() {
        // TODO Auto-generated method stub
        return null;
    }

}
