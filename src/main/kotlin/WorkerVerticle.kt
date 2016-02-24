import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject

/**
 * Created by chiaki on 24/02/16.
 */

class WorkerVerticle(val id: Int) : AbstractVerticle() {
    companion object {
        fun jobReceiverAddress() : String {
            return "worker-sendjob"
        }

        fun cleanupAddress() : String {
            return "worker-clean"
        }

        fun jobDoneAddress() : String {
            return "worker-jobdone"
        }

        val receivedMessage = "received"
    }

    override fun start() {
        println("Worker [$id] started")

        val jobReceiver = vertx.eventBus().consumer<String>(jobReceiverAddress(), { msg ->
            msg.reply(receivedMessage)
            val job = Job.fromJsonObject(JsonObject(msg.body()))
            for(i in 1 .. job.jobTimeCost) {
                println("Job $job running ($i/${job.jobTimeCost}) on Worker [$id]")
                Thread.sleep(1000)
            }
            vertx.eventBus().publish(jobDoneAddress(), job.toJsonObject().encode())
        })

        val cleaner = vertx.eventBus().consumer<String>(cleanupAddress())
        cleaner.handler { msg ->
            jobReceiver.unregister()
            cleaner.unregister()
        }

        println("Worker [$id] ready")
    }

    override fun stop() {
        println("Worker [$id] ended, removing event receivers")
        vertx.eventBus().publish(cleanupAddress(), "")
    }
}