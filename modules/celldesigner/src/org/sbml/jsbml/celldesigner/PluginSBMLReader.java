/*
 * $Id$
 * $URL$
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2014 jointly by the following organizations:
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
package org.sbml.jsbml.celldesigner;

import static org.sbml.jsbml.celldesigner.CellDesignerConstants.LINK_TO_CELLDESIGNER;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import jp.sbi.celldesigner.plugin.PluginAlgebraicRule;
import jp.sbi.celldesigner.plugin.PluginAssignmentRule;
import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginCompartmentType;
import jp.sbi.celldesigner.plugin.PluginConstraint;
import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginEventAssignment;
import jp.sbi.celldesigner.plugin.PluginFunctionDefinition;
import jp.sbi.celldesigner.plugin.PluginInitialAssignment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginRateRule;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginRule;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesAlias;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpeciesType;
import jp.sbi.celldesigner.plugin.PluginUnit;
import jp.sbi.celldesigner.plugin.PluginUnitDefinition;
import jp.sbi.celldesigner.plugin.util.PluginSpeciesSymbolType;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.celldesigner.libsbml.LibSBMLReader;
import org.sbml.jsbml.celldesigner.libsbml.LibSBMLUtils;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.gui.GUIErrorConsole;
import org.sbml.jsbml.util.ProgressListener;
import org.sbml.jsbml.util.SBMLtools;
import org.sbml.libsbml.libsbml;

/**
 *
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 0.8
 */
@SuppressWarnings("deprecation")
public class PluginSBMLReader implements SBMLInputConverter<PluginModel> {

  /**
   *
   */
  private static final int level = 3;
  /**
   *
   */
  private static final int version = 1;

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(PluginSBMLReader.class);

  /**
   *
   */
  private ProgressListener listener;

  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#setListener(org.sbml.jsbml.util.ProgressListener)
   */
  @Override
  public void setListener(ProgressListener listener) {
    this.listener = listener;
  }

  /**
   *
   */
  private Model model;

  /**
   *
   */
  private Set<Integer> possibleEnzymes;

  /**
   * @return the possibleEnzymes
   */
  public Set<Integer> getPossibleEnzymes() {
    return possibleEnzymes;
  }

  /**
   * @param possibleEnzymes the possibleEnzymes to set
   */
  public void setPossibleEnzymes(Set<Integer> possibleEnzymes) {
    this.possibleEnzymes = possibleEnzymes;
  }

  /**
   * get a model from the CellDesigner output, converts it to JSBML
   * format and stores it
   *
   * @param model
   * @throws XMLStreamException
   */
  public PluginSBMLReader(PluginModel model, Set<Integer> possibleEnzymes) throws XMLStreamException {
    this(possibleEnzymes);
    try {
      this.model = convertModel(model);
    } catch (RuntimeException exc) {
      logger.log(Priority.ERROR, exc.getLocalizedMessage() != null ? exc.getLocalizedMessage() : exc.getMessage(), exc);
      throw exc;
    }
  }

  /**
   * @return the model
   */
  public Model getModel() {
    return model;
  }

  /**
   *
   */
  public PluginSBMLReader(Set<Integer> possibleEnzymes) {
    super();
    this.possibleEnzymes = possibleEnzymes;
  }

  /**
   *
   */
  public PluginSBMLReader() {
    this(SBO.getDefaultPossibleEnzymes());
  }

  /**
   *
   * @return
   */
  public int getNumErrors() {
    return getErrorCount();
  }

  /**
   *
   * @return
   */
  public int getErrorCount() {
    return 0;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#getOriginalModel()
   */
  @Override
  public PluginModel getOriginalModel() {
    return (PluginModel) model.getUserObject(LINK_TO_CELLDESIGNER);
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLReader#getWarnings()
   */
  @Override
  public List<SBMLException> getWarnings() {
    return new LinkedList<SBMLException>();
  }

  /**
   *
   * @param compartment
   * @return
   * @throws XMLStreamException
   */
  private Compartment readCompartment(PluginCompartment compartment) throws XMLStreamException {
    Compartment c = new Compartment(compartment.getId(), level, version);
    PluginUtils.transferNamedSBaseProperties(compartment, c);
    if ((compartment.getOutside() != null) && (compartment.getOutside().length() > 0) && (c.getLevel() < 3)) {
      c.setOutside(compartment.getOutside());
    }
    if ((compartment.getCompartmentType() != null)
        && (compartment.getCompartmentType().length() > 0)
        && (model.getCompartmentType(compartment.getCompartmentType()) != null) && (c.getLevel() < 3)) {
      /*
       * Note: the third check is necessary because CellDesigner might return
       * one of its Compartment Classes as CompartmentType that has no real
       * counterpart!
       */
      c.setCompartmentType(compartment.getCompartmentType());
    }
    c.setConstant(compartment.getConstant());
    c.setSize(compartment.getSize());
    c.setSpatialDimensions((short) compartment.getSpatialDimensions());
    if (compartment.getUnits().length() > 0) {
      c.setUnits(compartment.getUnits());
    }
    return c;
  }

  /**
   *
   * @param compartmentType
   * @return
   * @throws XMLStreamException
   */
  private CompartmentType readCompartmentType(PluginCompartmentType compartmentType) throws XMLStreamException {
    CompartmentType com = new CompartmentType(compartmentType.getId(), level, version);
    PluginUtils.transferNamedSBaseProperties(compartmentType, com);
    return com;
  }

  /**
   *
   * @param constraint
   * @return
   * @throws XMLStreamException
   */
  private Constraint readConstraint(PluginConstraint constraint) throws XMLStreamException {
    Constraint c = new Constraint(level, version);
    PluginUtils.transferSBaseProperties(constraint, c);
    if (constraint.getMath() != null) {
      c.setMath(LibSBMLUtils.convert(libsbml.parseFormula(constraint.getMath()), c));
    }
    if (constraint.getMessage().length() > 0) {
      try {
        c.setMessage(constraint.getMessage());
      } catch (XMLStreamException exc) {
        logger.warn(exc.getLocalizedMessage() != null ? exc.getLocalizedMessage() : exc.getMessage(), exc);
      }
    }
    return c;
  }

  /**
   *
   * @param event
   * @return
   * @throws XMLStreamException
   */
  private Event readEvent(PluginEvent event) throws XMLStreamException {
    Event e = new Event(level, version);
    PluginUtils.transferNamedSBaseProperties(event, e);
    if (event.getTrigger() != null) {
      e.setTrigger(LibSBMLReader.readTrigger(event.getTrigger()));
    }
    if (event.getDelay() != null) {
      e.setDelay(LibSBMLReader.readDelay(event.getDelay()));
    }
    for (int i = 0; i < event.getNumEventAssignments(); i++) {
      e.addEventAssignment(readEventAssignment(event.getEventAssignment(i)));
    }
    if ((event.getTimeUnits() != null) && (event.getTimeUnits().length() > 0) && (e.getLevel() < 3)) {
      e.setTimeUnits(event.getTimeUnits());
    }
    e.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());
    return e;
  }

  /**
   *
   * @param eventass
   * @return
   * @throws XMLStreamException
   */
  private EventAssignment readEventAssignment(PluginEventAssignment eventass) throws XMLStreamException {
    EventAssignment ev = new EventAssignment(level, version);
    PluginUtils.transferSBaseProperties(eventass, ev);
    if ((eventass.getVariable() != null)
        && (eventass.getVariable().length() > 0)) {
      ev.setVariable(eventass.getVariable());
    }
    if (eventass.getMath() != null) {
      ev.setMath(LibSBMLUtils.convert(eventass.getMath(), ev));
    }
    return ev;
  }

  /**
   *
   * @param functionDefinition
   * @return
   * @throws XMLStreamException
   */
  private FunctionDefinition readFunctionDefinition(PluginFunctionDefinition functionDefinition) throws XMLStreamException {
    FunctionDefinition f = new FunctionDefinition(level, version);
    PluginUtils.transferNamedSBaseProperties(functionDefinition, f);
    if (functionDefinition.getMath() != null) {
      f.setMath(LibSBMLUtils.convert(functionDefinition.getMath(), f));
    }
    return f;
  }

  /**
   *
   * @param initialAssignment
   * @return
   * @throws XMLStreamException
   */
  private InitialAssignment readInitialAssignment(PluginInitialAssignment initialAssignment) throws XMLStreamException {
    if (initialAssignment.getSymbol() == null) {
      throw new IllegalArgumentException(
          "Symbol attribute not set for InitialAssignment");
    }
    InitialAssignment ia = new InitialAssignment(level, version);
    ia.setVariable(initialAssignment.getSymbol());
    PluginUtils.transferSBaseProperties(initialAssignment, ia);
    if (initialAssignment.getMath() != null) {
      ia.setMath(LibSBMLUtils.convert(libsbml.parseFormula(initialAssignment.getMath()), ia));
    }
    return ia;
  }

  /**
   *
   * @param kineticLaw
   * @return
   * @throws XMLStreamException
   */
  private KineticLaw readKineticLaw(PluginKineticLaw kineticLaw) throws XMLStreamException {
    KineticLaw kinlaw = new KineticLaw(level, version);
    PluginUtils.transferSBaseProperties(kineticLaw, kinlaw);
    for (int i = 0; i < kineticLaw.getNumParameters(); i++) {
      kinlaw.addLocalParameter(readLocalParameter(kineticLaw.getParameter(i)));
    }
    if (kineticLaw.getMath() != null) {
      kinlaw.setMath(LibSBMLUtils.convert(kineticLaw.getMath(), kinlaw));
    } else if (kineticLaw.getFormula().length() > 0) {
      kinlaw.setMath(LibSBMLUtils.convert(libsbml.readMathMLFromString(kineticLaw.getFormula()), kinlaw));
    }
    return kinlaw;
  }

  /**
   *
   * @param parameter
   * @return
   * @throws XMLStreamException
   */
  private LocalParameter readLocalParameter(PluginParameter parameter) throws XMLStreamException {
    return new LocalParameter(readParameter(parameter));
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLReader#readModel(java.lang.Object)
   */
  @Override
  public Model convertModel(PluginModel originalModel) throws XMLStreamException {
    try{
      int total = 0, curr = 0;

      total = originalModel.getNumCVTerms();
      total += originalModel.getNumUnitDefinitions();
      total += originalModel.getNumFunctionDefinitions();
      total += originalModel.getNumCompartmentTypes();
      total += originalModel.getNumSpeciesTypes();
      total += originalModel.getNumCompartments();
      total += originalModel.getNumSpecies();
      total += originalModel.getNumParameters();
      total += originalModel.getNumInitialAssignments();
      total += originalModel.getNumRules();
      total += originalModel.getNumConstraints();
      total += originalModel.getNumReactions();
      total += originalModel.getNumEvents();

      if (listener != null) {
        listener.progressStart(total);
      }

      model = new Model(originalModel.getId(), level, version);

      String namespace = LayoutConstants.getNamespaceURI(model.getLevel(), model.getVersion());
      LayoutModelPlugin modelPlugin = new LayoutModelPlugin(model);
      model.addExtension(namespace, modelPlugin);
      Layout layout = modelPlugin.createLayout("CellDesigner_Layout");

      PluginUtils.transferNamedSBaseProperties(originalModel, model);
      if (listener != null) {
        curr += originalModel.getNumCVTerms();
        listener.progressUpdate(curr, null);
      }

      int i;
      for (i = 0; i < originalModel.getNumUnitDefinitions(); i++) {
        model.addUnitDefinition(readUnitDefinition(originalModel.getUnitDefinition(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Unit definitions");
        }
      }

      // This is something, libSBML wouldn't do...
      SBMLtools.addPredefinedUnitDefinitions(model);
      for (i = 0; i < originalModel.getNumFunctionDefinitions(); i++) {
        model.addFunctionDefinition(readFunctionDefinition(originalModel.getFunctionDefinition(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Funciton definitions");
        }
      }

      for (i = 0; i < originalModel.getNumCompartmentTypes() && (model.getLevel() < 3); i++) {
        model.addCompartmentType(readCompartmentType(originalModel.getCompartmentType(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Compartment types");
        }
      }

      for (i = 0; i < originalModel.getNumSpeciesTypes() && (model.getLevel() < 3); i++) {
        model.addSpeciesType(readSpeciesType(originalModel.getSpeciesType(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Species types");
        }
      }

      for (i = 0; i < originalModel.getNumCompartments(); i++) {
        PluginCompartment pCompartment = originalModel.getCompartment(i);
        model.addCompartment(readCompartment(pCompartment));
        LayoutConverter.extractLayout(pCompartment, layout);
        layout.createDimensions("Layout_Size", originalModel.getCompartment(i).getWidth(),
          originalModel.getCompartment(i).getHeight(), 1d);
        if (listener != null) {
          listener.progressUpdate(++curr, "Compartments");
        }
      }

      for (i = 0; i < originalModel.getNumSpecies(); i++) {
        PluginSpecies pSpecies = originalModel.getSpecies(i);
        PluginListOf listOfAliases = pSpecies.getListOfSpeciesAlias();
        for (int j=0;j<listOfAliases.size();j++)
        {
          LayoutConverter.extractLayout((PluginSpeciesAlias)listOfAliases.get(j), layout);
        }
        model.addSpecies(readSpecies(pSpecies));
        if (listener != null) {
          listener.progressUpdate(++curr, "Species");
        }
      }

      for (i = 0; i < originalModel.getNumParameters(); i++) {
        model.addParameter(readParameter(originalModel.getParameter(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Parameters");
        }
      }

      for (i = 0; i < originalModel.getNumInitialAssignments(); i++) {
        model.addInitialAssignment(readInitialAssignment(originalModel.getInitialAssignment(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Initial assignments");
        }
      }

      for (i = 0; i < originalModel.getNumRules(); i++) {
        model.addRule(readRule(originalModel.getRule(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Rules");
        }
      }

      for (i = 0; i < originalModel.getNumConstraints(); i++) {
        model.addConstraint(readConstraint(originalModel.getConstraint(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Constraints");
        }
      }

      for (i = 0; i < originalModel.getNumReactions(); i++) {
        PluginReaction pReaction = originalModel.getReaction(i);
        model.addReaction(readReaction(pReaction));
        LayoutConverter.extractLayout(pReaction, layout);
        if (listener != null) {
          listener.progressUpdate(++curr, "Reactions");
        }
      }

      for (i = 0; i < originalModel.getNumEvents(); i++) {
        model.addEvent(readEvent(originalModel.getEvent(i)));
        if (listener != null) {
          listener.progressUpdate(++curr, "Events");
        }
      }

      if (listener != null) {
        listener.progressFinish();
      }
    }
    catch (Throwable e)
    {
      new GUIErrorConsole(e);
    }
    return model;
  }

  /**
   *
   * @param modifierSpeciesReference
   * @return
   * @throws XMLStreamException
   */
  private ModifierSpeciesReference readModifierSpeciesReference(
    PluginModifierSpeciesReference modifierSpeciesReference) throws XMLStreamException {
    ModifierSpeciesReference mod = new ModifierSpeciesReference(level, version);
    mod.setSpecies(modifierSpeciesReference.getSpecies());
    PluginUtils.transferNamedSBaseProperties(modifierSpeciesReference, mod);
    /*
     * Set SBO term.
     */
    mod.setSBOTerm(SBO.convertAlias2SBO(modifierSpeciesReference.getModificationType()));
    if (SBO.isCatalyst(mod.getSBOTerm())) {
      PluginSpecies species = modifierSpeciesReference.getSpeciesInstance();
      String speciesAliasType = species.getSpeciesAlias(0).getType()
          .equals("PROTEIN") ? species.getSpeciesAlias(0)
            .getProtein().getType() : species.getSpeciesAlias(0)
            .getType();
            int sbo = SBO.convertAlias2SBO(speciesAliasType);
            if (possibleEnzymes.contains(Integer.valueOf(sbo))) {
              mod.setSBOTerm(SBO.getEnzymaticCatalysis());
            }
    }
    return mod;
  }

  /**
   *
   * @param parameter
   * @return
   * @throws XMLStreamException
   */
  private Parameter readParameter(PluginParameter parameter) throws XMLStreamException {
    Parameter para = new Parameter(level, version);
    PluginUtils.transferNamedSBaseProperties(parameter, para);
    para.setValue(parameter.getValue());
    para.setConstant(parameter.getConstant());
    if ((parameter.getUnits() != null) && (parameter.getUnits().length() > 0)) {
      para.setUnits(parameter.getUnits());
    }
    return para;
  }

  /**
   *
   * @param reac
   * @return
   * @throws XMLStreamException
   */
  private Reaction readReaction(PluginReaction reac) throws XMLStreamException {
    logger.debug("Translating reaction " + reac.getId());
    Reaction reaction = new Reaction(reac.getId(), level, version);
    PluginUtils.transferNamedSBaseProperties(reac, reaction);
    logger.debug("NamedSBase properties done");
    for (int i = 0; i < reac.getNumReactants(); i++) {
      reaction.addReactant(readSpeciesReference(reac.getReactant(i)));
    }
    logger.debug("Reactants done");
    for (int i = 0; i < reac.getNumProducts(); i++) {
      reaction.addProduct(readSpeciesReference(reac.getProduct(i)));
    }
    logger.debug("Products done");
    for (int i = 0; i < reac.getNumModifiers(); i++) {
      reaction.addModifier(readModifierSpeciesReference(reac.getModifier(i)));
    }
    logger.debug("Modifiers done");
    reaction.setFast(reac.getFast());
    reaction.setReversible(reac.getReversible());
    int sbo = SBO.convertAlias2SBO(reac.getReactionType());
    if (SBO.checkTerm(sbo)) {
      reaction.setSBOTerm(sbo);
    }
    logger.debug("Reaction properties copied");
    if (reac.getKineticLaw() != null) {
      reaction.setKineticLaw(readKineticLaw(reac.getKineticLaw()));
    }
    logger.debug("Kinetic law copied");
    return reaction;
  }

  /**
   *
   * @param rule
   * @return
   * @throws XMLStreamException
   */
  private Rule readRule(PluginRule rule) throws XMLStreamException {
    Rule r;
    if (rule instanceof PluginAlgebraicRule) {
      r = new AlgebraicRule(level, version);
    } else {
      String variable = null;
      if (rule instanceof PluginAssignmentRule) {
        r = new AssignmentRule(level, version);
        variable = ((PluginAssignmentRule) rule).getVariable();
      } else {
        r = new RateRule(level, version);
        variable = ((PluginRateRule) rule).getVariable();
      }
      ExplicitRule er = (ExplicitRule) r;
      er.setVariable(variable);
    }
    PluginUtils.transferSBaseProperties(rule, r);
    if (rule.getMath() != null) {
      r.setMath(LibSBMLUtils.convert(rule.getMath(), r));
    }
    return r;
  }

  /**
   *
   * @param species
   * @return
   * @throws XMLStreamException
   */
  private Species readSpecies(PluginSpecies species) throws XMLStreamException {
    Species s = new Species(level, version);
    PluginUtils.transferNamedSBaseProperties(species, s);
    int sbo = SBO.convertAlias2SBO(species.getSpeciesAlias(0).getType());
    PluginSpeciesAlias alias = species.getSpeciesAlias(0);
    String type = alias.getType();
    if (alias.getType().equals(PluginSpeciesSymbolType.PROTEIN)) {
      type = alias.getProtein().getType();
    }
    sbo = SBO.convertAlias2SBO(type);
    if (SBO.checkTerm(sbo)) {
      s.setSBOTerm(sbo);
    }
    if (model.getLevel()<3) {
      s.setCharge(species.getCharge());
    }
    if ((species.getCompartment() != null) && (species.getCompartment().length() > 0)) {
      s.setCompartment(model.getCompartment(species.getCompartment()));
    }
    s.setBoundaryCondition(species.getBoundaryCondition());
    s.setConstant(species.getConstant());
    s.setHasOnlySubstanceUnits(species.getHasOnlySubstanceUnits());
    if (species.isSetInitialAmount()) {
      s.setInitialAmount(species.getInitialAmount());
    } else if (species.isSetInitialConcentration()) {
      s.setInitialConcentration(species.getInitialConcentration());
    }
    // before L2V3...
    if ((species.getSpatialSizeUnits() != null) && (species.getSpatialSizeUnits().length() > 0) && (model.getLevel() < 3)) {
      s.setSpatialSizeUnits(species.getSpatialSizeUnits());
    }
    if (species.getSubstanceUnits().length() > 0) {
      s.setSubstanceUnits(species.getSubstanceUnits());
    }
    if (species.getSpeciesType().length() > 0 && (model.getLevel() < 3)) {
      s.setSpeciesType(model.getSpeciesType(species.getSpeciesType()));
    }
    if (model.getLevel()>2) {
      s.setConstant(false);
      s.setCompartment(model.getCompartment(0));
    }
    return s;
  }

  /**
   *
   * @param speciesReference
   * @return
   * @throws XMLStreamException
   */
  private SpeciesReference readSpeciesReference(PluginSpeciesReference speciesReference) throws XMLStreamException {
    logger.debug("Reading speciesReference " + speciesReference.getSpecies());
    SpeciesReference spec = new SpeciesReference(level, version);
    PluginUtils.transferNamedSBaseProperties(speciesReference, spec);
    logger.debug("NamedSBase properties done");
    if ((speciesReference.getStoichiometryMath() != null) && (model.getLevel() < 3)) {
      spec.setStoichiometryMath(LibSBMLReader.readStoichiometricMath(speciesReference.getStoichiometryMath()));
      logger.debug("Reading stoichiometric math done");
    } else {
      spec.setStoichiometry(speciesReference.getStoichiometry());
      logger.debug("Reading stoichiometry done");
    }
    if (speciesReference.getReferenceType().length() > 0) {
      int sbo = SBO.convertAlias2SBO(speciesReference.getReferenceType());
      logger.debug("Converted reference type to SBO");
      if (SBO.checkTerm(sbo)) {
        spec.setSBOTerm(sbo);
      }
    }
    if (spec.getLevel() > 2) {
      spec.setConstant(true);
    }
    spec.setSpecies(speciesReference.getSpecies());
    return spec;
  }

  /**
   *
   * @param speciesType
   * @return
   * @throws XMLStreamException
   */
  private SpeciesType readSpeciesType(PluginSpeciesType speciesType) throws XMLStreamException {
    SpeciesType st = new SpeciesType(level, version);
    PluginUtils.transferNamedSBaseProperties(speciesType, st);
    return st;
  }

  /**
   *
   * @param unit
   * @return
   * @throws XMLStreamException
   */
  private Unit readUnit(PluginUnit unit) throws XMLStreamException {
    Unit u = new Unit(level, version);
    PluginUtils.transferSBaseProperties(unit, u);
    u.setKind(LibSBMLUtils.convertUnitKind(unit.getKind()));
    u.setExponent((double) unit.getExponent());
    u.setMultiplier(unit.getMultiplier());
    u.setScale(unit.getScale());
    if (u.isSetOffset() && (model.getLevel() < 3)) {
      u.setOffset(unit.getOffset());
    }
    return u;
  }

  /**
   *
   * @param unitDefinition
   * @return
   * @throws XMLStreamException
   */
  private UnitDefinition readUnitDefinition(PluginUnitDefinition unitDefinition) throws XMLStreamException {
    UnitDefinition ud = new UnitDefinition(level, version);
    PluginUtils.transferNamedSBaseProperties(unitDefinition, ud);
    for (int i = 0; i < unitDefinition.getNumUnits(); i++) {
      ud.addUnit(readUnit(unitDefinition.getUnit(i)));
    }
    return ud;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertSBMLDocument(java.lang.String)
   */
  @Override
  public SBMLDocument convertSBMLDocument(String fileName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertSBMLDocument(java.io.File)
   */
  @Override
  public SBMLDocument convertSBMLDocument(File sbmlFile) throws Exception {
    return null;
  }

}
