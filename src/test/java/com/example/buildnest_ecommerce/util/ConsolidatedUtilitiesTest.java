package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.validator.InputValidationHelper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsolidatedUtilitiesTest {

    @Test
    void validationHelpersDelegateAndThrowOnInvalid() {
        InputValidationHelper helper = mock(InputValidationHelper.class);
        when(helper.validateEmail("test@example.com")).thenReturn(InputValidationHelper.ValidationResult.valid());
        when(helper.validateEmail("bad")).thenReturn(InputValidationHelper.ValidationResult.invalid("invalid"));

        ConsolidatedUtilities utils = new ConsolidatedUtilities();
        ReflectionTestUtils.setField(utils, "validationHelper", helper);

        assertEquals("test@example.com", utils.validateAndGetEmail("test@example.com"));
        assertThrows(IllegalArgumentException.class, () -> utils.validateAndGetEmail("bad"));
    }

    @Test
    void stringAndCollectionUtilitiesWork() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertEquals("name", utils.requireNonEmpty(" name ", "Name"));
        assertTrue(utils.isNullOrEmpty(" "));
        assertEquals("abc", utils.truncateToLength("abcdef", 3));
        assertEquals("ab", utils.truncateToLength("ab", 3));

        assertTrue(utils.isNullOrEmpty(Collections.emptyList()));
        assertTrue(utils.getFirstItem(Collections.emptyList()).isEmpty());
        assertEquals("one", utils.getFirstItem(Arrays.asList("one", "two")).orElse(null));
        assertTrue(utils.getAtIndex(Arrays.asList("one"), 1).isEmpty());
        assertEquals("one", utils.getAtIndex(Arrays.asList("one", "two"), 0).orElse(null));
    }

    @Test
    void numberUtilitiesWork() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertTrue(utils.isZero(BigDecimal.ZERO));
        assertTrue(utils.isPositive(new BigDecimal("1.0")));
        assertTrue(utils.isNegative(new BigDecimal("-1.0")));

        assertEquals(new BigDecimal("10.00"), utils.roundToTwoDecimals(new BigDecimal("10")));
        assertEquals(BigDecimal.ZERO, utils.calculatePercentage(null, new BigDecimal("10")));
        assertEquals(new BigDecimal("5.00"), utils.calculatePercentage(new BigDecimal("50"), new BigDecimal("10")));
    }

    @Test
    void dateUtilitiesWork() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        LocalDate today = LocalDate.now();
        assertTrue(utils.isToday(today));
        assertTrue(utils.isInPast(today.minusDays(1)));
        assertTrue(utils.isInFuture(today.plusDays(1)));
        assertEquals(1, utils.daysBetween(today, today.plusDays(1)));
    }

    @Test
    void objectAndFallbackUtilitiesWork() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertEquals("value", utils.getOrDefault("value", "default"));
        assertEquals("default", utils.getOrDefault(null, "default"));
        assertThrows(IllegalArgumentException.class, () -> utils.requireNonNull(null, "Field"));

        String result = utils.executeWithFallback(() -> "ok", "fallback");
        assertEquals("ok", result);

        String fallback = utils.executeWithFallback(() -> {
            throw new RuntimeException("boom");
        }, "fallback");
        assertEquals("fallback", fallback);
    }

    @Test
    void paginationUtilitiesWork() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertEquals(20, utils.calculateOffset(1, 20));
        assertThrows(IllegalArgumentException.class, () -> utils.calculateOffset(-1, 10));

        assertEquals(5, utils.validatePageSize(1, 5, 20));
        assertEquals(20, utils.validatePageSize(30, 5, 20));
        assertEquals(10, utils.validatePageSize(10, 5, 20));
    }

    @Test
    void validationHelpersUsingRealHelper() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();
        ReflectionTestUtils.setField(utils, "validationHelper", new InputValidationHelper());

        assertEquals("user@example.com", utils.validateAndGetEmail("user@example.com"));
        assertEquals("ValidPass@123", utils.validateAndGetPassword("ValidPass@123"));
        assertEquals("14155552671", utils.validateAndGetPhoneNumber("+1 (415) 555-2671"));
        assertEquals(new BigDecimal("10.00"), utils.validateAndGetPrice(new BigDecimal("10.00")));
        assertEquals(10, utils.validateAndGetQuantity(10));

        assertThrows(IllegalArgumentException.class, () -> utils.validateAndGetPassword("short"));
        assertThrows(IllegalArgumentException.class, () -> utils.validateAndGetPhoneNumber("000"));
        assertThrows(IllegalArgumentException.class, () -> utils.validateAndGetPrice(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> utils.validateAndGetQuantity(-1));
    }

    @Test
    void nullHandlingInNumberOperations() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertFalse(utils.isZero(null));
        assertFalse(utils.isPositive(null));
        assertFalse(utils.isNegative(null));
        assertFalse(utils.isZero(new BigDecimal("5")));
        assertFalse(utils.isPositive(new BigDecimal("-5")));
        assertFalse(utils.isNegative(new BigDecimal("5")));

        assertNull(utils.roundToTwoDecimals(null));
        assertEquals(BigDecimal.ZERO, utils.calculatePercentage(BigDecimal.ZERO, new BigDecimal("10")));
        assertEquals(BigDecimal.ZERO, utils.calculatePercentage(new BigDecimal("10"), null));
    }

    @Test
    void nullHandlingInDateOperations() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertFalse(utils.isInPast(null));
        assertFalse(utils.isInFuture(null));
        assertFalse(utils.isToday(null));
        assertEquals(0, utils.daysBetween(null, LocalDate.now()));
        assertEquals(0, utils.daysBetween(LocalDate.now(), null));
        assertEquals(0, utils.daysBetween(null, null));
    }

    @Test
    void stringNullAndEmptyHandling() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertTrue(utils.isNullOrEmpty((String) null));
        assertTrue(utils.isNullOrEmpty(""));
        assertTrue(utils.isNullOrEmpty("   "));
        assertFalse(utils.isNullOrEmpty("text"));

        assertNull(utils.truncateToLength(null, 5));
        assertEquals("", utils.truncateToLength("", 5));
    }

    @Test
    void requireNonEmptyWithVariousCases() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertEquals("test", utils.requireNonEmpty("test", "Field"));
        assertEquals("text", utils.requireNonEmpty("  text  ", "Field"));
        assertThrows(IllegalArgumentException.class, () -> utils.requireNonEmpty(null, "Field"));
        assertThrows(IllegalArgumentException.class, () -> utils.requireNonEmpty("", "Field"));
        assertThrows(IllegalArgumentException.class, () -> utils.requireNonEmpty("   ", "Field"));
    }

    @Test
    void collectionOperationsWithEdgeCases() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertTrue(utils.isNullOrEmpty((List<?>) null));
        assertTrue(utils.isNullOrEmpty(Collections.emptyList()));
        assertFalse(utils.isNullOrEmpty(Arrays.asList("a", "b")));

        assertTrue(utils.getFirstItem(null).isEmpty());
        assertTrue(utils.getFirstItem(Collections.emptyList()).isEmpty());

        assertTrue(utils.getAtIndex(null, 0).isEmpty());
        assertTrue(utils.getAtIndex(Collections.emptyList(), 0).isEmpty());
        assertTrue(utils.getAtIndex(Arrays.asList("a"), -1).isEmpty());
        assertTrue(utils.getAtIndex(Arrays.asList("a"), 5).isEmpty());
    }

    @Test
    void percentageCalculationEdgeCases() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertEquals(BigDecimal.ZERO, utils.calculatePercentage(new BigDecimal("0"), new BigDecimal("50")));
        assertEquals(new BigDecimal("25.00"), utils.calculatePercentage(new BigDecimal("100"), new BigDecimal("25")));
        assertEquals(new BigDecimal("0.50"), utils.calculatePercentage(new BigDecimal("10"), new BigDecimal("5")));
    }

    @Test
    void paginationEdgeCases() {
        ConsolidatedUtilities utils = new ConsolidatedUtilities();

        assertEquals(0, utils.calculateOffset(0, 20));
        assertEquals(100, utils.calculateOffset(5, 20));
        assertThrows(IllegalArgumentException.class, () -> utils.calculateOffset(0, 0));
        assertThrows(IllegalArgumentException.class, () -> utils.calculateOffset(0, -5));

        assertEquals(5, utils.validatePageSize(3, 5, 20));
        assertEquals(5, utils.validatePageSize(5, 5, 20));
        assertEquals(20, utils.validatePageSize(25, 5, 20));
    }
}
