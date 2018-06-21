(ns b8 (:use [overtone.live][mud.core][mud.chords]) (:require [shadertone.tone :as t] [mud.timing :as time]))


(do
  (ctl-global-clock 5)

  (ctl time/root-s :rate 20.)

  (defsynth data-probes [timing-signal-bus 0]
    (let [beat-count (in:kr timing-signal-bus)
          _ (tap "global-beat-count" 60 (a2k beat-count))]
      (out 0 0)))
  (def active-data-probes (data-probes (:count time/beat-1th)))

  )


                                        ;Buses
                                        ;Control
(do
  (defonce my-bus0 (control-bus 1))
  (defonce my-bus1 (control-bus 1))
  (defonce my-bus2 (control-bus 1))
  (defonce my-bus3 (control-bus 1))
  (defonce my-bus4 (control-bus 1))
  (defonce my-bus5 (control-bus 1))
  (defonce my-bus6 (control-bus 1))
  (defonce my-bus7 (control-bus 1))
  (defonce my-bus8 (control-bus 1))
  (defonce my-bus9 (control-bus 1))

  (control-bus-set! my-bus0 10101))



;Buffers
;Kick drum buffer
(do
  (defonce kick-drum-buffer (buffer 256))
  (pattern! kick-drum-buffer [1 0 0 0 0 1 0 1 1 0 1 0])
  (defsynth kick-drum-data-probe [kick-drum-buffer 0 timing-signal-bus 0]
    (let [beat-count (in:kr timing-signal-bus)
          drum-beat (buf-rd:kr 1 kick-drum-buffer beat-count)
          _ (tap "kick-drum-beat" 60 (a2k drum-beat))]
      (out 0 0)))

  (defonce kick-drum-data-probef (kick-drum-data-probe kick-drum-buffer (:count time/beat-1th)))
  (def kick-drum-atom (atom {:synth kick-drum-data-probef :tap "kick-drum-beat"}))
  (def kick-drum-tap (get-in (:synth @kick-drum-atom) [:taps (:tap @kick-drum-atom)]))
  )

(pattern! kick-drum-buffer [1 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 1  0 0 0 0 0 0 0 1 0 0 0 0 0 0 0])

                                        ;Synths

(defsynth dualPulse [note 22 amp 1 attack 0.1 decay 0.1 sustain 0.2 release 1]
  (let [;freq (* 0.02 (sin-osc 100) (saw note))
        sp1 (sin-osc note)
        sp2 (pulse 0.5 0.25)
        env (env-gen (perc attack release) :action FREE)
        src2 (sin-osc sp2 (* Math/PI 0.5 sp2))]
    (out 0 (pan2 (* amp 0.25 (* sp1 sp2 src2)) ))))

(def dualPulsef (dualPulse :amp 0.1))


(defsynth noise [freq 44 amp 1 freq2 44]
  (let [noiseV (pink-noise)
        src1 (sin-osc noiseV)
        src2 (sin-osc (* noiseV 0.9 (in:kr freq2)))
        src3 (lf-saw freq)]
    (out 0 (pan2 (*  amp (+ src1 src2) src3)))))

(def noisef (noise :freq2 my-bus0))

(ctl noisef :amp 0.01)

(defsynth overpad [note 60 amp 0.7 attack 0.001 release 2]
  (let [freq (midicps note)
        env (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 10 freq (env-gen (perc 0.012 (- release 0.01)))))
        bfreq (/ freq 2)
        sig (apply - (concat (* 0.7 (sin-osc [bfreq (* 0.99 bfreq)])) (lpf (saw [freq (* freq 1.01)]) f-env)))
        _ (tap "sig" 60 (a2k sig))]
    (out 0 (pan2 (* amp env sig)))))

(overpad 30 :attack 0.01 :release 0.1)


(defsynth in-bus-synth [in-bus 0 gain 10 cutoff 10]
  (let [src (sound-in in-bus)
        srci (lpf src cutoff)
        srco (* gain srci)
        _ (tap "ivol" 60 srco)]
    (out 0 (pan2 srco))))

(def ibs (in-bus-synth))

(ctl ibs :cutoff 440 :gain 100)

(kill ibs)


                                        ;Video
                                        ;Histogram

(def ch (t/get-cam-histogram 0 :red))

(def v1rh (t/get-video-histogram 0 :red))

(t/toggle-analysis 0 true :histogram)


                                        ;Shader
(t/start "./b8.glsl" :width 1920 :height 1080 :cams [0 1] :videos ["../videos/jkl.mp4" "../videos/metro.mp4" "../videos/spede.mp4"])



                                        ;Watch
(add-watch kick-drum-tap :kick-drum-beat
           (fn [_ _ old new]
             (when (and (= old 0.0) (= 1.0 new))
               (dualPulse :amp 0.5)
               (t/set-dataArray-item 1 new)
               ;(overpad :attack 4.1 :release 0.2)

)))

(add-watch v1rh :v1rh (fn [_ _ old new]
                         ;(println (nth new 100))
                         (t/set-dataArray-item 0 (nth new 100))
                        ))
