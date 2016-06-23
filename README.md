# Radigost

Parse and verify Json Web Tokens (JWT) using Java. The goal of this library is to have as few
dependencies outside of Clojure as possible.

# Usage

```clojure
(def public-key "-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdlatRjRjogo3WojgGHFHYLugdUWAY9iR3fy4arWNA1KoS8kVw33cJibXr8bvwUAUparCwlvdbH6dvEOfou0/gCFQsHUfQrSDv+MuSUMAe8jzKE4qW+jK+xQU9a03GUnKHkkle+Q0pX/g6jXZ7r1/xAK5Do2kQ+X5xK9cipRgEKwIDAQAB
-----END PUBLIC KEY-----")

;; Private key for reference only

(def private-key "-----BEGIN RSA PRIVATE KEY-----
MIICWwIBAAKBgQDdlatRjRjogo3WojgGHFHYLugdUWAY9iR3fy4arWNA1KoS8kVw33cJibXr8bvwUAUparCwlvdbH6dvEOfou0/gCFQsHUfQrSDv+MuSUMAe8jzKE4qW+jK+xQU9a03GUnKHkkle+Q0pX/g6jXZ7r1/xAK5Do2kQ+X5xK9cipRgEKwIDAQABAoGAD+onAtVye4ic7VR7V50DF9bOnwRwNXrARcDhq9LWNRrRGElESYYTQ6EbatXS3MCyjjX2eMhu/aF5YhXBwkppwxg+EOmXeh+MzL7Zh284OuPbkglAaGhV9bb6/5CpuGb1esyPbYW+Ty2PC0GSZfIXkXs76jXAu9TOBvD0ybc2YlkCQQDywg2R/7t3Q2OE2+yo382CLJdrlSLVROWKwb4tb2PjhY4XAwV8d1vy0RenxTB+K5Mu57uVSTHtrMK0GAtFr833AkEA6avx20OHo61Yela/4k5kQDtjEf1N0LfI+BcWZtxsS3jDM3i1Hp0KSu5rsCPb8acJo5RO26gGVrfAsDcIXKC+bQJAZZ2XIpsitLyPpuiMOvBbzPavd4gY6Z8KWrfYzJoI/Q9FuBo6rKwl4BFoToD7WIUS+hpkagwWiz+6zLoX1dbOZwJACmH5fSSjAkLRi54PKJ8TFUeOP15h9sQzydI8zJU+upvDEKZsZc/UhT/SySDOxQ4G/523Y0sz/OZtSWcol/UMgQJALesy++GdvoIDLfJX5GBQpuFgFenRiRDabxrE9MNUZ2aPFaFp+DyAe+b4nDwuJaW2LURbr8AEZga7oQj0uYxcYw==
  -----END RSA PRIVATE KEY-----")

(require '[radigost.core :refer [valid-token? validate-token parse-token]])

(def token "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsIm5iZiI6MTQ2NjY5MDAwMCwiZXhwIjoxODY5OTg3ODY3fQ.iDV-a2OP7Ck-1u5ZdKSEXO6quE6xj9Lmd8SGcyhpQH8Y0I5D8D4QJxHZKxY-OLh85G6rmJBDSbmGb1D7teGTnT-6UDByjYt7f9ZdGezyu0kW937zzRKU03zG_55JLWc1nyFw7VQnmTaOfxzLc5PNnVvNnG2wxY0r_ibRrXEXym8")


=> (parse-token token)
=>
{:radigost.core/header "{\"alg\":\"RS256\",\"typ\":\"JWT\"}",
 :radigost.core/payload
 "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"admin\":true,\"nbf\":1466690000,\"exp\":1869987867}",
 :radigost.core/signature
 "iDV-a2OP7Ck-1u5ZdKSEXO6quE6xj9Lmd8SGcyhpQH8Y0I5D8D4QJxHZKxY-OLh85G6rmJBDSbmGb1D7teGTnT-6UDByjYt7f9ZdGezyu0kW937zzRKU03zG_55JLWc1nyFw7VQnmTaOfxzLc5PNnVvNnG2wxY0r_ibRrXEXym8"}

;; parse header and payload with json function for cheshire
=> (require '[cheshire.core :refer [parse-string]])
=> (def parsed-token (parse-token #(parse-string % true) token))
=> parsed-token
=>
{:radigost.core/header {:alg "RS256", :typ "JWT"},
 :radigost.core/payload
 {:sub "1234567890",
  :name "John Doe",
  :admin true,
  :nbf 1466690000,
  :exp 1869987867},
 :radigost.core/signature
 "iDV-a2OP7Ck-1u5ZdKSEXO6quE6xj9Lmd8SGcyhpQH8Y0I5D8D4QJxHZKxY-OLh85G6rmJBDSbmGb1D7teGTnT-6UDByjYt7f9ZdGezyu0kW937zzRKU03zG_55JLWc1nyFw7VQnmTaOfxzLc5PNnVvNnG2wxY0r_ibRrXEXym8"}

=> (validate-token #(parse-string % true) public-key token)
=> :radigost.core/good-standing
=> (valid-token? #(parse-string % true) public-key token)
=> true
```

### About the name

Radigost is a god of hospitality in slavic mythology.