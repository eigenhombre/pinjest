(ns pinjest.core
  (:gen-class)
  (:require [cemerick.url :refer [url-encode]]
            [cheshire.core :refer [parse-string]]
            [clj-http.client]
            [clojure.java.shell]
            [environ.core :refer [env]]
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
                      clj-http.client/get
                      :body
                      parse-string
                      clojure.walk/keywordize-keys)
          next-url (-> result :page :next)]
      (print ".")
      (flush)
      (concat (:data result)
              (when next-url
                (pin-seq next-url)))))))



(defn ^:private local-image-file-name [pin ext]
  (str "pins/" (:id pin) ext))


(defn write-full-pin-file! [pin]
  (let [filen (local-image-file-name pin ".html")]
    (spit filen
          (html [:div [:a {:href (:url pin)}
                       [:img {:src (str (:id pin) ".jpg")
                              :style "max-width: 100%"}]]]))))


(defn write-image-file-for-pin! [pin]
  (let [img-url (-> pin :image :original :url)
        filen (local-image-file-name pin ".jpg")]
    (write-full-pin-file! pin)
    (when-not (.exists (clojure.java.io/as-file filen))
      (println "Caching" filen "...")
      (clojure.java.io/make-parents filen)
      (with-open [o (clojure.java.io/output-stream filen)]
        (.write o (-> img-url
                      (clj-http.client/get {:as :byte-array})
                      :body))))))


(defn pin-html [pin]
  (write-image-file-for-pin! pin)
  [:div [:a {:href (local-image-file-name pin ".html")}
         [:img {:src (local-image-file-name pin ".jpg")
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


(defn pages-html [npages]
  [:div (for [i (range npages)]
          [:span
           [:a {:href (str "pins-" i ".html")}
            "Page " (inc i)]
           "&nbsp;"])])


(defn pins-html [pins npages]
  (html (pages-html npages)
        [:div (map pin-html pins)
         [:p {:style "clear: both"}]]
        (pages-html npages)))


(def aspect-ratio (comp double
                        (partial apply /)
                        (juxt :width :height)
                        :original
                        :image))


(defn -main []
  (print "Fetching pins...")
  (flush)
  (let [pins (pin-seq)
        npages (->> pins (partition-all 500) count)]
    (->> pins
         (sort-by :created-at)
         (partition-all 30)
         (mapcat (comp reverse (partial sort-by aspect-ratio)))
         (partition-all 500)
         (map-indexed (fn [i pins] [i (pins-html pins npages)]))
         (map (fn [[i htm]] (spit (str "pins-" i ".html") htm)))
         dorun)
    (println "\n\nDone.")))
