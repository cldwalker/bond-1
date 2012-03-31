(ns bond.james)

(defn with-spies)

(defn spy
  "wrap f, keeping track of its call count and arguments"
  [f]
  (let [calls (atom [])
        old-f f]
    (with-meta (fn [& args]
                 (try
                   (let [resp (apply old-f args)]
                     (swap! calls conj {:args args
                                        :return resp})
                     resp)
                   (catch Exception e
                     (swap! calls conj {:args args
                                        :throw e})
                     (throw e))))
      {::calls calls})))

(defn calls
  "Takes one arg, a fn that has previously been spied. Returns a seq
  of maps, one per call. Each map contains the keys :args and :return
  or :throw"
  [f]
  (-> f (meta) ::calls (deref)))

(defmacro with-spy
  "Takes a var pointing at a fn. Modifies the fn to track call counts, but does not change the fn's behavior"
  [v & body]
  `(with-redefs [~v (spy ~v)]
     (do ~@body)))

(defmacro with-stub
  "Takes a vector of fn vars. Replaces each fn with one that takes any
  number of args and returns nil. Also spies the stubbed-fn"
  [vs & body]

  `(with-redefs ~(->> (mapcat (fn [v]
                                [v `(spy (constantly nil))]) vs)
                      (vec))
     ~@body))