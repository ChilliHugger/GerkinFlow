package com.chillihugger.gerkinflow

import org.junit.Assert.assertEquals

@Scope(feature = "Example Feature")
@Scope(tag = "tag1")
class ExampleSteps {

    private var firstNumber:Int = 0
    private var secondNumber:Int = 0
    private var result:Int = 0

    @Given("I start with a value of (\\d+)")
    fun givenIStartWithAValue(value:Int)
    {
        println("givenIStartWithAValue $value - invoked")
        firstNumber = value
    }

    @Given("I start with an int value of (\\d+) and a string value of '(.*)'")
    fun givenIStartWithAnIntValue(value1:Int, value2:String)
    {
        println("givenIStartWithAnIntValue $value1, $value2 - invoked")
    }

    @Given("I have a second value of (\\d+)")
    fun givenIHaveASecondValue(value:Int)
    {
        println("givenIHaveASecondValue $value - invoked")
        secondNumber = value
    }

    @When("I add two numbers")
    fun whenIAddTwoNumbers()
    {
        println("whenIAddTwoNumbers - invoked")
        result = firstNumber + secondNumber
    }

    @Then("the result should be (\\d+)")
    fun thenTheResultShouldBe(value:String)
    {
        println("thenTheResultShouldBe $value - invoked")
        assertEquals( value.toInt(), result)
    }
}