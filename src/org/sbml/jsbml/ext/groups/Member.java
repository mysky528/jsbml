/*
 * $Id$
 * $URL$
 *
 *
 *==================================================================================
 * Copyright (c) 2009 the copyright is held jointly by the individual
 * authors. See the file AUTHORS for the list of authors.
 *
 * This file is part of jsbml, the pure java SBML library. Please visit
 * http://sbml.org for more information about SBML, and http://jsbml.sourceforge.net/
 * to get the latest version of jsbml.
 *
 * jsbml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jsbml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jsbml.  If not, see <http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>.
 *
 *===================================================================================
 *
 */

package org.sbml.jsbml.ext.groups;

import java.util.HashMap;

import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBase;

/**
 * 
 * @author 
 *
 */
public class Member extends AbstractSBase {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 1726020714284762330L;
	/**
	 * 
	 */
	protected String symbol; 
	
	/**
	 * 
	 */
	public Member() {
		
	}
	
	/**
	 * 
	 * @param level
	 * @param version
	 */
	public Member(int level, int version) {
		super(level, version);
	}
	
	/**
	 * 
	 * @param member
	 */
	public Member(Member member) {
		// TODO
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#clone()
	 */
	@Override
	public Member clone() {
		return new Member(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getSymbol() {
		return symbol == null ? "" : symbol;
	}

	/**
	 * 
	 * @return
	 */
	public SBase getSymbolInstance() {
		if (getModel() == null) {
			return null;
		}
		
		// TODO or remove method
		return null; // getModel().getCompartment(this.compartmentID);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetSymbol() {
		return symbol != null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.SBase#readAttribute(String attributeName,
	 * String prefix, String value)
	 */
	@Override
	public boolean readAttribute(String attributeName, String prefix,
			String value) {
		boolean isAttributeRead = super.readAttribute(attributeName, prefix,
				value);
		
		if (!isAttributeRead) {
			if (attributeName.equals("symbol")) {
				this.symbol = value;
				return true;
			}
		}
		return isAttributeRead;
	}
	
	/**
	 * 
	 * @param symbol
	 */
	public void setSymbol(NamedSBase symbol) {
		if (symbol == null) {
			this.symbol = null;
		}
		this.symbol = symbol.getId();
		stateChanged();
	}
	
	/**
	 * 
	 * @param symbolId
	 */
	public void setSymbol(String symbolId) {
		if (symbolId != null && symbolId.trim().length() == 0) {
			this.symbol = null;
		} else {
			this.symbol = symbolId;
		}
		
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.AbstractSBase#toString()
	 */
	@Override
	public String toString() {
		// TODO
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.element.SBase#writeXMLAttributes()
	 */
	@Override
	public HashMap<String, String> writeXMLAttributes() {
		HashMap<String, String> attributes = super.writeXMLAttributes();

		if (isSetSymbol()) {
			attributes.put("symbol", symbol);
		}
		
		return attributes;
	}
}
