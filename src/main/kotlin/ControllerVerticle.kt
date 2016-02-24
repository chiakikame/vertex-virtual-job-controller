/**
 * Created by chiaki on 23/02/16.
 */

import io.vertx.core.AbstractVerticle

class ControllerVerticle : AbstractVerticle() {
    val jobList : List<Job>? = Job.randomJobs(Settings.totalJobCount)
    var running : Boolean = false

    override fun start() {
        println("Controller started")
        vertx.eventBus().consumer<String>(Settings.EventNames.start, {message ->
            println("Message received, scheduling no matter what (Message body is: ${message.body()})")
            scheduleJob()
        })
        vertx.eventBus().consumer<String>(Settings.EventNames.jobEnds, {jobStrMsg ->
            println("Job ${jobStrMsg.body()} finished")
        })
        vertx.eventBus().publish(Settings.EventNames.ready, "ready")
        println("Controller ready")
    }

    override fun stop() {
        println("Controller stopped")
    }

    private fun scheduleJob() {
        if (jobList == null || running) return
        running = true
        var currentIndex = 0
        var availableSlots = Settings.availableSlots
        var finishedJobs = 0

        val timerId = vertx.setPeriodic(Settings.checkPeriod, {
            while (availableSlots > 0) {
                if (currentIndex >= jobList.size) {
                    println("All scheduled, shuting down the scheduler")
                    vertx.eventBus().publish(Settings.EventNames.stopScheduler, "")
                    vertx.setPeriodic(Settings.checkPeriod, {
                        println("Waiting for all jobs ...")
                        if (finishedJobs == this.jobList.count()) {
                            vertx.close()
                            println("Shutting down vertx")
                        }
                    })
                    break
                }

                val job = jobList[currentIndex]

                // The executeBlocking function will 'schedule' the 'jobs'
                // in queue-like fashion.
                // If you wish to see parallel execution of the blocked
                // operations, remove the comment in the 'false'
                //
                // Have no idea if it will cause wrong result
                vertx.executeBlocking<Job>({ future ->
                    for(i in 1 .. job.jobTimeCost) {
                        println("Job $job running ($i/${job.jobTimeCost})")
                        Thread.sleep(1000)
                    }
                    future.complete(job)
                }, /* false,*/ { jobResult ->
                    println("${jobResult}, ${jobResult.result()}")
                    vertx.eventBus().send(Settings.EventNames.jobEnds, jobResult.result()?.toString())
                    availableSlots += 1
                    finishedJobs += 1
                })

                println("Available slot: $availableSlots/ ${Settings.availableSlots}")
                println("current index: $currentIndex")
                availableSlots -= 1
                currentIndex += 1
            }
        })

        val timerStopper = vertx.eventBus().consumer<String>(Settings.EventNames.stopScheduler)
        timerStopper.handler {
            println("Stopping scheduler timer")
            vertx.cancelTimer(timerId)
            timerStopper.unregister()
        }
    }
}