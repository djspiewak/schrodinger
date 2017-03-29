# Typelevel Effects

Common communication protocol for IO / Task data types.

Aims to be a set of common interfaces to abstract over `cats.Eval`,
`monix.eval.Task`, `fs2.Task`, `scalaz.concurrent.Task`, `scalaz.effects.IO`,
`scala.concurrent.Future` or other data-types that evaluate side-effects and
trigger single results, potentially asynchronously.

Or in other words, this aims to be the 
[Reactive Streams](http://www.reactive-streams.org/)
protocol of `IO` and `Task` data types.

**Work in Progress:** Feedback appreciated! 

This project would be nothing without the colaboration of interested parties. Proposal is being raised here: https://github.com/typelevel/general/issues/66
