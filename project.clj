(defproject br.usp.icmc.cljhozo/core "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jp.hozo/hozo-api "1.0.11"]]
  ;:resource-paths ["lib/*"]
  :plugins [[lein-midje "3.1.3"]
            [lein-localrepo "0.5.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
