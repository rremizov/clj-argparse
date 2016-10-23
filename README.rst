clj-argparse
============

A convenience wrapper around org.clojure/tools.cli

.. image:: https://img.shields.io/clojars/v/org.clojars.rmremizov/clj-argparse.svg
   :target: https://clojars.org/org.clojars.rmremizov/clj-argparse
   :alt: Clojars Project

Usage
-----

.. code:: clojure

    (ns user (:require [clj-argparse.core :as argparse]))

    (argparse/defclispec cli-spec
      (argparse/option ["-t" "--test" "Option description"])
      (argparse/subcommand "subcommand" "Subcommand description")
      (argparse/argument "subcommand" "first-arg"
                         "Positional argument description")
      (argparse/option "subcommand" ["-o" "--option" "Option description"]))

    (argparse/parse cli-spec ["-t" "subcommand" "arg-value" "--option"])
    ;; =>
    ;; {"default"    {:options {:test true} :arguments {}}
    ;;  "subcommand" {:options {:option true}
    ;;                :arguments {"first-arg" "arg-value"}}}

License
-------

Distributed under the Eclipse Public License

