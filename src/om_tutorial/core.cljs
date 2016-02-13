(ns om-tutorial.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cljs.pprint :as pprint]))

(enable-console-print!)

(def app-state
  (atom {:lst [{:content "Me third"
                :priority 3}
               {:content "Me first"
                :priority 0}
               {:content "Me second"
                :priority 1}]}))

(defui Todo
  Object
  (render [this]
    (let [{:keys [content]} (om/props this)]
      (if (= "" dom)
        (content/input nil )
        (dom/div nil 
          (dom/span nil content)
          (dom/button nil "Remove"))))))

(def todo (om/factory Todo {:keyfn :priority}))

(defui TodoList
  Object
  (render [this]
    (let [{:keys [lst]} (om/props this)]
      (dom/div nil
        (map todo (sort-by :priority lst)))
      )))

(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
  TodoList (gdom/getElement "app"))


;; (def reconciler
;;   (om/reconciler
;;     {:state init-data
;;      :parser (om/parser
;;                {:read read :mutate mutate})}))

;; (om/add-root! reconciler Dashboard (gdom/getElement "app"))

;; (defui Post
;;   static om/IQuery
;;   (query [this]
;;     [:id :type :title :author :content])
;;   Object
;;   (render [this]
;;     (let [{:keys [title author content] :as props} (om/props this)]
;;       (dom/div nil
;;         (dom/h3 nil title)
;;         (dom/h4 nil author)
;;         (dom/p nil content)))))

;; (def post (om/factory Post))

;; (defui Photo
;;   static om/IQuery
;;   (query [this]
;;     [:id :type :title :image :caption])
;;   Object
;;   (render [this]
;;     (let [{:keys [title image caption]} (om/props this)]
;;       (dom/div nil
;;         (dom/h3 nil (str "Photo: " title))
;;         (dom/div nil image)
;;         (dom/p nil (str "Caption: " caption))))))

;; (def photo (om/factory Photo))

;; (defui Graphic
;;   static om/IQuery
;;   (query [this]
;;     [:id :type :title :image])
;;   Object
;;   (render [this]
;;     (let [{:keys [title image]} (om/props this)]
;;       (dom/div nil
;;         (dom/h3 nil (str "Graphic: " title))
;;         (dom/div nil image)))))

;; (def graphic (om/factory Graphic))

;; (defui DashboardItem
;;   static om/Ident
;;   (ident [this {:keys [id type]}]
;;     [type id])
;;   static om/IQuery
;;   (query [this]6-01-16 08:33 testfile.el
;;     (zipmap
;;       [:dashboard/post :dashboard/photo :dashboard/graphic]
;;       (map #(conj % :favorites)
;;         [(om/get-query Post)
;;          (om/get-query Photo)
;;          (om/get-query Graphic)])))
;;   Object
;;   (render [this]
;;     (let [{:keys [id type favorites] :as props} (om/props this)]
;;       (dom/li
;;         #js {:style #js {:padding 10 :borderBottom "1px solid black"}}
;;         (dom/div nil
;;           (dom/div #js {:style #js {:color "blue"}} "beans")
;;           ;; post, photo, and graphic are all component factory functions
;;           (({:dashboard/post    post
;;              :dashboard/photo   photo
;;              :dashboard/graphic graphic} type)
;;            (om/props this)))
;;         (dom/div nil
;;           (dom/p nil (str "Favorites: " favorites))
;;           (dom/button
;;             #js {:onClick
;;                  (fn [e]
;;                    (om/transact! this
;;                      `[(dashboard/favorite {:ref [~type ~id]})]))}
;;             "Favorite!"))))))

;; (def dashboard-item (om/factory DashboardItem))

;; (defui Dashboard
;;   static om/IQuery
;;   (query [this]
;;     [{:dashboard/items (om/get-query DashboardItem)}])
;;   Object
;;   (render [this]
;;     (let [{:keys [dashboard/items]} (om/props this)]
;;       (apply dom/ul
;;         #js {:style #js {:padding 0}}
;;         (map dashboard-item items)))))

;; (defmulti read om/dispatch)

;; (defmethod read :dashboard/items
;;   [{:keys [state]} k _]
;;   (let [st @state]
;;     {:value (into [] (map #(get-in st %)) (get st k))}))

;; (defmulti mutate om/dispatch)

;; (defmethod mutate 'dashboard/favorite
;;   [{:keys [state]} k {:keys [ref]}]
;;   {:action
;;    (fn []
;;      (swap! state update-in (conj ref :favorites) inc))})

