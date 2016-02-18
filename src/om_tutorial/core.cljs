;;** Namespace/Deps
(ns om-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]))

;;** Dev
(enable-console-print!)
(def p pprint/pprint)

;;** App State
(def app-state
  (atom {:todos [{:content "Me fourth"
                  :priority 4}
                 {:content "Me third"
                  :priority 3}
                 {:content "Me second"
                  :priority 2}
                 {:content "Me first"
                  :priority 1}]}))

;;** Mutate functions
(defmulti mutate om/dispatch)

(defmethod mutate 'todo/edit-init!
  [{:keys [state]} _ {:keys [priority] :as params}]
  ;; TODO why do I need the :value bit here
  {:value {:keys [:todos]}
   :action
   (fn []
     (swap! state update-in
       priority
       [:todos :todos/bypriority priority :content-edit]
       ""))})

(defmethod mutate 'todo/remove!
  [{:keys [state]} _ {:keys [priority] :as params}]
  {:value {:keys [:todos]}
   :action
   ;; Would be nice if I didn't have to update both
   (fn []
     (swap! state update-in
       [:todos/bypriority]
       #(dissoc % priority))
     (swap! state update-in
       [:todos]
       (fn [todos]
         (filterv #((@state :todos/bypriority) (last %)) todos))))})

(defmulti read om/dispatch)

(defn get-todo [state key]
  (let [st @state]
    (into [] (map #(get-in st %)) (get st key))))

(defmethod read :todos
  [{:keys [state] :as env} key params]
  {:value (get-todo state key)})

(defmethod read :todos/bypriority
  [{:keys [state] :as env} key params]
  {:value (get-todo state key)})

;;** Read functions
(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

;;** Helpers
(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn react-find-dom-node [obj]
  (.findDOMNode js/ReactDOM obj))

;;** Components
(defui Todo
  static om/Ident
  (ident [this {:keys [priority content]}]
    [:todos/bypriority priority])
  static om/IQuery
  (query [this]
    [:content :priority])
  Object
  ;; TODO need to focus here so the object is visible
  (componentDidUpdate [this _ _]
    (let [content-edit (-> (om/get-state this) :content-edit)]
      ;; TODO check if not focused
      (when content-edit
        (.focus (react-find-dom-node (aget (.. this -refs) "todo-input")))
        )))
  (initLocalState [this] {:content-edit nil})
  (render [this]
    (let [{:keys [content priority] :as props} (om/props this)
          {:keys [content-edit] :as props-local} (om/get-state this)]
      (dom/div nil 
        (dom/span #js {:style (display (not content-edit))
                       :onClick (fn [e]
                                  ;; Set local state
                                  (om/update-state! this update :content-edit #(identity ""))
                                  ;; Focus input
;;                                  (.focus (react-find-dom-node (aget (.. this -refs) "todo-input")))
                                  )
                       :value content-edit
                       } content)
        (dom/input #js {
                        :value content-edit
                        :style (display content-edit)
                        :onChange (fn [e] (om/update-state! this update :content-edit #(.. e -target -value)))
                        ;; TODO add todo in addition to unfocusing
                        :onBlur (fn [e]
                                  (if-not (empty? content-edit)
                                    (om/transact! this `[(todo/commit! {:content ~content-edit})])
                                    (om/update-state! this update :content-edit #(identity nil)))
                                  )
                        :ref "todo-input"
                        })
        (dom/button #js {:onClick (fn [e]  (om/transact! this `[(todo/remove! {:priority ~priority})   :todos]))} "Remove")))))

(def todo (om/factory Todo {:keyfn :priority}))

(defui Todos
  static om/IQuery
  (query [this]
    (let [subquery1 (om/get-query Todo)]
      `[{:todos ~subquery1}]))
  Object
  (render [this]
    (let [{:keys [todos] :as env} (om/props this)]
      (dom/div nil (apply dom/div nil (map todo (sort-by :priority todos)))))))

;;** Reconciler
(def reconciler
  (om/reconciler {:normalize true
                  :state app-state
                  :parser (om/parser {:read read :mutate mutate})}))

;;** App Mounting
(om/add-root! reconciler
  Todos (gdom/getElement "app"))
