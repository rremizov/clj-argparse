(ns clj-argparse.impl.helper
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clj-argparse.impl.core :as core]))

(defn print-help
  "Print default or a subcommand help message."
  ([args cli-spec]
   (print-help args cli-spec core/default-subcommand))
  ([args cli-spec subcommand-name]
   (let [subcommand-spec (get cli-spec subcommand-name)
         subcommands (get subcommand-spec :subcommands)
         arguments (get subcommand-spec :arguments)
         options-summary
         (->> (get subcommand-spec :options)
              (parse-opts args)
              (:summary))]
     (when (not= subcommand-name core/default-subcommand)
       (println (str "Help for the \"" subcommand-name "\" subcommand:")))
     (when (seq subcommands)
       (println "Subcommands:")
       (doseq [[subcommand-name subcommand-description] subcommands]
         (println "  " subcommand-name "\t" subcommand-description))
       (println))
     (when (seq arguments)
       (println "Positional arguments:")
       (doseq [[argument-name argument-description] arguments]
         (println "  " argument-name "\t" argument-description))
       (println))
     (println "Optional arguments:")
     (println options-summary))))

