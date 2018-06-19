(ns b8 (:use [overtone.live][mud.core][mud.chords]) (:require [shadertone.tone :as t] [mud.timing :as time]))



(ctl-global-clock 0.5)

(ctl time/root-s :rate 4.)


(defsynth data-probes [timing-signal-bus 0]
  (let [beat-count (in:kr timing-signal-bus)
        _ (tap "global-beat-count" 60 (a2k beat-count))]
    (out 0 0)))

(def active-data-probes (data-probes (:count time/beat-1th)))

