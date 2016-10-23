(ns clj-argparse.impl.core)

(def default-subcommand "default")

(defn in [x s] (not (nil? (first (filter #{x} s)))))
(def not-in (complement in))

(defn zip [& colls]
  (if (seq colls)
    (apply map vector colls)
    []))

(defn first-column [vs]
  (first (apply zip vs)))

