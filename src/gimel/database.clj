(ns gimel.database
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [next.jdbc :as jdbc]
            [mount.core :refer [defstate]]
            [gimel.config :refer [get-dbname]]))

(defstate datasource
  :start (jdbc/get-datasource {:dbtype "sqlite"
                               :dbname (get-dbname)}))

(defn delete-database []
  (let [dbfile (get-dbname)]
    (when (.exists (io/file dbfile))
      (io/delete-file dbfile))))

(defn- match-valid-table-or-column-name?
  "Retruns match if data is a valid table or column name."
  [data]
  (if (re-matches #"[a-zA-Z0-9_\s]+" data)
    true
    (throw (ex-info "Invalid table/column name!" {:message data}))))

(s/def :database/table-or-column-name match-valid-table-or-column-name?)

(defn create-database
  "Creates the table for pages table."
  []
  (delete-database)
  (jdbc/execute! datasource ["CREATE TABLE IF NOT EXISTS pages (
                      id INTEGER PRIMARY KEY,
                      url VARCHAR(128) NOT NULL,
                      title VARCHAR(128) NOT NULL,
                      content TEXT,
                      UNIQUE(url))"]))

(defn create-table
  "Creates a new table for a metadata type."
  [table]
  (s/valid? :database/table-or-column-name table)
  (jdbc/execute! datasource [(format "CREATE TABLE IF NOT EXISTS %s (
                      id INTEGER PRIMARY KEY,
                      name VARCHAR(64) NOT NULL,
                      UNIQUE(name))" table)])
  (jdbc/execute! datasource [(format "CREATE TABLE IF NOT EXISTS %s_lnk (
                      page INTEGER NOT NULL,
                      %s INTEGER NOT NULL,
                      FOREIGN KEY (page) REFERENCES pages(id),
                      FOREIGN KEY (%s) REFERENCES %s(id))"
                                     table table table table)]))

(defn get-page-id
  "Returns the id of a page"
  [url]
  (let [result
        (jdbc/execute-one! datasource ["SELECT id FROM pages WHERE url = ?" url])]
    (:pages/id result)))

(defn insert-page
  "Looks to see if page has already been inserted, and if not, inserts the page."
  [url title content]
  (let [id (get-page-id url)]
    (if id
      id
      (let [result
            (jdbc/execute-one!
             datasource ["INSERT INTO pages (url, title, content) VALUES (?, ?, ?) RETURNING id"
                         url title content])]
        (:pages/id result)))))

(defn get-id-table [table name]
  (s/valid? :database/table-or-column-name table)
  (let [result (jdbc/execute-one! datasource
                                  [(format "SELECT id FROM %s WHERE name = ?" table) name])]
    ((keyword (str table "/id")) result)))

(defn get-metadata-id
  "Returns the id for the metadata after query or insert."
  [table name]
  (s/valid? :database/table-or-column-name table)
  (let [id (get-id-table table name)]
    (if id
      id
      (let [result
            (jdbc/execute-one! datasource [(format "INSERT INTO %s (name) VALUES (?) RETURNING id" table) name])]
        ((keyword (str table "/id")) result)))))

(defn create-meta-link [table page-id meta-id]
  (s/valid? :database/table-or-column-name table)
  (let [link (jdbc/execute-one! datasource [(format "SELECT * FROM %s_lnk WHERE page = ? AND  %s = ?" table table)
                                            page-id meta-id])]
    (when-not link
      (jdbc/execute-one! datasource [(format "INSERT INTO %s_lnk (page, %s) VALUES (?, ?)" table table)
                                     page-id meta-id]))))

(defn insert-metadata [pid key val]
  (s/valid? :database/table-or-column-name key)
  (create-table key)
  (let [mid (get-metadata-id key val)]
    (create-meta-link key pid mid)))

(defn insert-data [url metadata content]
  (let [title (or (:Title metadata) (:title metadata))]
    (doseq [[key val] (dissoc metadata :Title)]
      (let [pid (insert-page url title content)]
        (if (and (not (string? val)) (seq? val))
          (doseq [v val]
            (insert-metadata pid (string/lower-case (name key)) v))
          (insert-metadata pid (string/lower-case (name key)) val))))))
