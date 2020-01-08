package core.org.akaza.openclinica.domain.xform.dto;

public class Head {

    private String title;
    private Model model;
    private String crossform_references;

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

    public String getCrossform_references() {
        return crossform_references;
    }

    public void setCrossform_references(String crossform_references) {
        this.crossform_references = crossform_references;
    }
}