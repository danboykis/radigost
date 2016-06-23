(ns radigost.crypto
  (:require [clojure.string :as s])
  (:import [java.util Base64]
           [java.security KeyFactory Signature]
           [java.security.spec X509EncodedKeySpec]
           [java.nio.charset StandardCharsets]))

(def ^:private BEGIN "-----BEGIN ")
(def ^:private END "-----END ")

(defn- decode-url-b64 [^String s]
  (.decode (Base64/getUrlDecoder) s))

(defn- decode-b64 [^String s]
  (.decode (Base64/getDecoder) s))

(defn- read-type [txt]
  (as-> txt $
      (subs $ (count BEGIN))
      (subs $ 0 (s/index-of $ "-"))))

(defn- parse-type [txt]
  (condp = (read-type txt)
    "PUBLIC KEY" :pub-key
    :prv-key))

  (defn- strip-headers [lines]
    (remove #(s/index-of % ":") lines))

(defn- parse-key [txt]
  (let [type (parse-type txt)
        key-lines (s/split (first (remove empty? (map s/trim (s/split txt #"-+.+-+"))))
                           #"\s+")]
    {:type type
     :key (-> key-lines
              strip-headers
              s/join
              decode-b64
              seq)}))

(defn- make-key [{:keys [type key]}]
  (let [x509-key-spec (X509EncodedKeySpec. (byte-array key))
        kf (KeyFactory/getInstance "RSA")]
    (condp = type
      :pub-key (.generatePublic kf x509-key-spec)
      :prv-key (.generatePrivate kf x509-key-spec) ;;TODO make this work by parsing PKCS#1
      (throw (IllegalArgumentException. "key is not valid")))))

(defn key-from-text [k]
  (some-> k parse-key make-key))

(defn- verify-signature [sig pub-key data]
  (let [signer (doto
                 (Signature/getInstance "SHA256withRSA")
                 (.initVerify pub-key)
                 (.update (.getBytes data StandardCharsets/ISO_8859_1)))]
    (.verify signer (decode-url-b64 sig))))

(defn good-signature? [pub-key token]
  (if-not (string? token)
    false
    (let [pk (key-from-text pub-key)
          i (s/last-index-of token ".")
          data (subs token 0 i)
          sig (subs token (inc i))]
      (verify-signature sig pk data))))
