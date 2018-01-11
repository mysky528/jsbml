/*
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2018 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 * 4. The University of California, San Diego, La Jolla, CA, USA
 * 5. The Babraham Institute, Cambridge, UK
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */

package org.sbml.jsbml.validator.offline.constraints;

import java.util.HashSet;
import java.util.Set;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.TreeNodeChangeEvent;
import org.sbml.jsbml.util.ValuePair;
import org.sbml.jsbml.validator.OverdeterminationValidator;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;
import org.sbml.jsbml.validator.SyntaxChecker;
import org.sbml.jsbml.validator.offline.ValidationContext;
import org.sbml.jsbml.validator.offline.constraints.helper.DuplicatedElementValidationFunction;
import org.sbml.jsbml.validator.offline.constraints.helper.ElementOrderValidationFunction;
import org.sbml.jsbml.validator.offline.constraints.helper.SBOValidationConstraints;
import org.sbml.jsbml.validator.offline.constraints.helper.UniqueValidation;
import org.sbml.jsbml.validator.offline.constraints.helper.UnknownAttributeValidationFunction;
import org.sbml.jsbml.validator.offline.constraints.helper.UnknownElementValidationFunction;
import org.sbml.jsbml.validator.offline.constraints.helper.ValidationTools;;

/**
 * @author Roman
 * @since 1.2
 */
public class ModelConstraints extends AbstractConstraintDeclaration {

  /**
   * 
   *
   */
  public static String[] MODEL_ELEMENTS_ORDER = 
    {"notes", "annotation", "listOfFunctionDefinitions", "listOfUnitDefinitions", "listOfCompartmentTypes",
    "listOfSpeciesTypes", "listOfCompartments", "listOfSpecies", "listOfParameters",
    "listOfInitialAssignments", "listOfRules", "listOfConstraints",
    "listOfReactions", "listOfEvents"};
  
  
  @Override
  public void addErrorCodesForAttribute(Set<Integer> set, int level,
    int version, String attributeName, ValidationContext context) 
  {
    // TODO - there are probably some constraints to apply for the new L3 units attributes.
  }


  @Override
  public void addErrorCodesForCheck(Set<Integer> set, int level, int version,
    CHECK_CATEGORY category, ValidationContext context) {

    switch (category) {
    case GENERAL_CONSISTENCY:

      if ((ValuePair.of(3, 2).compareTo(level, version)) > 0) {
        set.add(CORE_20202);
        set.add(CORE_20203);
      }
      
      set.add(CORE_20204);
      addRangeToSet(set, CORE_20205, CORE_20215);
      
      set.add(CORE_20222);
      
      if (level > 2 || (level == 2 && version > 1)) {
        set.add(CORE_20802);
        set.add(CORE_20803);
      }
      if (level >= 3) {

        addRangeToSet(set, CORE_20216, CORE_20221);
        addRangeToSet(set, CORE_20223, CORE_20233);
        set.add(CORE_20705);
      }
      break;
    case IDENTIFIER_CONSISTENCY:
      set.add(CORE_10301);
      set.add(CORE_10302);
      set.add(CORE_10304);

      if (level > 2) {
        set.add(CORE_10311);
        set.add(CORE_10313);
      }

      break;
    case MATHML_CONSISTENCY:
      break;
    case MODELING_PRACTICE:
      break;
    case OVERDETERMINED_MODEL:
      set.add(CORE_10601);
      break;
    case SBO_CONSISTENCY:
      if ((level == 2 && version > 1) || level > 2) {
        set.add(CORE_10701);
      }

      break;
    case UNITS_CONSISTENCY:
      if (level > 2) {
        set.add(CORE_10503);
        set.add(CORE_99130);
        set.add(CORE_99506);
        set.add(CORE_99507);
      }
      break;
    }
  }


  @Override
  @SuppressWarnings("deprecation")
  public ValidationFunction<?> getValidationFunction(int errorCode, ValidationContext context) {
    ValidationFunction<Model> func = null;

    switch (errorCode) {
    case CORE_10301: 
      // TODO - re-write this validation rule to recursively take all UniqueSId/UniqueNamedSBase objects ?
      
      // TODO - it would be good to be able to keep the created Hashset of ids so that we can use it
      // for other constraints, like when we will need to validate L3 package ids that are in the SId id space.
      
      func = new UniqueValidation<Model, String>() { 

        @Override
        public int getNumObjects(ValidationContext ctx, Model m) {

          return 1 + m.getNumFunctionDefinitions() + m.getNumCompartmentTypes()
            + m.getNumCompartments() + m.getNumSpeciesTypes()
            + m.getNumSpecies() + m.getNumReactions()
            + m.getNumSpeciesReferences() + m.getNumModifierSpeciesReferences()
            + m.getNumEvents() + m.getNumParameters();
        }


        @Override
        public String getNextObject(ValidationContext ctx, Model m, int n) {
          int offset = 0;

          if (n == 0) {
            return m.getId();
          }

          offset++;

          if (n < offset + m.getNumFunctionDefinitions()) {
            return m.getFunctionDefinition(n - offset).getId();
          }

          offset += m.getNumFunctionDefinitions();

          if (n < offset + m.getNumCompartmentTypes()) {
            return m.getCompartmentType(n - offset).getId();
          }

          offset += m.getNumCompartmentTypes();

          if (n < offset + m.getNumCompartments()) {
            return m.getCompartment(n - offset).getId();
          }

          offset += m.getNumCompartments();

          if (n < offset + m.getNumSpeciesTypes()) {
            return m.getSpeciesType(n - offset).getId();
          }

          offset += m.getNumSpeciesTypes();

          if (n < offset + m.getNumSpecies()) {
            return m.getSpecies(n - offset).getId();
          }

          offset += m.getNumSpecies();

          if (n < offset + m.getNumReactions()) {
            return m.getReaction(n - offset).getId();
          }

          offset += m.getNumReactions();

          if (n < offset + m.getNumEvents()) {
            return m.getEvent(n - offset).getId();
          }

          offset += m.getNumEvents();

          if (n < offset + m.getNumParameters()) {
            return m.getParameter(n - offset).getId();
          }

          offset += m.getNumParameters();

          if (m.isSetListOfReactions()) {
            for (Reaction r : m.getListOfReactions()) {
              if (n < offset + r.getReactantCount()) {
                return r.getReactant(n - offset).getId();
              }

              offset += r.getReactantCount();

              if (n < offset + r.getProductCount()) {
                return r.getProduct(n - offset).getId();
              }

              offset += r.getProductCount();

              if (n < offset + r.getModifierCount()) {
                return r.getModifier(n - offset).getId();
              }

              offset += r.getModifierCount();
            }
          }

          return null;
        }

      };
      break;

    case CORE_10302:
      func = new UniqueValidation<Model, String>() {

        @Override
        public String getNextObject(ValidationContext ctx, Model m, int n) {

          return m.getUnitDefinition(n).getId();
        }


        @Override
        public int getNumObjects(ValidationContext ctx, Model m) {
          return m.getNumUnitDefinitions();
        }
      };
      break;

    case CORE_10304:
      func = new UniqueValidation<Model, String>() {

        @Override
        public int getNumObjects(ValidationContext ctx, Model m) {
          int count = 0;

          if (m.isSetListOfRules()) {
            for (Rule r : m.getListOfRules()) {
              if (r instanceof ExplicitRule) {
                count++;
              }
            }
          }

          return count;
        }


        @Override
        public String getNextObject(ValidationContext ctx, Model m, int n) {

          int count = 0;

          if (m.isSetListOfRules()) {
            for (Rule r : m.getListOfRules()) {
              if (r instanceof ExplicitRule) {
                if (count == n) {
                  return ((ExplicitRule) r).getVariable();
                } else {
                  count++;
                }
              }
            }
          }

          return null;
        }
      };
      break;


    case CORE_10311:{
      func = new AbstractValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {

          boolean isCheckValid = true;
          boolean hasUnknownXML = m.getUserObject(JSBML.UNKNOWN_XML) != null;
          
          // checking all the units in the model: substanceUnits , volumeUnits , areaUnits , lengthUnits , timeUnits and extentUnits
          // And reporting one error for each problem.

          if (m.isSetSubstanceUnits())
          {
            boolean isUnitsValid = SyntaxChecker.isValidId(m.getSubstanceUnits(), ctx.getLevel(), ctx.getVersion());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10311, m.getSubstanceUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          else if (hasUnknownXML) 
          {
            String invalidUnits = ValidationTools.checkUnknownUnitSyntax(ctx, m, TreeNodeChangeEvent.substanceUnits);

            if (invalidUnits != null) {
              ValidationConstraint.logError(ctx, CORE_10311, invalidUnits, m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetVolumeUnits())
          {
            boolean isUnitsValid = SyntaxChecker.isValidId(m.getVolumeUnits(), ctx.getLevel(), ctx.getVersion());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10311, m.getVolumeUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          else if (hasUnknownXML) 
          {
            String invalidUnits = ValidationTools.checkUnknownUnitSyntax(ctx, m, TreeNodeChangeEvent.volumeUnits);

            if (invalidUnits != null) {
              ValidationConstraint.logError(ctx, CORE_10311, invalidUnits, m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetAreaUnits())
          {
            boolean isUnitsValid = SyntaxChecker.isValidId(m.getAreaUnits(), ctx.getLevel(), ctx.getVersion());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10311, m.getAreaUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          else if (hasUnknownXML) 
          {
            String invalidUnits = ValidationTools.checkUnknownUnitSyntax(ctx, m, TreeNodeChangeEvent.areaUnits);

            if (invalidUnits != null) {
              ValidationConstraint.logError(ctx, CORE_10311, invalidUnits, m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetLengthUnits())
          {
            boolean isUnitsValid = SyntaxChecker.isValidId(m.getLengthUnits(), ctx.getLevel(), ctx.getVersion());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10311, m.getLengthUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          else if (hasUnknownXML) 
          {
            String invalidUnits = ValidationTools.checkUnknownUnitSyntax(ctx, m, TreeNodeChangeEvent.lengthUnits);

            if (invalidUnits != null) {
              ValidationConstraint.logError(ctx, CORE_10311, invalidUnits, m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          
          if (m.isSetTimeUnits())
          {
            boolean isUnitsValid = SyntaxChecker.isValidId(m.getTimeUnits(), ctx.getLevel(), ctx.getVersion());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10311, m.getTimeUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          else if (hasUnknownXML) 
          {
            String invalidUnits = ValidationTools.checkUnknownUnitSyntax(ctx, m, TreeNodeChangeEvent.timeUnits);

            if (invalidUnits != null) {
              ValidationConstraint.logError(ctx, CORE_10311, invalidUnits, m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetExtentUnits())
          {
            boolean isUnitsValid = SyntaxChecker.isValidId(m.getExtentUnits(), ctx.getLevel(), ctx.getVersion());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10311, m.getExtentUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          else if (hasUnknownXML) 
          {
            String invalidUnits = ValidationTools.checkUnknownUnitSyntax(ctx, m, TreeNodeChangeEvent.extentUnits);

            if (invalidUnits != null) {
              ValidationConstraint.logError(ctx, CORE_10311, invalidUnits, m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          
          return isCheckValid;
        }
      };
      break;
    }

    case CORE_10313:{
      func = new AbstractValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {

          boolean isCheckValid = true;

          // checking all the units in the model: substanceUnits , volumeUnits , areaUnits , lengthUnits , timeUnits and extentUnits
          // And reporting one error for each problem.

          if (m.isSetSubstanceUnits())
          {
            boolean isUnitsValid = ValidationTools.checkUnit(ctx, m, m.getSubstanceUnits());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10313, m.getSubstanceUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetVolumeUnits())
          {
            boolean isUnitsValid = ValidationTools.checkUnit(ctx, m, m.getVolumeUnits());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10313, m.getVolumeUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetAreaUnits())
          {
            boolean isUnitsValid = ValidationTools.checkUnit(ctx, m, m.getAreaUnits());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10313, m.getAreaUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          
          if (m.isSetLengthUnits())
          {
            boolean isUnitsValid = ValidationTools.checkUnit(ctx, m, m.getLengthUnits());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10313, m.getLengthUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          
          if (m.isSetTimeUnits())
          {
            boolean isUnitsValid = ValidationTools.checkUnit(ctx, m, m.getTimeUnits());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10313, m.getTimeUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }

          if (m.isSetExtentUnits())
          {
            boolean isUnitsValid = ValidationTools.checkUnit(ctx, m, m.getExtentUnits());
        
            if (!isUnitsValid) {
              ValidationConstraint.logError(ctx, CORE_10313, m.getExtentUnits(), m.getElementName(), m.getId());
              isCheckValid = false;
            }
          }
          
          return isCheckValid;
        }
      };
      break;
    }

    case CORE_10503:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          boolean check = true;
          UnitDefinition firstUD = null;
          
          if (m.getReactionCount() > 0) {
            for (Reaction r : m.getListOfReactions()) {
              if (r.isSetKineticLaw()) {
                KineticLaw kl = r.getKineticLaw();
                
                if (kl.isSetMath()) {
                  UnitDefinition ud = null;
                  
                  try {
                    ud = ValidationTools.getDerivedUnitDefinition(ctx, kl);
                  } catch (Exception e) {
                    // on some invalid model, we get an exception thrown
                  }
                  
                  if (firstUD != null) {
                    // compare both units
                    if (!UnitDefinition.areEquivalent(firstUD, ud)) {
                      check = false;
                      break;
                    }
                  } else if (ud != null && !ud.isInvalid()) {
                    firstUD = ud;
                  } else {
                    break;
                  }
                }
              }
            }
          }

          return check;
        }
      };
      break;


    case CORE_10601:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          OverdeterminationValidator val = new OverdeterminationValidator(m);

          return !val.isOverdetermined();
        }
      };
      break;

    case CORE_10701:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {

          if (ctx.getLevel() == 2 && ctx.getVersion() < 4) {
            return SBOValidationConstraints.isModellingFramework.check(ctx, m);            
          } else if (ctx.isLevelAndVersionGreaterEqualThan(3, 1)) {            
            return SBOValidationConstraints.isModellingFramework.check(ctx, m) || SBOValidationConstraints.isInteraction.check(ctx, m);
          } else {            
            return SBOValidationConstraints.isInteraction.check(ctx, m);
          }
        }
      };
      break;

    case CORE_20202:
      func = new ElementOrderValidationFunction<Model>(MODEL_ELEMENTS_ORDER);
      break;
      
    case CORE_20203:
      func = new ValidationFunction<Model>() {

        public boolean check(ValidationContext ctx, Model m) {

          return !(m.isListOfCompartmentsEmpty()
            || m.isListOfCompartmentTypesEmpty() || m.isListOfConstraintsEmpty()
            || m.isListOfEventsEmpty() || m.isListOfFunctionDefinitionsEmpty()
            || m.isListOfInitialAssignmentsEmpty()
            || m.isListOfParametersEmpty() || m.isListOfReactionsEmpty()
            || m.isListOfRulesEmpty() || m.isListOfSpeciesEmpty()
            || m.isListOfSpeciesTypesEmpty()
            || m.isListOfUnitDefinitionEmpty());
        };
      };
      break;

    case CORE_20204:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.getNumSpecies() > 0) {
            return m.getNumCompartments() > 0;
          }

          return true;
        }
      };
      break;

    case CORE_20205:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          
          return new DuplicatedElementValidationFunction<Model>("listOfFunctionDefinitions").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfUnitDefinitions").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfCompartments").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfSpecies").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfParameters").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfInitialAssignments").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfRules").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfConstraints").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfReactions").check(ctx, m) 
              && new DuplicatedElementValidationFunction<Model>("listOfEvents").check(ctx, m); 
        }
      };
      break;
      
    case CORE_20206:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfFunctionDefinitions() || m.isListOfFunctionDefinitionsEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfFunctionDefinitions());
          }

          return true;
        }
      };
      break;

    case CORE_20207:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfUnitDefinitions() || m.isListOfUnitDefinitionEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfUnitDefinitions());
          }

          return true;
        }
      };
      break;

    case CORE_20208:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfCompartments() || m.isListOfCompartmentsEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfCompartments());
          }

          return true;
        }
      };
      break;

    case CORE_20209:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfSpecies() || m.isListOfSpeciesEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfSpecies());
          }

          return true;
        }
      };
      break;

    case CORE_20210:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfParameters() || m.isListOfParametersEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfParameters());
          }

          return true;
        }
      };
      break;

    case CORE_20211:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfInitialAssignments() || m.isListOfInitialAssignmentsEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfInitialAssignments());
          }

          return true;
        }
      };
      break;

    case CORE_20212:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfRules() || m.isListOfRulesEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfRules());
          }

          return true;
        }
      };
      break;

    case CORE_20213:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfConstraints() || m.isListOfConstraintsEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfConstraints());
          }

          return true;
        }
      };
      break;

    case CORE_20214:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfReactions() || m.isListOfReactionsEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfReactions());
          }

          return true;
        }
      };
      break;

    case CORE_20215:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfEvents() || m.isListOfEventsEmpty()) {
            return new UnknownElementValidationFunction<>().check(ctx, m.getListOfEvents());
          }

          return true;
        }
      };
      break;

    case CORE_20216:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetConversionFactor()) {
            return m.getConversionFactorInstance() != null;
          }
          return true;
        }
      };
      break;

    case CORE_20217:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetTimeUnits()) {
            UnitDefinition ud = m.getTimeUnitsInstance();

            if (ud.getUnitCount() == 0) {
              return false;
            }

            if (ud != null) {
              return ud.isVariantOfTime() || ud.isVariantOfDimensionless();
            }
          }

          return true;
        }
      };
      break;

    case CORE_20218:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetVolumeUnits()) {
            UnitDefinition ud = m.getVolumeUnitsInstance();

            if (ud.getUnitCount() == 0) {
              return false;
            }

            if (ud != null) {
              return ud.isVariantOfVolume() || ud.isVariantOfDimensionless();
            }
          }

          return true;
        }
      };
      break;

    case CORE_20219:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetAreaUnits()) {
            UnitDefinition ud = m.getAreaUnitsInstance();

            if (ud.getUnitCount() == 0) {
              return false;
            }

            if (ud != null) {
              return ud.isVariantOfArea() || ud.isVariantOfDimensionless();
            }
          }

          return true;
        }
      };
      break;

    case CORE_20220:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetLengthUnits()) {
            UnitDefinition ud = m.getLengthUnitsInstance();

            if (ud.getUnitCount() == 0) {
              return false;
            }
            if (ud != null) {
              return ud.isVariantOfLength() || ud.isVariantOfDimensionless();
            }
          }

          return true;
        }
      };
      break;

    case CORE_20221:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetExtentUnits()) {
            UnitDefinition ud = m.getExtentUnitsInstance();

            if (ud.getUnitCount() == 0) {
              return false;
            }

            return ud.isVariantOfSubstance() || ud.isVariantOfDimensionless();
          }

          return true;
        }
      };
      break;

    case CORE_20222:
      func = new UnknownAttributeValidationFunction<Model>();
      break;
      
    case CORE_20223:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfFunctionDefinitions() || m.isListOfFunctionDefinitionsEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfFunctionDefinitions());
          }

          return true;
        }
      };
      break;

    case CORE_20224:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfUnitDefinitions() || m.isListOfUnitDefinitionEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfUnitDefinitions());
          }

          return true;
        }
      };
      break;

    case CORE_20225:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfCompartments() || m.isListOfCompartmentsEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfCompartments());
          }

          return true;
        }
      };
      break;

    case CORE_20226:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfSpecies() || m.isListOfSpeciesEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfSpecies());
          }

          return true;
        }
      };
      break;

    case CORE_20227:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfParameters() || m.isListOfParametersEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfParameters());
          }

          return true;
        }
      };
      break;

    case CORE_20228:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfInitialAssignments() || m.isListOfInitialAssignmentsEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfInitialAssignments());
          }

          return true;
        }
      };
      break;

    case CORE_20229:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfRules() || m.isListOfRulesEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfRules());
          }

          return true;
        }
      };
      break;

    case CORE_20230:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfConstraints() || m.isListOfConstraintsEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfConstraints());
          }

          return true;
        }
      };
      break;

    case CORE_20231:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfReactions() || m.isListOfReactionsEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfReactions());
          }

          return true;
        }
      };
      break;

    case CORE_20232:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          if (m.isSetListOfEvents() || m.isListOfEventsEmpty()) {
            return new UnknownAttributeValidationFunction<>().check(ctx, m.getListOfEvents());
          }

          return true;
        }
      };
      break;

    case CORE_20233:{
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
     
          // check the substanceUnits value
          if (m.isSetSubstanceUnits()) {
            UnitDefinition ud = m.getSubstanceUnitsInstance();
            
            return ud.isVariantOfSubstance() || ud.isVariantOfDimensionless();
          }
          
          return true;
        }
      };
      break;
    }

    case CORE_20705:
      func = new AbstractValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {

          if (m.isSetConversionFactor()) {
            Parameter fac = m.getConversionFactorInstance();

            if (fac != null && !fac.isConstant()) {
              
              ValidationConstraint.logError(ctx, CORE_20705, m.getConversionFactor());
              return false;
            }
          }

          return true;
        }
      };
      break;

    case CORE_20802:
      func = new UniqueValidation<Model, String>() {

        @Override
        public int getNumObjects(ValidationContext ctx, Model m) {

          return m.getNumInitialAssignments();
        }


        @Override
        public String getNextObject(ValidationContext ctx, Model m, int n) {

          return m.getInitialAssignment(n).getVariable();
        }
      };
      break;

    case CORE_20803:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          Set<String> iaIds = new HashSet<String>();

          if (m.isSetListOfInitialAssignments()) {
            for (InitialAssignment ia : m.getListOfInitialAssignments()) { // TODO - use the existing map and methods for this one
              iaIds.add(ia.getVariable());
            }
          }

          if (m.isSetListOfRules()) {
            for (Rule r : m.getListOfRules()) {
              String id = null;

              if (r instanceof AssignmentRule) {
                id = ((AssignmentRule) r).getVariable();
              }
              
              // Is the id already used by an InitialAssignment?
              if (id != null && id.trim().length() > 0 && iaIds.contains(id)) {
                return false;
              }
            }
          }

          return true;
        }
      };
      break;

    case CORE_99130:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          
          if (m.isSetSubstanceUnits()) {
            UnitDefinition def = m.getSubstanceUnitsInstance();

            return def != null
                && (def.isVariantOfSubstance() || def.isVariantOfDimensionless());
          }
          
          return true;
        }
      };
      break;
    case CORE_99506:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          boolean timeUsed = m.getNumRules() > 0 || m.getNumConstraints() > 0
            || m.getNumEvents() > 0;

          for (int n = 0; !timeUsed && n < m.getNumReactions(); n++) {
            if (m.getReaction(n).isSetKineticLaw()) {
              timeUsed = true;
            }

          }

          return !timeUsed || m.isSetTimeUnits();
        }
      };
      break;

    case CORE_99507:
      func = new ValidationFunction<Model>() {

        @Override
        public boolean check(ValidationContext ctx, Model m) {
          boolean extendUsed = false;

          for (int n = 0; !extendUsed && n < m.getNumReactions(); n++) {
            if (m.getReaction(n).isSetKineticLaw()) {
              extendUsed = true;
            }

          }

          return !extendUsed || m.isSetExtentUnits();
        }
      };
    }

    return func;
  }
}
