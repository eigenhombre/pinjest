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



(defn ^:private local-image-file-name [pin]
  (str "pins/" (:id pin) ".jpg"))


(defn write-image-file-for-pin! [pin]
  (let [img-url (-> pin :image :original :url)
        filen (local-image-file-name pin)]
    (when-not (.exists (clojure.java.io/as-file filen))
      (clojure.java.io/make-parents filen)
      (with-open [o (clojure.java.io/output-stream filen)]
        (.write o (-> img-url (get {:as :byte-array}) :body))))))



(defn pin-html [pin]
  (write-image-file-for-pin! pin)
  [:div [:a {:href (:url pin)}
         [:img {:src (local-image-file-name pin)
                :style "float: left;"
                :width 250}]]
   ;; [:a {:href (:url pin)} [:strong (:note pin)]]
   ;; " (via "
   ;; [:a {:href (:original_link pin)}
   ;;  (-> pin :metadata :link :site_name)]
   ;; ")"
   ]
  ;;[:pre (with-out-str (clojure.pprint/pprint pin))]
  )


(defn pins-html [pins]
  (html [:div (map pin-html pins)
         [:p {:style "clear: both"}]]))


(comment
  (def pins (pin-seq))

  (first pins)
  ;;=>
  {:counts {:likes 0, :comments 0, :repins 0}, :creator {:url "https://www.pinterest.com/eigenhombre/", :first_name "John", :last_name "Jacobsen", :id "311241161656265419"}, :color "#cec6ae", :original_link "http://io9.com/could-this-be-the-most-radiant-collection-of-dune-art-e-1693993134", :note " ", :link "https://www.pinterest.com/r/pin/311241024228988307/4779055074072594921/7051bbb4ef26efb0372671357a0248133c3a7622fe26540a118ca9b155cb7774", :id "311241024228988307", :attribution nil, :url "https://www.pinterest.com/pin/311241024228988307/", :image {:original {:url "https://s-media-cache-ak0.pinimg.com/originals/d7/e7/64/d7e7640ee0f171a45196c24fa45b1a8b.jpg", :width 636, :height 911}}, :media {:type "image"}, :metadata {:article {:published_at nil, :description "The Folio Society is doing a gorgeous new edition of Frank Herbert's Dune, and we were excited to show you the front cover a while back. But just wait until you get a load of the interior art, also created by artist extraordinaire Sam Weber. [Warning: One picture might be NSFW.]", :name "Could This Be The Most Radiant Collection Of Dune Art Ever Assembled?", :authors []}, :link {:locale "en", :title "Could This Be The Most Radiant Collection Of Dune Art Ever Assembled?", :site_name "io9", :description "The Folio Society is doing a gorgeous new edition of Frank Herbert's Dune, and we were excited to show you the front cover a while back. But just wait until you get a load of the interior art, also created by artist extraordinaire Sam Weber. [Warning: One picture might be NSFW.]", :favicon "https://s-media-cache-ak0.pinimg.com/favicons/b8c4c7d8d6d681a3747cf4e818450d52a722437ea36deaf7ab2c25c8.png?4665c5a44711691e9de02e62e2aae042"}}, :created_at "2016-03-27T17:04:47", :board {:url "https://www.pinterest.com/eigenhombre/cover-art-illustration/", :id "311241092937050525", :name "Cover Art / Illustration"}}
  

  (spit "index.html" (pins-html pins))
  (clojure.java.shell/sh "open" "index.html"))
