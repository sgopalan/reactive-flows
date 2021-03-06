/*
 * Copyright 2015 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.reactiveflows

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.contrib.pattern.DistributedPubSubExtension
import akka.event.Logging

object ReactiveFlowsApp {

  private val opt = """-D(\S+)=(\S+)""".r

  def main(args: Array[String]): Unit = {
    for (opt(key, value) <- args) System.setProperty(key, value)

    val system = ActorSystem("reactive-flows-system")
    FlowFacade.startSharding(
      system,
      DistributedPubSubExtension(system).mediator,
      Settings(system).flowFacade.shardCount
    )
    system.actorOf(
      ReactiveFlows.props(Cluster(system).selfRoles.contains(ReactiveFlows.SharedJournal)),
      ReactiveFlows.Name
    )

    Logging(system, getClass).info("Reactive Flows up and running")
    system.awaitTermination()
  }
}
