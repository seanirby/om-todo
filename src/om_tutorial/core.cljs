(ns om-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(def init-data
  {:list/one [{:name "John" :points 0}
              {:name "Mary" :points 0}
              {:name "Bob"  :points 0}]
   :list/two [{:name "Mary" :points 0 :age 27}
              {:name "Gwen" :points 0}
              {:name "Jeff" :points 0}]})

;; -----------------------------------------------------------------------------
;; Parsing


;;;Defines a multimethod using om/dispatch as the dispatcher
;;;multimethod will dispatch on key
(defmulti read om/dispatch)

;;; Gets the people, key is line/one or line/two
(defn get-people [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

;;; two different lists
(defmethod read :list/one
  [{:keys [state] :as env} key params]
  (println key)
  {:value (get-people state key)})

(defmethod read :list/two
  [{:keys [state] :as env} key params]
  {:value (get-people state key)})

(defmulti mutate om/dispatch)

;; the muatte signature is [env key params]
(defmethod mutate 'points/increment
  [{:keys [state]} _ {:keys [name]}]
  {:action
   (fn []
     (swap! state update-in
       ;; whats being swapped here?
       [:person/by-name name :points]
       inc))})

(defmethod mutate 'points/decrement
  [{:keys [state]} _ {:keys [name]}]
   {:action
   (fn []
     (swap! state update-in
       [:person/by-name name :points]
       #(let [n (dec %)] (if (neg? n) 0 n))))})

;; -----------------------------------------------------------------------------
;; Components

(defui Person
  static om/Ident
  ;;; declares that :person/by-name are associated with i lo
  (ident [this {:keys [name]}]
    [:person/by-name name])
  ;;; declares how this component gets its data
  static om/IQuery
  (query [this]
    '[:name :points :age])
  Object
  (render [this]
    (println "Render Person" (-> this om/props :name))
    (let [{:keys [points name] :as props} (om/props this)]
      (dom/li nil
        (dom/label nil (str name ", points: " points))
        (dom/button
          #js {:onClick
               (fn [e]
                 (om/transact! this
                   `[(points/increment ~props)]))}
          "+")
        (dom/button
          #js {:onClick
               (fn [e]
                 (om/transact! this
                   `[(points/decrement ~props)]))}
          "-")))))

(def person (om/factory Person {:keyfn :name}))

(defui ListView
  Object
  (render [this]
    ;; wtf does om/path do?
    (println this)
    ;; it looks like a a way to get a nested structed of things
    (println (om/path this))
    (println "props")
    (println (om/props this))
    (println "Render ListView" (-> this om/path first))'
    (println "reconciler")
    (println @reconciler)
    (println 'b)
    (let [list (om/props this)]
      (apply dom/ul nil
        (map person list)))))

;; a thing that can create instances of ListView
(def list-view (om/factory ListView))

(defui RootView
  static om/IQuery
  (query [this]
    (let [subquery (om/get-query Person)]
      ;; I can refer to these keys later on with one and two
      `[{:list/one ~subquery} {:list/two ~subquery}]))
  Object
  (render [this]
    (println "Render RootView")
    (let [{:keys [list/one list/two]} (om/props this)]
      (println "one")
      (println "beans")
      (println one)
      (apply dom/div nil
        [(dom/h2 nil "List A")
         (list-view one)
         (dom/h2 nil "List B")
         (list-view two)]))))

(def reconciler
;the application state. If not an atom the reconciler will normalize the data with the query supplied by the root component.
  (om/reconciler
    {:state  init-data
     :parser (om/parser {:read read :mutate mutate})}))

(println "First reconciler:")
(println @reconciler)
(om/add-root! reconciler
  RootView (gdom/getElement "app"))
