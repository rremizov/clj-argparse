(ns clj-argparse.impl.parser
  (:require [clojure.tools.cli :refer [parse-opts]]
            [schema.core :as schema])
  (:require [clj-argparse.impl.core :as core]))

(def ^:private subcommand-parse-result-schema
  {:options {schema/Keyword schema/Any}
   :arguments {schema/Str schema/Any}})

(def parse-result-schema
  "The schema of args sequence parsing result."
  {(schema/required-key core/default-subcommand)
   subcommand-parse-result-schema
   schema/Str
   subcommand-parse-result-schema})

(defn- parse-command [command-name command-spec args]
  (let [{subcommands :subcommands
         command-arguments :arguments
         command-options :options} command-spec
        subcommands-names (core/first-column subcommands)
        is-default-subcommand? (= command-name core/default-subcommand)
        is-with-subcommands? (> (count subcommands) 0)
        is-with-arguments? (and (> (count command-arguments) 0)
                                (not is-with-subcommands?))
        {parsed-options :options
         parsed-arguments :arguments
         summary :summary}
        (parse-opts args command-options :in-order is-with-subcommands?)]
    (letfn [(throw-ex-info [reason]
              (throw (ex-info "Invalid arguments"
                              {:type :error-invalid-arguments
                               :subcommand command-name})))]
      (when (and is-with-subcommands?
                 (not (nil? (first parsed-arguments)))
                 (core/not-in (first parsed-arguments) subcommands-names))
        (throw-ex-info (str "Got " (first parsed-arguments)
                            " instead of subcommand")))
      (when (and is-with-arguments?
                 (> (count command-arguments)
                    (count (if is-default-subcommand?
                             parsed-arguments
                             (rest parsed-arguments)))))
        (throw-ex-info "Not enough arguments provided")))
    [is-default-subcommand?
     is-with-subcommands?
     is-with-arguments?
     subcommands
     subcommands-names
     command-arguments
     parsed-options
     parsed-arguments]))

(defn- command-parse-result
  ([] (command-parse-result core/default-subcommand))
  ([command-name]
   {command-name {:options {} :arguments {}}}))

(defn parse
  "Parse args sequence using cli-spec.
  Returns a map conforming to parse-result-schema"
  [cli-spec args]
  (loop [result (command-parse-result)
         current-args args
         current-subcommand core/default-subcommand]
    (let [[is-default-subcommand?
           is-with-subcommands?
           is-with-arguments?
           subcommands
           subcommands-names
           command-arguments
           parsed-options
           parsed-arguments]
          (parse-command current-subcommand
                         (get cli-spec current-subcommand)
                         current-args)
          next-subcommand-name
          (->> subcommands-names
               (filter #{(first parsed-arguments)})
               (first))
          next-result
          (assoc-in result [current-subcommand :options] parsed-options)]
      (if (and is-with-subcommands? next-subcommand-name)
        (recur next-result parsed-arguments next-subcommand-name)
        (assoc-in next-result
                  [current-subcommand :arguments]
                  (into {} (core/zip (core/first-column command-arguments)
                                     (if is-default-subcommand?
                                       parsed-arguments
                                       (rest parsed-arguments)))))))))

