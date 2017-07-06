import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.pattern.after
import akka.actor.Scheduler

/**
 * Given an operation that produces a T, returns a Future containing the result of T, unless an exception is thrown,
 * in which case the operation will be retried after _delay_ time, if there are more possible retries, which is configured through
 * the _retries_ parameter. If the operation does not succeed and there is no retries left, the resulting Future will contain the last failure.
 **/
def retry[T](op: => T, delay: FiniteDuration, retries: Int)(implicit ec: ExecutionContext, s: Scheduler): Future[T] =
  Future(op) recoverWith { case _ if retries > 0 => after(delay, s)(retry(op, delay, retries - 1)) }




object FutureWithRetry {

  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext
  import scala.concurrent.Future
  import akka.pattern.after
  import akka.actor.Scheduler

  def run[T](
              op: => T,
              delay: FiniteDuration,
              retries: Int
            )(implicit ec: ExecutionContext, s: Scheduler): Future[T] =
    Future(op) recoverWith {
      case _ if retries > 0 =>
        after(delay, s)(
          run(op, delay, retries - 1)
        )
    }

}





  def executeQuery(request: MotoRequest): String = {

    val fullDS = fullTable.getOrLoadTable()
    if (fullDS.isEmpty) return """{"message":"Initializing the full table, please try again later."}"""

    val optCampaign = CampaignManager.getOrCreateCampaign(fullTable, request.campaignName, request)
    if (optCampaign.isEmpty) return """{"message":"Failure. Campaign does not exist."}"""

    val campaign = optCampaign.get

    val resultF = FutureWithRetry.run[String](
      try {
        matchProcessStage(request.jobStage)
          .run(campaign, request)
      } catch {
        case e: Exception => s"""{"message":"${e.toString}"}"""
      },
      FiniteDuration(20, "minute"),
      2
    )

    Await.result(resultF, FiniteDuration(60, "minutes"))
  }