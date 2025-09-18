package ro.gs1.btmock.creditCards;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.enterprise.context.ApplicationScoped;
import ro.gs1.btmock.entity.CreditCardEntity;

@ApplicationScoped
public class CreditCardService {

	private static final SecureRandom random = new SecureRandom();
	private static final DateTimeFormatter expiryFormatter = DateTimeFormatter.ofPattern("MM/yy");
	private static final List<String> FIRST_NAMES = List.of(
	        "Ioana", "Andrei", "Maria", "George", "Elena", "Vlad", "Anca", "Mihai", "Ana", "Darius"
	    );

	    private static final List<String> LAST_NAMES = List.of(
	        "Popescu", "Ionescu", "Stan", "Dumitrescu", "Radu", "Neagu", "Matei", "Stoica", "Enache", "Vasilescu"
	    );

	public CreditCardEntity generateAndStoreCard() {
		String cardNumber = generateRandomCardNumber();
		String expiryDate = generateRandomExpiryDate();
		String securityCode = generateRandomCVV();
		String nameOnCard = generateRandomCardholderName();

		CreditCardEntity card = new CreditCardEntity(cardNumber, expiryDate, securityCode, nameOnCard);
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


	public static String generateRandomCardholderName() {
        String first = FIRST_NAMES.get(ThreadLocalRandom.current().nextInt(FIRST_NAMES.size()));
        String last = LAST_NAMES.get(ThreadLocalRandom.current().nextInt(LAST_NAMES.size()));
        return first + " " + last;
    }

	private String generateRandomExpiryDate() {
		YearMonth futureDate = YearMonth.now().plusMonths(random.nextInt(36) + 1);
		return futureDate.format(expiryFormatter);
	}

	private String generateRandomCVV() {
		return String.format("%03d", random.nextInt(1000));
	}
}