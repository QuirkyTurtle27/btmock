package ro.gs1.btmock.paymentmethods;

import jakarta.transaction.Transactional;
import ro.gs1.btmock.entity.CreditCardEntity;
import ro.gs1.btmock.entity.OrderEntity;

public class PaymentOutcomeSimulator {
	 // Your convention: PAN format "4444 4444 4444 XXXX" -> last 4 encode the actionCode suffix
	  private Integer actionCodeFromPan(String pan) {
	    if (pan == null) return null;
	    String digits = pan.replaceAll("\\s", "");
	    if (digits.length() < 16) return null;
	    String last4 = digits.substring(digits.length() - 4);
	    // Map “XXXX” to a full code:
	    // direct 4-digit codes -> 861, 871, 905, 906, 913, 914, 915, 917, 952, 998
	    // 0104 -> 104, 0124 -> 124, 0320 -> 320, 0801 -> 801, 0803 -> 803, 0804 -> 804, 0805 -> 805
	    // 6016 -> 341016 ... 6020 -> 341020
	    switch (last4) {
	      case "0104": return 104;
	      case "0124": return 124;
	      case "0320": return 320;
	      case "0801": return 801;
	      case "0803": return 803;
	      case "0804": return 804;
	      case "0805": return 805;
	      case "0861": return 861;
	      case "0871": return 871;
	      case "0905": return 905;
	      case "0906": return 906;
	      case "0913": return 913;
	      case "0914": return 914;
	      case "0915": return 915;
	      case "0917": return 917;
	      case "0952": return 952;
	      case "0998": return 998;
	      case "6016": return 341016;
	      case "6017": return 341017;
	      case "6018": return 341018;
	      case "6019": return 341019;
	      case "6020": return 341020;
	      default: return null;
	    }
	  }

	  private static String maskPan(String pan) {
	    if (pan == null) return null;
	    String digits = pan.replaceAll("\\s", "");
	    if (digits.length() < 12) return "********";
	    return digits.substring(0, 6).replaceAll(".", "*") + digits.substring(6, digits.length() - 4).replaceAll(".", "*") + digits.substring(digits.length() - 4);
	  }

	  private static boolean isExpired(String expiryYYYYMM) {
	    if (expiryYYYYMM == null || !expiryYYYYMM.matches("\\d{6}")) return true;
	    java.time.YearMonth exp = java.time.YearMonth.of(
	      Integer.parseInt(expiryYYYYMM.substring(0,4)),
	      Integer.parseInt(expiryYYYYMM.substring(4,6))
	    );
	    return exp.isBefore(java.time.YearMonth.now());
	  }

	  /** Apply outcome to an existing order (after “payment”). */
	  @Transactional
	  public void applyOutcome(OrderEntity order, CreditCardEntity card) {
	    // Default: assume success unless a rule triggers an actionCode

	    // Extra validation rules matching documentation’s common declines:

	      order.approvalCode = null;
	      order.eci = null;
	    }

	  }

