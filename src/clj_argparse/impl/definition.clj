(ns clj-argparse.impl.definition
  (:require [schema.core :as schema])
  (:require [clj-argparse.impl.core :as core]))

(def ^:private options-schema
  [(schema/one [(schema/one schema/Str "-h")
                (schema/one schema/Str "--help")
                (schema/one schema/Str "Print help")]
               "Print help option")
   (schema/pred vector? "vector")])

(def ^:private arguments-schema
  [[(schema/one schema/Str "Argument name")
    (schema/one schema/Str "Argument description")]])

(def ^:private subcommands-schema
  [[(schema/one schema/Str "Subcommand name")
    (schema/one schema/Str "Subcommand description")]])

(def cli-spec-schema
  "The schema for validation of ready to use cli specs."
  {(schema/required-key core/default-subcommand)
   {:options options-schema
    :arguments arguments-schema
    :subcommands subcommands-schema}
   schema/Str
   {(schema/optional-key :options) options-schema
    (schema/optional-key :arguments) arguments-schema
    (schema/optional-key :subcommands) subcommands-schema}})

(defn- create-command-spec []
  {:options [["-h" "--help" "Print help"]]
   :arguments []
   :subcommands []})

(defn create-cli-spec
  "Create empty cli spec."
  []
  {core/default-subcommand (create-command-spec)})

(defn subcommand
  "Add a subcommand to the cli-spec."
  [cli-spec subcommand-name subcommand-description]
  (when (not= 0 (count (get-in cli-spec
                               [core/default-subcommand :arguments])))
    (throw
     (ex-info "Cannot define subcommand for command with positional arguments.")))
  (when (contains? cli-spec subcommand-name)
    (throw (ex-info (str "Subcommand " subcommand-name " defined already"))))
  (-> cli-spec
      (assoc subcommand-name (create-command-spec))
      (update-in [core/default-subcommand :subcommands]
                 conj [subcommand-name
                       subcommand-description])))

(defn argument
  "Add a positional argument to the cli-spec.
  Works for subcommands."
  ([cli-spec arg-name arg-description]
   (argument cli-spec core/default-subcommand arg-name arg-description))
  ([cli-spec subcommand-name arg-name arg-description]
   (when (not= 0 (count (get-in cli-spec
                                [subcommand-name :subcommands])))
     (throw (ex-info (str "Cannot define positional argument"
                          "for command with subcommands."))))
   (when (not (contains? cli-spec subcommand-name))
     (throw (ex-info (str "Cannot define positional argument for a subcommand "
                          "without definig the subcommand first."))))
   (update-in cli-spec
              [subcommand-name :arguments]
              #(conj % [arg-name arg-description]))))

(defn option
  "Add an option to the cli-spec.
  The option-specs come in form required by the org.clojure/tools.cli.
  Works for subcommands."
  ([cli-spec option-specs]
   (option cli-spec core/default-subcommand option-specs))
  ([cli-spec subcommand-name option-specs]
   (when (not (contains? cli-spec subcommand-name))
     (throw (ex-info (str "Cannot define an option for a subcommand "
                          "without definig the subcommand first."))))
   (update-in cli-spec
              [subcommand-name :options]
              #(conj % option-specs))))

