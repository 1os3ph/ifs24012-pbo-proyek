package org.delcom.app.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JobApplicationDTOTest {

    @Test
    @DisplayName("DTO harus dapat menyimpan dan mengambil data dengan benar (Lombok @Data)")
    void testDtoGetterSetter() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDate now = LocalDate.now();
        JobApplicationDTO dto = new JobApplicationDTO();

        // Act
        dto.setId(id);
        dto.setCompanyName("Delcom Corp");
        dto.setAppliedDate(now);
        dto.setExpectedSalary(5000000);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals("Delcom Corp", dto.getCompanyName());
        assertEquals(now, dto.getAppliedDate());
        assertEquals(5000000, dto.getExpectedSalary());
        assertNull(dto.getNotes()); // Pastikan field yang tidak di-set null
    }
}