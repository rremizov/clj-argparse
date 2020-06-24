(ns clj-argparse.test.parser
  (:require [clojure.test :refer :all])
  (:require [schema.core :as schema])
  (:require [clj-argparse.impl.core :as core]
            [clj-argparse.impl.definition :as definition]
            [clj-argparse.impl.parser :as parser]))

(def ^:private cli-spec-0
  (-> (definition/create-cli-spec)
      (definition/option ["-t" "--test" "Test option"])
      (definition/subcommand "subcommand0" "Subcommand description")
      (definition/option "subcommand0" ["-t" "--test" "Test option"])
      (definition/subcommand "subcommand1"  "Subcommand description")
      (definition/argument "subcommand1" "argument-name" "Argument description")
      (definition/option "subcommand1" ["-t" "--test" "Test option"])))

(def ^:private cli-spec-1
  (-> (definition/create-cli-spec)
      (definition/argument "argument-name" "Argument description")))

(deftest test-parse-result-schema
  (doseq [args [[]
                ["-t"]
                ["--test"]
                ["-t" "subcommand0"]
                ["--test" "subcommand0"]
                ["--test" "subcommand0" "-t"]
                ["--test" "subcommand0" "--test"]
                ["subcommand0"]
                ["subcommand0" "--t"]
                ["subcommand0" "--test"]
                ["subcommand1" "--test" "argument value"]]]
    (schema/validate parser/parse-result-schema
                     (parser/parse cli-spec-0
                                   args))))

(deftest test-parse-errors
  (testing "Unavailable subcommand provided"
    (is (thrown? clojure.lang.ExceptionInfo
                 (parser/parse cli-spec-0 ["subcommand2"]))))
  (testing "Not enough arguments provided"
    (is (thrown? clojure.lang.ExceptionInfo
                 (parser/parse cli-spec-0 ["subcommand1"])))
    (is (thrown? clojure.lang.ExceptionInfo
                 (parser/parse cli-spec-1 [])))))

(deftest test-parse-results
  (testing "Argument without subcommands"
    (is (= "value"
           (-> (parser/parse cli-spec-1 ["value"])
               (get core/default-subcommand)
               :arguments
               (get "argument-name")))))
  (testing "Argument of a subcommand"
    (is (= "value"
           (-> (parser/parse cli-spec-0 ["subcommand1" "value"])
               (get "subcommand1")
               :arguments
               (get "argument-name")))))
  (testing "Global options separated from subcommand options"
    (is (not
         (-> (parser/parse cli-spec-0 ["--test" "subcommand0"])
             (get "subcommand0")
             :options
             (contains? :test))))))

