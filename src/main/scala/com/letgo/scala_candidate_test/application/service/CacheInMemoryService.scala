package com.letgo.scala_candidate_test.application.service

import scala.collection.mutable

import com.letgo.scala_candidate_test.application.util.Clock
import com.letgo.scala_candidate_test.domain.{Tweet, TweetCache}

class CacheInMemoryService(cacheTime: Long, clock: Clock) {

  val inMemoryCache: mutable.HashMap[String, TweetCache] = mutable.HashMap.empty

  def getByUserName(username: String, amount: Int): Option[Seq[Tweet]] =
    inMemoryCache
      .get(username)
      .filter(validCacheTime)
      .map(_.tweets.take(amount))

  def save(username: String, tweets: Seq[Tweet]): Unit =
    inMemoryCache.put(username, TweetCache(clock.currentTimeMillis, tweets))

  private def validCacheTime(tweetCache: TweetCache): Boolean =
    (tweetCache.storageTime + cacheTime) > clock.currentTimeMillis

}