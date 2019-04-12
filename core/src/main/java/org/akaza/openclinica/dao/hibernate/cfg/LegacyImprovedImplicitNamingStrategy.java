package org.akaza.openclinica.dao.hibernate.cfg;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.spi.MetadataBuildingContext;

public class LegacyImprovedImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {
	private static final long serialVersionUID = 5627834311320589904L;

	@Override
    protected Identifier toIdentifier(String stringForm, MetadataBuildingContext buildingContext) {
        return super.toIdentifier(addUnderscores(stringForm), buildingContext);
    }

	protected static String addUnderscores(String name) {
		StringBuilder buf = new StringBuilder( name.replace('.', '_') );
		for (int i=1; i<buf.length()-1; i++) {
			if (
				Character.isLowerCase( buf.charAt(i-1) ) &&
				Character.isUpperCase( buf.charAt(i) ) &&
				Character.isLowerCase( buf.charAt(i+1) )
			) {
				buf.insert(i++, '_');
			}
		}
		return buf.toString().toLowerCase(Locale.ROOT);
	}
}
