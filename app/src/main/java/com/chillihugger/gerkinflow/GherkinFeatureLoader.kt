package com.chillihugger.gerkinflow

class GherkinFeatureLoader {

    private var currentFeature = GherkinFeature()
    private var currentScenario = GherkinScenario()
    private var currentSteps: MutableList<String> = mutableListOf()
    private var currentScenarios: MutableList<GherkinScenario> = mutableListOf()
    private var description = ""
    private var currentMode = LineType.None
    private var lastMode = LineType.None

    private fun String.fullTrim(): String = (this as CharSequence).trimStart(' ', '\t').toString()

    fun load(feature: String) : GherkinFeature
    {
        currentFeature = GherkinFeature()
        currentScenario = GherkinScenario()
        currentSteps = mutableListOf()
        currentScenarios = mutableListOf()

        description = ""
        currentMode = LineType.None
        lastMode = LineType.None

        feature.lines().forEach {

            it.trimStart()
            val line = it.fullTrim()

            val type = getLineType(line)

            // start feature section
            if(type == LineType.Feature) {
                startFeatureSection(line.substring(8).fullTrim(), type)
            }

            // consume feature description
            if(type == LineType.None && currentMode == LineType.Feature) {
                description += line
            }

            // start background section
            if(type == LineType.Background) {
                startBackgroundSection(line.substring(11).fullTrim(), type)
            }

            // start a scenario
            if(type == LineType.Scenario) {
                startScenarioSection(line.substring(9).fullTrim(), type)
            }

            if(type == LineType.Given) {
                if(currentMode != LineType.Scenario && currentMode != LineType.Background) {
                    throw Exception("Given out of order")
                }
                currentSteps.add(line)
                currentMode = type
            }

            if(type == LineType.And) {
                if(currentMode != LineType.Given && currentMode != LineType.Then) {
                    throw Exception("And out of order")
                }
                currentSteps.add(line)
            }

            if(type == LineType.When) {
                if(currentMode != LineType.Given) {
                    throw Exception("When out of order")
                }
                currentSteps.add(line)
                currentMode = type
            }

            if(type == LineType.Then) {
                if(currentMode != LineType.When) {
                    throw Exception("Then out of order")
                }
                currentSteps.add(line)
                currentMode = type
            }
        }

        //
        endScenarioSection()

        currentFeature.scenarios = currentScenarios

        return currentFeature
    }

    private fun startScenarioSection(it: String, type: LineType) {
        if (currentMode == LineType.Feature) {
            currentFeature.description = description
            currentFeature.background = GherkinScenario()
        }

        endScenarioSection()

        currentScenario = GherkinScenario(it, currentFeature)
        currentSteps = mutableListOf()
        currentMode = type
        lastMode = type
    }

    private fun endScenarioSection() {
        currentScenario.steps = currentSteps

        if (lastMode == LineType.Background) {
            currentFeature.background = currentScenario
        }

        if (lastMode == LineType.Scenario) {
            currentScenarios.add(currentScenario)
        }
    }

    private fun startFeatureSection(it: String, type: LineType) {
        if (currentMode != LineType.None) {
            throw Exception("Feature already initialised")
        }

        currentFeature = GherkinFeature(it)
        description = ""
        currentMode = type
    }

    private fun startBackgroundSection(it: String, type: LineType) {
        if (currentMode == LineType.Feature) {
            currentFeature.description = description
            currentScenario = GherkinScenario(it, currentFeature)
        }

        if (currentMode != LineType.Feature) {
            throw Exception("Background not allowed here")
        }

        currentSteps = mutableListOf()
        currentMode = type
        lastMode = type
    }

    private fun getLineType(line: String) : LineType
    {
        return when {
            line.startsWith("Feature:", true) -> LineType.Feature
            line.startsWith("Background:", true) -> LineType.Background
            line.startsWith("Scenario:", true) -> LineType.Scenario
            line.startsWith("Given ", true) -> LineType.Given
            line.startsWith("And ", true) -> LineType.And
            line.startsWith("When ", true) -> LineType.When
            line.startsWith("Then ", true) -> LineType.Then
            else -> LineType.None
        }
    }
}