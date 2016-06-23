(ns radigost.core
  (:require
    [radigost.crypto :refer [good-signature?]]
    ;[clojure.spec :as spec])
    [clojure.string :as s])
  (:import [java.util Base64]
           [java.nio.charset StandardCharsets]
           [java.time Instant]))

(defn- b64-decode [s]
  (String. (.decode (Base64/getUrlDecoder) s) StandardCharsets/ISO_8859_1))

(defn- b64-encode [ba]
  (.encodeToString (Base64/getUrlEncoder) ba))

(defn parse-token
  ([token]
   (parse-token identity token))
  ([parse-data-fn token]
   (let [parsed (s/split token #"\.")]
     {::header (-> parsed first b64-decode parse-data-fn)
      ::payload (-> parsed second b64-decode parse-data-fn)
      ::signature (last parsed)})))

(defn expired?
  ([parsed-token]
   (expired? (::payload parsed-token) (Instant/now)))
  ([parsed-token now]
   (cond
     (not (map? parsed-token)) true
     (not (some? now)) true
     :else
     (let [[nbf exp] (map #(some-> % parsed-token Instant/ofEpochSecond) [:nbf :exp])]
       (cond (and nbf exp) (or (.isAfter nbf now) (.isAfter now exp))
             (some? exp) (.isAfter now exp)
             :else true)))))

(defn validate-token [parse-json-fn pub-key token]
  (if (good-signature? pub-key token)
    (if (expired? (parse-token parse-json-fn token))
      ::expired
      ::good-standing)
  ::bad-signature))

(defn valid-token? [parse-json-fn pub-key token]
  ( = ::good-standing (validate-token parse-json-fn pub-key token)))

;; specs

;(spec/fdef parse-token
;  :args (spec/cat :s string? :f fn?)
;  :ret (spec/and map? (spec/keys :req-un [::header ::payload ::signature])))
;
;(spec/instrument #'parse-token)
