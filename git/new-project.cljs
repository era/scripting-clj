(require '[babashka.curl :as curl])
(require '[cheshire.core :as json])
(require '[clojure.tools.cli :refer [parse-opts]])
(require '[clojure.java.shell :as shell])

(def codeberg-url "https://codeberg.org/api/v1/user/repos")

(defn request-create-codeberg [name description auth_token]
  (curl/post codeberg-url {:form-params {:auto_init "false"
                                         :description description
                                         :name name
                                         :private "false"}
                           :headers {:Authorization (apply str ["token " auth_token])}}))

(defn create-codeberg [name description auth_token]
  (request-create-codeberg name description auth_token))

(def github-url "https://api.github.com/user/repos")

(defn request-create-github [name description auth_token]
  (curl/post github-url {:body (json/generate-string {:description description
                                                      :name name})
                           :headers {:Authorization (apply str ["token " auth_token])}}))
(defn create-github [name description auth_token]
  (request-create-github name description auth_token))


(def cli-options
  [["-n" "--name NAME" "Repo name"]
   ["-d" "--description DESCRIPTION" "Repo description"]])

(let [{name :name, description :description} (:options (parse-opts *command-line-args* cli-options))] 
  (shell/sh "/bin/sh" "-c" (apply str ["mkdir " (System/getenv "CODE_PATH") name ]))
  (shell/sh "/bin/sh" "-c" (apply str ["cd " (System/getenv "CODE_PATH") name ";"
                                       "git init; "
                                       "git remote add origin git@github.com:" "era" "/" name " ;"
                                       "git remote set-url --add --push origin git@codeberg.org:" "era" "/" name ";"
                                       "git remote set-url --add --push origin git@github.com:" "era" "/" name]))
  (create-codeberg name description (System/getenv "CODEBERG_ACCESS_TOKEN"))
  (create-github name description (System/getenv "GITHUB_KEY")))
