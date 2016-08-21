(ns br.usp.icmc.caed.cljhozo.core
  (:require [clojure.string :as string]
            [br.usp.icmc.caed.cljhozo.util :as util]
            [br.usp.icmc.caed.cljhozo.values :as values])
  (:import [jp.hozo.core HozoObject HozoOntologyObject HozoModelObject
                         RelationConcept PartialConceptStructure
                         Ontology Model Concept Relation
                         BasicConcept WholeConcept
                         Slot RoleHolder RoleConcept
                         RelationInstance 
                         RConceptRelationInstance
                         ConceptInstance BasicConceptInstance SlotInstance
                         RoleHolderInstance RoleConceptInstance Term]
           [jp.hozo.reasoner HZReasonerFactory]
           [jp.hozo.core.util OntologyUtility SlotUtility ModelUtility]
           [jp.hozo.corex OntologyFactory ModelFactory HZOntology HZModel
                          HZBasicConceptInstance]
           [jp.hozo.corex.util HZConceptUtility]))

;; 
(defn create-ontology
  "Create a new ontology using HOZO"
  [] (HZOntology.))

(defn load-ontology
  "Load an ontology development in HOZO"
  ([^String src]
   (OntologyFactory/createHZOntology src))
  ([^String src parser]
   (OntologyFactory/createHZOntology src parser)))

(defn create-model
  "Create a new model to deal with instances in HOZO"
  ([& ontologies]
   (let [model (ModelFactory/createDefaultModel)]
     (doseq [x ontologies] (.addReferenceOntology model x)) model)))

(defn load-model
  "Load a model that deals with instances in HOZO"
  ([^String src] (ModelFactory/createHZModel src))
  ([^String src parser] (ModelFactory/createHZModel src parser)))

(defn save-model
  "Save a model that deals with instances in HOZO"
  ([^HZModel mdl]
   (.save mdl))
  ([^HZModel mdl ^String src]
   (.setSource mdl src) (.save mdl))
  ([^HZModel mdl ^String src ^String encoding]
   (.setSource mdl src) (.save mdl encoding)))

;; Functions associate to "create" function in CRUD

;(defn- create-whole-name [ont w-name]
;  (OntologyUtility/createNewConceptName ont w-name WholeConcept/CONCEPT_TYPE))
;
;(defn- create-role-name [ont r-name] 
;  (OntologyUtility/createNewConceptName ont r-name RoleConcept/CONCEPT_TYPE))
;
;(defn- create-role-holder-name [ont rh-name]
;  (OntologyUtility/createNewConceptName ont rh-name RoleHolder/CONCEPT_TYPE))
;
;(defn- whole-concept-args-to-map
;  "Converts a concept's argument into a map. The arguments are either
;  positional, named, or already in map. This function is a fixpoint"
;  [[f & r :as args]]
;  (cond (and (map? f)
;             (= 1 (count args))
;             (every? #{:parent :label :description :slots} (keys f))) f
;        (keyword? f) (apply map args)
;        ;; walk down the arguments and pull them out positionally
;        :default
;          (let [[parent a] (util/check-arg #(or (string? %1)
;                                           (instance? WholeConcept %1)) args)
;                [label a] (util/check-arg string? args)
;                [description a] (util/check-arg string? a)
;                [slots a] (util/check-arg sequential? a)]
;            (->> {:parent parent :label label
;                  :description description :slots slots}
;                 (filter second) (into {})))))
;
;(defn create-slot
;  "Create a new slot part-of for a BasicConcept of an ontology
;  - concept: The concept of an ontology
;  - slot: The map that describes a slot using the keys: kind, role,
;  role-holder, card, played-by, and value. The optional key :kind is used
;  to identify the kind as part-of (p/o) or attribute-of (a/o)"
;  [concept slot]
;  (let [ont (.getOntology concept)]
;    (if (contains? slot :parent)
;      ;; create a specialization of slot (override slot)
;      (let [p-slot (.get (.findSlotsByRoleName
;                           (.findConceptByLabel
;                             ont
;                             (get (get slot :parent) :concept)
;                             Concept/CONCEPT_TYPE)
;                           (get (get slot :parent) :role)) 0)
;            o-slot (HZConceptUtility/createOverrideSlot concept p-slot
;                     false  ;; TODO: try to put it as parameter slots-of-slots
;                     false)]
;        (if (contains? slot :kind)
;          (.setSlotKind o-slot (get slot :kind)))
;        (if (and (contains? slot :role)
;                 (not (= (get slot :role) (.getRoleName p-slot))))
;          (.setRoleName o-slot (create-role-name ont (get slot :role))))
;        (if (and (contains? slot :role-holder)
;                 (not (= (get slot :role-holder) (.getRoleHolderName p-slot))))
;          (.setRoleHolderName
;            o-slot (create-role-holder-name ont (get slot :role-holder))))
;        (if (contains? slot :card)
;          (.setSlotCardinality
;            o-slot ((fn [x] (if-not (map? x) (str x)
;                              (SlotUtility/createSlotCardinality
;                                (str (get x :min "")) (str (get x :max "")))))
;                    (get slot :card))))
;        (if (contains? slot :played-by)
;          (do (.setSlotConstraintConceptLabel o-slot (get slot :played-by))
;              (if-not (OntologyUtility/setConstraintConceptByLabel
;                        ont o-slot (get slot :played-by))
;                ;(let [const-concept (create-whole-concept
;                ;                      {:label (get slot :played-by)})]
;                ;;TODO (.setConstraintConcept o-slot const-concept)
;                  (print "TODO: this part must be throw and expection") ;)
;                )))
;        (if (contains? slot :value)
;          (.setConstraintValue o-slot (get slot :value)))
;        o-slot)
;      ;; create a normal slots of kind attribute a/o or part-of p/o
;      (let [role (create-role-name ont (get slot :role))
;            played-by (get slot :played-by)
;            role-holder (create-role-holder-name ont (get slot :role-holder ""))
;            card ((fn [x] (if-not (map? x) (str x)
;                            (SlotUtility/createSlotCardinality
;                              (str (get x :min "")) (str (get x :max "")))))
;                  (get slot :card ""))
;            value (get slot :value "")]
;        (if-not (= "a/o" (get slot :kind))
;          (HZConceptUtility/createPartOfSlot concept role
;                                             role-holder card played-by value)
;          (HZConceptUtility/createAttributeOfSlot concept role role-holder
;                                                  card played-by value))))))
;
;(defn create-whole-concept
;  "Create a new WholeConcept in an ontology
;  - ont: The ontology where this concept will be stored
;  Remaining argument are optional. They may be positional args, or a map using
;  the keys: parent, label, description and slots. Positional arguments:
;  - parent: The label for this concept (String, or WholeConcept).
;  - label: The label for this concept (String).
;  - description: The description for this concept (String).
;  - slots: The sequence of slots that play roles in concept (Map)."
;  [ont & args]
;  (let [args (whole-concept-args-to-map args)
;        parent (get args :parent)
;        label (create-whole-name ont (get args :label))
;        description (get args :description)
;        slots (get args :slots)
;        whole-concept (.createWholeConcept ont label description)]
;    (if-not (nil? parent)
;        (.createIsaRelation ont (if (instance? WholeConcept parent) parent
;                                  (.findWholeConceptByLabel ont parent))
;                            whole-concept))
;    (if-not (nil? slots)
;      (doseq [s slots] (create-slot whole-concept s)))
;    whole-concept))

;; Functions associate to "retrieve" function in CRUD

(defn- retrieve-list-ont-obj-by-type
  "Internal function to list HozoOntologyObjects from an Ontology"
  ([^Ontology ont] (seq (.getAllOntologyObjects ont)))
  ([^Ontology ont t]
    (let [t (if (keyword? t) t (keyword t))
          p-result (cond
                     (isa? util/h-types t :whole-concept)
                     (seq (.getConcepts ont WholeConcept/CONCEPT_TYPE))
                     (isa? util/h-types t :relation-concept)
                     (seq (.getConcepts ont RelationConcept/CONCEPT_TYPE))
                     (isa? util/h-types t :role)
                     (seq (.getRoleConcepts ont))
                     (isa? util/h-types t :role-holder)
                     (seq (.getRoleHolders ont))
                     (isa? util/h-types t :concept)
                     (seq (.getConcepts ont))
                     (isa? util/h-types t :slot)
                     (seq (.getSlots ont))
                     (isa? util/h-types t :relation)
                     (seq (.getRelations ont))
                     :else (retrieve-list-ont-obj-by-type ont))]
      (if (.contains [:whole-concept :relation-concept :role
                      :role-holder :concept :slot :relation] t) p-result
        (filter #(= (util/keyword-class %) t) p-result)))))

(defn- retrieve-list-mdl-obj-by-type
  "Internal function to list HozoModelObjects from a Model"
  ([^Model mdl] (seq (.getAllInstances mdl)))
  ([^Model mdl t]
    (let [t (if (keyword? t) t (keyword t))
          p-result (cond
                     (isa? util/h-types t :role-instance)
                     (seq (.getRoleConceptInstances mdl))
                     (isa? util/h-types t :concept-instance)
                     (seq (.getConceptInstances mdl))
                     (isa? util/h-types t :slot-instance)
                     (seq (.getSlotInstances mdl))
                     (isa? util/h-types t :relation-instance)
                     (seq (.getRelationInstances mdl))
                     :else (retrieve-list-mdl-obj-by-type mdl))]
      (if (.contains [:role-instance :concept-instance
                      :slot-instance :relation-instance] t) p-result
        (filter #(= (util/keyword-class %) t) p-result))))
  ([^Model mdl ^Concept c full-instances]
   (if full-instances (seq (.findAllInstancesByConcept mdl c))
     (seq (.findInstanceByConcept mdl c)))))

(defn- retrieve-ont-obj
  "Internal function to retrieve HozoOntologyObjects from an Ontology.
  In the search, :upper-concept must be have {:id :root} to get root concepts,
  otherwise the query returns a list of is-a concepts (.getLowerConcepts)
  By default, it returns one list - set true the 3rd arg to get only one obj"
  [^Ontology ont search & [only-one]]
  (if (nil? search)
    (throw (Exception. "Search param can't be nil in retrieve-ont-obj")))
  (let [objs (if-not (nil? (:upper-concept search))
               (let [t (keyword (or (:type (:upper-concept search))
                                    (:type search)))]
                 (if (and (= (:id (:upper-concept search)) :root)
                          (isa? util/h-types t :concept))
                   (.getRootConcept ont (util/keyword-int t))
                   (let [upper (retrieve-ont-obj ont
                                                 (merge (:upper-concept search)
                                                        (if-not (nil? t)
                                                          {:type t})) true)]
                     (if-not (nil? upper)
                       (seq (.getLowerConceptList upper))))))
               (if-not (nil? (:type search))
                 (retrieve-list-ont-obj-by-type ont (:type search))
                 (retrieve-list-ont-obj-by-type ont)))]
    (loop [[obj & r-objs] objs result (list)]
      (if (nil? obj) (if (and (empty? result) only-one) nil result)
        ;; (TODO) complete by conditions in :slots, :relations, and others
        (if (and (if (empty? (:id search)) true
                   (= (:id search) (.getId obj)))
                 (if (empty? (:label search)) true
                   (= (:label search) (.getLabel obj))))
          (if only-one obj
            (recur r-objs (concat result (list obj))))
          (recur r-objs result))))))

(defn- retrieve-mdl-obj
  "Internal function to retrive HozoModelObjects from a Model.
  By default, it returns one element - to obtain a list use the 3rd arg"
  [^Model mdl search & [only-one & [full-instances]]]
  (let [concept (if-not (nil? (:ontology-object search))
                  (retrieve-ont-obj (.getBaseOntology mdl)
                                    (:ontology-object search) true))
        otype (condp #(isa? util/h-types %2 %1)
                (get-in search [:ontology-object :type])
                :role :role-instance
                :concept :concept-instance
                :slot :slot-instance
                :relation :relation-instance
                (:type search))
        objs (if (instance? Concept concept)
               (retrieve-list-mdl-obj-by-type mdl concept full-instances)
               (if-not (nil? otype)
                 (retrieve-list-mdl-obj-by-type mdl otype)
                 (retrieve-list-mdl-obj-by-type mdl)))]
    (loop [[obj & r-objs] objs result (list)]
      (if (nil? obj) (if (and (empty? result) only-one) nil result)
        ;; (TODO) complete by conditions in :slots, :relations, and others
        (if (and (if (empty? (:id search)) true
                   (= (:id search) (.getId obj)))
                 (if (empty? (:label search)) true
                   (= (:label search) (.getLabel obj)))
                 (if (instance? Concept concept) true
                   (and (if (empty? (:id (:ontology-object search))) true
                          (= (:id (:ontology-object search))
                             (.getId (.getOntologyObject obj))))
                        (if (empty? (:label (:ontology-object search))) true
                          (= (:label (:ontology-object search))
                             (.getLabel (.getOntologyObject obj)))))))
          (if only-one obj
            (recur r-objs (concat result (list obj))))
          (recur r-objs result))))))

(defn- retrieve-args-to-map
  "Internal function to convets arguments of retrieve function into a map"
  [[f & r :as args]]
  (cond (and (map? f) (= 1 (count args))
             (every? #(contains? f %) [:search])) f
        (keyword? f) (apply map args)
        ;; walk down the arguments and pull them out positionally
        :default (let [[search a] (util/check-arg map? args)
                       [only-one a] (util/check-arg #(or (= % true) (= % false)) a)
                       [as-hz-objects a] (util/check-arg #(or (= % true) (= % false)) a)
                       full-instances (boolean (first a))]
                   (->> {:search search
                         :only-one (boolean only-one)
                         :full-info (boolean as-hz-objects)
                         :full-instances full-instances}
                        (filter second) (into {})))))

(defn retrieve
  "Function to retrieve HozoObjects from a set of ontologies/models
  - sources: Ontologies/Models as Objects or FilePaths to search
  Remaining arguments are optional. They may be positional or a map:
  :search - map with the definition of HozoObjects to search
  :only-one - boolean with true to return only the first HozoObject
  :full-info - boolean with true to obtain a complete information of HozoObject
               (only a label and description are included by default)
  :full-instances - boolean with true to include instances of subconcepts
  Note: The :full-instances are only valid to retrieve data from models
  If :only-one and :full-info are true the concept show inherance slots"
  [sources & args]
  (let [sources (if-not (sequential? sources) (list sources) sources)
        args (retrieve-args-to-map args)]
    (loop [[src & r-sources] sources result (list)]
      (if (nil? src) result
        (let [[is-ont ont-mdl] (cond (instance? Model src) [false src]
                                     (instance? Ontology src) [true src]
                                     (ModelUtility/isModelFile src)
                                     [false (load-model src)]
                                     (OntologyUtility/isOntologyFile src)
                                     [true (load-ontology src)])
              objs (if is-ont
                     (retrieve-ont-obj ont-mdl (:search args) (:only-one args))
                     (retrieve-mdl-obj ont-mdl (:search args) (:only-one args)
                                       (:full-instances args)))]
          (if (and (:only-one args) (not (nil? objs)))
            (if (:full-info args)
              (values/convert objs true)
              (values/convert-hz-object objs))
            (recur r-sources
                   (if (:full-info args)
                     (concat result (map values/convert objs))
                     (concat result (map values/convert-hz-object objs))))))))))

;; Functions associate to create function in CRUD

(def create-concept-instance)

(defn- get-concept-instance-label
  "Internal function to get a validated label for a concept instance. If this
  label exists then a new valid label will be created adding a number as suffix"
  ([^Model mdl ^Concept c]
   (let [lbl (str (.getLabel c) "_Ins_"
                  (+ (.size (.findInstanceByConcept mdl c)) 1))]
     (get-concept-instance-label mdl c lbl)))
  ([^Model mdl ^Concept c lbl]
   (if (or (nil? lbl) (empty? (string/trim lbl)))
     (get-concept-instance-label mdl c)
     ((fn [c-lbl idx]
        (if-not (.isOverlappingLabel mdl c-lbl) c-lbl
          (recur (str lbl "_" idx) (+ idx 1)))) lbl 1))))

(defn- create-missing-slots-slot-instance
  "Internal function that creates missing slots of slots for an instance"
  ([^Model mdl ^SlotInstance slot-ins]
   (create-missing-slots-slot-instance mdl slot-ins (.getReferenceSlot slot-ins)))
  ([^Model mdl ^SlotInstance slot-ins ^Slot slot]
   (if (nil? slot) slot-ins
     (do
       (doseq [chi-slot (.getEssentialSlotList slot false)]
         (dotimes [n (- (Slot/getMinimumSlotCardinality chi-slot)
                        (.getSlotInstanceSize slot-ins slot))]
           (let [ins-constr (create-concept-instance mdl (.getConstraintConcept chi-slot))
                 new-slot-ins (create-missing-slots-slot-instance
                                mdl
                                (doto (SlotInstance. slot-ins)
                                      (.setReferenceSlot chi-slot)
                                      (.setConstraintConceptInstance ins-constr))
                                chi-slot)]
             (.addSlotInstance slot-ins new-slot-ins))))
         slot-ins))))

(defn- create-missing-slots-instance
  "Internal function that creates missing slots for an instance"
  ([^Model mdl ^ConceptInstance ins]
   (create-missing-slots-instance mdl ins (.getReferenceConcept ins)))
  ([^Model mdl ^ConceptInstance ins ^Concept c]
   (if (nil? c) ins
     (do
       (doseq [slot (.getEssentialSlotList c false)]
         ;; TODO When getEssentialSlotList obtains detailed information (true),
         ;; the HZ API returns all slots from ancents non-making diferentiation
         ;; in the overriden slots in ancents, the differentiation is only done
         ;; when the overriden slots is explict defined in the concept c
         (dotimes [n (- (Slot/getMinimumSlotCardinality slot)
                        (.getSlotInstanceSize ins slot))]
           (let [ins-constr (create-concept-instance mdl (.getConstraintConcept slot))
                 new-ins-slots (create-missing-slots-slot-instance
                                 mdl
                                 (doto (SlotInstance. ins)
                                       (.setReferenceSlot slot)
                                       (.setConstraintConceptInstance ins-constr))
                                 slot)]
             (.addSlotInstance ins new-ins-slots))))
         ins))))

(defn- create-concept-instance-args-to-map
  "Converts argument for create-concept-instance into a map. The arguments are
  either positional, named, or already in map form."
  [[f & r :as args]]
  (cond (and (map? f) (= 1 (count args))) f
        (keyword? f) (apply map args)
        ;; walk down the arguments and pull them out positionally
        :default (let [[label a] (util/check-arg string? args)
                       [description a] (util/check-arg string? a)
                       [slots a] (util/check-arg sequential? a)]
                   (->> {:label label :description description :slots slots}
                        (filter second) (into {})))))

(defn- create-concept-instance
  "Create a new concept instance in a model using as base a HZConcept.
  - mdl: The HZModel where this instance will be stored.
  - c: The HZConcept that references this instance.
  Remaining argument are optional. They may be positional or a map:
  :label - String that defines the label of instance that will be created.
  :description - String that defines the description of instance
  :slots - Sequencial of slots that will be created"
  [^Model mdl ^Concept c & args]
  (let [args (create-concept-instance-args-to-map args)
        label (get-concept-instance-label mdl c (:label args))
        description (get args :description "")
        slots (get args :slots [])
        new-ins (doto (cond (isa? util/h-types
                                  (util/keyword-class c) :role)
                            (RoleConceptInstance. mdl)
                            (isa? util/h-types
                                  (util/keyword-class c) :role-holder)
                            (RoleHolderInstance. mdl)
                            (isa? util/h-types
                                  (util/keyword-class c) :basic-concept)
                            (BasicConceptInstance. mdl)
                            :else (ConceptInstance. mdl))
                      (.setLabel label)
                      (.setDescription description)
                      (.setReferenceConcept c))]
    (.addConceptInstance mdl (create-missing-slots-instance mdl new-ins c))
    new-ins))

(defn- create-args-to-map
  "Internal function to convets arguments of create function into a map"
  [[f & r :as args]]
  (cond (and (map? f) (= 1 (count args))
             (every? #(contains? f %) [:create])) f
        (keyword? f) (apply map args)
        ;; walk down the arguments and pull them out positionally
        :default (let [[create a] (util/check-arg map? args)
                       [only-one a] (util/check-arg #(or (= % true) (= % false)) a)
                       full-info (boolean (first a))]
                   (->> {:create create
                         :only-one (boolean only-one)
                         :full-info (boolean full-info)}
                        (filter second) (into {})))))

(defn create
  "Function to create a HozoObject in an ontology or model
  - dests: Ontology/Model or FilePath where to create the HozoObject
  Remaining arguments are optional. They may be positional or a map:
  :create - map with the definition of HozoObjects to create
  :only-one - boolean with true to create only the first HozoObject
  :full-info - boolean with true to obtain a complete information of HozoObject
               (only a label and description are included by default)
  If :only-one and :full-info are true the concept show inherance slots"
  [dests & args]
  (let [dests (if-not (sequential? dests) (list dests) dests)
        args (create-args-to-map args)]
    (loop [[dest & r-dests] dests result (list)]
      (if (nil? dest) result
        (let [[is-ont ont-mdl] (cond (instance? Model dest) [false dest]
                                     (instance? Ontology dest) [true dest]
                                     (ModelUtility/isModelFile dest)
                                     [false (load-model dest)]
                                     (OntologyUtility/isOntologyFile dest)
                                     [true (load-ontology dest)])
              obj (if is-ont
                    nil ;; (TODO) create function to HozoOntologyObject
                    (let [c (retrieve-ont-obj
                              (.getBaseOntology ont-mdl)
                              (:ontology-object (:create args)) true)]
                      (create-concept-instance ont-mdl c (:create args))))]
          (if (and (:only-one args) (not (nil? obj)))
            (if (:full-info args)
              (values/convert obj true)
              (values/convert-hz-object obj))
            (recur r-dests
                   (if (:full-info args)
                     (concat result (values/convert obj))
                     (concat result (values/convert-hz-object obj))))))))))

;; Functions associate to delete function in CRUD

(defmulti delete-mdl-obj
  "A simple multimethod to delete a HozoObject from a model"
  (fn [^Model mdl obj] (class obj)))

(defn- delete-slot-ins
  "Internal function to delete a list of slot intances"
  [^Model mdl slot-instances]
  (doseq [slot-ins slot-instances]
    (if (not (nil? slot-ins))
      (let [slot-slot-instances (.getSlotInstanceList slot-ins)
            rel-slot-instances (.getRelationInstanceList slot-ins)]
        (delete-slot-ins mdl slot-slot-instances)
        (delete-mdl-obj mdl rel-slot-instances)
        (delete-mdl-obj mdl slot-ins)))))

(defn- delete-con-ins
  "Internal funciton to delete a ConceptInstance in a HZModel"
  [^Model mdl obj]
  (let [slot-instances (.getSlotInstanceList obj)
        rel-instances (.getRelationInstanceList obj)]
    (delete-slot-ins mdl slot-instances)
    (delete-mdl-obj mdl rel-instances)))

(defmethod delete-mdl-obj RoleConceptInstance [^Model mdl obj]
  (do
    (delete-con-ins mdl obj)
    (.removeRoleConceptInstance mdl obj)))

(defmethod delete-mdl-obj ConceptInstance [^Model mdl obj]
  (do
    (delete-con-ins mdl obj)
    (.removeConceptInstance mdl obj)))

(defmethod delete-mdl-obj RConceptRelationInstance [^Model mdl obj]
  (doseq [rs-rel-ins (.getSlotRelationInstanceList obj)]
    (if (.hasConstraintConceptInstance rs-rel-ins)
      (let [const-con-ins (.getConstraintConceptInstance rs-rel-ins)]
        (if (not (nil? const-con-ins))
          (.removeRelationInstance const-con-ins obj)))
      (let [rel-slot-ins (.getRelationSlotInstance rs-rel-ins)]
        (if (not (nil? rel-slot-ins))
          (.removeRelationInstance rel-slot-ins obj))))
    (.removeRelationInstance mdl obj)))

(defmethod delete-mdl-obj RelationInstance [^Model mdl obj]
  (.removeRelationInstance mdl obj))

(defmethod delete-mdl-obj SlotInstance [^Model mdl obj]
  (let [slot-instances (.getSlotInstanceList obj)
        rel-instances (.getRelationInstanceList obj)]
    (delete-slot-ins mdl slot-instances)
    (delete-mdl-obj mdl rel-instances)
    (.removeSlotInstance mdl obj)))

(defmethod delete-mdl-obj java.util.Collection [^Model mdl objs]
  (doseq [obj objs]
    (delete-mdl-obj mdl obj)))

;;

(defn- delete-args-to-map
  "Internal function to convets arguments of delete function into a map"
  [[f & r :as args]]
  (cond (and (map? f) (= 1 (count args))
             (every? #(contains? f %) [:delete])) f
        (keyword? f) (apply map args)
        ;; walk down the arguments and pull them out positionally
        :default (let [[delete a] (util/check-arg map? args)
                       only-one (if-not (nil? (first a)) (first a) true)]
                   (->> {:delete delete
                         :only-one (boolean only-one)}
                        (filter second) (into {})))))

(defn delete
  "Function to delete a HozoObject in an ontology or model
  - dests: Ontology/Model or FilePath where to delete the HozoObject
  Remaining arguments are optional. They may be positional or a map:
  :delete - map with the definition of HozoObjects to delete
  :only-one - boolean with true to delete only the first HozoObject"
  [dests & args]
  (let [dests (if-not (sequential? dests) (list dests) dests)
        args (delete-args-to-map args)]
    (loop [[dest & r-dests] dests result (list)]
      (if (nil? dest) result
        (let [[is-ont ont-mdl] (cond (instance? Model dest) [false dest]
                                     (instance? Ontology dest) [true dest]
                                     (ModelUtility/isModelFile dest)
                                     [false (load-model dest)]
                                     (OntologyUtility/isOntologyFile dest)
                                     [true (load-ontology dest)])
              objs (if is-ont
                     (retrieve-ont-obj ont-mdl (:delete args) (:only-one args))
                     (retrieve-mdl-obj ont-mdl (:delete args) (:only-one args) true))]
          (if (and (:only-one args) (not (nil? objs)))
            (if-not is-ont
              ;; change to if and use delete-ont-obj for OntologyObject
              (let [id (:id objs)]
                (delete-mdl-obj ont-mdl objs) id))
            (if-not is-ont
              ;; change to if and use delete-ont-obj for Ontology Objects
              (let [ids (map :id objs)]
                (delete-mdl-obj ont-mdl objs)
                (recur r-dests (concat result ids))))))))))

;; Functions associate to update function in CRUD

(defn- update-hz-obj
  "Internal function that updates HozoObject using a clj-structure"
  [^HozoObject obj update]
  (let [label (:label update)
        description (:description update)]
    (if-not (nil? label) (.setLabel obj label))
    (if-not (nil? description) (.setDescription obj description))
    obj))

(defn- update-hz-mdl-obj
  "Internal function that updates HozoModelObject using a clj-structure"
  [^HozoModelObject obj update]
  (let [result (update-hz-obj obj update)]
        ;TODO implement update function for ont-obj (.getOntologyObject obj)
        ;ont-obj (:ontology-object update)]
    ;(if-not (nil? ont-obj) {:ontology-object (update-hz-ont-obj ont-obj)})
    result))

(defprotocol HZUpdate
  "A simple protocol to update a HozoObject using a clj-structure"
  (update-by-clj [this update]))

;(defn- update-concept
;  "Internal function to update Concept using a structure for clj"
;  [^Concept obj update]
;  (let [obj (update-hz-obj obj update)
;        ;TODO update using relations (update-relation args), upper- and lower-
;        ;relations (.getRelationList obj)
;        ;upper-concept (.getUpperConcept obj)
;        ;lower-concepts (.getLowerConceptList obj)
;        slots (:slots update)
;        terms (:terms update)
;        ;upper-concept (:upper-concept update)
;        ;lower-concepts (:lower-concepts update)
;        ;slots (if show-upper-slots (.getAllSlotList obj) (.getSlotList obj))
;                ]
;    (if-not (empty? slots)
;      (update-by-clj update))
;    (if-not (empty? terms)
;      (do (doseq [x (.getTerms obj)] (.removeTerm obj x))
;          (doseq [x terms] (.addTerm obj (doto (Term.)
;                                (.setName (:name x))
;                                (.setLabel (:label x))
;                                (.setSymbol (:symbol x))
;                                (.setLanguage (:lang x)))) terms)))
;    obj))

;(defn- update-relation
;  "Internal function to update Relation using a structure for clj"
;  [^Relation obj]
;  (let [result (convert-hz-ont-obj obj)
;        master-label (.getMasterLabel obj)
;        master-concept (.getMasterConcept obj)
;        slave-label (.getSlaveLabel obj)
;        slave-concept (.getSlaveConcept obj)]
;    (merge
;      result
;      (if-not (nil? master-concept)
;        {:master-label master-label
;         :master-concept (convert-hz-ont-obj master-concept)})
;      (if-not (nil? slave-concept)
;        {:slave-label slave-label
;         :slave-concept (convert-hz-ont-obj slave-concept)}))))

(defn- create-update-concept-instance-by-clj
  "Internal function to create or update a ConceptInstance using clj-structure"
  ([^Model mdl clj]
   (let [ont (.getBaseOntology mdl)]
     (create-update-concept-instance-by-clj mdl ont clj)))
  ([^Model mdl ^Ontology ont clj]
   (let [c (retrieve-ont-obj ont (:ontology-object clj) true)]
     (if-not (empty? (:id clj))
       (update-by-clj (retrieve-mdl-obj mdl clj true true) clj)
       (create-concept-instance mdl c clj)))))

(def create-slot-slot-instance)

(defn- int-create-slot-instance
  "Internal function to create a SlotInstance in ins using a clj-structure"
  [^Model mdl ^Ontology ont ^Slot slot ins to-create]
  (let [new-slot-ins (doto (SlotInstance. ins) (.setReferenceSlot slot))
        ;TODO complete the creation including:
        ;depend-on (:depend-on to-create)
        ;relations (.getRelationInstanceList this)
        role (:role to-create)
        role-holder (:role-holder to-create)
        slots (:slots to-create)
        constraint (:constraint to-create) 
        value (:value to-create)]
    (if-not (nil? role)
      (.setRoleConceptInstance
        new-slot-ins
        (create-update-concept-instance-by-clj mdl ont role)))
    (if-not (nil? role-holder)
      (.setRoleHolderInstance
        new-slot-ins
        (create-update-concept-instance-by-clj mdl ont role-holder)))
    (if-not (empty? value) (.setConstraintValue new-slot-ins value))
    (if-not (nil? constraint)
      (.setConstraintConceptInstance
        new-slot-ins
        (create-update-concept-instance-by-clj mdl ont constraint)))
    (if-not (empty? slots)
      (doseq [x slots]
        (.addSlotInstance new-slot-ins
                          (create-slot-slot-instance new-slot-ins x))))
    (.addSlotInstance ins new-slot-ins) new-slot-ins))

(defn- create-slot-slot-instance
  "Internal function to create a SlotInstance using a clj-structure"
  [^SlotInstance ins to-create]
  (let [mdl (.getModel ins)
        ont (.getReferenceOntology ins)
        slot (retrieve-ont-obj ont (:ontology-object to-create) true)
        cur1-slot (.getSlotInstanceSize ins slot)
        cur2-slot (.getSlotInstanceSize ins (.getInheritedSlot slot))
        max-slot (Slot/getMaximumSlotCardinality slot)]
    (if (and (< cur1-slot max-slot) (< cur2-slot max-slot))
      (int-create-slot-instance mdl ont slot ins to-create))))

(defn- create-slot-instance
  "Internal function to create a SlotInstance using a clj-structure"
  [^ConceptInstance ins to-create]
  (let [mdl (.getModel ins)
        ont (.getReferenceOntology ins)
        slot (retrieve-ont-obj ont (:ontology-object to-create) true)
        cur-slot (.getSlotInstanceSize ins slot)
        max-slot (Slot/getMaximumSlotCardinality slot)]
    (if (< cur-slot max-slot)
      (int-create-slot-instance mdl ont slot ins to-create))))

(defn- update-slots-by-clj
  [ins cur-slots slots]
  (if-not (empty? slots)
    (let [to-remove (filter #(loop [[slot & r-slots] slots]
                               (if (nil? slot) true
                                 (if (and (not (empty? (.getId %)))
                                          (not (empty? (:id slot)))
                                          (= (.getId %) (:id slot))) false
                                   (recur r-slots)))) cur-slots)]
      (doseq [x to-remove] (.removeSlotInstance ins x))
      (doseq [slot slots]
        (if (empty? (:id slot))
          (.addSlotInstance ins (create-slot-instance ins slot))
          (.addSlotInstance
            ins
            (loop [[ite-slot & r-slots] cur-slots]
              (if (nil? ite-slot) (create-slot-instance ins slot)
                (if (= (:id slot) (.getId ite-slot))
                  (update-by-clj ite-slot slot)
                  (recur r-slots)))))))
      ins)))

(defn- update-concept-instance 
  "Internal function to update a ConceptInstance using a clj-structure"
  [^ConceptInstance ins update]
  (let [result (update-hz-mdl-obj ins update)
        ;TODO update or create relations 
        ;relations (map convert-hz-object (.getRelationInstanceList ins))
        slots (:slots update)]
    (if-not (empty? slots)
      (update-slots-by-clj result (.getSlotInstanceList result) slots))
    result))

;(defn- update-slot-relation-instance 
;  "Internal function that convert RSlotRelationInstance into a clj"
;  [^RSlotRelationInstance ins show-upper-slots]
;  (let [slot (.getRelationSlotInstance ins)
;        role (.getRelationRoleConceptInstance ins)
;        constraint (.getConstraintConceptInstance ins)
;        label (.getRSRelationInstanceLabel ins)]
;    (with-meta
;      (merge (if-not (empty? label)
;               {:label label})
;             (if-not (nil? slot)
;               {:slot (convert-to-clj slot show-upper-slots)})
;             (if-not (nil? role)
;               {:role (convert-hz-mdl-obj role)})
;             (if-not (nil? constraint)
;               {:constraint (convert-hz-mdl-obj constraint)}))
;      (merge (let [ref-slot (.getReferenceRelationSlot ins)]
;               (if-not (nil? ref-slot)
;                 {:ontology-object (convert-hz-ont-obj ref-slot)}))))))

(extend-protocol HZUpdate
;  P_Operator
;  (update-by-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          part (.getPartOfConcept this)]
;      (merge obj
;             (if-not (nil? part)
;               {:part-of (convert-hz-ont-obj part)}))))
;  
;  R_Operator
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          reg (.getRegionOfConcept this)]
;      (merge
;        obj
;        (if-not (nil? reg)
;          {:region-of (convert-hz-ont-obj reg)}))))
;  
;  ReferenceConcept
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          ori (.getOriginalConcept this)]
;      (merge obj
;             (if-not (nil? ori)
;               {:original (convert-hz-ont-obj ori)}))))
;  
;  RoleConcept
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          dep-slot (.getDependentSlot this)
;          dep-concept (.getDependentConcept this)
;          part-role (.getPartRoleConcept this)
;          constraint (.getConstraintConcept this)]
;      (merge obj
;             (if (or (not (nil? dep-slot)) (not (nil? dep-concept)))
;               (let [depend (if-not (nil? dep-slot)
;                              (convert-hz-ont-obj dep-slot)
;                              (convert-hz-ont-obj dep-concept))]
;                  {:depend-on depend}))
;              (if-not (nil? part-role)
;                {:part-role (convert-hz-ont-obj part-role)})
;              (if-not (nil? constraint)
;                {:constraint (convert-hz-ont-obj constraint)})))) 
;
;  RoleHolder
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          role (.getRoleConcept this)]
;      (merge obj
;             (if-not (nil? role)
;               {:role (convert-hz-ont-obj role)}))))
;  
;  SpeciesConcept
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          ori (.getOriginalConcept this)]
;      (merge obj
;             (if-not (nil? ori)
;               {:original (convert-hz-ont-obj ori)}))))
;
;  ValueConcept
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-concept this show-upper-slots)
;          value (.getValue this)
;          data-type (.getDataType this)]
;      (merge obj
;             (if (or (nil? (:id obj)) (empty? (:id obj)))
;               {:id (case (:type obj)
;                      :boolean-value (str  (:xsd d-ns) "boolean")
;                      :date-value (str (:dc d-ns) "date")
;                      :time-value (str (:xsd d-ns) "dateTime")
;                      :decimal-value (str (:xsd d-ns) "decimal")
;                      :float-value (str (:xsd d-ns) "float")
;                      :integer-value (str (:xsd d-ns) "integer")
;                      :number-value (str (:xsd d-ns) "double")
;                      :string-value (str (:xsd d-ns) "string")
;                      :uri-value (str (:xsd d-ns) "anyURI"))})
;             (if-not (empty? value)
;               {:value value})
;             (if-not (empty? data-type)
;               {:data-type data-type}))))
;  
;  Concept
;  (convert-to-clj [this show-upper-slots]
;    (convert-concept this show-upper-slots))
;  
;  PartialConceptStructure
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-hz-ont-obj this)
;          lang (.getLanguage this)
;          slots (.getSlotList this) 
;          owner (.getOwnerConcept this)
;          partial-structures (.getPartialSlotStructureList this)]
;      (merge obj
;             (if-not (empty? lang)
;               {:lang lang})
;             (if-not (nil? owner)
;               {:owner (convert-hz-ont-obj owner)})
;             (if-not (empty? slots)
;               {:slots (vec (map convert-to-clj
;                                 slots (repeat show-upper-slots)))})
;             (if-not (empty? partial-structures)
;               {:partial-structures
;                 (vec (map #(let [slot (.getSlot %)
;                                  path (.getSlotPath %)]
;                              (merge (if-not (nil? slot) {:slot slot})
;                                     (if-not (empty? path) {:path path})))
;                           partial-structures))}))))
;  
;  AttributeOfRelation
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-relation this)
;          attr-slot (.getAttributeSlot this)
;          attr-slot-size (.getAttributeSlotSize this)]
;      (merge obj
;             (if-not (nil? attr-slot)
;               {:attribute-slot (convert-hz-ont-obj attr-slot)
;                :attribute-slot-size (str attr-slot-size)}))))
;  
;  PartOfRelation
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-relation this)
;          part-slot (.getPartSlot this)
;          part-slot-size (.getPartSlotSize this)]
;      (merge obj
;             (if-not (nil? part-slot)
;               {:part-slot (convert-hz-ont-obj part-slot)
;                :part-slot-size (str part-slot-size)}))))
;  
;  RConceptRelation
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-relation this)
;          relation (.getRelationConcept this)
;          slot-relations (.getSlotRelationList this)]
;      (merge
;        obj
;        (if-not (nil? relation)
;          {:relation (convert-hz-ont-obj relation)})
;        (if (or (not (nil? slot-relations)) (not (empty? slot-relations)))
;          {:slot-relations
;          (vec
;            (map
;              #(let [rel-label (.getRSRelationLabel %)
;                     rel-slot (.getRelationSlot %)
;                     rel-role (.getRelationRoleConcept %)
;                     constraint (.getConstraintConcept %)
;                     ref-rel-slot (.getReferenceRelationSlot %)]
;                 (merge
;                   (if-not (nil? constraint)
;                     {:constraint (convert-hz-ont-obj constraint)})
;                   (if-not (nil? ref-rel-slot)
;                     {:ref-relation-slot (convert-hz-ont-obj ref-rel-slot)})
;                   (if (or (not (empty? rel-label))
;                           (not (nil? rel-slot)) (not (nil? rel-role)))
;                     {:relation
;                      (merge
;                        (if-not (empty? rel-label)
;                          {:label rel-label})
;                        (if-not (nil? rel-slot)
;                          {:slot (convert-to-clj rel-slot show-upper-slots)})
;                        (if-not (nil? rel-role)
;                          {:role (convert-hz-ont-obj rel-role)}))})))
;              slot-relations))}))))
;  
;  Relation
;  (convert-to-clj [this show-upper-slots] (convert-relation this))
;  
;  Slot
;  (convert-to-clj [this show-upper-slots]
;    (let [obj (convert-hz-ont-obj this)
;          role (.getRoleConcept this)
;          roles (.getRoleConceptList this)
;          role-holder (.getRoleHolder this)
;          dependent-concept (.getDependentConcept this)
;          parent-slot (.getParentSlot this)
;          slots (if show-upper-slots
;                  (.getAllSlotList this) (.getSlotList this)) 
;          relations (.getRelationList this)
;          consts (let [consts (.getConstraintConceptList this)]
;                   (filter identity
;                           (conj (if-not (empty? consts)
;                                   (iterator-seq (.iterator consts)) [])
;                           (.getConstraintConcept this))))
;          value (.getConstraintValue this)]
;      (merge obj
;             {:card-min (str (SlotUtility/getMinimumSlotCardinality this))
;              :card-max (str (SlotUtility/getMaximumSlotCardinality this))
;              :kind (.getSlotKind this)
;              :slot-type (.getSlotType this)}
;             (if-not (nil? role)
;               {:role (convert-hz-ont-obj role)})
;             (if-not (empty? consts)
;               {:constraints (vec (map #(convert-hz-ont-obj %) consts))})
;             (if-not (empty? value)
;               {:value value})
;             ;(if-not (empty? roles) {:roles (vec (map #(convert-hz-ont-obj %) (iterator-seq (.iterator roles))))})
;             (if-not (nil? role-holder)
;               {:role-holder (convert-hz-ont-obj role-holder)})
;             (if-not (nil? dependent-concept)
;               {:depend-on (convert-hz-ont-obj dependent-concept)})
;             (if-not (nil? parent-slot)
;               {:parent-slot (convert-hz-ont-obj parent-slot)})
;             (if-not (empty? slots)
;               {:slots (vec (map convert-to-clj
;                                 slots (repeat show-upper-slots)))})
;             (if-not (empty? relations)
;               {:relations (vec (map convert-hz-ont-obj relations))}))))
  ;; function to convert instances
  RoleConceptInstance
  (update-by-clj [this update]
    (let [ins (update-concept-instance this update)]
          ;dep-slot (.getDependentSlotIns this)
          ;dep-concept (.getDependentConceptIns this)
      ins))
  
  RoleHolderInstance
  (update-by-clj [this update]
    (let [ins (update-concept-instance this update)]
          ;dep-slot (.getDependentSlotIns this)
          ;dep-concept (.getDependentConceptIns this)
      ins))
  
  ConceptInstance
  (update-by-clj [this update] (update-concept-instance this update))
  
;  InstanceOfRelation
;  (update-by-clj [this update]
;    (let [ins (update-hz-mdl-obj this update)
;          ;con-obj (.getConceptObject this)
;          ins-obj (:instance update)
;          ins-obj (.getInstanceObject this)]
;      (.setInstanceObject this 
;                          (if-not (empty? (:id ins-obj))
;                            (update (.getInstanceObject this) ins-obj)
;                            (create-instance ins-obj))
;                          (create-update (.getInstanceObject this) ins-obj))
;      ins))
  
;  RConceptRelationInstance
;  (update-by-clj [this show-upper-slots]
;    (let [ins (convert-hz-mdl-obj this)
;          slots (map convert-slot-relation-instance
;                     (.getSlotRelationInstanceList this)
;                     (repeat show-upper-slots))
;          rc (.getRelationConcept this)]
;      (merge ins
;             (if-not (empty? slots)
;               {:slots slots})
;             (if-not (nil? rc)
;               {:relation-concept (convert-hz-ont-obj rc)}))))
  
  RelationInstance
  (update-by-clj [this update] (update-hz-mdl-obj this update))
 
  SlotInstance
  (update-by-clj [this update]
    (let [ins (update-hz-mdl-obj this update)
          mdl (.getModel ins)
          ont (.getReferenceOntology ins)
          role (:role update) 
          role-holder (:role-holder update)
          ; TODO implement update using the following information
          ;dependent-concept (.getDependentConceptInstance this)
          ;relations (.getRelationInstanceList this)
          ;parent-slot (.getParentSlotInstance this)
          value (:value update)
          constraint (:constraint update)
          slots (:slots update)]
      (if-not (nil? role)
        (.setRoleConceptInstance
          ins
          (create-update-concept-instance-by-clj mdl ont role)))
      (if-not (nil? role-holder)
        (.setRoleHolderInstance
          ins
          (create-update-concept-instance-by-clj mdl ont role-holder)))
      (if-not (empty? value) (.setConstraintValue ins value))
      (if-not (nil? constraint)
        (.setConstraintConceptInstance
          ins
          (create-update-concept-instance-by-clj mdl ont constraint)))
      (update-slots-by-clj ins (.getSlotInstanceList ins) slots)
      ins)))

;;

(defn- update-args-to-map
  "Internal function to convets arguments of update function into a map"
  [[f & r :as args]]
  (cond (and (map? f) (= 1 (count args))
             (every? #(contains? f %) [:update])) f
        (keyword? f) (apply map args)
        ;; walk down the arguments and pull them out positionally
        :default (let [[update a] (util/check-arg map? args)
                       full-info (if-not (nil? (first a)) (first a) true)]
                   (->> {:update update
                         :full-info (boolean full-info)}
                        (filter second) (into {})))))

(defn hz-update
  "Function to update a HozoObject in an ontology or model
  - dests: Ontology/Model or FilePath where to update the HozoObject
  Remaining arguments are optional. They may be positional or a map:
  :update - map with the definition of HozoObjects to update
  :full-info - boolean default=true to obtain a complete information"
  [dests & args]
  (let [dests (if-not (sequential? dests) (list dests) dests)
        args (update-args-to-map args)]
    (loop [[dest & r-dests] dests result (list)]
      (if (nil? dest) result
        (let [[is-ont ont-mdl] (cond (instance? Model dest) [false dest]
                                     (instance? Ontology dest) [true dest]
                                     (ModelUtility/isModelFile dest)
                                     [false (load-model dest)]
                                     (OntologyUtility/isOntologyFile dest)
                                     [true (load-ontology dest)])
              search (merge {:id (:id (:update args))
                             :type (keyword (:type (:update args)))}
                            (if-not (nil? (:ontology-object (:update args)))
                              {:ontology-object (:ontology-object (:update args))}))
              obj (if is-ont
                     (retrieve-ont-obj ont-mdl search true)
                     (retrieve-mdl-obj ont-mdl search true true))]
          (if-not (nil? obj)
            (if (:full-info args)
              (values/convert (update-by-clj obj (:update args)) true)
              (values/convert-hz-object (update-by-clj obj (:update args))))
            (recur r-dests result)))))))

