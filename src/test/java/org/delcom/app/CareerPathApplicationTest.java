package org.delcom.app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CareerPathApplicationTest {

    @Test
    @DisplayName("Aplikasi harus bisa start (Context Load)")
    void contextLoads() {
        // Test ini otomatis sukses jika aplikasi berhasil start.
        // Jika ada bean yang error atau konfigurasi salah, test ini akan gagal.
    }

    @Test
    @DisplayName("Method main harus bisa dipanggil tanpa error")
    void testMain() {
        // Kita panggil method main secara manual agar baris 'SpringApplication.run(...)' terjamah.
        // PENTING: Kita kirim parameter "--server.port=0" agar menggunakan PORT ACAK.
        // Ini mencegah error "Port 8080 was already in use" saat test dijalankan.
        CareerPathApplication.main(new String[]{"--server.port=0"});
    }
}