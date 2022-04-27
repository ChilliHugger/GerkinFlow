package com.chillihugger.gerkinflow

import kotlin.reflect.KCallable
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.jvmErasure

class StepMatch(
    val callable:KCallable<*>,
    var target:Any,
    val statement:String,
    val step:Annotation,
    val matchResult:MatchResult)

class GherkinStepClass(
    val targetClass: Any,
    val callables: List<KCallable<*>>,
    val scope: Any? = null,
    val tags: Any? = null
)

class GherkinScenario(
    var description: String = "",
    var feature:GherkinFeature? = null
) {
    var steps: List<String> = emptyList()
}

class GherkinFeature(
    var name: String = ""
) {
    var description: String = ""
    var background: GherkinScenario? = null
    var scenarios: List<GherkinScenario> = emptyList()
}

enum class LineType
{
    None,
    Feature,
    Background,
    Scenario,
    Given,
    And,
    When,
    Then
}

enum class StepType
{
    Given,
    And,
    When,
    Then
}


class GherkinProcessor {

    private var stepClasses: MutableList<GherkinStepClass> = mutableListOf()

    fun registerSteps(targetClass: Any): Boolean {

        val stepClass = GherkinStepClass(
            targetClass,
            findMethods(targetClass)
            )

        stepClasses.add(stepClass)
        return true
    }

    fun executeScenario(scenario: String, feature: GherkinFeature): Boolean {
        return feature
            .scenarios
            .first { s -> s.description == scenario }
            .run {
                executeScenario(this)
            }
    }

    private fun executeScenario(scenario: GherkinScenario): Boolean {
        scenario.feature?.background?.steps?.run {
            executeSteps(this)
        }
        executeSteps(scenario.steps)
        return true
    }

    private fun executeSteps(steps: List<String>) {
        var lastStep = StepType.Given
        steps.forEach {
            val type = when (getLineType(it.trim(' ', '\t'))) {
                LineType.Given -> StepType.Given
                LineType.And -> lastStep
                LineType.When -> StepType.When
                LineType.Then -> StepType.Then
                else -> throw Exception("Invalid step type")
            }
            if(!executeStep(type,it)){
                throw Exception("Missing Step '$it'")
            }
            lastStep = type
        }
    }

    internal fun executeStep(type: StepType, step:String) : Boolean {
        val statement = when (getLineType(step.trim(' ', '\t'))) {
            LineType.Given -> step.substring(6)
            LineType.And -> step.substring(4)
            LineType.When -> step.substring(5)
            LineType.Then -> step.substring(5)
            else -> throw Exception("Invalid step type")
        }

        val matchingStep = findStepsMatching(type,statement).firstOrNull()

        matchingStep?.let{
            println("Method: '${it.callable.name}' Statement:${it.statement}")

            // extract args
            println("Arguments:")
            it.callable.parameters.forEach { param ->
                println("[${param.index}] ${param.name}:${param.type}")
            }

            //val argumentCount = it.callable.parameters.count()
            val result = it.matchResult

            // first one is the whole string so we can skip it
            val givenArguments = result.groupValues.toTypedArray<Any?>() //.drop(1) //?: emptyList<String>()

            // convert given arguments to correct target type
            for ((index, value) in it.callable.parameters.withIndex()) {
                if(index==0) continue
                givenArguments[index] = when (value.type.jvmErasure) {
                    String::class -> givenArguments[index]
                    Int::class -> givenArguments[index]?.toString()?.toInt()
                    Boolean::class -> givenArguments[index]?.toString()?.toBoolean()
                    else -> null
                }
            }
            givenArguments[0] = it.target
            //

            // build array of values to pass to the caller
            //val args = arrayOf(
            //    *(arrayOf<Any?>( it.target )),
            //    *(givenArguments /*.toTypedArray<Any?>*()*/))

            println("Calling ${it.callable.name} with ${givenArguments.count()} arguments")
            givenArguments.forEach { arg ->
                println(arg)
            }

            // TODO: allow
            // it.callable.callSuspend(*args)
            it.callable.call(*givenArguments)

            return true
        }

        return false
    }

    private fun findMethods(targetClass: Any) : List<KCallable<*>> {
        return targetClass.javaClass.kotlin.functions
            .filter { m -> m.hasAnnotation<Given>() || m.hasAnnotation<When>() || m.hasAnnotation<Then>()}
    }

    private fun findMethods(type: StepType, targetClass: GherkinStepClass) : List<KCallable<*>> {
        return when(type) {
            StepType.Given -> targetClass.callables.filter { m -> m.hasAnnotation<Given>()}
            StepType.When -> targetClass.callables.filter { m -> m.hasAnnotation<When>()}
            StepType.Then -> targetClass.callables.filter { m -> m.hasAnnotation<Then>()}
            else -> throw Exception("Invalid step type")
        }
    }

    private fun isRegexMatch(target: Any, callable: KCallable<*>, statement: String) : StepMatch? {

        val steps = callable.annotations.filter {
            it is Given || it is When || it is Then
        }

        steps.forEach {
            val stepStatement = getAnnotationStatement(it)
            val regex = stepStatement.toRegex()
            val result = regex.matchEntire(statement)
            if (result?.groups?.any() == true) {
                return StepMatch(callable, target, stepStatement, it, result)
            }
        }
        return null
    }

    private fun getAnnotationStatement(annotation: Annotation): String {
        return when (annotation) {
            is Given -> annotation.statement
            is When -> annotation.statement
            is Then -> annotation.statement
            else -> throw Exception("Invalid annotation type")
        }
    }

    private fun getLineType(line: String) : LineType {
        return when {
            line.startsWith("Given ", true) -> LineType.Given
            line.startsWith("And ", true) -> LineType.And
            line.startsWith("When ", true) -> LineType.When
            line.startsWith("Then ", true) -> LineType.Then
            else -> throw Exception("Invalid step type")
        }
    }

    private fun findStepsMatching(type:StepType, statement: String) : List<StepMatch> {
        val results : MutableList<StepMatch> = mutableListOf()
        // TODO: Filter classes by scope
        stepClasses.forEach { stepClass ->
            // TODO: filter methods by scope
            val steps = findMethods(type, stepClass).mapNotNull { isRegexMatch(stepClass.targetClass, it, statement) }
            results.addAll(steps)
        }
        return results
    }

}