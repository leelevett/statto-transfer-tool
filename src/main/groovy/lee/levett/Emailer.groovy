package lee.levett

import org.apache.commons.mail.SimpleEmail
import org.yaml.snakeyaml.Yaml

class Emailer {
  def port
  def host
  def from
  def to
  def subject = "Threshold exceeded for %s"
  def message = "It's all gone wrong on %s. It took %sms to do something that shouldn't take longer than %sms..."

  def static Emailer newInstance() {
    new Yaml().loadAs(Thread.currentThread().contextClassLoader.getResourceAsStream("email.yml"), Emailer)
  }

  def send(actorName, value, threshold) {
    def email = new SimpleEmail()
    email.setHostName(host)
    email.setSmtpPort(port)
    email.setFrom(from)
    email.addTo(to as String)
    email.setSubject(sprintf(subject, actorName))
    email.setMsg(sprintf(message, [actorName, value, threshold]))
    email.send()
  }
}