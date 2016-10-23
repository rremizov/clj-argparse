(ns clj-argparse.core
  (:require [schema.core :as schema])
  (:require [clj-argparse.impl.core :refer [first-column]]
            [clj-argparse.impl.definition :as definition]
            [clj-argparse.impl.helper :as helper]
            [clj-argparse.impl.parser :as parser]))

(defn subcommand
  {:doc (:doc (meta #'definition/subcommand))}
  [cli-spec subcommand-name subcommand-description]
  (definition/subcommand cli-spec subcommand-name subcommand-description))

(defn argument
  {:doc (:doc (meta #'definition/argument))}
  ([cli-spec arg-name arg-description]
   (definition/argument cli-spec arg-name arg-description))
  ([cli-spec subcommand-name arg-name arg-description]
   (definition/argument cli-spec subcommand-name arg-name arg-description)))

(defn option
  {:doc (:doc (meta #'definition/option))}
  ([cli-spec option-specs]
   (definition/option cli-spec option-specs))
  ([cli-spec subcommand-name option-specs]
   (definition/option cli-spec subcommand-name option-specs)))

(defn parse
  "Parse args sequence using cli-spec which is defined with the defclispec macro.
  Returns a map with :arguments and :options for each defined subcommand
  and the \"default\" one.
  {\"default\" {:options {} :arguments {}}"
  [cli-spec args]
  (schema/validate definition/cli-spec-schema cli-spec)
  (let [parsed-cli-spec
        (try (parser/parse cli-spec args)
             (catch clojure.lang.ExceptionInfo exc
               (when (-> (ex-data exc) :type (= :error-invalid-arguments))
                 (helper/print-help args cli-spec (:subcommand (ex-data exc)))
                 (System/exit 1))))
        help-requested-for-subcommands
        (->> parsed-cli-spec
             (filter (fn [[subcommand parsed-subcommand-spec]]
                       (contains? (:options parsed-subcommand-spec) :help)))
             (first-column))]
    (when (seq help-requested-for-subcommands)
      (helper/print-help args cli-spec (first help-requested-for-subcommands))
      (System/exit 0))
    parsed-cli-spec))

(defmacro defclispec
  "Define cli spec using the subcommand, option and argument functions.
  E.g.:
  (defclispec cli-spec
    (option [\"-t\" \"--test\" \"Test option\"])
    (subcommand \"subcommand0\" \"Subcommand description\")
    (option \"subcommand0\" [\"-t\" \"--test\" \"Test option\"])
    (subcommand \"subcommand1\" \"Subcommand description\")
    (argument \"subcommand1\" \"payload\" \"Argument description\")
    (option \"subcommand1\" [\"-t\" \"--test\" \"Test option\"])) "
  [name & body]
  `(def ~name
     (-> (definition/create-cli-spec)
         ~@body)))

