package org.akaza.openclinica.renderer;

public class ItemGroup {

  public ItemGroup (int repeatNumber, boolean repeating) {
    this.repeatNumber = repeatNumber;
    this.repeating = repeating;
  }
  
  public int repeatNumber;
  public boolean repeating;
  
  public int getRepeatNumber() {
    return repeatNumber;
  }
  public void setRepeatNumber(int repeatNumber) {
    this.repeatNumber = repeatNumber;
  }
  
  public boolean isRepeating() {
    return repeating;
  }
  public void setRepeating(boolean repeating) {
    this.repeating = repeating;
  }
  
}