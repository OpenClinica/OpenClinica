package org.akaza.openclinica.renderer;

public class ItemGroup {

  public int repeatNumber;
  public boolean repeating;
  public int repeatMax;
  public String name;
  public String groupHeader;

  public ItemGroup (int repeatNumber, boolean repeating, int repeatMax, String name, String groupHeader ) {
    this.repeatNumber = repeatNumber;
    this.repeating = repeating;
    this.repeatMax = repeatMax;
    this.name = name;
    this.groupHeader = groupHeader;
  }
}