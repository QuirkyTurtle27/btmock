package ro.gs1.btmock.converters;

import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import ro.gs1.btmock.entity.CreditCardEntity;

@FacesConverter(value = "creditCardConverter", managed = true)
public class CreditCardConverter implements Converter<CreditCardEntity> {

	private static final Logger LOG = Logger.getLogger(CreditCardConverter.class);

	@Override
	public String getAsString(FacesContext context, UIComponent component, CreditCardEntity card) {
		if (card == null) {
			return null;
		}
		return (card == null || card.id == null) ? "" : card.id.toString();
	}

	@Override
	public CreditCardEntity getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.isBlank())
			return null;
		CreditCardEntity retval = CreditCardEntity.findById(new ObjectId(value));
		LOG.debugf("Converted found %s , %s",value, retval);
		return retval;
	}
}
