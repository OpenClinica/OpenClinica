package org.akaza.openclinica.renderer;

public class Item {

  public int orderNumber;
  public boolean mandatory;
  public String itemGroupKey;
  public int itemGroupLength;

  public Item (int orderNumber, boolean mandatory, String itemGroupKey, int itemGroupLength ) {
    this.orderNumber = orderNumber;
    this.mandatory = mandatory;
    this.itemGroupKey = itemGroupKey;
    this.itemGroupLength = itemGroupLength;
  }
}