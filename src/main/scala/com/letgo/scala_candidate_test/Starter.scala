package com.letgo.scala_candidate_test

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import com.letgo.scala_candidate_test.application.rest.ShoutController
import com.letgo.scala_candidate_test.application.service.{CacheInMemoryService, TweetService}
import com.letgo.scala_candidate_test.application.util.Clock
import com.letgo.scala_candidate_test.infrastructure.TweetRepositoryInMemory

object Starter {
  def main(args: Array[String]): Unit = {

    implicit val actorSystem: ActorSystem        = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)
    implicit val ec: ExecutionContext            = ExecutionContext.global

    val timeInMillis = 10000
    lazy val clock = new Clock()
    lazy val cache = new CacheInMemoryService(timeInMillis, clock)
    lazy val tweetRepository = new TweetRepositoryInMemory()
    lazy val tweetService = new TweetService(tweetRepository, cache)
    val shoutController = new ShoutController(tweetService)

    Await.result(Http().bindAndHandle(shoutController.route, "0.0.0.0", 9000), Duration.Inf)
  }
}
