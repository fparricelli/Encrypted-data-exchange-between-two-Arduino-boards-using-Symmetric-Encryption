package it.chat.gui;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

public class Main {
	
	public static void main(String[] args) {
		PasswordValidator validator = new PasswordValidator(
				  // length between 8 and 16 characters
				  new LengthRule(8, 16),

				  // at least one upper-case character
				  new CharacterRule(EnglishCharacterData.UpperCase, 1),

				  // at least one lower-case character
				  new CharacterRule(EnglishCharacterData.LowerCase, 1),

				  // at least one digit character
				  new CharacterRule(EnglishCharacterData.Digit, 1),

				  // at least one symbol (special character)
				  new CharacterRule(EnglishCharacterData.Special, 1),

				  // no whitespace
				  new WhitespaceRule());

				String password = "ciao";
				RuleResult result = validator.validate(new PasswordData(password));
				if (result.isValid()) {
				  System.out.println("Password is valid");
				} else {
				  System.out.println("Invalid password:");
				  for (String msg : validator.getMessages(result)) {
				    System.out.println(msg);
				  }
				}
	}

}
