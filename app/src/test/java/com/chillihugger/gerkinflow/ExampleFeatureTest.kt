package com.chillihugger.gerkinflow

import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ExampleFeatureTest {

    private var gherkinLoader = GherkinFeatureLoader()

    private var feature : String = """
    Feature: Example Feature
    
    Background: This is the background section
        Given I do this step in the background
        And then I do this step in the background
      
    Scenario: This is the first scenario section
        Given I start with this step
        And I proceed to this step
        When I do this step
        Then this step will be ok
        And this step will be ok too
      
    Scenario: This is the second scenario section
        Given I start with this step
        When I do this step
        Then this step will be ok too
        """

    @Test
    fun steps_loadFeature() {
        val result = gherkinLoader.load(feature)
        assertEquals("Example Feature", result.name)
    }

    @Test
    fun steps_background() {
        val result = gherkinLoader.load(feature)
        assertEquals("This is the background section", result.background?.description)
    }

    @Test
    fun steps_scenariosLoaded() {
        val result = gherkinLoader.load(feature)
        assertEquals(2, result.scenarios.count())
    }

    @Test
    fun steps_scenariosFirst() {
        val result = gherkinLoader.load(feature)
        assertEquals("This is the first scenario section", result.scenarios.first().description)
        assertEquals(5, result.scenarios.first().steps.count())
    }

    @Test
    fun steps_scenariosSecond() {
        val result = gherkinLoader.load(feature)
        assertEquals("This is the second scenario section", result.scenarios.last().description)
        assertEquals(3, result.scenarios.last().steps.count())
    }
}