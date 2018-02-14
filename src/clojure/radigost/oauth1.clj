(ns radigost.oauth1
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [radigost.util :refer [url-encode]]
            [radigost.crypto :as rc])
  (:import [java.net URI URLEncoder URLDecoder]
           [java.time Instant]
           [java.security SecureRandom]
           [java.util Base64]))

(defn parse-query-params [q]
  (into {} (filter #(= 2 (count %))) (map #(string/split % #"=") (some-> q (string/split #"&")))))

(defn normalize-query-params [query]
  (string/join "&" (map (fn [[k v]] (str (name k) "=" (name v)))
                                (sort query))))

(defn conform-to-spec [spec data]
  (let [parsed (s/conform spec data)]
    (if (= ::s/invalid parsed)
      (throw (ex-info "can't conform" (s/explain-data spec data)))
      parsed)))

(s/def ::params (s/keys :req-un [::http-method ::url ::consumer-key ::signature-method]))
(s/def ::http-method (s/conformer (fn [method]
                                    (if (#{"GET" "POST"} method)
                                      method
                                      (if-let [k (#{:get :post} method)]
                                        (string/upper-case (name k))
                                        '::s/invalid)))))
(s/def ::url (s/and string? #(try (.isAbsolute (URI/create %)) (catch Exception _ false))))
(s/def ::consumer-key string?)
(s/def ::consumer-secret string?)
(s/def ::signature-method #{:hmac-sha1})
(s/def ::headers (s/map-of string? string?))

(def secure-random (SecureRandom/getInstance "SHA1PRNG"))

(defn gen-random! []
  (.nextInt secure-random))

(defn current-time!   [] (-> (Instant/now) .getEpochSecond))
(defn generate-nonce! [] (gen-random!))

(def sigmethods {:hmac-sha1 "HMAC-SHA1"})

(defn base-string-headers [m]
  (string/join "&"
               (reduce (fn [accum k] (conj accum (str (name k) "=" (str (get m k)))))
                       []
                       [:oauth_consumer_key :oauth_nonce :oauth_signature_method :oauth_timestamp :oauth_version])))


(defn base-string [{:keys [http-method url] :as parsed}]
  (let [[u query-params] (string/split url #"\?")]
    (->> [u (normalize-query-params (parse-query-params query-params)) (base-string-headers parsed)]
         (remove empty?)
         (map url-encode)
         (string/join "&")
         (str http-method "&"))))

(defn oauth-signature [{:keys [consumer-secret] :as params}]
  (rc/hmac-sha1 (str (url-encode consumer-secret) "&") (base-string params)))

(defn oauth-header [params]
  (let [parsed (conform-to-spec ::params params)
        ts    (current-time!)
        nonce (generate-nonce!)

        fields [:http-method :oauth_consumer_key :oauth_signature_method :oauth_timestamp :oauth_nonce :oauth_version]
        oauth-params (-> parsed
                         (clojure.set/rename-keys {:consumer-key :oauth_consumer_key :signature-method :oauth_signature_method})
                         (merge {:oauth_timestamp ts :oauth_nonce nonce :oauth_version "1.0"})
                         (update :oauth_signature_method sigmethods))
        sig (url-encode (oauth-signature oauth-params))]
    (str "OAuth "
         (string/join ", "
                      (map (fn [[k v]] (str (name k) "=" (str \" v \")))
                           (assoc (select-keys oauth-params fields) :oauth_signature sig))))))
