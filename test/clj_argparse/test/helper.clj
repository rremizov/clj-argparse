(ns clj-argparse.test.helper
  (:require [clojure.test :refer :all])
  (:require [clj-argparse.impl.core :as core]
            [clj-argparse.impl.definition :as definition]
            [clj-argparse.impl.helper :refer [print-help]]))

(def ^:private cli-spec
  (-> (definition/create-cli-spec)
      (definition/option ["-t" "--test" "Test option"])
      (definition/subcommand "subcommand0" "Subcommand description")
      (definition/option "subcommand0" ["-t" "--test" "Test option"])
      (definition/subcommand "subcommand1"  "Subcommand description")
      (definition/argument "subcommand1" "argument-name" "Argument description")
      (definition/option "subcommand1" ["-t" "--test" "Test option"])))

(deftest test-help-message
  (testing "Help for main command"
    (let [got-output
          (with-out-str (print-help ["-h"] cli-spec))
          expected-output
          (str "Subcommands:\n"
               "   subcommand0 \t Subcommand description\n"
               "   subcommand1 \t Subcommand description\n\n"
               "Optional arguments:\n"
               "  -h, --help  Print help\n"
               "  -t, --test  Test option\n")]
      (is (= got-output expected-output))))
  (testing "Help for subcommand"
    (let [got-output
          (with-out-str (print-help ["subcommand0" "-h"] cli-spec "subcommand0"))
          expected-output
          (str "Help for the \"subcommand0\" subcommand:\n"
               "Optional arguments:\n"
               "  -h, --help  Print help\n"
               "  -t, --test  Test option\n")]
      (is (= got-output expected-output))))
  (testing "Help for subcommand with positional arguments"
    (let [got-output
          (with-out-str (print-help ["subcommand1" "-h"] cli-spec "subcommand1"))
          expected-output
          (str "Help for the \"subcommand1\" subcommand:\n"
               "Positional arguments:\n"
               "   argument-name \t Argument description\n\n"
               "Optional arguments:\n"
               "  -h, --help  Print help\n"
               "  -t, --test  Test option\n")]
      (is (= got-output expected-output)))))

