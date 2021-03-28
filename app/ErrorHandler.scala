import controllers.Responses
import javax.inject.Singleton
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      statusCode match {
        case 404 => Status(statusCode)(Responses.clientErrorResponse("Route not found."))
        case _ => Status(statusCode)(Responses.clientErrorResponse(message))
      }
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      InternalServerError(Responses.serverErrorResponse(exception.getMessage))
    )
  }
}

