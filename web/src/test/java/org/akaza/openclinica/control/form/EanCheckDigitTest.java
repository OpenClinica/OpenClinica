package org.akaza.openclinica.control.form;

import org.junit.Assert;
import org.junit.Test;

public class EanCheckDigitTest {

  @Test
  public void testIsValid() {
    Assert.assertFalse(new EanCheckDigit().isValid("0"));
    Assert.assertFalse(new EanCheckDigit().isValid("000000000000000"));
    Assert.assertFalse(new EanCheckDigit().isValid("1000000000000"));
    Assert.assertFalse(new EanCheckDigit().isValid("1000000000005"));
    Assert.assertFalse(new EanCheckDigit().isValid("1000000000045"));
    Assert.assertFalse(new EanCheckDigit().isValid("000000000000a"));
    Assert.assertTrue(new EanCheckDigit().isValid("0000000000000"));
    Assert.assertTrue(new EanCheckDigit().isValid("\u0030\u0030\u0030\u0030\u0030\u0030\u0030\u0030\u0030\u0030" +
            "\u0030\u0030\u0030"));
  }
}
