/**
 * Created by chiaki on 23/02/16.
 */

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject

class WorkerControllerVerticle : AbstractVerticle() {
    val jobList : MutableList<Job>? = Job.randomJobs(Settings.totalJobCount).toMutableList()
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
        val totalJobs = jobList.size
        var finishedJobs = 0

        for(i in 1 .. Settings.availableSlots) {
            vertx.deployVerticle(WorkerVerticle(i), DeploymentOptions().setWorker(true))
        }

        vertx.eventBus().consumer<String>(WorkerVerticle.jobDoneAddress(), {msg ->
            val job = Job.fromJsonObject(JsonObject(msg.body()))
            println("Job $job finished")
            finishedJobs += 1
        })

        vertx.setPeriodic(Settings.checkPeriod, {timerId ->
            if (jobList.isEmpty()) {
                println("All jobs launched, ending the scheduler")
                vertx.cancelTimer(timerId)

                println("Setting up cleaner")
                vertx.setPeriodic(Settings.checkPeriod, {timerId ->
                    if (finishedJobs == totalJobs) {
                        vertx.cancelTimer(timerId)
                        vertx.close()
                    }
                })

            } else {
                val job = jobList.removeAt(0)
                println("Dispatching job $job")
                vertx.eventBus().send<String>(WorkerVerticle.jobReceiverAddress(), job.toJsonObject().encode(), {
                    if (it.succeeded() && it.result().body() == WorkerVerticle.receivedMessage) {
                        println("Job $job is being processed")
                    } else {
                        println("Job $job not accepted by anyone")
                        jobList.add(job)
                    }
                })
            }
        })
    }
}