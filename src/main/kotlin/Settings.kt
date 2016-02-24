/**
 * Created by chiaki on 24/02/16.
 */
object Settings {
    val checkPeriod : Long = 1000
    val availableSlots = 3
    val totalJobCount = 10

    val maxTimeCostOfJob = 5
    val minTimeCostOfJob = 1

    object EventNames {
        val start = "my.controller.start"
        val jobEnds = "my.controller.jobEnded"
        val ready = "my.controller.ctlReady"
        val allDone = "my.controller.allDone"
        val stopScheduler = "my.controller.stopScheduler"
    }
}