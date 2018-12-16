package com.letgo.scala_candidate_test.service

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Random

import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks

import com.letgo.scala_candidate_test.application.service.{CacheInMemoryService, TweetService}
import com.letgo.scala_candidate_test.application.util.Clock
import com.letgo.scala_candidate_test.domain.{Tweet, TweetRepository}


class TweetServiceTest extends FeatureSpec with GivenWhenThen with TableDrivenPropertyChecks with MockFactory  {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  val mockedTweetRepository: TweetRepository = mock[TweetRepository]
  val mockedClock: Clock = mock[Clock]
  val tweetService = new TweetService(mockedTweetRepository, new CacheInMemoryService(1000, mockedClock))

  private val tweets: Seq[Tweet] = Seq(
    "Science advances funeral by funeral.",
    "A witty saying proves nothing.",
    "You can recognize a pioneer by the arrows in his back.",
    "A lie repeated often enough becomes the truth.",
    "All great truths begin as blasphemies.",
    "Man's greatest asset is the unsettled mind.",
    "Wisest is she who knows she does not know.",
    "Biologists can be just as sensitive to heresy as theologians.",
    "Inquiry is fatal to certainty.",
    "The universe is wider than our views of it."
  ).map(Tweet.apply)

  feature("Return shouted tweets") {

    scenario("Requesting less than 0") {
      assertThrows[IllegalArgumentException] {
        tweetService.retrieveShoutTweets("username", -1)
      }
    }

    scenario("Requesting more than 10") {
      assertThrows[IllegalArgumentException] {
        tweetService.retrieveShoutTweets("username", 11)
      }
    }

    scenario("Requesting non cached tweets") {
      val examples = Table(
        ("username", "amount"),
        ("nonCached2", 2),
        ("nonCached5", 5)
      )

      forAll(examples) { (username, amount) =>
        When(s"Request $amount tweets")
        (mockedTweetRepository.searchByUserName _)
          .expects(username, 10)
          .returning(Future.successful(Random.shuffle(tweets)))
          .once()
        // cache.save(user)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(0)
          .once()
        val shoutedTweets = tweetService.retrieveShoutTweets(username, amount)

        Then(s"$amount of tweets are returned")
        assert(shoutedTweets.length.equals(amount))
        assertIsShouted(shoutedTweets)
      }
    }

    scenario("Requesting cached tweets") {
      Given("A request for 2 shouted tweets")
      val username = "toBeCachedUser"
      val amount = 2

      When("Requesting twice, second call contains a cache")
      (mockedTweetRepository.searchByUserName _)
        .expects(username, 10)
        .returning(Future.successful(Random.shuffle(tweets)))
        .once()
      inSequence {
        // cache.save(user)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(0)
          .once()
        // cache.get(user)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(500)
          .once()
      }
      val shoutedTweetsFirst = tweetService.retrieveShoutTweets(username, amount)
      val shoutedTweetsSecond = tweetService.retrieveShoutTweets(username, amount)

      Then(s"$amount of tweets are returned")
      assert(shoutedTweetsFirst.length.equals(amount))
      assert(shoutedTweetsFirst == shoutedTweetsSecond)
      assertIsShouted(shoutedTweetsFirst)
    }

    scenario("Requesting cached tweets merging usernames") {
      Given("A request for 2 shouted tweets")
      val amount = 2
      val firstUser = "userAToBeCached"
      val secondUser = "userBNonCached"

      When("Requesting twice, second call contains a cache for userA")
      inSequence {
        (mockedTweetRepository.searchByUserName _)
          .expects(firstUser, 10)
          .returning(Future.successful(Random.shuffle(tweets)))
          .once()
        (mockedTweetRepository.searchByUserName _)
          .expects(secondUser, 10)
          .returning(Future.successful(Random.shuffle(tweets)))
          .once()
      }
      inSequence {
        // cache.save(userA)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(0)
          .once()
        // cache.save(userB)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(0)
          .once()
        // cache.save(userA)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(500)
          .once()
      }

      val shoutedTweetsFirstUser1 = tweetService.retrieveShoutTweets(firstUser, amount)
      val shoutedTweetsSecondUser1 = tweetService.retrieveShoutTweets(secondUser, amount)
      val shoutedTweetsFirstUser2 = tweetService.retrieveShoutTweets(firstUser, amount)

      Then(s"$amount of tweets are returned")
      assert(shoutedTweetsFirstUser1.length.equals(amount))
      assert(shoutedTweetsSecondUser1.length.equals(amount))
      assert(shoutedTweetsFirstUser1 == shoutedTweetsFirstUser2)
      assert(shoutedTweetsFirstUser1 != shoutedTweetsSecondUser1)
      assertIsShouted(shoutedTweetsFirstUser1)
      assertIsShouted(shoutedTweetsSecondUser1)
    }

    scenario("Requesting cached tweets that expired") {
      Given("A request for 2 shouted tweets")
      val username = "toBeCachedAndExpiredUser"
      val amount = 2

      When("Requesting twice with a 10 second delay, second call does not contain a cache")
      (mockedTweetRepository.searchByUserName _)
        .expects(username, 10)
        .returning(Future.successful(Random.shuffle(tweets)))
        .once()
      (mockedTweetRepository.searchByUserName _)
        .expects(username, 10)
        .returning(Future.successful(Random.shuffle(tweets)))
        .once()
      inSequence {
        // cache.save(user)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(0)
          .once()
        // cache.get(user)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(1200)
          .once()
        // cache.save(user)
        (mockedClock.currentTimeMillis _)
          .expects()
          .returning(1500)
          .once()
      }

      val shoutedTweetsFirst = tweetService.retrieveShoutTweets(username, amount)
      val shoutedTweetsSecond = tweetService.retrieveShoutTweets(username, amount)

      Then(s"$amount of tweets are returned")
      assert(shoutedTweetsFirst.length.equals(amount))
      assert(shoutedTweetsFirst != shoutedTweetsSecond)
      assertIsShouted(shoutedTweetsFirst)
      assertIsShouted(shoutedTweetsSecond)
    }
  }

  def assertIsShouted(tweets: Seq[Tweet]) {
    for {
      tweet <- tweets
    } {
      assert(tweet.text.last == '!')
      assert(tweet.text.eq(tweet.text.toUpperCase))
    }
  }
}
