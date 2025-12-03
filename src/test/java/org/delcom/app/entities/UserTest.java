package org.delcom.app.entities;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserEntity() {
        // 1. Test Constructor Kosong
        User user = new User();
        assertNotNull(user);

        // 2. Test Setter & Getter
        UUID id = UUID.randomUUID();
        String name = "Budi";
        String email = "budi@example.com";
        String password = "rahasia123";

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());

        // 3. Test Constructor dengan Parameter
        User user2 = new User(email, password); // Constructor 2 parameter
        assertEquals(email, user2.getEmail());
        assertEquals(password, user2.getPassword());

        User user3 = new User(name, email, password); // Constructor 3 parameter
        assertEquals(name, user3.getName());
        assertEquals(email, user3.getEmail());
    }

    @Test
    void testLifecycleMethods() throws Exception {
        // 1. Setup User
        User user = new User();
        
        // 2. Test onCreate (PrePersist) menggunakan Reflection
        // Kita "memaksa" ambil method protected bernama 'onCreate'
        java.lang.reflect.Method onCreate = User.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true); // Buka aksesnya
        onCreate.invoke(user);        // Jalankan methodnya

        // Assertion
        assertNotNull(user.getCreatedAt(), "createdAt harus terisi");
        assertNotNull(user.getUpdatedAt(), "updatedAt harus terisi");

        // Simpan waktu lama untuk perbandingan
        java.time.LocalDateTime oldTime = user.getUpdatedAt();
        
        // Jeda 10ms biar waktunya beda dikit
        Thread.sleep(10); 

        // 3. Test onUpdate (PreUpdate) menggunakan Reflection
        java.lang.reflect.Method onUpdate = User.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(user);

        // Assertion: Waktu update harus berubah jadi lebih baru
        assertTrue(user.getUpdatedAt().isAfter(oldTime), "updatedAt harus berubah setelah update");
    }
}