package org.akaza.openclinica.controller.helper.table;

public class ItemCountInForm {
    int itemCountInFormData = 0;
    int insertedUpdatedItemCountInForm = 0;
    int insertedUpdatedSkippedItemCountInForm = 0;

    public ItemCountInForm(int itemCountInFormData, int insertedUpdatedItemCountInForm, int insertedUpdatedSkippedItemCountInForm) {
        this.itemCountInFormData = itemCountInFormData;
        this.insertedUpdatedItemCountInForm = insertedUpdatedItemCountInForm;
        this.insertedUpdatedSkippedItemCountInForm = insertedUpdatedSkippedItemCountInForm;
    }

    public int getItemCountInFormData() {
        return itemCountInFormData;
    }

    public void setItemCountInFormData(int itemCountInFormData) {
        this.itemCountInFormData = itemCountInFormData;
    }

    public int getInsertedUpdatedItemCountInForm() {
        return insertedUpdatedItemCountInForm;
    }

    public void setInsertedUpdatedItemCountInForm(int insertedUpdatedItemCountInForm) {
        this.insertedUpdatedItemCountInForm = insertedUpdatedItemCountInForm;
    }

    public int getInsertedUpdatedSkippedItemCountInForm() {
        return insertedUpdatedSkippedItemCountInForm;
    }

    public void setInsertedUpdatedSkippedItemCountInForm(int insertedUpdatedSkippedItemCountInForm) {
        this.insertedUpdatedSkippedItemCountInForm = insertedUpdatedSkippedItemCountInForm;
    }
}
