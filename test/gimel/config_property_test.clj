(ns gimel.config-property-test
  "Feature: toml-config-migration
   Property 1: TOML parse produces spec-valid config map
   Property 2: Config round-trip equivalence
   Property 4: Accessor values match TOML input"
  (:require [clojure.string :as str]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [gimel.config :as config]
            [gimel.config-spec :refer [check-config]]))

;; ---------------------------------------------------------------------------
;; Generators
;; ---------------------------------------------------------------------------

(def gen-port
  "Generates a valid TCP port number (1-65535)."
  (gen/choose 1 65535))

(def gen-non-empty-string
  "Generates a non-empty alphanumeric string (1-30 chars)."
  (gen/fmap str/join
            (gen/vector gen/char-alphanumeric 1 30)))

(def gen-web-url
  "Generates a valid http or https URL."
  (gen/fmap (fn [[scheme host path]]
              (str scheme "://" host ".example.com/" path))
            (gen/tuple
             (gen/elements ["http" "https"])
             (gen/fmap str/join (gen/vector gen/char-alphanumeric 3 15))
             (gen/fmap str/join (gen/vector gen/char-alphanumeric 0 10)))))

(def gen-valid-server-config
  "Generates a valid [server] config map."
  (gen/hash-map
   :port           gen-port
   :web-url        gen-web-url
   :webroot        gen-non-empty-string
   :snippet-output     gen-non-empty-string
   :org-source gen-non-empty-string
   :template       gen-non-empty-string
   :footer         gen-non-empty-string))

(def gen-valid-database-config
  "Generates a valid [database] config map."
  (gen/hash-map :dbname gen-non-empty-string))

;; ---------------------------------------------------------------------------
;; Helpers
;; ---------------------------------------------------------------------------

(defn- config->toml-string
  "Serialises a server+database config pair to a TOML string."
  [{:keys [port web-url webroot snippet-output org-source template footer]}
   {:keys [dbname]}]
  (str "[server]\n"
       "port = " port "\n"
       "web-url = \"" web-url "\"\n"
       "webroot = \"" webroot "\"\n"
       "snippet-output = \"" snippet-output "\"\n"
       "org-source = \"" org-source "\"\n"
       "template = \"" template "\"\n"
       "footer = \"" footer "\"\n"
       "\n"
       "[database]\n"
       "dbname = \"" dbname "\"\n"))

(defn- write-temp-toml!
  "Writes content to a temp file and returns the java.io.File."
  [content]
  (let [f (java.io.File/createTempFile "gimel-prop-test-" ".toml")]
    (.deleteOnExit f)
    (spit f content)
    f))

;; ---------------------------------------------------------------------------
;; Property 1: TOML parse produces spec-valid config map
;; Validates: Requirements 2.3, 3.1, 3.2, 3.3, 3.5
;; ---------------------------------------------------------------------------

(defspec toml-parse-produces-spec-valid-config-map
  50
  (prop/for-all
   [server   gen-valid-server-config
    database gen-valid-database-config]
   (let [toml-str  (config->toml-string server database)
         temp-file (write-temp-toml! toml-str)
         result    (#'gimel.config/parse-toml (.getAbsolutePath temp-file))]
     (try
       (check-config result)
       true
       (catch clojure.lang.ExceptionInfo _
         false)))))

;; ---------------------------------------------------------------------------
;; Property 2: Config round-trip equivalence
;; Validates: Requirements 2.7
;; ---------------------------------------------------------------------------

(defn- config-map->server
  "Extracts the server map from a parsed config map."
  [config-map]
  (get-in config-map [:configuration :public]))

(defn- config-map->database
  "Extracts the database map from a parsed config map."
  [config-map]
  (get-in config-map [:configuration :database]))

(defspec config-round-trip-equivalence
  50
  ;; Feature: toml-config-migration, Property 2: Config round-trip equivalence
  (prop/for-all
   [server   gen-valid-server-config
    database gen-valid-database-config]
   (let [;; First pass: write to TOML and parse to get config map A
         toml-str-1  (config->toml-string server database)
         temp-file-1 (write-temp-toml! toml-str-1)
         config-a    (#'gimel.config/parse-toml (.getAbsolutePath temp-file-1))
         ;; Second pass: extract values from A, write to TOML again, parse to get config map B
         server-a    (config-map->server config-a)
         database-a  (config-map->database config-a)
         toml-str-2  (config->toml-string server-a database-a)
         temp-file-2 (write-temp-toml! toml-str-2)
         config-b    (#'gimel.config/parse-toml (.getAbsolutePath temp-file-2))]
     (= config-a config-b))))

;; ---------------------------------------------------------------------------
;; Property 4: Accessor values match TOML input
;; Validates: Requirements 7.3, 2.6
;; ---------------------------------------------------------------------------

(defspec accessor-values-match-toml-input
  50
  ;; Feature: toml-config-migration, Property 4: Accessor values match TOML input
  (prop/for-all
   [server   gen-valid-server-config
    database gen-valid-database-config]
   (let [toml-str  (config->toml-string server database)
         temp-file (write-temp-toml! toml-str)]
     (reset! config/config-data {})
     (config/load-config (.getAbsolutePath temp-file))
     (and (= (:port server)           (config/get-port))
          (= (:snippet-output server)     (config/get-snippet-output))
          (= (:webroot server)        (config/get-webroot))
          (= (:web-url server)        (config/get-web-url))
          (= (:template server)       (config/get-template-dir))
          (= (:footer server)         (config/get-footer))
          (= (:dbname database)       (config/get-dbname))
          (= (:org-source server) (config/get-org-source))))))
