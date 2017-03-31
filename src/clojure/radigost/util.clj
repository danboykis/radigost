(ns radigost.util
  (:import [javax.xml.bind DatatypeConverter]
           [java.nio.charset StandardCharsets]
           [java.util Base64]
           [java.net URLEncoder URLDecoder]))

(defn hex->string [^String s]
  (String. (DatatypeConverter/parseHexBinary s) StandardCharsets/UTF_8))

(defn b64-encode [ba]
  (.encodeToString (Base64/getUrlEncoder) ba))

(defn b64-decode [^String s]
  (String. (.decode (Base64/getUrlDecoder) s) StandardCharsets/ISO_8859_1))

(defn url-encode [s]
  (-> (URLEncoder/encode s "UTF-8")
      (.replace "+" "%20")
      (.replace "*" "%2A")
      (.replace "%7E" "~")))

(defn url-decode [s] (URLDecoder/decode s "UTF-8"))
