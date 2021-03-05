package no.liflig.properties

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Properties

// By default no properties files will match the default names and be loaded
class PropertiesLoaderTest {

    @Test
    fun `should load properties for normal runtime with SSM params`() {
        val awsPath = "/construct/current"
        val mockProperties = Properties().apply {
            put("hacker.name", "Henrik")
        }

        val griidPropertiesFetcher = mockk<GriidPropertiesFetcher> {
            every { forPrefix(awsPath) } returns mockProperties
        }

        val properties = loadPropertiesInternal(
            applicationProperties = "testdata/application.properties",
            griidPropertiesFetcher = griidPropertiesFetcher,
            getenv = { awsPath }
        )

        assertEquals("Henrik", properties.getProperty("hacker.name"))
    }

    @Test
    fun `loads invalid files`() {
        // Java properties loading does not perform any validation
        val properties = loadPropertiesInternal(
            applicationProperties = "testdata/invalid.properties"
        )
        assertEquals("Smith", properties.getProperty("hacker.nameAgent"))
    }

    @Test
    fun `all sources are optional`() {
        val properties = loadPropertiesInternal()
        assertEquals(0, properties.size)
    }

    @Test
    fun `override properties are loaded`() {
        val properties = loadPropertiesInternal(
            applicationProperties = "testdata/application.properties",
            overridesProperties = "overrides-for-test.properties"
        )
        assertEquals("Morpheus", properties.getProperty("hacker.name"))
    }

    @Test
    fun `test overrides are loaded`() {
        val properties = loadPropertiesInternal(
            applicationProperties = "testdata/application.properties",
            applicationTestProperties = "testdata/application-test.properties"
        )
        assertEquals("Trinity", properties.getProperty("hacker.name"))
    }

    @Test
    fun `test properties have precedence over all other properties`() {
        val awsPath = "/construct/current"
        val griidPropertiesFetcher = mockk<GriidPropertiesFetcher> {
            every { forPrefix(awsPath) } returns Properties().apply {
                put("hacker.name", "Henrik")
            }
        }

        val properties = loadPropertiesInternal(
            applicationProperties = "testdata/application.properties",
            applicationTestProperties = "testdata/application-test.properties",
            overridesProperties = "overrides-for-test.properties",
            griidPropertiesFetcher = griidPropertiesFetcher,
            getenv = { awsPath }
        )
        assertEquals("Trinity", properties.getProperty("hacker.name"))
    }
}
