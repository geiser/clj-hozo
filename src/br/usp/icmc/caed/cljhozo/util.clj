(ns br.usp.icmc.caed.cljhozo.util
  (:require [clojure.string :as string])
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
           [jp.hozo.core.literal AnyConcept BlankConcept
                                 LargerThanRelationConcept
                                 NotEqualRelationConcept EqualRelationConcept
                                 SameAsRelationConcept DifferentRelationConcept
                                 BooleanValueConcept DateValueConcept
                                 DecimalValueConcept FloatValueConcept
                                 IntegerValueConcept NumberValueConcept
                                 StringValueConcept TimeValueConcept
                                 UriValueConcept]
           [jp.hozo.reasoner HZReasonerFactory]
           [jp.hozo.core.util OntologyUtility SlotUtility ModelUtility]
           [jp.hozo.corex OntologyFactory ModelFactory HZOntology HZModel
                          HZBasicConceptInstance]
           [jp.hozo.corex.util HZConceptUtility]))

(def d-ns {:xsd "http://www.w3.org/2001/XMLSchema#"
           :dc "http://purl.org/dc/elements/1.1/"})

(def h-types
  (-> (make-hierarchy)
      (derive :hz-ontology-object :hz-object)
        (derive :concept :hz-ontology-object)
          (derive :basic-concept :concept)
            (derive :any :basic-concept)
            (derive :blank :basic-concept)
            (derive :relation-concept :basic-concept)
              (derive :different :relation-concept)
              (derive :equal :relation-concept)
              (derive :larger-than :relation-concept)
              (derive :not-equal :relation-concept)
              (derive :same-as :relation-concept)
            (derive :whole-concept :basic-concept)
          (derive :p-operator :concept)
          (derive :r-operator :concept)
          (derive :reference :concept)
          (derive :role :concept)
          (derive :role-holder :concept)
          (derive :species :concept)
          (derive :undefined :concept)
          (derive :value :concept)
            (derive :boolean-value :value)
            (derive :date-value :value)
            (derive :decimal-value :value)
            (derive :float-value :value)
            (derive :integer-value :value)
            (derive :number-value :value)
            (derive :string-value :value)
            (derive :time-value :value)
            (derive :uri-value :value)
        (derive :partial-structure :hz-ontology-object)
        (derive :relation :hz-ontology-object)
          (derive :attribute-of :relation)
          (derive :is-a :relation)
          (derive :part-of :relation)
          (derive :r-concept :relation)
        (derive :slot :hz-ontology-object)
      (derive :hz-model-object :hz-object)
        (derive :concept-instance :hz-model-object)
          (derive :basic-concept-instance :concept-instance)
          (derive :role-instance :concept-instance)
          (derive :role-holder-instance :concept-instance)
          (derive :species-instance :concept-instance)
        (derive :relation-instance :hz-model-object)
          (derive :instance-of :relation-instance)
          (derive :r-concept-instance :relation-instance)
        (derive :slot-instance :hz-model-object)))

(defn keyword-class
  "Function to obtain the keyword that represents the class of HozoObject"
  [^HozoObject obj]
  (cond
    (instance? SlotInstance obj) :slot-instance
    (instance? RConceptRelationInstance obj) :r-concept-instance
    (instance? InstanceOfRelation obj) :instance-of
    (instance? RelationInstance obj) :relation-instance
    (instance? SpeciesConceptInstance obj) :species-instance
    (instance? RoleHolderInstance obj) :role-holder-instance
    (instance? RoleConceptInstance obj) :role-instance
    (instance? BasicConceptInstance obj) :basic-concept-instance
    (instance? ConceptInstance obj) :concept-instance
    (instance? HozoModelObject obj) :hz-model-object
    (instance? Slot obj) :slot
    (instance? RConceptRelation obj) :r-concept
    (instance? PartOfRelation obj) :part-of
    (instance? IsaRelation obj) :is-a
    (instance? AttributeOfRelation obj) :attribute-of
    (instance? Relation obj) :relation
    (instance? PartialConceptStructure obj) :partial-structure
    (instance? BooleanValueConcept obj) :boolean-value
    (instance? DateValueConcept obj) :date-value
    (instance? DecimalValueConcept obj) :decimal-value
    (instance? FloatValueConcept obj) :float-value
    (instance? IntegerValueConcept obj) :integer-value
    (instance? NumberValueConcept obj) :number-value
    (instance? StringValueConcept obj) :string-value
    (instance? TimeValueConcept obj) :time-value
    (instance? UriValueConcept obj) :uri-value
    (instance? ValueConcept obj) :value
    (instance? UndefinedConcept obj) :undefined
    (instance? SpeciesConcept obj) :species
    (instance? RoleHolder obj) :role-holder
    (instance? RoleConcept obj) :role
    (instance? ReferenceConcept obj) :reference
    (instance? R_Operator obj) :r-operator
    (instance? P_Operator obj) :p-operator
    (instance? WholeConcept obj) :whole-concept
    (instance? SameAsRelationConcept obj) :same-as
    (instance? NotEqualRelationConcept obj) :not-equal
    (instance? LargerThanRelationConcept obj) :larger-than
    (instance? EqualRelationConcept obj) :equal
    (instance? DifferentRelationConcept obj) :different
    (instance? RelationConcept obj) :relation-concept
    (instance? BlankConcept obj) :blank
    (instance? AnyConcept obj) :any
    (instance? BasicConcept obj) :basic-concept
    (instance? Concept obj) :concept
    (instance? HozoOntologyObject obj) :hz-ontology-object
    :else :hz-object))

(defn keyword-int
  "Function to obtain an int that represent the class of HozoObject"
  [k]
  (cond
    (= :concept (keyword k)) Concept/CONCEPT_TYPE
    (= :basic-concept (keyword k)) BasicConcept/CONCEPT_TYPE
    (= :any (keyword k)) AnyConcept/CONCEPT_TYPE
    (= :blank (keyword k)) BlankConcept/CONCEPT_TYPE
    (= :relation-concept (keyword k)) RelationConcept/CONCEPT_TYPE
    (= :different (keyword k)) DifferentRelationConcept/CONCEPT_TYPE
    (= :equal (keyword k)) EqualRelationConcept/CONCEPT_TYPE
    (= :larger-than (keyword k)) LargerThanRelationConcept/CONCEPT_TYPE
    (= :not-equal (keyword k)) NotEqualRelationConcept/CONCEPT_TYPE
    (= :same-as (keyword k)) SameAsRelationConcept/CONCEPT_TYPE
    (= :whole-concept (keyword k)) WholeConcept/CONCEPT_TYPE
    (= :p-operator (keyword k)) P_Operator/CONCEPT_TYPE
    (= :r-operator (keyword k)) R_Operator/CONCEPT_TYPE
    (= :reference (keyword k)) ReferenceConcept/CONCEPT_TYPE
    (= :role (keyword k)) RoleConcept/CONCEPT_TYPE
    (= :role-holder (keyword k)) RoleHolder/CONCEPT_TYPE
    (= :species (keyword k)) SpeciesConcept/CONCEPT_TYPE
    (= :undefined (keyword k)) UndefinedConcept/CONCEPT_TYPE
    (= :value (keyword k)) ValueConcept/CONCEPT_TYPE
    (= :boolean-value (keyword k)) BooleanValueConcept/CONCEPT_TYPE
    (= :date-value (keyword k)) DateValueConcept/CONCEPT_TYPE
    (= :decimal-value (keyword k)) DecimalValueConcept/CONCEPT_TYPE
    (= :float-value (keyword k)) FloatValueConcept/CONCEPT_TYPE
    (= :integer-value (keyword k)) IntegerValueConcept/CONCEPT_TYPE
    (= :number-value (keyword k)) NumberValueConcept/CONCEPT_TYPE
    (= :string-value (keyword k)) StringValueConcept/CONCEPT_TYPE
    (= :time-value (keyword k)) TimeValueConcept/CONCEPT_TYPE
    (= :uri-value (keyword k)) UriValueConcept/CONCEPT_TYPE))

(defn check-arg 
  "Internal function to validate an argument. Return nil if the f is false.
  Function Licensed under the Apache License, Version 2.0 (the License);
  http://www.apache.org/licenses/LICENSE-2.0
    Copyright (C) 2014 Clark & Parsia
    Copyright (C) 2014 Paula Gearon
  Stardog-clj Project:
  https://github.com/Complexible/stardog-clj/blob/master/src/stardog/core.clj"
  [pred [f & r :as a]] (if (pred f) [f r] [nil a]))

