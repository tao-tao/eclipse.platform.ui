/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.conversion;

import java.text.Format;

/**
 * Converts from a Number to a Short.
 * <p>
 * Class is thread safe.
 * </p>
 * @since 1.0
 */
public class NumberToShortConverter extends NumberToNumberConverter<Short> {
	/**
	 * @param numberFormat
	 * @param fromType
	 * @param primitive
	 */
	public NumberToShortConverter(Format numberFormat, Class<?> fromType, boolean primitive) {
		super(numberFormat, fromType, (primitive) ? Short.TYPE : Short.class);
	}

	@Override
	protected Short doConvert(Number number) {
		if (StringToNumberParser.inShortRange(number)) {
			return Short.valueOf(number.shortValue());
		}

		return null;
	}
}
