import io.vertx.core.Vertx

/**
 * Created by chiaki on 23/02/16.
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val vertx = Vertx.vertx()
        // You may replace WorkerControllerVerticle to ControllerVerticle
        // and see what would happen
        vertx.deployVerticle(WorkerControllerVerticle(), {
            vertx.eventBus().publish(Settings.EventNames.start, "start")
        })
    }
}