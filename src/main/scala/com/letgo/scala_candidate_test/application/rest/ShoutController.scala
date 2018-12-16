package com.letgo.scala_candidate_test.application.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import com.letgo.scala_candidate_test.application.service.TweetService
import com.letgo.scala_candidate_test.domain.Tweet

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[Tweet] = jsonFormat1(Tweet)
}

class ShoutController(val tweetService: TweetService) extends Directives with JsonSupport {

  private val errorHandler = ExceptionHandler {
    case _: IllegalArgumentException => complete(
      HttpResponse(StatusCodes.BadRequest, entity = "Limit should be a number between 0 and 10")
    )
  }

  val route: Route = get {
    path("shout" / Segment) { twitterUserName =>
      parameters('limit.as[Int]) { limit =>
        handleExceptions(errorHandler) {
          val tweetResponse = tweetService.retrieveShoutTweets(twitterUserName, limit)
          complete(tweetResponse)
        }
      }
    }
  }
}
