(ns br.usp.icmc.caed.cljhozo.values
  (:require [clojure.string :as string]
            [br.usp.icmc.caed.cljhozo.util :as util])
  (:import [jp.hozo.core HozoObject HozoOntologyObject
                         AttributeOfRelation InstanceOfRelation RelationConcept
                         SpeciesConcept UndefinedConcept
                         Ontology Model Concept Relation RSlotRelationInstance
                         BasicConcept WholeConcept PartialConceptStructure
                         Slot RoleHolder RoleConcept
                         P_Operator R_Operator ReferenceConcept
                         HozoModelObject RelationInstance IsaRelation
                         RConceptRelation ValueConcept SpeciesConceptInstance
                         RConceptRelationInstance PartOfRelation
                         ConceptInstance BasicConceptInstance SlotInstance
                         RoleHolderInstance RoleConceptInstance]
           [jp.hozo.core.util OntologyUtility SlotUtility]
           [jp.hozo.corex OntologyFactory ModelFactory HZOntology HZModel]
           [jp.hozo.corex.util HZConceptUtility]))

;; Internal functions to convert abstract objects
(defn- int-convert-hz-object
  "Internal function to convert HozoObject into a structure for clj"
  [^HozoObject obj]
  (let [result {:id (.getId obj)
                :type (util/keyword-class obj)}
        label (.getLabel obj)
        description (.getDescription obj)]
    (with-meta
      (merge result
             (if-not (empty? label)
               {:label label})
             (if-not (empty? description)
               {:description description}))
      {:java {:class (class obj) :object obj}})))

(defn- convert-hz-ont-obj
  "Internal function to convert HozoOntologyObject into a structure for clj"
  [^HozoOntologyObject obj]
  (let [result (int-convert-hz-object obj)]
    (with-meta
      result
      (merge (meta result)
             (let [ont (.getOntology obj)
                   ref-ont (.getReferenceOntology obj)]
               (merge
                 (if-not (nil? ont)
                   (let [label (.getLabel ont)]
                     {:ontology (merge {:id (.getId ont)}
                                       (if-not (empty? label)
                                         {:label label}))}))
                 (if-not (nil? ref-ont)
                   (let [label (.getLabel ref-ont)]
                     {:ref-ontology (merge {:id (.getId ref-ont)}
                                           (if-not (empty? label)
                                             {:label label}))}))))))))

(defn- convert-hz-mdl-obj
  "Internal function that convert HozoModelObject into a structure for clj"
  [^HozoModelObject obj]
  (let [result (int-convert-hz-object obj)
        ont-obj (.getOntologyObject obj)]
    (with-meta
      (merge
        result
        (if-not (nil? ont-obj)
          {:ontology-object (convert-hz-ont-obj ont-obj)}))
      (merge
        (meta result)
        (if-not (nil? (.getModel obj))
          (let [model (.getModel obj)]
            {:model (merge {:id (.getId model)}
                           (if-not (empty? (.getLabel model))
                             {:label (.getLabel model)}))}))))))


(defn convert-hz-object
  "A simple function to convert a hz-object to clj-structure"
  [hz-obj]
  (let [f (cond (instance? HozoOntologyObject hz-obj) convert-hz-ont-obj
                (instance? HozoModelObject hz-obj) convert-hz-mdl-obj
                :else int-convert-hz-object)]
    (apply f [hz-obj])))

;; Functions to convert HozoObject into a structure for clj 

(defprotocol HZConvert
  "A simple protocol to convert a HozoObject into a structure for clj"
  (convert-to-clj [this show-upper-slots]))

(defn- convert-concept
  "Internal function to convert Concept into a structure for clj"
  [^Concept obj show-upper-slots]
  (let [result (convert-hz-ont-obj obj)
        slots (if show-upper-slots (.getAllSlotList obj) (.getSlotList obj))
        relations (.getRelationList obj)
        terms (.getTerms obj)
        upper-concept (.getUpperConcept obj)
        lower-concepts (.getLowerConceptList obj)]
    (merge
      result
      (if-not (empty? slots)
        {:slots (vec (map convert-to-clj slots (repeat show-upper-slots)))})
      (if-not (empty? relations)
        {:relations (vec (map convert-to-clj
                              relations (repeat show-upper-slots)))})
      (if-not (empty? terms)
        {:terms (vec (map #(let [name (.getName %)
                                 label (.getLabel %)
                                 symbol (.getSymbol %)
                                 lang (.getLanguage %)]
                             (merge
                               (if-not (empty? name) {:name name})
                               (if-not (empty? label) {:label label})
                               (if-not (empty? symbol) {:symbol symbol})
                               (if-not (empty? lang) {:lang lang}))) terms))})
      (if-not (nil? upper-concept)
        {:upper-concept (convert-hz-ont-obj upper-concept)})
      (if-not (empty? lower-concepts)
        {:lower-concepts (vec (map convert-hz-ont-obj lower-concepts))}))))

(defn- convert-relation
  "Internal function to convert Relation into a structure for clj"
  [^Relation obj]
  (let [result (convert-hz-ont-obj obj)
        master-label (.getMasterLabel obj)
        master-concept (.getMasterConcept obj)
        slave-label (.getSlaveLabel obj)
        slave-concept (.getSlaveConcept obj)]
    (merge
      result
      (if-not (nil? master-concept)
        {:master-label master-label
         :master-concept (convert-hz-ont-obj master-concept)})
      (if-not (nil? slave-concept)
        {:slave-label slave-label
         :slave-concept (convert-hz-ont-obj slave-concept)}))))

(defn- convert-concept-instance 
  "Internal function that convert ConceptInstance into a structure for clj"
  [^ConceptInstance ins show-upper-slots]
  (let [result (convert-hz-mdl-obj ins)
        slots (map convert-to-clj (.getSlotInstanceList ins)
                   (repeat show-upper-slots))
        relations (map convert-hz-object (.getRelationInstanceList ins))]
    (merge result
           (if-not (empty? slots)
             {:slots (vec slots)})
           (if-not (empty? relations)
             {:relations (vec relations)}))))

(defn- convert-slot-relation-instance 
  "Internal function that convert RSlotRelationInstance into a clj"
  [^RSlotRelationInstance ins show-upper-slots]
  (let [slot (.getRelationSlotInstance ins)
        role (.getRelationRoleConceptInstance ins)
        constraint (.getConstraintConceptInstance ins)
        label (.getRSRelationInstanceLabel ins)]
    (with-meta
      (merge (if-not (empty? label)
               {:label label})
             (if-not (nil? slot)
               {:slot (convert-to-clj slot show-upper-slots)})
             (if-not (nil? role)
               {:role (convert-hz-mdl-obj role)})
             (if-not (nil? constraint)
               {:constraint (convert-hz-mdl-obj constraint)}))
      (merge (let [ref-slot (.getReferenceRelationSlot ins)]
               (if-not (nil? ref-slot)
                 {:ontology-object (convert-hz-ont-obj ref-slot)}))))))

(extend-protocol HZConvert
  P_Operator
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          part (.getPartOfConcept this)]
      (merge obj
             (if-not (nil? part)
               {:part-of (convert-hz-ont-obj part)}))))
  
  R_Operator
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          reg (.getRegionOfConcept this)]
      (merge
        obj
        (if-not (nil? reg)
          {:region-of (convert-hz-ont-obj reg)}))))
  
  ReferenceConcept
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          ori (.getOriginalConcept this)]
      (merge obj
             (if-not (nil? ori)
               {:original (convert-hz-ont-obj ori)}))))
  
  RoleConcept
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          dep-slot (.getDependentSlot this)
          dep-concept (.getDependentConcept this)
          part-role (.getPartRoleConcept this)
          constraint (.getConstraintConcept this)]
      (merge obj
             (if (or (not (nil? dep-slot)) (not (nil? dep-concept)))
               (let [depend (if-not (nil? dep-slot)
                              (convert-hz-ont-obj dep-slot)
                              (convert-hz-ont-obj dep-concept))]
                  {:depend-on depend}))
              (if-not (nil? part-role)
                {:part-role (convert-hz-ont-obj part-role)})
              (if-not (nil? constraint)
                {:constraint (convert-hz-ont-obj constraint)}))))
  
  RoleHolder
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          role (.getRoleConcept this)]
      (merge obj
             (if-not (nil? role)
               {:role (convert-hz-ont-obj role)}))))
  
  SpeciesConcept
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          ori (.getOriginalConcept this)]
      (merge obj
             (if-not (nil? ori)
               {:original (convert-hz-ont-obj ori)}))))

  ValueConcept
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-concept this show-upper-slots)
          value (.getValue this)
          data-type (.getDataType this)]
      (merge obj
             (if (or (nil? (:id obj)) (empty? (:id obj)))
               {:id (case (:type obj)
                      :boolean-value (str  (:xsd util/d-ns) "boolean")
                      :date-value (str (:dc util/d-ns) "date")
                      :time-value (str (:xsd util/d-ns) "dateTime")
                      :decimal-value (str (:xsd util/d-ns) "decimal")
                      :float-value (str (:xsd util/d-ns) "float")
                      :integer-value (str (:xsd util/d-ns) "integer")
                      :number-value (str (:xsd util/d-ns) "double")
                      :string-value (str (:xsd util/d-ns) "string")
                      :uri-value (str (:xsd util/d-ns) "anyURI"))})
             (if-not (empty? value)
               {:value value})
             (if-not (empty? data-type)
               {:data-type data-type}))))
  
  Concept
  (convert-to-clj [this show-upper-slots]
    (convert-concept this show-upper-slots))
  
  PartialConceptStructure
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-hz-ont-obj this)
          lang (.getLanguage this)
          slots (.getSlotList this) 
          owner (.getOwnerConcept this)
          partial-structures (.getPartialSlotStructureList this)]
      (merge obj
             (if-not (empty? lang)
               {:lang lang})
             (if-not (nil? owner)
               {:owner (convert-hz-ont-obj owner)})
             (if-not (empty? slots)
               {:slots (vec (map convert-to-clj
                                 slots (repeat show-upper-slots)))})
             (if-not (empty? partial-structures)
               {:partial-structures
                 (vec (map #(let [slot (.getSlot %)
                                  path (.getSlotPath %)]
                              (merge (if-not (nil? slot) {:slot slot})
                                     (if-not (empty? path) {:path path})))
                           partial-structures))}))))
  
  AttributeOfRelation
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-relation this)
          attr-slot (.getAttributeSlot this)
          attr-slot-size (.getAttributeSlotSize this)]
      (merge obj
             (if-not (nil? attr-slot)
               {:attribute-slot (convert-hz-ont-obj attr-slot)
                :attribute-slot-size (str attr-slot-size)}))))
  
  PartOfRelation
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-relation this)
          part-slot (.getPartSlot this)
          part-slot-size (.getPartSlotSize this)]
      (merge obj
             (if-not (nil? part-slot)
               {:part-slot (convert-hz-ont-obj part-slot)
                :part-slot-size (str part-slot-size)}))))
  
  RConceptRelation
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-relation this)
          relation (.getRelationConcept this)
          slot-relations (.getSlotRelationList this)]
      (merge
        obj
        (if-not (nil? relation)
          {:relation (convert-hz-ont-obj relation)})
        (if (or (not (nil? slot-relations)) (not (empty? slot-relations)))
          {:slot-relations
          (vec
            (map
              #(let [rel-label (.getRSRelationLabel %)
                     rel-slot (.getRelationSlot %)
                     rel-role (.getRelationRoleConcept %)
                     constraint (.getConstraintConcept %)
                     ref-rel-slot (.getReferenceRelationSlot %)]
                 (merge
                   (if-not (nil? constraint)
                     {:constraint (convert-hz-ont-obj constraint)})
                   (if-not (nil? ref-rel-slot)
                     {:ref-relation-slot (convert-hz-ont-obj ref-rel-slot)})
                   (if (or (not (empty? rel-label))
                           (not (nil? rel-slot)) (not (nil? rel-role)))
                     {:relation
                      (merge
                        (if-not (empty? rel-label)
                          {:label rel-label})
                        (if-not (nil? rel-slot)
                          {:slot (convert-to-clj rel-slot show-upper-slots)})
                        (if-not (nil? rel-role)
                          {:role (convert-hz-ont-obj rel-role)}))})))
              slot-relations))}))))
  
  Relation
  (convert-to-clj [this show-upper-slots] (convert-relation this))
  
  Slot
  (convert-to-clj [this show-upper-slots]
    (let [obj (convert-hz-ont-obj this)
          role (.getRoleConcept this)
          roles (.getRoleConceptList this)
          role-holder (.getRoleHolder this)
          dependent-concept (.getDependentConcept this)
          parent-slot (.getParentSlot this)
          slots (if show-upper-slots
                  (.getAllSlotList this) (.getSlotList this)) 
          relations (.getRelationList this)
          consts (let [consts (.getConstraintConceptList this)]
                   (filter identity
                           (conj (if-not (empty? consts)
                                   (iterator-seq (.iterator consts)) [])
                           (.getConstraintConcept this))))
          value (.getConstraintValue this)]
      (merge obj
             {:card-min (str (SlotUtility/getMinimumSlotCardinality this))
              :card-max (str (SlotUtility/getMaximumSlotCardinality this))
              :kind (.getSlotKind this)
              :slot-type (.getSlotType this)}
             (if-not (nil? role)
               {:role (convert-hz-ont-obj role)})
             (if-not (empty? consts)
               {:constraints (vec (map #(convert-hz-ont-obj %) consts))})
             (if-not (empty? value)
               {:value value})
             ;(if-not (empty? roles) {:roles (vec (map #(convert-hz-ont-obj %) (iterator-seq (.iterator roles))))})
             (if-not (nil? role-holder)
               {:role-holder (convert-hz-ont-obj role-holder)})
             (if-not (nil? dependent-concept)
               {:depend-on (convert-hz-ont-obj dependent-concept)})
             (if-not (nil? parent-slot)
               {:parent-slot (convert-hz-ont-obj parent-slot)})
             (if-not (empty? slots)
               {:slots (vec (map convert-to-clj
                                 slots (repeat show-upper-slots)))})
             (if-not (empty? relations)
               {:relations (vec (map convert-hz-ont-obj relations))}))))
  
  ;; function to convert instances
  RoleConceptInstance
  (convert-to-clj [this show-upper-slots]
    (let [ins (convert-concept-instance this show-upper-slots)
          dep-slot (.getDependentSlotIns this)
          dep-concept (.getDependentConceptIns this)]
      (merge ins
             (if (or (not (nil? dep-slot)) (not (nil? dep-concept))) 
               (let [depend (if-not (nil? dep-slot)
                              (convert-hz-mdl-obj dep-slot)
                              (convert-hz-mdl-obj dep-concept))]
                 {:depend-on depend})))))
  
  RoleHolderInstance
  (convert-to-clj [this show-upper-slots]
    (let [ins (convert-concept-instance this show-upper-slots)
          dep-slot (.getDependentSlotIns this)
          dep-concept (.getDependentConceptIns this)]
      (merge ins
             (if (or (not (nil? dep-slot)) (not (nil? dep-concept)))
               (let [depend (if-not (nil? dep-slot)
                              (convert-hz-mdl-obj dep-slot)
                              (convert-hz-mdl-obj dep-concept))]
                 {:depend-on depend})))))
  
  ConceptInstance
  (convert-to-clj [this show-upper-slots]
    (convert-concept-instance this show-upper-slots))
  
  InstanceOfRelation
  (convert-to-clj [this show-upper-slots]
    (let [ins (convert-hz-mdl-obj this)
          con-obj (.getConceptObject this)
          ins-obj (.getInstanceObject this)]
      (merge ins
             (if-not (nil? con-obj)
               {:concept (convert-hz-ont-obj con-obj)})
             (if-not (nil? ins-obj)
               {:instance (convert-hz-mdl-obj ins-obj)}))))
  
  RConceptRelationInstance
  (convert-to-clj [this show-upper-slots]
    (let [ins (convert-hz-mdl-obj this)
          slots (map convert-slot-relation-instance
                     (.getSlotRelationInstanceList this)
                     (repeat show-upper-slots))
          rc (.getRelationConcept this)]
      (merge ins
             (if-not (empty? slots)
               {:slots slots})
             (if-not (nil? rc)
               {:relation-concept (convert-hz-ont-obj rc)}))))
  
  RelationInstance
  (convert-to-clj [this show-upper-slots] (convert-hz-mdl-obj this))
  
  SlotInstance
  (convert-to-clj [this show-upper-slots]
    (let [ins (convert-hz-mdl-obj this)
          role (.getRoleConceptInstance this)
          ;roles (.getRoleConceptInstanceList this)
          ;inherit-slots (.getInheritSlotList this)
          role-holder (.getRoleHolderInstance this)
          dependent-concept (.getDependentConceptInstance this)
          parent-slot (.getParentSlotInstance this)
          slots (.getSlotInstanceList this)
          constraint (.getConstraintConceptInstance this) 
          value (.getConstraintValue this)
          relations (.getRelationInstanceList this)]
      (merge ins
             (if-not (nil? constraint)
               {:constraint (convert-hz-mdl-obj constraint)})
             (if-not (empty? value)
               {:value value})
             (if-not (nil? role)
               {:role (convert-hz-mdl-obj role)})
             ;(if-not (empty? roles)
             ;  {:roles (vec (map #(convert-hz-mdl-obj %)
             ;                    (iterator-seq (.iterator roles))))})
             (if-not (nil? role-holder)
               {:role-holder (convert-hz-mdl-obj role-holder)})
             (if-not (nil? dependent-concept)
               {:depend-on (convert-hz-mdl-obj dependent-concept)})
             (if-not (nil? parent-slot)
               {:parent-slot (convert-hz-mdl-obj parent-slot)})
             (if-not (empty? slots)
               {:slots (vec (map convert-to-clj
                                 slots (repeat show-upper-slots)))})
             ;(if (and (show-upper-slots) (not (empty? inherit-slots)))
             ;  {:inherit-slots
             ;   (vec (map convert-to-clj
             ;             inherit-slots (repeat show-upper-slots)))})
             (if-not (empty? relations)
               {:relations (vec (map convert-hz-mdl-obj relations))})))))

(defn convert
  "A default function that convert a HozoObject into a structure for clj.
  Inherited slots are hidden by default"
  ([^HozoObject hz-obj] (convert-to-clj hz-obj false))
  ([^HozoObject hz-obj show-upper-slots]
   (convert-to-clj hz-obj show-upper-slots)))

