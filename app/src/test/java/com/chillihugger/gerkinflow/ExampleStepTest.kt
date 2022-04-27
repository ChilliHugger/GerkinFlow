package com.chillihugger.gerkinflow

import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleStepTest {

    private var gherkinProcessor = GherkinProcessor()

    @Before
    fun before()
    {
        val steps = ExampleSteps()
        gherkinProcessor.registerSteps(steps)
    }

    @Test
    fun steps_matchingWithoutParameters() {

        val statement = "When I add two numbers"
        val result = gherkinProcessor.executeStep(StepType.When, statement)
        assertTrue(result)
    }

    @Test
    fun steps_matchingWithOneParameter() {
        val statement = "Given I start with a value of 200"
        val result = gherkinProcessor.executeStep(StepType.Given, statement)
        assertTrue(result)
    }

    @Test
    fun steps_matchingWithTwoParameter() {
        val statement = "Given I start with an int value of 200 and a string value of 'fred'"
        val result = gherkinProcessor.executeStep(StepType.Given, statement)
        assertTrue(result)
    }

    @Test
    fun steps_givenStepAsAnd() {
        val statement = "And I start with a value of 200"
        val result = gherkinProcessor.executeStep(StepType.Given, statement)
        assertTrue(result)
    }
}