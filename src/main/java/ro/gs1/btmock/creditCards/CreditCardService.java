package ro.gs1.btmock.creditCards;

import jakarta.enterprise.context.ApplicationScoped;
import ro.gs1.btmock.entity.CreditCardEntity;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class CreditCardService {

	private static final SecureRandom random = new SecureRandom();
	private static final DateTimeFormatter expiryFormatter = DateTimeFormatter.ofPattern("MM/yy");

	public CreditCardEntity generateAndStoreCard() {
		String cardNumber = generateRandomCardNumber();
		String expiryDate = generateRandomExpiryDate();
		String securityCode = generateRandomCVV();

		CreditCardEntity card = new CreditCardEntity(cardNumber, expiryDate, securityCode);
		card.persist();
		return card;
	}

	private String generateRandomCardNumber() {
		StringBuilder sb = new StringBuilder("4");
		for (int i = 0; i < 15; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}

	private String generateRandomExpiryDate() {
		YearMonth futureDate = YearMonth.now().plusMonths(random.nextInt(36) + 1);
		return futureDate.format(expiryFormatter);
	}

	private String generateRandomCVV() {
		return String.format("%03d", random.nextInt(1000));
	}
}