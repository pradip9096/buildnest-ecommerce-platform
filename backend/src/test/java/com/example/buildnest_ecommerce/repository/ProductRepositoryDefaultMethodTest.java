package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ProductRepository default method tests")
class ProductRepositoryDefaultMethodTest {

    @Test
    @DisplayName("Should default null days to zero")
    void testFindExpiringSoonNullDays() {
        ProductRepository repository = mock(ProductRepository.class, CALLS_REAL_METHODS);
        when(repository.findExpiringSoonByDate(any(LocalDate.class))).thenReturn(List.of());

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        repository.findExpiringSoon(null);

        verify(repository).findExpiringSoonByDate(dateCaptor.capture());
        assertEquals(LocalDate.now(), dateCaptor.getValue());
    }

    @Test
    @DisplayName("Should clamp negative days to zero")
    void testFindExpiringSoonNegativeDays() {
        ProductRepository repository = mock(ProductRepository.class, CALLS_REAL_METHODS);
        when(repository.findExpiringSoonByDate(any(LocalDate.class))).thenReturn(List.of());

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        repository.findExpiringSoon(-5);

        verify(repository).findExpiringSoonByDate(dateCaptor.capture());
        assertEquals(LocalDate.now(), dateCaptor.getValue());
    }

    @Test
    @DisplayName("Should offset days when provided")
    void testFindExpiringSoonPositiveDays() {
        ProductRepository repository = mock(ProductRepository.class, CALLS_REAL_METHODS);
        when(repository.findExpiringSoonByDate(any(LocalDate.class))).thenReturn(List.of(new Product()));

        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        List<Product> result = repository.findExpiringSoon(3);

        verify(repository).findExpiringSoonByDate(dateCaptor.capture());
        assertEquals(LocalDate.now().plusDays(3), dateCaptor.getValue());
        assertEquals(1, result.size());
    }
}
