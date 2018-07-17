(ns b8 (:use [overtone.live][mud.core][mud.chords]) (:require [shadertone.tone :as t] [mud.timing :as time]))

(do
  (do
    (ctl-global-clock 5)

    (ctl time/root-s :rate 10.)

    (defsynth data-probes [timing-signal-bus 0]
      (let [beat-count (in:kr timing-signal-bus)
            _ (tap "global-beat-count" 60 (a2k beat-count))]
        (out 0 0)))
    (defonce active-data-probes (data-probes (:count time/beat-1th)))
    (defonce active-data-probe-atom (atom {:synth active-data-probes :tap "global-beat-count"}))
    (defonce active-data-probe-tap (get-in (:synth @active-data-probe-atom) [:taps (:tap @active-data-probe-atom)]))
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

    (control-bus-set! my-bus0 10101)

    (do
      (defonce root-trg-bus (control-bus)) ;; global metronome pulse
      (defonce root-cnt-bus (control-bus)) ;; global metronome count
      (defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
      (defonce beat-cnt-bus (control-bus))) ;; beat count

    (def BEAT-FRACTION "Number of global pulses per beat" 30)


                                        ;Audio
    (do
      (defonce my-audio-bus0 (audio-bus))
      (defonce my-audio-bus1 (audio-bus))
      (defonce my-audio-bus2 (audio-bus))
      (defonce my-audio-bus3 (audio-bus))
      )


                                        ;Control synths
    (do
      (defsynth root-trg [rate 100]
        (out:kr root-trg-bus (impulse:kr rate)))

      (defsynth root-cnt []
        (out:kr root-cnt-bus (pulse-count:kr (in:kr root-trg-bus))))

      (defsynth beat-trg [div BEAT-FRACTION]
        (out:kr beat-trg-bus (pulse-divider (in:kr root-trg-bus) div)))

      (defsynth beat-cnt []
        (out:kr beat-cnt-bus (pulse-count (in:kr beat-trg-bus)))))

    (do
      (def r-trg (root-trg))
      (def r-cnt (root-cnt [:after r-trg]))
      (def b-trg (beat-trg [:after r-trg]))
      (def b-cnt (beat-cnt [:after b-trg])))

    (ctl r-trg :rate 10))


                                        ;Buffers
  (do
    (defonce buf-0 (buffer 8))
    (defonce buf-1 (buffer 8))
    (defonce buf-2 (buffer 8))
    (defonce buf-3 (buffer 8))))




                                        ;overpad buffer
(do (defonce overpad-buffer (buffer 256))
    (pattern! overpad-buffer [0 1 0 0 0 0 0 1 0])
    (defsynth overpad-data-probe [overpad-buffer 0 timing-signal-bus 0]
      (let [beat-count (in:kr timing-signal-bus)
            overpad-beat (buf-rd:kr 1 overpad-buffer beat-count)
            _ (tap "overpad-beat" 60 (a2k overpad-beat))]
        (out 0 0)))
    (defonce overpad-data-probef (overpad-data-probe overpad-buffer (:count time/beat-1th)))
    (def overpad-atom (atom {:synth overpad-data-probef :tap "overpad-beat"}))
    (def overpad-tap (get-in (:synth @overpad-atom) [:taps (:tap @overpad-atom)])))


(def ob1 [1 0 1 0 1 0 0 0])

(def ob2 [1 0 1 0 1 0 1 0])

(def ob3 [0 1 0 1 0 0 1 0])


(pattern! overpad-buffer ob2)


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


(def kdb1 [1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0
           1 0 0 0 0 0 0 0])

(def kdb2 [1 0 0 0 0 0 0 0
           1 0 1 0 1 0 1 0])

(def kdb3 [1 0 1 0 0 1 0 1])

(pattern! kick-drum-buffer kdb1)

                                        ;Synths
(defsynth dualPulse [note 22 amp 1 attack 0.1 decay 0.1 sustain 0.2 release 1]
  (let [;freq (* 0.02 (sin-osc 100) (saw note))
        sp1 (sin-osc note)
        sp2 (pulse 0.5 0.25)
        env (env-gen (perc attack release) :action FREE)
        src2 (sin-osc sp2 (* Math/PI 0.5 sp2))]
    (out 0 (pan2 (* amp 0.25 (* sp1 sp2 src2)) ))))

(def dualPulsef (dualPulse :amp 0.1))

(defsynth sin-generator [freq 50 phase 0 out-bus 0]
  (out out-bus (sin-osc freq phase out-bus)))

(def sin-generatorf (sin-generator 50 0 my-audio-bus0))

(ctl sin-generatorf :freq 50 :phase (* Math/PI 2))

(kill sin-generatorf)

(def buf (buffer 8))

(buffer-write! buf 0 (map #(+ 12 %) [10 20 10 20 10 20 10 20]))

(defsynth noisInput [ amp 0.1  fraction BEAT-FRACTION in-bus 0 dec 0.1 attack 0.1 release 0.1]
  (let [src1 (in in-bus)
        tr_in (pulse-divider (in:kr root-trg-bus) fraction)
        indexes (dseq (range 8) INF)
        freqs (dbufrd buf indexes)
        note-gen (demand:kr tr_in 0 freqs)
        env (env-gen (perc attack release) :gate tr_in)
        sawsrc (saw note-gen)
        src2 (+ src1 (decay sawsrc dec) amp)]
    (out 0 (pan2 (* src2 amp env)))))

(def noisInputf (noisInput 1 BEAT-FRACTION my-audio-bus0))

(buffer-set! buf 5 100)


(ctl noisInputf :fraction 2)

(ctl noisInputf :dec 0.1 :release 0.1 :attack 0.2)

(ctl noisInputf :amp 0.01)

(kill noisInputf)

(stop)

(defsynth noise [freq 44 amp 1 freq2 44]
  (let [noiseV (pink-noise)
        src1 (sin-osc noiseV)
        src2 (sin-osc (* noiseV 0.9 (in:kr freq2)))
        src3 (lf-saw freq)]
    (out 0 (pan2 (*  amp (+ src1 src2) src3)))))

(def noisef (noise :freq2 my-bus0))

(ctl noisef :amp 0.01)


(kill noisef)


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



                                        ;Sequencer





                                        ;Video
                                        ;Histogram

(def ch (t/get-cam-histogram 0 :red))

(def v1rh (t/get-video-histogram 0 :red))

(t/toggle-analysis 0 true :histogram)


                                        ;Shader
(t/start "./b8.glsl" :width 1920 :height 1080 :cams [0 1] :videos ["../videos/jkl.mp4" "../videos/metro.mp4" "../videos/spede.mp4"])



                                        ;Watch



(add-watch overpad-tap :overpad-beat
           (fn [_ _ old new]
             (when (and (= old 0.0) (= new 1.0))
               (overpad 30 :attack 0.01 :release 0.1))))

(remove-watch overpad-tap :overpad-beat)

(add-watch kick-drum-tap :kick-drum-beat
           (fn [_ _ old new]
             (when (and (= old 0.0) (= 1.0 new))
               (dualPulse :amp 0.5)
               (t/set-dataArray-item 1 (* new 10))
               ;(overpad :attack 4.1 :release 0.2)

)))


(add-watch v1rh :v1rh (fn [_ _ old new]
                         ;(println (nth new 100))
                         (t/set-dataArray-item 0 (nth new 20))
                        ))
