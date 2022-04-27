package com.chillihugger.gerkinflow

import junit.framework.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleFeature {

    private var gherkinLoader = GherkinFeatureLoader()
    private var gherkinfeature:GherkinFeature? = null
    private var gherkinProcessor = GherkinProcessor()


    private var feature : String = """
    Feature: Example Feature
    
    Scenario: Adding 100 and 50 should equal 150
        Given I start with a value of 100
        And I have a second value of 50
        When I add two numbers
        Then the result should be 150
        
    Scenario: Adding 20 and 75 should equal 95
        Given I start with a value of 20
        And I have a second value of 75
        When I add two numbers
        Then the result should be 95
        """

    @Before
    fun before() {
        gherkinfeature = gherkinLoader.load(feature)
        gherkinProcessor.registerSteps(ExampleSteps())
    }

    @Test
    fun steps_executeFeature1() {
        val result = gherkinProcessor.executeScenario("Adding 100 and 50 should equal 150", gherkinfeature!!)
        assertTrue(result)
    }

    @Test
    fun steps_executeFeature2() {
        val result = gherkinProcessor.executeScenario("Adding 20 and 75 should equal 95", gherkinfeature!!)
        assertTrue(result)
    }
}