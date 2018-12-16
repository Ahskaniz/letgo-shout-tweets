package com.letgo.scala_candidate_test.application.service

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

import com.letgo.scala_candidate_test.domain.{Tweet, TweetRepository}

class TweetService(tweetRepository: TweetRepository, cacheService: CacheInMemoryService)
                  (implicit val ec: ExecutionContext) {

  private val minAmount = 0
  private val maxAmount = 10

  def retrieveShoutTweets(username: String, amount: Int): Seq[Tweet] = {
    require(amount >= minAmount, "Amount must be >= 0")
    require(amount <= maxAmount, "Amount must be <= 10")

    retrieveTweets(username, amount).map(shoutTweet)
  }

  private def retrieveTweets(username: String, amount: Int): Seq[Tweet] = {
      cacheService
        .getByUserName(username, amount)
        .getOrElse {
          val newTweets = Await.result(tweetRepository.searchByUserName(username, maxAmount), DurationInt(1).second)
          cacheService.save(username, newTweets)
          newTweets.take(amount)
        }
  }

  private def shoutTweet(tweet: Tweet): Tweet = Tweet(tweet.text.toUpperCase() + "!")

}
