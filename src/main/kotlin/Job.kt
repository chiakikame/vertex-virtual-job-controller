import io.vertx.core.json.JsonObject
import java.util.*

/**
 * Created by chiaki on 23/02/16.
 */
class Job(val jobId: Int, val jobTimeCost: Int) {
    companion object {
        fun fromJsonObject(jo: JsonObject): Job {
            return Job(jo.getInteger("id"), jo.getInteger("cost"))
        }

        fun randomJobs(count: Int) : List<Job> {
            val rnd = Random()
            val jobs = ArrayList<Job>()
            var id = 0

            while(id < count) {
                val cost = rnd.nextInt(Settings.maxTimeCostOfJob - Settings.minTimeCostOfJob) +
                        Settings.minTimeCostOfJob
                id += 1
                jobs.add(Job(id, cost))
            }

            return jobs
        }
    }

    fun toJsonObject() : JsonObject {
        val obj = JsonObject()
        obj.put("id", this.jobId)
        obj.put("cost", this.jobTimeCost)
        return obj
    }

    override fun toString(): String {
        return "Job(id=$jobId, cost=$jobTimeCost)"
    }
}