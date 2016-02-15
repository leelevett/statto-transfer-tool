package lee.levett

import com.timgroup.statsd.NonBlockingStatsDClient
import groovy.xml.DOMBuilder
import groovyx.gpars.actor.DefaultActor
import org.apache.xpath.XPathAPI
import org.w3c.dom.Document
import org.w3c.dom.NodeList

import java.util.concurrent.TimeUnit

class Poller extends DefaultActor {
  def actorName
  def url
  def pollEvery
  def dataPath
  def threshold

  def domBuilder
  def statsdClient
  def emailer

  void act() {
    initialiseClients()

    loop(
        {
          TimeUnit.SECONDS.sleep(pollEvery)
          true
        },
        {
          def total = extractTotalTimeFromUrl()

          if (total > threshold) {
            println "Threshold exceeded $total > $threshold for $actorName"
            emailer.send(actorName, total, threshold)
          }
          statsdClient.recordGaugeValue("time", total)
        }
    )
  }

  void afterStart() {
    println "Started Poller for $actorName for $url"
  }
  void onException(e) {
    println e
    zeroStatsdGauge()
  }

  def initialiseClients() {
    domBuilder = DOMBuilder.newInstance()
    statsdClient = newStatsdClient()
    url = new URL("http://$url")
    emailer = Emailer.newInstance()
  }

  def extractTotalTimeFromUrl() {
    def doc = domBuilder.parseText(url.getText())
    def nodeList = getNodesWithXPath(doc)
    def total = sumNodes(nodeList) as Long;

    total
  }

  def static sumNodes(nodeList) {
    nodeList.inject(0, {
      result, i -> result + (i.getTextContent().replace("ms", "") as Integer)
    })
  }

  private NodeList getNodesWithXPath(Document doc) {
    XPathAPI.eval(doc, dataPath).nodelist()
  }

  def NonBlockingStatsDClient newStatsdClient() {
    new NonBlockingStatsDClient("hack.day.$actorName", "localhost", 8125)
  }

  def zeroStatsdGauge() {
    newStatsdClient().recordGaugeValue("time", 0)
  }
}