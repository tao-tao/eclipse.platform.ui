/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.registry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Provides the parameter values for the show view command.
 *
 * @since 3.1
 */
public final class ViewParameterValues implements IParameterValues {

	@Override
	public Map getParameterValues() {
		final Map values = new HashMap();

		final IViewDescriptor[] views = PlatformUI.getWorkbench()
				.getViewRegistry().getViews();
		for (final IViewDescriptor view : views) {
			values.put(view.getLabel(), view.getId());
		}

		return values;
	}
}
