package com.chillihugger.gerkinflow

@Target(AnnotationTarget.FUNCTION)
annotation class Given(val statement: String)
@Target(AnnotationTarget.FUNCTION)
annotation class When(val statement: String)
@Target(AnnotationTarget.FUNCTION)
annotation class Then(val statement: String)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Repeatable
annotation class Scope(val tag: String="", val feature: String="")


