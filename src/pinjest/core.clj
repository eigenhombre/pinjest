(ns pinjest.core
  (:require [cemerick.url :refer [url-encode]]
            [environ.core :refer [env]]
            [cheshire.core :refer [parse-string]]
            [clj-http.client :refer [get]]
            [hiccup.core :refer [html]]))


(def token (:pinterest-token env))


(defn ^:private start-url [token]
  (let [base "https://api.pinterest.com/v1/me/pins/"
        fields [:id :link :note :url :attribution :board :color
                :counts :created_at :creator :image :media :metadata
                :original_link]]
    (->> fields
         (map name)
         (clojure.string/join ",")
         url-encode
         (format "%s?access_token=%s&fields=%s" base token))))


(defn pin-seq
  ([] (pin-seq (start-url token)))
  ([url]
   (lazy-seq
    (let [result (->> url
                      get
                      :body
                      parse-string
                      clojure.walk/keywordize-keys)
          next-url (-> result :page :next)]
      (concat (:data result)
              (when next-url
                (pin-seq next-url)))))))


(defn pin-html [pin]
  [:div
   [:p [:a {:href (:url pin)}
        [:img {:src (-> pin :image :original :url)
               :width 250}]]]
   [:p
    [:a {:href (:url pin)} [:strong (:note pin)]]
    " (via "
    [:a {:href (:original_link pin)}
     (-> pin :metadata :link :site_name)]
    ")"]
   ;;[:pre (with-out-str (clojure.pprint/pprint pin))]
   ])



(defn randomized-pins-html []
  (html [:div
         (map pin-html (shuffle (take 3000 (pin-seq))))]))


(comment
  ;; Generate links back to Pinterest with thumbnails, for all pins,
  ;; in random order; then open in browser.
  (spit "index.html" (randomized-pins-html))
  (clojure.java.shell/sh "open" "index.html"))
