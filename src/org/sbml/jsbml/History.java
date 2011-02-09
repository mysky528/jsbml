/*
 * $Id$
 * $URL$
 * ----------------------------------------------------------------------------
 * This file is part of JSBML. Please visit <http://sbml.org/Software/JSBML>
 * for the latest version of JSBML and more information about SBML.
 *
 * Copyright (C) 2009-2011 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 3. The California Institute of Technology, Pasadena, CA, USA
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online as <http://sbml.org/Software/JSBML/License>.
 * ----------------------------------------------------------------------------
 */

package org.sbml.jsbml;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains all the history information about a {@link Model} (or other
 * {@link SBase} if level >= 3).
 * 
 * @author marine
 * @author Andreas Dr&auml;ger
 * 
 */
public class History implements Cloneable, Serializable {
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -1699117162462037149L;
	/**
	 * Date of creation
	 */
	private Date creation;
	/**
	 * Contains all the {@link Creator} instances of this {@link History}.
	 */
	private List<Creator> listOfCreators;
	/**
	 * Contains all the modified date instances of this {@link History}.
	 */
	private List<Date> listOfModification;
	/**
	 * Last date of modification
	 */
	private Date modified;

	/**
	 * Creates a {@link History} instance. By default, the creation and modified
	 * are null. The {@link #listOfModification} and {@link #listOfCreators} are empty.
	 */
	public History() {
		listOfCreators = new LinkedList<Creator>();
		listOfModification = new LinkedList<Date>();
		creation = null;
		modified = null;
	}

	/**
	 * Creates a {@link History} instance from a given {@link History}.
	 * 
	 * @param modelHistory
	 */
	public History(History modelHistory) {
		this();
		listOfCreators.addAll(modelHistory.getListCreators());
		listOfModification.addAll(modelHistory.getListModifiedDates());
		Calendar calendar = Calendar.getInstance();
		if (modelHistory.isSetCreatedDate()) {
			calendar.setTime(modelHistory.getCreatedDate());
			creation = calendar.getTime();
		}
		if (modelHistory.isSetModifiedDate()) {
			calendar.setTime(modelHistory.getModifiedDate());
			modified = calendar.getTime();
		}
	}

	/**
	 * Adds a {@link Creator} instance to this {@link History}.
	 * 
	 * @param mc
	 */
	public void addCreator(Creator mc) {
		listOfCreators.add(mc);
	}

	/**
	 * Adds a {@link Date} of modification to this {@link History}.
	 * 
	 * @param date
	 */
	public void addModifiedDate(Date date) {
		setModifiedDate(date);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public History clone() {
		return new History(this);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof History) {
			boolean equal = super.equals(o);
			History mh = (History) o;
			equal &= listOfCreators.size() == mh.getListCreators().size();

			if (equal) {
				for (int i = 0; i < listOfCreators.size(); i++) {
					Creator c1 = listOfCreators.get(i);
					Creator c2 = mh.getListCreators().get(i);

					if (c1 != null && c2 != null) {
						equal &= c1.equals(c2);
					} else if ((c1 == null && c2 != null)
							|| (c2 == null && c1 != null)) {
						return false;
					}
				}
				equal &= listOfModification.size() == mh.getListModifiedDates()
						.size();
				if (equal) {
					for (int i = 0; i < listOfModification.size(); i++) {
						Date d1 = listOfModification.get(i);
						Date d2 = mh.getListModifiedDates().get(i);

						if (d1 != null && d2 != null) {
							equal &= d1.equals(d2);
						} else if ((d1 == null && d2 != null)
								|| (d2 == null && d1 != null)) {
							return false;
						}
					}
				}
				equal &= isSetModifiedDate() == mh.isSetModifiedDate();
				if (equal) {
					equal &= getModifiedDate().equals(mh.getModifiedDate());
				}
				equal &= isSetCreatedDate() == mh.isSetCreatedDate();
				// isSetCreatedDate() may still be null.
				if (equal && isSetCreatedDate()) {
					equal &= getCreatedDate().equals(mh.getCreatedDate());
				}
			}
			return equal;
		}
		return false;
	}

	/**
	 * Returns the createdDate from the {@link History}.
	 * 
	 * @return {@link Date} object representing the createdDate from the {@link History}.
	 *         Can be null if it is not set.
	 */
	public Date getCreatedDate() {
		return creation;
	}

	/**
	 * Get the nth {@link Creator} object in this {@link History}.
	 * 
	 * @param i
	 * @return the nth {@link Creator} of this {@link History}. Can be null.
	 */
	public Creator getCreator(int i) {
		return listOfCreators.get(i);
	}

	/**
	 * Get the list of {@link Creator} objects in this {@link History}.
	 * 
	 * @return the list of {@link Creator}s for this {@link History}.
	 */
	public List<Creator> getListCreators() {
		return listOfCreators;
	}

	/**
	 * Get the list of ModifiedDate objects in this {@link History}.
	 * 
	 * @return the list of ModifiedDates for this {@link History}.
	 */
	public List<Date> getListModifiedDates() {
		return listOfModification;
	}

	/**
	 * Returns the modifiedDate from the {@link History}.
	 * 
	 * @return Date object representing the modifiedDate from the {@link History}.
	 *         Can be null if it is not set.
	 */
	public Date getModifiedDate() {
		return modified;
	}

	/**
	 * Get the nth {@link Date} object in the list of ModifiedDates in this
	 * {@link History}.
	 * 
	 * @param n
	 *            the nth {@link Date} in the list of ModifiedDates of this
	 *            {@link History}.
	 * @return the nth {@link Date} object in the list of ModifiedDates in this
	 *         {@link History}. Can be null if it is not set.
	 */
	public Date getModifiedDate(int n) {
		return listOfModification.get(n);
	}

	/**
	 * Get the number of {@link Creator} objects in this {@link History}.
	 * 
	 * @return the number of {@link Creator}s in this {@link History}.
	 */
	public int getNumCreators() {
		return isSetListOfCreators() ? listOfCreators.size() : 0;
	}

	/**
	 * Get the number of ModifiedDate objects in this {@link History}.
	 * 
	 * @return the number of ModifiedDates in this {@link History}.
	 */
	public int getNumModifiedDates() {
		return isSetListOfModification() ? listOfModification.size() : 0;
	}

	/**
	 * Checks whether at least one attribute has been set for this
	 * {@link History}.
	 * 
	 * @return true if at least one of the possible attributes is set, i.e., not
	 *         null:
	 *         <ul>
	 *         <li> {@link #creation} date</li>
	 *         <li> {@link #listOfCreators} is not null and contains at least one
	 *         element</li>
	 *         <li>
	 *         {@link #listOfModification} is not null and contains at least one
	 *         element.</li>
	 *         <li> {@link #modified} is not null.</li>
	 *         </ul>
	 */
	public boolean isEmpty() {
		return !isSetCreatedDate() && (getNumCreators() == 0)
				&& (getNumModifiedDates() == 0) && !isSetModifiedDate();
	}
	
	/**
	 * Predicate returning true or false depending on whether this
	 * {@link History}'s createdDate has been set.
	 * 
	 * @return true if the createdDate of this {@link History} has been set, false
	 *         otherwise.
	 */
	public boolean isSetCreatedDate() {
		return creation != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetListOfCreators() {
		return listOfCreators != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetListOfModification() {
		return listOfModification != null;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * {@link History}'s modifiedDate has been set.
	 * 
	 * @return true if the modifiedDate of this {@link History} has been set, false
	 *         otherwise.
	 */
	public boolean isSetModifiedDate() {
		return modified != null;
	}
	
	/**
	 * 
	 * @param nodeName
	 * @param attributeName
	 * @param prefix
	 * @param value
	 * @return true if the XML attribute is known by this {@link History}.
	 */
	public boolean readAttribute(String nodeName, String attributeName,
			String prefix, String value) {
		if (nodeName.equals("creator") || nodeName.equals("created")
				|| nodeName.equals("modified")) {
			if (attributeName.equals("parseType") && value.equals("Resource")) {
				return true;
			}
		}
		return false;
	}

	/**
	 *If there is no i<sup>th</sup> {@link Creator}, it returns null.
	 * 
	 * @param i
	 * @return the {@link Creator} removed from the {@link #listOfCreators}.
	 */
	public Creator removeCreator(int i) {
		return listOfCreators.remove(i);
	}

	/**
	 * If there is no i<sup>th</sup> modified {@link Date}, it returns null.
	 * 
	 * @param i
	 * @return the modified {@link Date} removed from the listOfModification.
	 */
	public Date removeModifiedDate(int i) {
		if (i < listOfModification.size()) {
			if (i == listOfModification.size() - 1) {
				if (i - 2 >= 0) {
					this.modified = listOfModification.get(i - 2);
				} else {
					this.modified = null;
				}
			}
			return listOfModification.remove(i);
		}
		throw new IndexOutOfBoundsException(String.format("No modified date %d available.", i));
	}

	/**
	 * Sets the createdDate.
	 * 
	 * @param date
	 *            a {@link Date} object representing the date the {@link History} was
	 *            created.
	 */
	public void setCreatedDate(Date date) {
		creation = date;
	}

	/**
	 * Sets the modifiedDate.
	 * 
	 * @param date
	 *            a {@link Date} object representing the date the {@link History} was
	 *            modified.
	 */
	public void setModifiedDate(Date date) {
		listOfModification.add(date);
		modified = date;
	}

	/**
	 * Sets the created of this {@link History} to null.
	 */
	public void unsetCreatedDate() {
		this.creation = null;
	}

}
