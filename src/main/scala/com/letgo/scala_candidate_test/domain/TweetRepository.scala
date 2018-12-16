package com.letgo.scala_candidate_test.domain

import scala.concurrent.Future

trait TweetRepository {
  def searchByUserName(username: String, limit: Int): Future[Seq[Tweet]]
}
