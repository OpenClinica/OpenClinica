package org.akaza.openclinica.domain.xform;

import java.util.List;

import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.domain.xform.dto.Text;
import org.akaza.openclinica.domain.xform.dto.Translation;
import org.akaza.openclinica.domain.xform.dto.Value;

public class XformUtils {

    public static String getDefaultTranslation(Html html, String ref) {
        Translation translation = null;
        List<Translation> translations = html.getHead().getModel().getItext().getTranslation();

        // Get default translation
        for (Translation trans : translations) {
            if (trans.getDefaultLang() != null && trans.getDefaultLang().equals("true()"))
                translation = trans;
        }
        if (translation == null)
            translation = translations.get(0);

        List<Text> texts = translation.getText();

        // Lookup text translation
        for (Text text : texts) {
            if (text.getId().equals(ref)) {
                List<Value> values = text.getValue();
                for (Value value : values) {
                    if (value.getForm() == null && value.getValue() != null && !value.getValue().equals(""))
                        return value.getValue();
                }
            }
        }
        return "";
    }
}
