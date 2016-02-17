(ns om-tutorial.app-database.exercises)

(def cars-table
  {:cars/by-id {1 {:make "Nissan" :model "Leaf"}
                2 {:make "Dodge" :model "Dart"}
                3 {:make "Ford" :model "Mustang"}}
   })

(def favorites
  (merge cars-table
    {:favorite-car [:cars/by-id 1]})
                                        ; TODO (exercise 2): Add a :favorite-car key that points to the Nissan Leaf via an ident
  )

(def ex3-uidb
  {:main-panel {:toolbar  [:toolbar :main]
                :canvas   [:canvas  :main]}

   :toolbar    {:main {:tools [[:tools/by-id 1]
                               [:tools/by-id 2]]}}   

   :canvas     {:main {:data [[:data/by-id 5]]}}

   :tools/by-id {1 {:label "Cut"}
                 2 {:label "Copy"}}

   :data/by-id  {5 {:x 1 :y 3}}
   })




