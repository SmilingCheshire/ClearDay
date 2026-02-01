package com.example.clearday.models

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    @Test
    fun `User default values are set correctly`() {
        val user = User()
        
        assertEquals("", user.uid)
        assertEquals("", user.name)
        assertEquals("", user.email)
        assertEquals("", user.dob)
        assertTrue(user.trackedAllergens.isEmpty())
        assertEquals("metric", user.units)
        assertEquals(7, user.morningBriefingHour)
        assertEquals(0, user.morningBriefingMinute)
    }

    @Test
    fun `User with custom values initializes correctly`() {
        val allergens = listOf("TREE_BIRCH", "GRASS_TIMOTHY")
        val user = User(
            uid = "test123",
            name = "John Doe",
            email = "john@example.com",
            dob = "1990-01-01",
            trackedAllergens = allergens,
            units = "imperial",
            morningBriefingHour = 8,
            morningBriefingMinute = 30
        )
        
        assertEquals("test123", user.uid)
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
        assertEquals("1990-01-01", user.dob)
        assertEquals(2, user.trackedAllergens.size)
        assertTrue(user.trackedAllergens.contains("TREE_BIRCH"))
        assertEquals("imperial", user.units)
        assertEquals(8, user.morningBriefingHour)
        assertEquals(30, user.morningBriefingMinute)
    }

    @Test
    fun `User copy method works correctly`() {
        val original = User(uid = "abc", name = "Alice")
        val modified = original.copy(name = "Alice Modified", email = "alice@test.com")
        
        assertEquals("abc", modified.uid)
        assertEquals("Alice Modified", modified.name)
        assertEquals("alice@test.com", modified.email)
        assertEquals("", modified.dob) // Should keep default
    }
}
