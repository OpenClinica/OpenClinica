package core.org.akaza.openclinica.domain.xform.dto;

public class Head {

    private String title;
    private Model model;
    private String crossform_reference;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getCrossform_reference() {
        return crossform_reference;
    }

    public void setCrossform_reference(String crossform_reference) {
        this.crossform_reference = crossform_reference;
    }
}