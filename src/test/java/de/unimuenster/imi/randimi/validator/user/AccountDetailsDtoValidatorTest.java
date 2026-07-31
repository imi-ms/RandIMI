package de.unimuenster.imi.randimi.validator.user;

import de.unimuenster.imi.randimi.dto.user.AccountDetailsDTO;
import de.unimuenster.imi.randimi.validator.ValidatorTestBase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AccountDetailsDtoValidatorTest extends ValidatorTestBase {

	@Autowired
	private Validator validator;

	@Test
	public void validateValid() {
		var validDto = getValidDto();

		var violations = validator.validate(validDto);
		assertTrue(violations.isEmpty(), "There should be no violations!");
	}

	@Test
	public void validateUsernameWithAt() {
		var validDto = getValidDto();
		validDto.setUsername("user@name");

		var violations = validator.validate(validDto);
		testViolation(violations, "username", getMsg("validator.user.username.mustNotContainAt"));
	}

	@Test
	public void validateUsernameTaken() {
		var validDto = getValidDto();
		validDto.setUsername("ACTIVE_TEST_USER");

		var violations = validator.validate(validDto);
		testViolation(violations, "username", getMsg("validator.user.username.taken"));
	}

	private void testViolation(final Set<ConstraintViolation<AccountDetailsDTO>> violations, final String expectedPath,
	                           final String expectedMessage) {
		var iterator = violations.iterator();
		assertTrue(iterator.hasNext(), "There should be one violation!");

		var violation = iterator.next();
		assertEquals(expectedPath, violation.getPropertyPath().toString(), "Violation has an unexpected path!");
		assertEquals(expectedMessage, violation.getMessage(), "Violation has an unexpected message!");

		assertFalse(iterator.hasNext(), "There should be only one violation!");
	}

	private AccountDetailsDTO getValidDto() {
		AccountDetailsDTO dto = new AccountDetailsDTO();
		dto.setId(1);
		dto.setUsername("username");
		dto.setFirstName("John");
		dto.setLastName("Doe");
		dto.setMailAddress("email@mail.de");
		dto.setUpdatePassword(false);
		return dto;
	}
}
