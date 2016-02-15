package lee.levett

import org.yaml.snakeyaml.Yaml

new Yaml().loadAll(
    Thread.currentThread().contextClassLoader.getResourceAsStream("statto-transfer-tool.yml")
).collect({
  def poller = it as Poller
  poller.start()
  poller
})*.join()