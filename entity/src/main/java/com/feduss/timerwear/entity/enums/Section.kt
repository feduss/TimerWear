package com.feduss.timerwear.entity.enums

sealed class Section(val baseRoute: String, val parametricRoute: String = "") {
    data object Navigation: Section("navigation")
    data object CustomWorkout: Section("customWorkout")
    data object AddCustomWorkout: Section("addCustomWorkout","addCustomWorkout?${Params.WorkoutId.name}={${Params.WorkoutId.name}}")
    data object Timer: Section("timer","timer/{${Params.WorkoutId.name}}/{${Params.TimerType.name}}?${Params.CurrentTimerIndex.name}={${Params.CurrentTimerIndex.name}}&${Params.CurrentRepetition.name}={${Params.CurrentRepetition.name}}&${Params.CurrentTimerSecondsRemaining.name}={${Params.CurrentTimerSecondsRemaining.name}}")

    fun withArgs(args: List<String>? = null, optionalArgs: Map<String, String>? = null): String {
        var destinationRoute = baseRoute
        args?.let { argsNotNull ->
            for(arg in argsNotNull) {
                destinationRoute += "/$arg"
            }
        }
        optionalArgs?.let { optionalArgsNotNull ->
            destinationRoute+= "?"
            optionalArgsNotNull.onEachIndexed { index, (optionalArgName, optionalArgValue) ->
                destinationRoute += "$optionalArgName=$optionalArgValue"

                if (optionalArgsNotNull.count() > 1 && index < optionalArgsNotNull.count() - 1) {
                    destinationRoute += "&"
                }
            }
        }
        return destinationRoute
    }
}
