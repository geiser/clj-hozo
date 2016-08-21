(ns br.usp.icmc.caed.cljhozo.core-test
  (:use [midje.sweet] [clojure.set])
  (:require [clojure.test :refer :all]
            [br.usp.icmc.caed.cljhozo.core :refer :all])
  (:import [jp.hozo.core BasicConcept WholeConcept RelationConcept
                         RoleConcept RoleHolder SpeciesConcept ValueConcept
                         P_Operator Concept Slot]
           [jp.hozo.core.literal StringValueConcept]
           [jp.hozo.core.util OntologyUtility ConceptUtility SlotUtility]
           [jp.hozo.corex.util HZOntologyUtility HZConceptUtility]))

(facts "About load ontologies (loading dev-resources/*)"
  (let [bike-ont (load-ontology "dev-resources/BIKE_sample.xml")
        travel-ont (load-ontology "dev-resources/travelling.xml")
        vehicle-ont (load-ontology "dev-resources/vehicle.xml")]
    (fact "Validate basic informations"
      (.isLoaded bike-ont) => true
      (.getId bike-ont) => "1410986548000ont"
      (.getOntologyName bike-ont) => nil
      (.getProjectName bike-ont) => nil
      (.getAuthor bike-ont) => nil
      (.getLabel bike-ont) => "BIKE_sample.xml"

      (.isLoaded travel-ont) => true
      (.getId travel-ont) => "1187617980000ont"
      (.getOntologyName travel-ont) => nil
      (.getProjectName travel-ont) => nil
      (.getAuthor travel-ont) => nil
      (.getLabel travel-ont) => "travelling.xml"

      (.isLoaded vehicle-ont) => true
      (.getId vehicle-ont) => "1187617980000ont"
      (.getOntologyName vehicle-ont) => nil
      (.getProjectName vehicle-ont) => nil
      (.getAuthor vehicle-ont) => nil
      (.getLabel vehicle-ont) => "vehicle.xml")
    (fact "Validate number of basic concepts"
      (.getConceptCount bike-ont BasicConcept/CONCEPT_TYPE) => 17
      (.getConceptCount travel-ont BasicConcept/CONCEPT_TYPE) => 49
      (.getConceptCount vehicle-ont BasicConcept/CONCEPT_TYPE) => 131)
    (fact "Validate number of whole concepts"
      (.getConceptCount bike-ont WholeConcept/CONCEPT_TYPE) => 15
      (.getConceptCount travel-ont WholeConcept/CONCEPT_TYPE) => 49
      (.getConceptCount vehicle-ont WholeConcept/CONCEPT_TYPE) => 122)
    (fact "Validate number of relation concepts"
      (.getConceptCount bike-ont RelationConcept/CONCEPT_TYPE) => 2
      (.getConceptCount travel-ont RelationConcept/CONCEPT_TYPE) => 0
      (.getConceptCount vehicle-ont RelationConcept/CONCEPT_TYPE) => 9)
    (fact "Validate number of role concepts"
      (.getConceptCount bike-ont RoleConcept/CONCEPT_TYPE) => 18
      (.getConceptCount travel-ont RoleConcept/CONCEPT_TYPE) => 65
      (.getConceptCount vehicle-ont RoleConcept/CONCEPT_TYPE) => 102)
    (fact "Validate number of role-holder concepts"
      (.getConceptCount bike-ont RoleHolder/CONCEPT_TYPE) => 0
      (.getConceptCount travel-ont RoleHolder/CONCEPT_TYPE) => 0
      (.getConceptCount vehicle-ont RoleHolder/CONCEPT_TYPE) => 30)
    (fact "Validate number of species concepts"
      (.getConceptCount bike-ont SpeciesConcept/CONCEPT_TYPE) => 0
      (.getConceptCount travel-ont SpeciesConcept/CONCEPT_TYPE) => 0
      (.getConceptCount vehicle-ont SpeciesConcept/CONCEPT_TYPE) => 0)
    (fact "Validate number of value concepts"
      (.getConceptCount bike-ont ValueConcept/CONCEPT_TYPE) => 0
      (.getConceptCount travel-ont ValueConcept/CONCEPT_TYPE) => 0
      (.getConceptCount vehicle-ont ValueConcept/CONCEPT_TYPE) => 0)
    (fact "Validate number of pred operators"
      (.getConceptCount bike-ont P_Operator/CONCEPT_TYPE) => 0
      (.getConceptCount travel-ont P_Operator/CONCEPT_TYPE) => 0
      (.getConceptCount vehicle-ont P_Operator/CONCEPT_TYPE) => 0)
    (fact "Validate sum of of concepts (basic, whole, relation, role, ...)"
      (.getConceptCount bike-ont Concept/CONCEPT_TYPE) => 35
      (.getConceptCount travel-ont Concept/CONCEPT_TYPE) => 114
      (.getConceptCount vehicle-ont Concept/CONCEPT_TYPE) => 263)))

(def w-color {:label "color"})
(def w-blue {:label "blue" :parent "color"})
(def w-red {:label "red" :parent "color"})
(def w-black {:label "black" :parent "color"})


(def w-spoke {:label "spoke"
              :description "Concrete entity used to support a radial thing"})

(def w-saddle {:label "saddle"})

(def w-frame {:label "frame"})

(def w-light {:label "light"})

(def w-rim {:label "rim"})

(def w-carrier {:label "carrier"
                :description "Concrete entity used to carry anything"})

(def w-wheel {:label "wheel"
              :slots [{:card 1 :role "rim" :played-by "rim"}
                      {:card {:min 1} :role "spoke" :played-by "spoke"}]})
(def w-city-wheel {:label "city-wheel" :parent "wheel"})
(def w-sports-wheel {:label "sports-wheel" :parent "wheel"})
(def w-race-wheel {:label "race-wheel" :parent "sports-wheel"})

(def w-bike {:label "bike"
             :description "Bike is a concrete entity used as vehicle"
             :slots [{:card 1 :role "frame" :played-by "frame"}
                     {:card 1 :role "saddle" :played-by "saddle"}
                     {:card 1 :role "front-wheel-role"
                      :role-holder "front-weel" :played-by "wheel"
                      :slots [{:card 1 :role "light-role"
                               :role-holder "front-light" :played-by "light"}
                              {:card 1 :role "rim" :played-by "rim"
                               :parent {:concept "wheel" :role "rim"}}]}
                     {:card 1 :role "rear-wheel-role"
                      :role-holder "rear-wheel" :played-by "wheel"}
                     {:card 1 :role "size" :kind "a/o" :played-by "string"}
                     {:card 1 :role "body-color"
                      :kind "a/o" :played-by "color"}]})

(def w-sport-bike {:label "sport bike" :parent "bike"
                   :slots [{:card 1 :role "front-wheel-role"
                            :parent {:concept "bike" :role "front-wheel-role"}
                            :played-by "sports-wheel"}
                           {:card 1 :role "rear-wheel-role"
                            :parent {:concept "bike" :role "rear-wheel-role"}
                            :played-by "sports-wheel"}]})

(def w-city-bike {:label "city bike" :parent "bike"
                  :slots [{:card {:max 1} :role "carrier" :played-by "carrier"}
                          {:card 1 
                           :parent {:concept "bike" :role "front-wheel-role"}
                           :played-by "city-wheel"}
                          {:card 1 
                           :parent {:concept "bike" :role "rear-wheel-role"}
                           :played-by "city-wheel"}]})

;(facts "About operations with slots"
;  (let [ont (create-ontology)]
;    (fact "Validate creation of part-of"
;      (let [person (create-whole-concept ont {:label "person"})
;            school (create-whole-concept ont {:label "school"})
;            teacher-slot (create-slot school {:kind "p/o" ;; "p/i" also be a kind of slot participate-in
;                                              :card {:min 1}
;                                              :role "teacher role"
;                                              :played-by "person"})
;            student-slot (create-slot school {:role "student role"
;                                              :played-by "person"})]
;        (.size (.getPartOfSlotList school)) => 2
;        (let [school (.findConceptByLabel ont "school" 0)]
;          (set (map #(.getRoleName %1)
;                    (into [] (.getAllExtendedSlotList school))))
;          => #{"teacher role" "student role"}
;          (.size (.findSlotsByRoleName school "student role")) => 1
;          (let [teacher (.get (.findSlotsByRoleName school "teacher role") 0)]
;            (.isPartOfSlot teacher) => true
;            (.getSlotCardinality teacher) => "1.."
;            (Slot/getMinimumSlotCardinality teacher) => 1
;            (Slot/getMaximumSlotCardinality teacher) => Slot/MAX_SIZE
;            (.getConstraintConcept teacher) => person)
;          (.size (.findSlotsByRoleName school "student role")) => 1
;          (let [student (.get (.findSlotsByRoleName school "student role") 0)]
;            (.getSlotKind student) => "p/o"
;            (.getSlotCardinality student) => ""
;            (Slot/getMinimumSlotCardinality student) => -1
;            (Slot/getMaximumSlotCardinality student) => 0))))
;    (fact "Validate creation of attribute-of slots"
;      (let [school (.findConceptByLabel ont "school" WholeConcept/CONCEPT_TYPE)
;            short-name-slot (create-slot school {:kind "a/o"
;                                                 :card 1
;                                                 :role "short-name"
;                                                 :played-by "string"})]
;        (.size (.getSlotList school)) => 3
;        (.size (.findSlotsByRoleName school "short-name")) => 1
;        (let [s-n (.get (.findSlotsByRoleName school "short-name") 0)]
;          (.isAttributeOfSlot s-n) => true
;          (.getSlotCardinality s-n) => "1"
;          (Slot/getMinimumSlotCardinality s-n) => 1
;          (Slot/getMaximumSlotCardinality s-n) => 1
;          (instance? StringValueConcept (.getConstraintConcept s-n)) => true)))
;    (fact "Validate creation of specialization (override) slots  "
;      (let [h-school (create-whole-concept ont {:label "high-school"
;                                                :parent "school"})
;            learner-s (create-slot h-school {:parent {:concept "school"
;                                                      :role "student role"}
;                                             :role "learner role"
;                                             :role-holder "learner"})
;            h-teacher-s (create-slot h-school {:parent {:concept "school"
;                                                        :role "teacher role"}
;                                               :role "high-teacher role"
;                                               :card {:min 1 :max 10}
;                                               :role-holder "high-teacher"})]
;        (.getRoleHolderName learner-s) => "learner"
;        (.getRoleName (.getInheritedSlot learner-s)) => "student role"
;        (.getRoleHolderName h-teacher-s) => "high-teacher"
;        (.getSlotCardinality h-teacher-s) => "1..10"
;        (.getRoleName (.getInheritedSlot h-teacher-s)) => "teacher role"
;        (set (map #(.getLabel (.getRoleConcept %1))
;                  (into [] (.getAllSlotList h-school))))
;        => #{"short-name" "high-teacher role" "learner role"}))
;    (fact "Validate creation of specialization (override) slots two levels"
;      (let [pos-school (create-whole-concept ont {:label "pos-school"
;                                                  :parent "high-school"})
;            pos-student-s (create-slot pos-school {:parent {:concept "high-school"
;                                                            :role "learner role"}
;                                                   :role-holder "pos-student"})
;            br-pos-school (create-whole-concept ont {:label "br-pos-school"
;                                                     :parent "pos-school"})
;            doc-teacher-s (create-slot br-pos-school
;                                       {:parent {:concept "high-school"
;                                                 :role "high-teacher role"}
;                                        :role "posgrad-teacher role"
;                                        :card {:min 1}
;                                        :role-holder "posgrad-teacher"})
;            eu-pos-school (create-whole-concept ont {:label "eu-pos-school"
;                                                     :parent "eu-school"})
;            phd-teacher-s (create-slot eu-pos-school
;                                       {:parent {:concept "high-school"
;                                                 :role "high-teacher role"}
;                                        :role "phd-teacher role"
;                                        :card {:min 10}
;                                        :role-holder "phd-teacher"})]
;        (.getRoleName pos-student-s) => "learner role"
;        (.getRoleHolderName pos-student-s) => "pos-student"
;        (Slot/getMinimumSlotCardinality doc-teacher-s) => 1
;        (Slot/getMaximumSlotCardinality doc-teacher-s) => Slot/MAX_SIZE
;        (Slot/getMinimumSlotCardinality phd-teacher-s) => 10
;        (Slot/getMaximumSlotCardinality phd-teacher-s) => Slot/MAX_SIZE
;        (Slot/isSameInheritedSlot doc-teacher-s phd-teacher-s) => true
;        (set (map #(.getRoleName %1)
;                  (into [] (.getAncestorSlotList doc-teacher-s))))
;        => (set (map #(.getRoleName %1)
;                     (into [] (.getAncestorSlotList phd-teacher-s))))))))
;
;(facts "About create a new WholeConcept in an ontology"
;  (let [ont (create-ontology)]
;    (fact "Validate simple concept without slot"
;      (let [t WholeConcept/CONCEPT_TYPE
;            color (create-whole-concept ont w-color)
;            rim (create-whole-concept ont w-rim)
;            saddle (create-whole-concept ont w-saddle)
;            frame (create-whole-concept ont w-frame)
;            light (create-whole-concept ont w-light)
;            carrier (create-whole-concept ont w-carrier)
;            spoke (create-whole-concept ont w-spoke)]
;        (.getConceptCount ont t) => 7
;        (.getLabel (.findConceptByLabel ont "color" t)) => "color"
;        (.getLabel (.findConceptByLabel ont "rim" t)) => "rim"
;        (.getLabel (.findConceptByLabel ont "saddle" t)) => "saddle"
;        (.getLabel (.findConceptByLabel ont "frame" t)) => "frame"
;        (.getLabel (.findConceptByLabel ont "light" t)) => "light"
;        (.getDescription (.findConceptByLabel ont "carrier" t))
;        => (:description w-carrier)
;        (.getDescription (.findConceptByLabel ont "spoke" t))
;        => (:description w-spoke)))
;    (fact "Validate a concept with simple slots"
;      (let [wheel (create-whole-concept ont w-wheel)]
;        (.getLabel wheel) => "wheel"
;        (.size (.getPartOfSlotList wheel)) => 2))
;    (fact "Validate a set of sub-concepts without slots"
;      (let [wheel (.findConceptByLabel ont "wheel" WholeConcept/CONCEPT_TYPE)
;            city-wheel (create-whole-concept ont w-city-wheel)
;            sports-wheel (create-whole-concept ont w-sports-wheel)
;            race-wheel (create-whole-concept ont w-race-wheel)
;            color (.findConceptByLabel ont "color" WholeConcept/CONCEPT_TYPE)
;            blue (create-whole-concept ont w-blue)
;            red (create-whole-concept ont w-red)
;            black (create-whole-concept ont w-black)]
;        (into [] (.getAncestorConceptList city-wheel true)) => [wheel]
;        (into [] (.getAncestorConceptList sports-wheel true)) => [wheel]
;        (into [] (.getAncestorConceptList race-wheel true))
;        => [wheel sports-wheel]
;        (into [] (.getAncestorConceptList race-wheel false))
;        => [sports-wheel wheel]
;        (set (map #(.getLabel %1) (into [] (.getDescendantConceptList color))))
;        => (set ["blue" "red" "black"])))
;    (fact "Validate concepts with complex structure"
;      (let [bike (create-whole-concept ont w-bike)
;            s-bike (create-whole-concept ont w-sport-bike)
;            c-bike (create-whole-concept ont w-city-bike)]
;        (.getSlotSize bike) => 6
;        (set (map #(.getRoleName %1) (into [] (.getAllExtendedSlotList bike))))
;        => #{"frame" "saddle" "front-wheel-role" "size" "body-color"
;             "rear-wheel-role"}
;        (.getSlotSize s-bike) => 2
;        (.getUpperConcept s-bike) => bike
;        (set (map #(.getRoleName %1)
;                  (into [] (.getAllSlotList s-bike))))
;        => #{"frame" "saddle" "front-wheel-role"
;             "size" "body-color" "rear-wheel-role"}
;        (.getInheritedSlot
;          (.get (.findSlotsByRoleName s-bike "front-wheel-role") 0))
;        => (.get (.findSlotsByRoleName bike "front-wheel-role") 0)
;        (.getSlotSize c-bike) => 3
;        (.getUpperConcept c-bike) => bike        
;        (set (map #(.getLabel (.getRoleConcept %1))
;                  (into [] (.getAllSlotList c-bike))))
;        => #{"frame" "saddle" "front-wheel-role"
;             "size" "body-color" "carrier" "rear-wheel-role"}
;        (.getInheritedSlot
;          (.get (.findSlotsByRoleName c-bike "rear-wheel-role") 0))
;        => (.get (.findSlotsByRoleName bike "rear-wheel-role") 0)))))
;
;(facts "About list operations in an ontology and its intances"
;  (let [bike-ont (load-ontology "dev-resources/BIKE_sample.xml")]
;    (fact "Validate list whole-concepts"
;      (let [w-concepts (list-whole-concepts bike-ont)]
;        (count w-concepts) => 15
;        (some #(= "Vehicles" (get %1 :label)) w-concepts) => true
;        (some #(= "CityCycle" (get %1 :label)) w-concepts) => true
;    ;; (TODO) find a method to compare simple text written by user and the list
;;     (some #(= {:concept "Bicycle"
;;               :slots [{:card "2", :kind "p/o", :played-by "Pedal", :role "Pedal", :role-holder ""}
;;                       {:card "1", :kind "p/o", :played-by "Saddle", :role "Saddle", :role-holder ""}]} %) w-concepts) => true
;      ))
;    (fact "Validate list intances"
;      (let [bike-mod (load-model "dev-resources/BIKE_sample_ins.xml")
;            instances (list-instances bike-mod "concept" "Bicycle")]
;        (get-in (meta (first instances)) [:reference :label]) => "Bicycle"
;        (count instances) => 1))
;    (fact "Validate instance"
;      (let [bike-mod (load-model "dev-resources/BIKE_sample_ins.xml")
;            instance (get-instance bike-mod "CI00000005")]
;        (:label instance) => "Wheel_1"
;        (:label (get-instance bike-mod "CI00000020")) => "Bicycle_1")
;          )
;    )
;)
;
;
;; (TODO) translate and clj idiomatic
;; pseudo-algoritm to select proper player roles, given a set of learners
;(defn pseudo-select-scenarios [psycho-needs mot-stage] 
;  (filter
;    (fn [scenario]
;        (filter
;          (fn [mot-strategy]
;            (filter (fn [ind-goal]
;                      (or (containts? (.getRoleConceptChainByNames "initial stage" ind-goal) #{psycho-needs})
;                          (containts? (.getRoleConceptChainByNames "initial stage" ind-goal) #{mot-stage})))
;              (vec (map .getRoleConceptsChainByNames mot-strategy ["I-mot goal(I)"] [] true))))
;          (vec (map .getHozoObject (.getRoleConceptsChainByNames scenario ["Motivational Strategy"] [] true)))))
;    (vec (map .getHozoObject (.getRootResults (.getSubConceptsByLabel hz-res ont "Gamified CL Scenario" true)))))
;)

(facts "About retrieving HozoOntologyObjects"
  (let [ont "dev-resources/BIKE_sample.xml"
        search-args {:search {:label "Human" :type :whole-concept}}]
    (fact "Using param :search with values in :label and :type"
      (let [hz-humans (retrieve ont search-args)]
        (seq? hz-humans) => true
        (count hz-humans) => 1
        (:label (first hz-humans)) => "Human"))
    (fact "Using params :search, :only-one and :full-info"
      (let [args (merge search-args {:only-one true :full-info true})
            hz-human (retrieve ont args)]
        (map? hz-human) => true
        (count (:slots hz-human)) => 5))
    (fact "Using string as FilePath of an ontology"
      (let [args (merge search-args {:only-one true})
            hz-expected-human (retrieve ont args)
            hz-obtained-human (retrieve "dev-resources/BIKE_sample.xml" args)]
        hz-obtained-human => hz-expected-human))
    (fact "Passing args as positional"
      (let [args (merge search-args {:only-one true})
            hz-expected-human (retrieve ont args)
            hz-obtained-human (retrieve ont {:label "Human" :type :concept} true)]
        hz-obtained-human => hz-expected-human))
    (facts "From multiples ontologies"
      (let [args (merge search-args {:only-one false})
            hz-humans (retrieve [ont "dev-resources/travelling.xml"] args)]
        (count hz-humans) => 2))))

(facts "About retrieve HozoModelObjects"
  (let [mdl "dev-resources/BIKE_sample_ins.xml"]
    (fact "Using param :search with value in :ontology-object"
      (let [args {:search {:ontology-object {:label "Bicycle" :type :concept}}}
            hz-ins-bikes (retrieve mdl args)]
        (seq? hz-ins-bikes) => true
        (count hz-ins-bikes) => 2))
    (fact "Using param :search, :full-info and :only-one"
      (let [args {:search {:ontology-object {:label "MountainBike"
                                             :type :whole-concept}}
                  :full-info true :only-one true}
            hz-ins (retrieve mdl args)]
        (:label hz-ins) => "MountainBike_1"
        (:type hz-ins) => :basic-concept-instance
        (:label (:ontology-object hz-ins)) => "MountainBike"
        (:type (:ontology-object hz-ins)) => :whole-concept
        (count (:slots hz-ins)) => 7))
    (fact "Using param :search and :full-instances"
      (let [args {:search {:ontology-object {:label "Bicycle"
                                             :type :whole-concept}}
                  :full-info false :full-instances true}
            hz-ins (retrieve mdl args)]
        (seq? hz-ins) => true
        (count hz-ins) => 3))))

(facts "About create HozoModelObjects"
  (let [mdl "dev-resources/BIKE_sample_ins.xml"]
    (fact "Using only :label and :ontology-object"
      (let [ont-obj {:label "Bicycle" :type :whole-concept}
            ins (create mdl {:label "new-bike0" :ontology-object ont-obj} true)]
        (nil? (:id ins)) => false
        (:label ins) => "new-bike0"
        (:type ins) => :basic-concept-instance))
    (fact "Using :create and :full-info :only-one as params"
      (let [ont-obj {:label "MountainBike" :type :whole-concept}
            args {:create {:label "mountain bike 01" :ontology-object ont-obj}
                  :full-info true :only-one true}
            ins (create mdl args)]
        (nil? (:id ins)) => false
        (:label ins) => "mountain bike 01"
        (nil? (:id (:ontology-object ins))) => false
        (:label (:ontology-object ins)) => "MountainBike"))
    (fact "Using positional arguments"
      (let [ont-obj {:label "Human" :type :whole-concept}
            ins (create mdl {:label "new human 01"
                             :ontology-object ont-obj} true false)]
        (nil? (:id ins)) => false
        (:label ins) => "new human 01"
        (:type ins) => :basic-concept-instance))))


