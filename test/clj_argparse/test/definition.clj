(ns clj-argparse.test.definition
  (:require [clojure.test :refer :all])
  (:require [clojure.data.generators :as g]
            [schema.core :as schema])
  (:require [clj-argparse.impl.definition :as definition]))

(deftest test-cli-spec-schema
  (testing "Initial schema"
    (schema/validate definition/cli-spec-schema (definition/create-cli-spec)))
  (testing "Subcommand schema"
    (schema/validate definition/cli-spec-schema
                     (definition/subcommand
                       (definition/create-cli-spec)
                       (g/string)
                       (g/string))))
  (testing "Argument schema"
    (schema/validate definition/cli-spec-schema
                     (definition/argument (definition/create-cli-spec)
                       (g/string) (g/string))))
  (testing "Subcommand argument schema"
    (let [subcommand-name (g/string)
          cli-spec
          (definition/subcommand (definition/create-cli-spec)
            subcommand-name (g/string))]
      (schema/validate definition/cli-spec-schema
                       (definition/argument
                         cli-spec
                         subcommand-name
                         (g/string)
                         (g/string)))))
  (testing "Define a subcommand argument without defining the subcommand"
    (is (thrown? java.lang.RuntimeException
                 (schema/validate definition/cli-spec-schema
                                  (definition/argument
                                    (definition/create-cli-spec)
                                    (g/string)
                                    (g/string)
                                    (g/string))))))
  (testing "Define an argument after subcommand has been defined"
    (let [cli-spec
          (definition/subcommand (definition/create-cli-spec)
            (g/string) (g/string))]
      (is (thrown? java.lang.RuntimeException
                   (schema/validate definition/cli-spec-schema
                                    (definition/argument
                                      cli-spec (g/string) (g/string)))))))
  (testing "Define a subcommand after positional argument has been defined"
    (let [cli-spec
          (definition/argument (definition/create-cli-spec)
            (g/string) (g/string))]
      (is (thrown? java.lang.RuntimeException
                   (schema/validate definition/cli-spec-schema
                                    (definition/subcommand
                                      cli-spec (g/string) (g/string)))))))
  (testing "Define a subcommand multiple times"
    (let [subcommand-name (g/string)
          cli-spec
          (definition/subcommand (definition/create-cli-spec)
            subcommand-name (g/string))]
      (is (thrown? java.lang.RuntimeException
                   (schema/validate definition/cli-spec-schema
                                    (definition/subcommand
                                      cli-spec subcommand-name (g/string)))))))
  (testing "Option schema"
    (schema/validate definition/cli-spec-schema
                     (definition/option (definition/create-cli-spec)
                       (g/vec g/anything))))
  (testing "Subcommand option schema"
    (let [subcommand-name (g/string)
          cli-spec
          (-> (definition/create-cli-spec)
              (definition/subcommand subcommand-name (g/string))
              (definition/option subcommand-name (g/vec g/anything))
              (definition/option subcommand-name (g/vec g/anything)))]
      (schema/validate definition/cli-spec-schema cli-spec)))
  (testing "Define a subcommand option without defining the subcommand"
    (is (thrown? java.lang.RuntimeException
                 (schema/validate definition/cli-spec-schema
                                  (definition/option (definition/create-cli-spec)
                                    (g/string)
                                    (g/vec g/string)))))))

