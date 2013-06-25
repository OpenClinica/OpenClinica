package org.akaza.openclinica.renderer;

public class ParseUtil {

  public static boolean parseYesNo(String value) {
    if (value == "Yes") {
      return true;
    }
    else if (value == "No") {
      return false;
    }
    return false;
  }
}
